package kr.or.nia.dpg.vpos.web.api.certificate;

import kr.or.nia.dpg.vpos.entity.api.AppVO;
import kr.or.nia.dpg.vpos.entity.api.PersonalVO;
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
        Map<String, Object> responseData = new HashMap<>();
        try {
            String personalNumber = requestData.get("personalNumber");
            if (personalNumber == null || personalNumber.isEmpty()) {
                log.warn("[openCertifiWindow] personalNumber가 비어있습니다.");
                responseData.put("error", "personalNumber는 필수 값입니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            List<PersonalVO> personalInfo = certifiService.certificateSearch(personalNumber);
            if (personalInfo == null || personalInfo.isEmpty()) {
                log.warn("[openCertifiWindow] 인증 정보를 찾을 수 없습니다. - personalNumber: {}", personalNumber);
                responseData.put("error", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            PersonalVO personal = personalInfo.get(0);

            responseData.put("personalName", personal.getPersonalName());
            responseData.put("personalNumber", personal.getPersonalNumber());
            responseData.put("certifiDate", DateUtil.getTodayDashString());

            log.info("[openCertifiWindow] 공동인증 API 호출 성공");
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            log.error("[openCertifiWindow] 공동인증 API 호출 중 오류 발생", e);
            responseData.put("error", "공동인증 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(responseData);
        }
    }

    // 휴대전화인증 API호출
    @PostMapping("/openPhonePCCWindow")
    public ResponseEntity<Map<String, Object>> openPhonePCCWindow(@RequestBody Map<String, String> requestData) {
        Map<String, Object> responseData = new HashMap<>();
        try {
            String personalNumber = requestData.get("personalNumber");
            if (personalNumber == null || personalNumber.isEmpty()) {
                log.warn("[openPhonePCCWindow] personalNumber가 비어있습니다.");
                responseData.put("error", "personalNumber는 필수 값입니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            List<PersonalVO> personalInfoList = certifiService.phoneSearch(personalNumber);
            if (personalInfoList == null || personalInfoList.isEmpty()) {
                log.warn("[openPhonePCCWindow] 인증 정보를 찾을 수 없습니다. - personalNumber: {}", personalNumber);
                responseData.put("error", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            PersonalVO personal = personalInfoList.get(0);

            responseData.put("personalName", personal.getPersonalName());
            responseData.put("personalNumber", personal.getPersonalNumber());
            responseData.put("CI", personal.getCI());
            responseData.put("DI", personal.getDI());
            responseData.put("personalPhoneNumber", personal.getPersonalPhoneNumber());
            responseData.put("vctmAplySn", personal.getVctmAplySn());

            if (personalNumber.length() < 7) {
                log.warn("[openPhonePCCWindow] personalNumber 길이가 부족합니다. - length: {}", personalNumber.length());
                responseData.put("error", "주민번호 형식이 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            String gender = (personal.getPersonalNumber().charAt(6) % 2 == 1) ? "M" : "F";
            personal.setGender(gender);
            int age = ageChange(personalNumber);
            personal.setAge(age);

            log.info("[openPhonePCCWindow] 휴대전화인증 API 호출 성공");
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            log.error("[openPhonePCCWindow] 휴대전화인증 API 호출 중 오류 발생", e);
            responseData.put("error", "휴대전화인증 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(responseData);
        }
    }

    // 아이핀인증 API호출
    @PostMapping("/openCBAWindow")
    public ResponseEntity<Map<String, Object>> openCBAWindow(@RequestBody Map<String, String> requestData) {
        Map<String, Object> responseData = new HashMap<>();
        try {
            String personalNumber = requestData.get("personalNumber");
            if (personalNumber == null || personalNumber.isEmpty()) {
                log.warn("[openCBAWindow] personalNumber가 비어있습니다.");
                responseData.put("error", "personalNumber는 필수 값입니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            List<PersonalVO> personalInfoList = certifiService.pbaSearch(personalNumber);
            if (personalInfoList == null || personalInfoList.isEmpty()) {
                log.warn("[openCBAWindow] 인증 정보를 찾을 수 없습니다. - personalNumber: {}", personalNumber);
                responseData.put("error", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            PersonalVO personal = personalInfoList.get(0);

            responseData.put("personalName", personal.getPersonalName());
            responseData.put("personalNumber", personal.getPersonalNumber());
            responseData.put("CI", personal.getCI());
            responseData.put("DI", personal.getDI());
            responseData.put("personalPhoneNumber", personal.getPersonalPhoneNumber());
            responseData.put("vctmAplySn", personal.getVctmAplySn());

            String ci = personalInfoList.get(0).getCI();
            if (ci == null || ci.isEmpty()) {
                log.warn("[openCBAWindow] CI 값이 비어있습니다.");
                responseData.put("error", "CI 정보가 없습니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            String checkCi = mapper.searchVctmAplySn(ci);

            if (personalNumber.length() < 7) {
                log.warn("[openCBAWindow] personalNumber 길이가 부족합니다. - length: {}", personalNumber.length());
                responseData.put("error", "주민번호 형식이 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            String gender = (personal.getPersonalNumber().charAt(6) % 2 == 1) ? "남자" : "여자";
            personal.setGender(gender);
            int age = ageChange(personalNumber);
            personal.setAge(age);

            if (checkCi != null) {
                mapper.updatePersonal(personal);
                log.info("[openCBAWindow] 개인정보 업데이트 완료 - CI: {}", ci);
            } else {
                mapper.insertPersonal(personal);
                log.info("[openCBAWindow] 개인정보 신규 등록 완료");
            }

            //결과화면을 위한 데이터 insert update 분기
            AppVO appVO = appMapper.search(ci);
            if (appVO == null || appVO.getCI() == null || appVO.getCI().isBlank()) {
                appMapper.insert(ci);
                log.info("[openCBAWindow] 앱 정보 신규 저장 완료");
            } else {
                appMapper.update(ci);
                log.info("[openCBAWindow] 앱 정보 업데이트 완료");
            }

            log.info("[openCBAWindow] 아이핀인증 API 호출 성공");
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            log.error("[openCBAWindow] 아이핀인증 API 호출 중 오류 발생", e);
            responseData.put("error", "아이핀인증 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(responseData);
        }
    }

    // 디지털원패스인증 API호출
    @PostMapping("/openPassWindow")
    public ResponseEntity<Map<String, Object>> openPassWindow(@RequestBody Map<String, String> requestData) {
        Map<String, Object> responseData = new HashMap<>();
        try {
            String personalNumber = requestData.get("personalNumber");
            if (personalNumber == null || personalNumber.isEmpty()) {
                log.warn("[openPassWindow] personalNumber가 비어있습니다.");
                responseData.put("error", "personalNumber는 필수 값입니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            List<PersonalVO> personalInfoList = certifiService.onepassSearch(personalNumber);
            if (personalInfoList == null || personalInfoList.isEmpty()) {
                log.warn("[openPassWindow] 인증 정보를 찾을 수 없습니다. - personalNumber: {}", personalNumber);
                responseData.put("error", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            PersonalVO personal = personalInfoList.get(0);

            responseData.put("personalName", personal.getPersonalName());
            responseData.put("personalNumber", personal.getPersonalNumber());
            responseData.put("CI", personal.getCI());
            responseData.put("DI", personal.getDI());
            responseData.put("personalPhoneNumber", personal.getPersonalPhoneNumber());
            responseData.put("vctmAplySn", personal.getVctmAplySn());

            String ci = personalInfoList.get(0).getCI();
            if (ci == null || ci.isEmpty()) {
                log.warn("[openPassWindow] CI 값이 비어있습니다.");
                responseData.put("error", "CI 정보가 없습니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            String checkCi = mapper.searchVctmAplySn(ci);

            if (personalNumber.length() < 7) {
                log.warn("[openPassWindow] personalNumber 길이가 부족합니다. - length: {}", personalNumber.length());
                responseData.put("error", "주민번호 형식이 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(responseData);
            }

            String gender = (personal.getPersonalNumber().charAt(6) % 2 == 1) ? "남자" : "여자";
            personal.setGender(gender);
            int age = ageChange(personalNumber);
            personal.setAge(age);

            if (checkCi != null) {
                mapper.updatePersonal(personal);
                log.info("[openPassWindow] 개인정보 업데이트 완료 - CI: {}", ci);
            } else {
                mapper.insertPersonal(personal);
                log.info("[openPassWindow] 개인정보 신규 등록 완료");
            }

            //결과화면을 위한 데이터 insert update 분기
            AppVO appVO = appMapper.search(ci);
            if (appVO == null || appVO.getCI() == null || appVO.getCI().isBlank()) {
                appMapper.insert(ci);
                log.info("[openPassWindow] 앱 정보 신규 저장 완료");
            } else {
                appMapper.update(ci);
                log.info("[openPassWindow] 앱 정보 업데이트 완료");
            }

            log.info("[openPassWindow] 디지털원패스인증 API 호출 성공");
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            log.error("[openPassWindow] 디지털원패스인증 API 호출 중 오류 발생", e);
            responseData.put("error", "디지털원패스인증 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(responseData);
        }
    }

    public int ageChange(String personalNumber) {
        try {
            if (personalNumber == null || personalNumber.length() < 7) {
                log.warn("[ageChange] personalNumber가 유효하지 않습니다.");
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
                    log.warn("[ageChange] 알 수 없는 성별코드: {}", genderCode);
                    century = "19";
            }

            String birthStr = century + birthPart;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate birthDate = LocalDate.parse(birthStr, formatter);

            LocalDate today = LocalDate.now();
            return Period.between(birthDate, today).getYears();
        } catch (Exception e) {
            log.error("[ageChange] 나이 계산 중 오류 발생 - personalNumber 길이: {}",
                    personalNumber != null ? personalNumber.length() : "null", e);
            return 0;
        }
    }

}
