package kr.or.nia.dpg.vpos.enums.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VpsExceptionType {

      UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.")
    , TOKEN_IS_NULL(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않습니다.")

    , API_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "API 서버 에러가 발생했습니다.")
    , API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "API 응답 시간을 초과했습니다.")
    , API_CONNECTION_REFUSED(HttpStatus.SERVICE_UNAVAILABLE, "API 서버 연결에 실패했습니다.")
    , API_EMPTY_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "API 응답 본문이 없습니다.")

    , TYPE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "타입이 일치하지 않습니다.")
    , INVALID_SSN(HttpStatus.BAD_REQUEST, "유효하지 않은 주민등록번호입니다.")

    , NO_DATA(HttpStatus.NO_CONTENT, "조회 결과가 없습니다.")

    , PERSONAL_INFO_CONSENT_REQUIRED(HttpStatus.BAD_REQUEST, "개인정보 제3자 제공 동의가 필요합니다.")
    , SIGNATURE_REQUIRED(HttpStatus.BAD_REQUEST, "전자서명이 필요합니다.")
    , SIGNATURE_INVALID(HttpStatus.BAD_REQUEST, "전자서명이 완료되지 않았습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
