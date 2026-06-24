package kr.or.nia.dpg.vpos.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 서버/세션/토큰에 저장될 인증된 사용자 객체
 */
@Getter
@Setter
public class SessionUser implements Serializable {

	private static final long serialVersionUID = 1L;

    String userName;
    String ssn;
    String mobileNumber;
    String email;
    String di;
}
