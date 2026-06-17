package kr.or.nia.dpg.vpos.entity.api;

import lombok.*;

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
