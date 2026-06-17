package kr.or.nia.dpg.vpos.entity.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

// TODO 삭제 필요
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PersonalVO{

	private int vctmAplySn;
	private String certifiDate;
	private String personalPhoneNumber;
	private String personalNumber;
	private String personalName;
	@JsonProperty("CI")
	private String CI;
	@JsonProperty("DI")
	private String DI;
	private int age;
	private String gender;

}
