package kr.or.nia.dpg.vpos.entity.api;

import lombok.*;

/** 앱 서비스 가입 현황 VO. CI를 기준으로 각 서비스(계좌/대출/사고/통신/결제) 가입 여부를 저장한다. */
// TODO 삭제 필요
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppVO {

	private String CI;
	private String account;
    private String loan;
    private String accident;
    private String mobile;
    private String payment;

}
