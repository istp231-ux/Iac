package kr.or.nia.dpg.vpos.web.api.certificate;

import kr.or.nia.dpg.vpos.entity.api.AppVO;
import kr.or.nia.dpg.vpos.entity.api.PersonalVO;
import kr.or.nia.dpg.vpos.enums.exception.VpsExceptionType;
import kr.or.nia.dpg.vpos.exception.VpsException;
import kr.or.nia.dpg.vpos.mapper.AppMapper;
import kr.or.nia.dpg.vpos.mapper.TempMapper;
import kr.or.nia.dpg.vpos.service.api.CertificateApiService;
import kr.or.nia.dpg.vpos.util.DateUtil;
import kr.or.nia.dpg.vpos.util.SessionUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

    private static final int SSN_MIN_LENGTH = 7;

    private final CertificateApiService certifiService;
    private final TempMapper mapper;
    private final AppMapper appMapper;

    public ApiCertificateController(CertificateApiService certifiService, TempMapper mapper, AppMapper appMapper) {
        this.certifiService = certifiService;
        this.mapper = mapper;
        this.appMapper = appMapper;
    }

    // 공동인증 API호출 (1차 인증)
    @PostMapping("/openCertifiWindow")
    public ResponseEntity<Map<String, Object>> openCertifiWindow(
            @RequestBody Map<String, String> requestData, HttpServletRequest request) {

        log.info("[1차인증:공동인증] API 호출 시작. {}", describeSession(request));
        String personalNumber = extractPersonalNumber(requestData);

        PersonalVO personal = firstOrThrow(certifiService.certificateSearch(personalNumber), "openCertifiWindow");

        // 1차 인증 성공 → 세션에 플래그 저장
        SessionUserUtil.setFirstAuthCompleted();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("personalName", personal.getPersonalName());
        responseData.put("personalNumber", personal.getPersonalNumber());
        responseData.put("certifiDate", DateUtil.getTodayDashString());

        log.info("[1차인증:공동인증] 완료. sessionId={}", getSessionId(request));
        return ResponseEntity.ok(responseData);
    }

    // 휴대전화인증 API호출 (2차 인증)
    @PostMapping("/openPhonePCCWindow")
    public ResponseEntity<Map<String, Object>> openPhonePCCWindow(
            @RequestBody Map<String, String> requestData, HttpServletRequest request) {

        log.info("[2차인증:휴대전화] API 호출 시작. {}", describeSession(request));

        // ── 1차 인증 상태 확인 (세션 진단 핵심) ──
        checkFirstAuthWithDiagnosis(request);

        String personalNumber = extractPersonalNumber(requestData);

        PersonalVO personal = firstOrThrow(certifiService.phoneSearch(personalNumber), "openPhonePCCWindow");
        Map<String, Object> responseData = buildPersonalResponse(personal);

        personal.setGender(resolveGender(personalNumber, false));
        personal.setAge(ageChange(personalNumber));

        log.info("[2차인증:휴대전화] 완료.");
        return ResponseEntity.ok(responseData);
    }

    // 아이핀인증 API호출
    @PostMapping("/openCBAWindow")
    public ResponseEntity<Map<String, Object>> openCBAWindow(
            @RequestBody Map<String, String> requestData, HttpServletRequest request) {

        log.info("[인증:아이핀] API 호출 시작. {}", describeSession(request));
        String personalNumber = extractPersonalNumber(requestData);

        PersonalVO personal = firstOrThrow(certifiService.pbaSearch(personalNumber), "openCBAWindow");
        Map<String, Object> responseData = buildPersonalResponse(personal);

        personal.setGender(resolveGender(personalNumber, true));
        personal.setAge(ageChange(personalNumber));
        upsertPersonalAndApp(personal, "openCBAWindow");

        log.info("[인증:아이핀] 완료.");
        return ResponseEntity.ok(responseData);
    }

    // 디지털원패스인증 API호출
    @PostMapping("/openPassWindow")
    public ResponseEntity<Map<String, Object>> openPassWindow(
            @RequestBody Map<String, String> requestData, HttpServletRequest request) {

        log.info("[인증:원패스] API 호출 시작. {}", describeSession(request));
        String personalNumber = extractPersonalNumber(requestData);

        PersonalVO personal = firstOrThrow(certifiService.onepassSearch(personalNumber), "openPassWindow");
        Map<String, Object> responseData = buildPersonalResponse(personal);

        personal.setGender(resolveGender(personalNumber, true));
        personal.setAge(ageChange(personalNumber));
        upsertPersonalAndApp(personal, "openPassWindow");

        log.info("[인증:원패스] 완료.");
        return ResponseEntity.ok(responseData);
    }

    /**
     * 2차 인증 진입 시 1차 인증 상태를 확인하고, 실패 시 원인을 상세히 로그에 남긴다.
     *
     * <p>로그를 보고 바로 판단할 수 있도록 원인별 가이드를 포함한다:</p>
     * <pre>
     * ┌─────────────────────────┬────────────────────────────────────────────────┐
     * │ 로그 패턴               │ 원인                                           │
     * ├─────────────────────────┼────────────────────────────────────────────────┤
     * │ requestedSessionId=null │ 브라우저가 쿠키를 안 보냄 → 프록시/Secure 설정  │
     * │ sessionIdValid=false    │ 세션 만료 or 다른 인스턴스 → LB/세션 설정       │
     * │ isNew=true              │ 새 세션 생성됨 → 이전 세션 유실                 │
     * │ scheme=http+secure쿠키  │ AJP scheme 불일치 → application-ajp.yml 확인    │
     * │ 모두 정상인데 미완료     │ 1차에서 setFirstAuthCompleted() 미호출          │
     * └─────────────────────────┴────────────────────────────────────────────────┘
     * </pre>
     */
    private void checkFirstAuthWithDiagnosis(HttpServletRequest request) {
        if (!SessionUserUtil.isFirstAuthCompleted()) {
            HttpSession session = request.getSession(false);
            log.error("┌──────────────────────────────────────────────────────────────────────┐");
            log.error("│ ★ [2차인증] 1차 인증 미완료 상태에서 2차 인증 시도 — 원인 분석 시작 ★ │");
            log.error("├──────────────────────────────────────────────────────────────────────┤");
            log.error("│ sessionId          = {}", session != null ? session.getId() : "NO_SESSION");
            log.error("│ session.isNew      = {}", session != null ? session.isNew() : "N/A");
            log.error("│ requestedSessionId = {}", request.getRequestedSessionId());
            log.error("│ sessionIdValid     = {}", request.isRequestedSessionIdValid());
            log.error("│ fromCookie         = {}", request.isRequestedSessionIdFromCookie());
            log.error("│ scheme             = {}", request.getScheme());
            log.error("│ isSecure           = {}", request.isSecure());
            log.error("│ X-Forwarded-Proto  = {}", request.getHeader("X-Forwarded-Proto"));
            log.error("│ X-Forwarded-For    = {}", request.getHeader("X-Forwarded-For"));
            log.error("│ remoteAddr         = {}", request.getRemoteAddr());
            log.error("├──────────────────────────────────────────────────────────────────────┤");

            if (request.getRequestedSessionId() == null) {
                log.error("│ [진단결과:우리서버/인프라] 브라우저가 JSESSIONID 쿠키를 보내지 않음.");
                log.error("│ → 확인1: Set-Cookie 응답에 Secure 플래그가 있는데 HTTP로 접속하고 있지 않은지");
                log.error("│ → 확인2: AJP 프록시가 Set-Cookie 헤더를 제거하고 있지 않은지");
                log.error("│ → 확인3: Cookie Path가 /vpos 인데 요청 URI가 다른 경로가 아닌지");
                log.error("│ → 확인4: 도메인/SameSite 설정이 맞는지");
            } else if (!request.isRequestedSessionIdValid()) {
                log.error("│ [진단결과:우리서버] 브라우저가 보낸 세션ID가 서버에서 유효하지 않음.");
                log.error("│ → 확인1: 세션 타임아웃(현재 20분)이 지나지 않았는지");
                log.error("│ → 확인2: 서버가 여러 대인 경우 Sticky Session이 설정되어 있는지");
                log.error("│ → 확인3: 서버 재기동으로 세션이 초기화되지 않았는지");
            } else if (session != null && session.isNew()) {
                log.error("│ [진단결과:우리서버] 세션ID는 유효한데 새 세션이 생성됨 (비정상).");
                log.error("│ → 서버 내부에서 세션이 교체(changeSessionId 등)되었을 가능성");
            } else {
                log.error("│ [진단결과:우리서버/코드] 세션은 정상인데 1차인증 플래그가 없음.");
                log.error("│ → 1차인증(openCertifiWindow)에서 setFirstAuthCompleted()가 호출되지 않았을 가능성");
                log.error("│ → 1차인증이 다른 경로(금융인증서 등)로 진행되어 세션 플래그 미설정");
            }

            log.error("└──────────────────────────────────────────────────────────────────────┘");
        } else {
            log.info("[2차인증:휴대전화] 1차 인증 완료 확인됨. sessionId={}", getSessionId(request));
        }
    }

    /**
     * 요청의 세션 상태를 진단 문자열로 만든다.
     */
    private String describeSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return String.format(
                "sessionId=%s, isNew=%s, requestedSessionId=%s, sessionIdValid=%s, "
                + "fromCookie=%s, scheme=%s, isSecure=%s, remoteAddr=%s",
                session != null ? session.getId() : "NO_SESSION",
                session != null ? String.valueOf(session.isNew()) : "N/A",
                request.getRequestedSessionId(),
                request.isRequestedSessionIdValid(),
                request.isRequestedSessionIdFromCookie(),
                request.getScheme(),
                request.isSecure(),
                request.getRemoteAddr());
    }

    private String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : "NO_SESSION";
    }

    private String extractPersonalNumber(Map<String, String> requestData) {
        String personalNumber = requestData != null ? requestData.get("personalNumber") : null;
        if (personalNumber == null || personalNumber.trim().isEmpty()) {
            log.warn("[ApiCertificateController] 필수 파라미터 personalNumber가 비어있습니다.");
            throw new VpsException(VpsExceptionType.INVALID_SSN);
        }
        return personalNumber.trim();
    }

    private PersonalVO firstOrThrow(List<PersonalVO> personalInfoList, String methodName) {
        if (personalInfoList == null || personalInfoList.isEmpty()) {
            log.warn("[{}] [원인:외부서버] 인증 API 응답에 데이터가 없습니다.", methodName);
            throw new VpsException(VpsExceptionType.NO_DATA,
                    "인증 API 응답 결과 비어있음", "인증서API(certifi)");
        }
        return personalInfoList.get(0);
    }

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

    private void upsertPersonalAndApp(PersonalVO personal, String methodName) {
        String ci = personal.getCI();
        if (ci == null || ci.trim().isEmpty()) {
            log.warn("[{}] CI 값이 비어있어 저장을 진행할 수 없습니다.", methodName);
            throw new VpsException(VpsExceptionType.NO_DATA);
        }

        try {
            if (mapper.searchVctmAplySn(ci) != null) {
                mapper.updatePersonal(personal);
                log.info("[{}] 개인정보 업데이트 완료", methodName);
            } else {
                mapper.insertPersonal(personal);
                log.info("[{}] 개인정보 신규 등록 완료", methodName);
            }
        } catch (Exception e) {
            log.error("[{}] [원인:내부서버] 개인정보 DB 저장 실패 - exType={}, error={}", methodName, e.getClass().getSimpleName(), e.getMessage());
            throw new VpsException(VpsExceptionType.DB_ERROR,
                    "개인정보 저장 실패: " + e.getMessage());
        }

        try {
            AppVO appVO = appMapper.search(ci);
            if (appVO == null || appVO.getCI() == null || appVO.getCI().isBlank()) {
                appMapper.insert(ci);
                log.info("[{}] 앱 정보 신규 저장 완료", methodName);
            } else {
                appMapper.update(ci);
                log.info("[{}] 앱 정보 업데이트 완료", methodName);
            }
        } catch (Exception e) {
            log.error("[{}] [원인:내부서버] 앱 정보 DB 저장 실패 - exType={}, error={}", methodName, e.getClass().getSimpleName(), e.getMessage());
            throw new VpsException(VpsExceptionType.DB_ERROR,
                    "앱 정보 저장 실패: " + e.getMessage());
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
            log.error("[ageChange] 생년월일 파싱 실패로 나이를 0으로 처리합니다. exType={}, error={}", e.getClass().getSimpleName(), e.getMessage());
            return 0;
        }
    }

}
