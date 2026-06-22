package kr.or.nia.dpg.vpos.web.api;

import kr.or.nia.dpg.vpos.enums.exception.VpsExceptionType;
import kr.or.nia.dpg.vpos.exception.VpsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * API 컨트롤러 전역 예외 핸들러.
 *
 * <p>예외 발생 시 {@link VpsExceptionType.ErrorOrigin}에 따라 원인을 분류하고,
 * 요청/세션 상태를 상세히 기록하여 내부서버/외부서버/네트워크 중 어디에서 문제가 발생했는지
 * 서버 로그만으로 즉시 판단할 수 있도록 한다.</p>
 *
 * <p>네트워크 예외(SocketTimeout, ConnectException, UnknownHost, SSL)는
 * 별도 핸들러로 분리하여 통신 구간별 원인을 세분화한다.</p>
 */
@RestControllerAdvice(basePackages = "kr.or.nia.dpg.vpos.web.api")
@Slf4j
public class ApiGlobalExceptionHandler {

    /** VpsException 처리 — ErrorOrigin에 따라 로그 태그와 레벨을 분기 */
    @ExceptionHandler(VpsException.class)
    public ResponseEntity<Map<String, Object>> handleVpsException(VpsException ex, HttpServletRequest request) {
        VpsExceptionType type = ex.getType();
        HttpStatus status = type.getHttpStatus();
        VpsExceptionType.ErrorOrigin origin = type.getOrigin();
        String reqInfo = describeRequest(request);

        switch (origin) {
            case EXTERNAL:
                log.error("[API예외:외부서버] type={}, api={}, detail={}, message={}, {}",
                        type.name(),
                        ex.getExternalApiName() != null ? ex.getExternalApiName() : "unknown",
                        ex.getDetail(),
                        type.getMessage(),
                        reqInfo, ex);
                break;
            case NETWORK:
                log.error("[API예외:네트워크] type={}, api={}, detail={}, {} - 네트워크 경로 확인 필요",
                        type.name(),
                        ex.getExternalApiName() != null ? ex.getExternalApiName() : "unknown",
                        ex.getDetail(),
                        reqInfo, ex);
                break;
            case INTERNAL:
                log.error("[API예외:내부서버] type={}, detail={}, message={}, {}",
                        type.name(), ex.getDetail(), type.getMessage(), reqInfo, ex);
                break;
            case CLIENT:
                log.warn("[API예외:클라이언트] type={}, detail={}, {}", type.name(), ex.getDetail(), reqInfo);
                break;
            default:
                log.error("[API예외] type={}, detail={}, {}", type.name(), ex.getDetail(), reqInfo, ex);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("error", type.name());
        body.put("origin", origin.name());
        body.put("message", ex.getMessage());
        if (ex.getDetail() != null) {
            body.put("detail", ex.getDetail());
        }

        return ResponseEntity.status(status).body(body);
    }

    /** 외부 API 응답 시간 초과 — readTimeout 초과 시 발생 */
    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleSocketTimeout(
            SocketTimeoutException ex, HttpServletRequest request) {
        log.error("[API예외:외부서버] 외부 API 응답 시간 초과 - error={}, {}", ex.getMessage(), describeRequest(request), ex);
        return buildErrorResponse(HttpStatus.GATEWAY_TIMEOUT, "EXTERNAL",
                "API_TIMEOUT", "외부 API 서버가 응답 시간을 초과했습니다.");
    }

    /** 외부 API 서버 연결 실패 — 서버 다운 또는 포트 미오픈 */
    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<Map<String, Object>> handleConnectException(
            ConnectException ex, HttpServletRequest request) {
        log.error("[API예외:외부서버] 외부 API 서버 연결 실패 - error={}, {}", ex.getMessage(), describeRequest(request), ex);
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL",
                "API_CONNECTION_REFUSED", "외부 API 서버에 연결할 수 없습니다.");
    }

    /** DNS 조회 실패 — 외부 API 호스트명 미등록 또는 네트워크 단절 */
    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity<Map<String, Object>> handleUnknownHost(
            UnknownHostException ex, HttpServletRequest request) {
        log.error("[API예외:네트워크] DNS 조회 실패 - host={}, {}", ex.getMessage(), describeRequest(request), ex);
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "NETWORK",
                "API_DNS_ERROR", "외부 API 서버 도메인을 찾을 수 없습니다.");
    }

    /** SSL/TLS 통신 오류 — 인증서 만료/불일치 등 */
    @ExceptionHandler(SSLException.class)
    public ResponseEntity<Map<String, Object>> handleSSLException(
            SSLException ex, HttpServletRequest request) {
        log.error("[API예외:외부서버] SSL 통신 오류 - error={}, {}", ex.getMessage(), describeRequest(request), ex);
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, "EXTERNAL",
                "API_SSL_ERROR", "외부 API 서버와 SSL 통신에 실패했습니다.");
    }

    /** @Valid 검증 실패 — 요청 파라미터 유효성 오류 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        FieldError error = (FieldError) ex.getBindingResult().getAllErrors().get(0);
        log.warn("[API예외:클라이언트] 요청 파라미터 검증 실패 - field={}, rejected={}, message={}, {}",
                error.getField(), error.getRejectedValue(), error.getDefaultMessage(),
                describeRequest(request));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "CLIENT",
                "VALIDATION_ERROR", error.getDefaultMessage());
    }

    /** 예상치 못한 RuntimeException 포괄 처리 — 위 핸들러에 매칭되지 않는 모든 런타임 예외 */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        log.error("[API예외:내부서버] 예상치 못한 오류 발생 - exType={}, error={}, {}",
                ex.getClass().getSimpleName(), ex.getMessage(), describeRequest(request), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL",
                "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.");
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String origin, String errorCode, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", errorCode);
        body.put("origin", origin);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    /**
     * 요청/세션 상세 진단 문자열 생성.
     * <p>세션 ID, 쿠키 상태, 프록시 헤더 등을 포함하여
     * 세션 끊김/인증 실패 원인을 로그만으로 추적할 수 있게 한다.</p>
     */
    private String describeRequest(HttpServletRequest request) {
        if (request == null) {
            return "request=null";
        }
        HttpSession session = request.getSession(false);
        return String.format("uri=%s, method=%s, remoteAddr=%s, sessionId=%s, isNew=%s, "
                        + "requestedSessionId=%s, sessionIdValid=%s, fromCookie=%s, "
                        + "scheme=%s, isSecure=%s, X-Forwarded-Proto=%s, X-Forwarded-For=%s, "
                        + "User-Agent=%s",
                request.getRequestURI(),
                request.getMethod(),
                request.getRemoteAddr(),
                session != null ? session.getId() : "NO_SESSION",
                session != null ? String.valueOf(session.isNew()) : "N/A",
                request.getRequestedSessionId(),
                request.isRequestedSessionIdValid(),
                request.isRequestedSessionIdFromCookie(),
                request.getScheme(),
                request.isSecure(),
                request.getHeader("X-Forwarded-Proto"),
                request.getHeader("X-Forwarded-For"),
                request.getHeader("User-Agent"));
    }
}
