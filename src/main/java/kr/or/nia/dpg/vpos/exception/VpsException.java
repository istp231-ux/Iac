package kr.or.nia.dpg.vpos.exception;

import kr.or.nia.dpg.vpos.enums.exception.VpsExceptionType;

/**
 * VPOS 도메인 공통 예외.
 * <p>로깅은 예외 생성 시점이 아니라 실제로 처리하는 곳(전역 예외 핸들러/서비스 catch)에서 수행한다.
 * 생성자에서 로깅하면 동일 예외가 중복 기록되고, 정상 흐름 제어용 예외까지 불필요하게 남는다.</p>
 */
public class VpsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final VpsExceptionType type;
    private final String detail;

    public VpsException(VpsExceptionType type) {
        super(type.getMessage());
        this.type = type;
        this.detail = null;
    }

    // errorBody 담을 때 사용
    public VpsException(VpsExceptionType type, String detail) {
    	super(type.getMessage());
        this.type = type;
        this.detail = detail;
    }

    public VpsExceptionType getType(){
        return type;
    }

	public String getDetail() {
		return detail;
	}
}
