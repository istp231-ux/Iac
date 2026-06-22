package kr.or.nia.dpg.vpos.enums.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * VPOS 도메인 예외 유형 정의.
 *
 * <p>각 예외 타입은 HTTP 상태코드, 사용자 메시지, 오류 원인({@link ErrorOrigin})을 가진다.
 * ErrorOrigin은 예외 발생 시 서버 로그에 원인 태그([원인:외부서버], [원인:내부서버] 등)로 기록되어
 * 우리 서버/외부 서버/네트워크/클라이언트 중 어디에서 문제가 발생했는지 즉시 식별할 수 있다.</p>
 */
@Getter
@RequiredArgsConstructor
public enum VpsExceptionType {

    // ── 인증/세션 ──
      UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.", ErrorOrigin.INTERNAL)
    , TOKEN_IS_NULL(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않습니다.", ErrorOrigin.INTERNAL)

    // ── 외부 API 통신 오류 (원인: 외부 서버) ──
    , API_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "API 서버 에러가 발생했습니다.", ErrorOrigin.EXTERNAL)
    , API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "API 응답 시간을 초과했습니다.", ErrorOrigin.EXTERNAL)
    , API_CONNECTION_REFUSED(HttpStatus.SERVICE_UNAVAILABLE, "API 서버 연결에 실패했습니다.", ErrorOrigin.EXTERNAL)
    , API_EMPTY_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "API 응답 본문이 없습니다.", ErrorOrigin.EXTERNAL)
    , API_RESPONSE_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "API 응답 파싱에 실패했습니다.", ErrorOrigin.EXTERNAL)
    , API_RESULT_CODE_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 응답 결과코드가 실패입니다.", ErrorOrigin.EXTERNAL)
    , API_SSL_ERROR(HttpStatus.BAD_GATEWAY, "API 서버 SSL 인증에 실패했습니다.", ErrorOrigin.EXTERNAL)
    , API_DNS_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "API 서버 도메인 조회에 실패했습니다.", ErrorOrigin.NETWORK)

    // ── 내부 처리 오류 (원인: 우리 서버) ──
    , TYPE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "타입이 일치하지 않습니다.", ErrorOrigin.INTERNAL)
    , INVALID_SSN(HttpStatus.BAD_REQUEST, "유효하지 않은 주민등록번호입니다.", ErrorOrigin.CLIENT)
    , ENCRYPTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "암호화 처리 중 오류가 발생했습니다.", ErrorOrigin.INTERNAL)
    , DB_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 처리 중 오류가 발생했습니다.", ErrorOrigin.INTERNAL)
    , CONFIG_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 설정 오류가 발생했습니다.", ErrorOrigin.INTERNAL)

    // ── 데이터 ──
    , NO_DATA(HttpStatus.NO_CONTENT, "조회 결과가 없습니다.", ErrorOrigin.CLIENT)

    // ── 업무 검증 ──
    , PERSONAL_INFO_CONSENT_REQUIRED(HttpStatus.BAD_REQUEST, "개인정보 제3자 제공 동의가 필요합니다.", ErrorOrigin.CLIENT)
    , SIGNATURE_REQUIRED(HttpStatus.BAD_REQUEST, "전자서명이 필요합니다.", ErrorOrigin.CLIENT)
    , SIGNATURE_INVALID(HttpStatus.BAD_REQUEST, "전자서명이 완료되지 않았습니다.", ErrorOrigin.CLIENT)
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final ErrorOrigin origin;

    /**
     * 오류 원인 분류.
     * <ul>
     *   <li>INTERNAL : 우리 서버 내부 오류 (설정, DB, 암호화 등)</li>
     *   <li>EXTERNAL : 외부 API 서버 오류 (HTTP 에러, 응답 실패 등)</li>
     *   <li>NETWORK  : 네트워크 구간 오류 (DNS, 라우팅 등)</li>
     *   <li>CLIENT   : 클라이언트 입력 오류 (파라미터 검증 실패 등)</li>
     * </ul>
     */
    public enum ErrorOrigin {
        INTERNAL,
        EXTERNAL,
        NETWORK,
        CLIENT
    }
}
