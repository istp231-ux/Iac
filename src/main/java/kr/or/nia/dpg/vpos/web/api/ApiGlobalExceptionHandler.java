package kr.or.nia.dpg.vpos.web.api;

import kr.or.nia.dpg.vpos.exception.VpsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "kr.or.nia.dpg.vpos.web.api")
@Slf4j
public class ApiGlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("[ApiGlobalExceptionHandler] RuntimeException 발생", ex);
        return ResponseEntity.internalServerError().body("오류가 발생했습니다.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        FieldError error = (FieldError) ex.getBindingResult().getAllErrors().get(0);
        log.error("[ApiGlobalExceptionHandler] 요청 파라미터 검증 실패 - field: {}, message: {}",
                error.getField(), error.getDefaultMessage());
        return ResponseEntity.badRequest().body(error.getDefaultMessage());
    }

    @ExceptionHandler(VpsException.class)
    public ResponseEntity<String> handleVpsException(VpsException ex) {
        log.error("[ApiGlobalExceptionHandler] VpsException 발생 - type: {}, detail: {}",
                ex.getType().name(), ex.getDetail());
        return ResponseEntity.status(ex.getType().getHttpStatus()).body(ex.getMessage());
    }
}
