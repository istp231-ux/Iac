package kr.or.nia.dpg.vpos.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kr.or.nia.dpg.vpos.enums.exception.VpsExceptionType;

public class VpsException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(VpsException.class);

	private final VpsExceptionType type;
    private final String detail;

    public VpsException(VpsExceptionType type) {
        super(type.getMessage());
        this.type = type;
        this.detail = null;
        log.warn("[VpsException] 예외 발생 - type: {}, message: {}", type.name(), type.getMessage());
    }

    // errorBody 담을 때 사용
    public VpsException(VpsExceptionType type, String detail) {
    	super(type.getMessage());
        this.type = type;
        this.detail = detail;
        log.warn("[VpsException] 예외 발생 - type: {}, message: {}, detail: {}", type.name(), type.getMessage(), detail);
    }

    public VpsExceptionType getType(){
        return type;
    }

	public String getDetail() {
		return detail;
	}
}
