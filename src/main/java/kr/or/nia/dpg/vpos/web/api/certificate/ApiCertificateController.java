package kr.or.nia.dpg.vpos.web.api.certificate;

import kr.or.nia.dpg.vpos.entity.api.AppVO;
import kr.or.nia.dpg.vpos.entity.api.PersonalVO;
import kr.or.nia.dpg.vpos.enums.exception.VpsExceptionType;
import kr.or.nia.dpg.vpos.exception.VpsException;
import kr.or.nia.dpg.vpos.mapper.AppMapper;
import kr.or.nia.dpg.vpos.mapper.TempMapper;
import kr.or.nia.dpg.vpos.service.api.CertificateApiService;
import kr.or.nia.dpg.vpos.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO 추후 삭제 필요
// 인증 솔루션
@Controller
@Slf4j
@RequestMapping({"api/certificate"})
public class ApiCertificateController {

    /**
     * 주민등록번호에서 성별/생년월일을 판별하기 위한 최소 길이(YYMMDD + 성별코드 1자리).
     */
    private static final int SSN_MIN_LENGTH = 7;

    private final CertificateApiService certifiService;
    private final TempMapper mapper;
    private final AppMapper appMapper;

    public ApiCertificateController(CertificateApiService certifiService, TempMapper mapper, AppMapper appMapper) {
        this.certifiService = certifiService;
        this.mapper = mapper;
        this.appMapper = appMapper;
    }

    // 공동인증 API호출
    @PostMapping("/openCertifiWindow")
    public ResponseEntity<Map<String, Object>> openCertifiWindow(@RequestBody Map<String, String> requestData) {
        log.info("[openCertifiWindow] 공동인증 API 호출 시작");
        String personalNumber = extractPersonalNumber(requestData);

        PersonalVO personal = firstOrThrow(certifiService.certificateSearch(personalNumber), "openCertifiWindow");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("personalName", personal.getPersonalName());
        responseData.put("personalNumber", personal.getPersonalNumber());
        responseData.put("certifiDate", DateUtil.getTodayDashString());

        log.info("[openCertifiWindow] 공동인증 API 호출 성공");
        return ResponseEntity.ok(responseData);
    }

    // 휴대전화인증 API호출
    @PostMapping("/openPhonePCCWindow")
    public ResponseEntity<Map<String, Object>> openPhonePCCWindow(@RequestBody Map<String, String> requestData) {
        log.info("[openPhonePCCWindow] 휴대전화인증 API 호출 시작");
        String personalNumber = extractPersonalNumber(requestData);

        PersonalVO personal = firstOrThrow(certifiService.phoneSearch(personalNumber), "openPhonePCCWindow");
        Map<String, Object> responseData = buildPersonalResponse(personal);

        personal.setGender(resolveGender(personalNumber, false));
        personal.setAge(ageChange(personalNumber));

        log.info("[openPhonePCCWindow] 휴대전화인증 API 호출 성공");
        return ResponseEntity.ok(responseData);
    }

    // 아이핀인증 API호출
    @PostMapping("/openCBAWindow")
    public ResponseEntity<Map<String, Object>> openCBAWindow(@RequestBody Map<String, String> requestData) {
        log.info("[openCBAWindow] 아이핀인증 API 호출 시작");
        String personalNumber = extractPersonalNumber(requestData);

        PersonalVO personal = firstOrThrow(certifiService.pbaSearch(personalNumber), "openCBAWindow");
        Map<String, Object> responseData = buildPersonalResponse(personal);

        personal.setGender(resolveGender(personalNumber, true));
        personal.setAge(ageChange(personalNumber));
        upsertPersonalAndApp(personal, "openCBAWindow");

        log.info("[openCBAWindow] 아이핀인증 API 호출 성공");
        return ResponseEntity.ok(responseData);
    }

    // 디지털원패스인증 API호출
    @PostMapping("/openPassWindow")
    public ResponseEntity<Map<String, Object>> openPassWindow(@RequestBody Map<String, String> requestData) {
        log.info("[openPassWindow] 디지털원패스인증 API 호출 시작");
        String personalNumber = extractPersonalNumber(requestData);

        PersonalVO personal = firstOrThrow(certifiService.onepassSearch(personalNumber), "openPassWindow");
        Map<String, Object> responseData = buildPersonalResponse(personal);

        personal.setGender(resolveGender(personalNumber, true));
        personal.setAge(ageChange(personalNumber));
        upsertPersonalAndApp(personal, "openPassWindow");

        log.info("[openPassWindow] 디지털원패스인증 API 호출 성공");
        return ResponseEntity.ok(responseData);
    }

    /**
     * 요청 본문에서 주민등록번호를 추출하고 비어있는지 검증한다.
     * <p>개인정보 보호를 위해 값 자체는 로그에 남기지 않는다.</p>
     */
    private String extractPersonalNumber(Map<String, String> requestData) {
        String personalNumber = requestData != null ? requestData.get("personalNumber") : null;
        if (personalNumber == null || personalNumber.trim().isEmpty()) {
            log.warn("[ApiCertificateController] 필수 파라미터 personalNumber가 비어있습니다.");
            throw new VpsException(VpsExceptionType.INVALID_SSN);
        }
        return personalNumber.trim();
    }

    /**
     * 인증 조회 결과 목록에서 첫 번째 항목을 반환하되, 결과가 없으면 예외를 던진다.
     */
    private PersonalVO firstOrThrow(List<PersonalVO> personalInfoList, String methodName) {
        if (personalInfoList == null || personalInfoList.isEmpty()) {
            log.warn("[{}] 인증 정보를 찾을 수 없습니다.", methodName);
            throw new VpsException(VpsExceptionType.NO_DATA);
        }
        return personalInfoList.get(0);
    }

    /**
     * 인증 결과 화면에 공통으로 내려주는 응답 데이터를 구성한다.
     */
    private Map<String, Object> buildPersonalResponse(PersonalVO personal) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("personalName", personal.getPersonalName());
        responseData.put("personalNumber", personal.getPersonalNumber());
        responseData.put("CI", personal.getCI());
        responseData.put("DI", personal.getDI());
        responseData.put("personalPhoneNumber", personal.getPersonalPhoneNumber());
        responseData.put("vctmAplySn", personal.getVctmAplySn());
        return responseData;
    }

    /**
     * 주민등록번호 성별코드로 성별을 판별한다.
     *
     * @param korean true면 "남자"/"여자", false면 "M"/"F"
     */
    private String resolveGender(String personalNumber, boolean korean) {
        if (personalNumber.length() < SSN_MIN_LENGTH) {
            log.warn("[ApiCertificateController] 주민등록번호 형식이 올바르지 않습니다. (length 부족)");
            throw new VpsException(VpsExceptionType.INVALID_SSN);
        }
        boolean isMale = personalNumber.charAt(6) % 2 == 1;
        if (korean) {
            return isMale ? "남자" : "여자";
        }
        return isMale ? "M" : "F";
    }

    /**
     * 개인정보 및 결과화면용 앱 정보를 CI 기준으로 신규 등록 또는 갱신한다.
     */
    private void upsertPersonalAndApp(PersonalVO personal, String methodName) {
        String ci = personal.getCI();
        if (ci == null || ci.trim().isEmpty()) {
            log.warn("[{}] CI 값이 비어있어 저장을 진행할 수 없습니다.", methodName);
            throw new VpsException(VpsExceptionType.NO_DATA);
        }

        if (mapper.searchVctmAplySn(ci) != null) {
            mapper.updatePersonal(personal);
            log.info("[{}] 개인정보 업데이트 완료", methodName);
        } else {
            mapper.insertPersonal(personal);
            log.info("[{}] 개인정보 신규 등록 완료", methodName);
        }

        // 결과화면을 위한 데이터 insert/update 분기
        AppVO appVO = appMapper.search(ci);
        if (appVO == null || appVO.getCI() == null || appVO.getCI().isBlank()) {
            appMapper.insert(ci);
            log.info("[{}] 앱 정보 신규 저장 완료", methodName);
        } else {
            appMapper.update(ci);
            log.info("[{}] 앱 정보 업데이트 완료", methodName);
        }
    }

    public int ageChange(String personalNumber) {
        if (personalNumber == null || personalNumber.length() < SSN_MIN_LENGTH) {
            log.warn("[ageChange] 주민등록번호가 유효하지 않아 나이를 계산할 수 없습니다.");
            return 0;
        }

        String birthPart = personalNumber.substring(0, 6);
        char genderCode = personalNumber.charAt(6);

        String century;
        switch (genderCode) {
            case '1':
            case '2':
                century = "19";
                break;
            case '3':
            case '4':
                century = "20";
                break;
            default:
                log.warn("[ageChange] 알 수 없는 성별코드입니다.");
                century = "19";
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate birthDate = LocalDate.parse(century + birthPart, formatter);
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (Exception e) {
            // 생년월일 파싱 실패는 치명적이지 않으므로 0으로 처리하되, 주민번호 값은 로그에 남기지 않는다.
            log.error("[ageChange] 생년월일 파싱 실패로 나이를 0으로 처리합니다.", e);
            return 0;
        }
    }

}
