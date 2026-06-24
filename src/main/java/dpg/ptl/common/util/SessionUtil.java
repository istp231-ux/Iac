package dpg.ptl.common.util;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * <pre>
 * @Class Name : SessionUtil.java
 * @Description : 세션 및 Request 정보 유틸
 * </pre>
 *
 * @Author : cwj
 * @Date : 2024. 10. 30.
 */
public class SessionUtil {

	private static final Logger log = LoggerFactory.getLogger(SessionUtil.class);

	/**
	 * <pre>
	 * 개요 : session attribute 값을 가져 오기 위한  method
	 * </pre>
	 * @Author cwj
	 * @Date : 2024. 10. 30.
	 * @param name
	 * @return
	 */
	 public static Object getSessionAttribute(String name) {
		 try {
			 if(RequestContextHolder.getRequestAttributes() == null
					 || RequestContextHolder.getRequestAttributes().getAttribute(name, RequestAttributes.SCOPE_SESSION) == null) {
				 return null;
			 } else {
				 return (Object)RequestContextHolder.getRequestAttributes().getAttribute(name, RequestAttributes.SCOPE_SESSION);
			 }
		 } catch (Exception e) {
			 log.error("[SessionUtil.getSessionAttribute] 세션 속성 조회 실패 - name: {}", name, e);
			 return null;
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : session attribute 설정  method
	  * </pre>
	  * @Author cwj
	  * @Date : 2024. 10. 30.
	  * @param name
	  * @param object
	  */
	 public static void setSessionAttribute(String name, Object object){
		 try {
			 RequestContextHolder.getRequestAttributes().setAttribute(name, object, RequestAttributes.SCOPE_SESSION);
		 } catch (Exception e) {
			 log.error("[SessionUtil.setSessionAttribute] 세션 속성 설정 실패 - name: {}", name, e);
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : Request 객체를 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2024. 10. 30.
	  * @return
	  */
	 public static HttpServletRequest getRequest() {
		 try {
			 ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
			 HttpServletRequest req = sra.getRequest();
			 return req;
		 } catch (Exception e) {
			 log.error("[SessionUtil.getRequest] Request 객체 조회 실패", e);
			 return null;
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : Session 객체를 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2024. 11. 1.
	  * @return HttpSession
	  */
	 public static HttpSession getSession() {
		 try {
			 HttpServletRequest request = getRequest();
			 if(request == null) {
				 log.warn("[SessionUtil.getSession] Request 객체가 null입니다.");
				 return null;
			 }
			 return request.getSession();
		 } catch (Exception e) {
			 log.error("[SessionUtil.getSession] Session 객체 조회 실패", e);
			 return null;
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 세션에 에러메시지를 세팅한다. (시스템 내부에서만 사용하며 1회성)
	  * </pre>
	  * @Author cwj
	  * @Date : 2024. 11. 1.
	  * @param errorMsg 에러메시지
	  */
	 public static void setErrorMsg(String errorMsg) {
		 try {
			 setSessionAttribute("ERROR_MSG", errorMsg);
		 } catch (Exception e) {
			 log.error("[SessionUtil.setErrorMsg] 에러메시지 세팅 실패 - errorMsg: {}", errorMsg, e);
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 세션에 에러메시지를 세팅한다. (api call용)
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 3. 24.
	  * @param json 파라미터
	  */
	 public static void setCallParam(String json) {
		 try {
			 if(RequestContextHolder.getRequestAttributes() != null){
				 setSessionAttribute("CALL_PARAM", json);
			 }
		 } catch (Exception e) {
			 log.error("[SessionUtil.setCallParam] API Call 파라미터 세팅 실패", e);
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 세션의 에러메시지를 가져온다.(내부 처리용)
	  * </pre>
	  * @Author cwj
	  * @Date : 2024. 11. 1.
	  * @return String
	  */
	 public static String getErrorMsg() {
		 try {
			 return StringUtil.nullToStr(String.valueOf(getSessionAttribute("ERROR_MSG")));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getErrorMsg] 에러메시지 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 세션의 에러메시지를 가져온다.(api call용)
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 3. 24.
	  * @return String
	  */
	 public static String getCallParam() {
		 try {
			 return StringUtil.nullToStr(String.valueOf(getSessionAttribute("CALL_PARAM")));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getCallParam] API Call 파라미터 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 세션 에러메시지를 삭제한다.(내부 처리용)
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 3. 24.
	  */
	 public static void removeErrorMsg() {
		 try {
			 HttpSession session = getSession();
			 if(session != null) {
				 session.removeAttribute("ERROR_MSG");
			 } else {
				 log.warn("[SessionUtil.removeErrorMsg] Session 객체가 null입니다.");
			 }
		 } catch (Exception e) {
			 log.error("[SessionUtil.removeErrorMsg] 에러메시지 삭제 실패", e);
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 세션 에러메시지를 삭제한다.(api call용)
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 3. 24.
	  */
	 public static void removeCallParam() {
		 try {
			 HttpSession session = getSession();
			 if(session != null) {
				 session.removeAttribute("CALL_PARAM");
			 } else {
				 log.warn("[SessionUtil.removeCallParam] Session 객체가 null입니다.");
			 }
		 } catch (Exception e) {
			 log.error("[SessionUtil.removeCallParam] API Call 파라미터 삭제 실패", e);
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 회원 아이디를 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2024. 10. 30.
	  * @return String
	  */
	 public static String getMbrId() {
		 try {
			 return String.valueOf(getSessionAttribute("mbrId"));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getMbrId] 회원 아이디 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 회원 일련번호를 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2024. 11. 28.
	  * @return String
	  */
	 public static String getMbrSn() {
		 try {
			 return String.valueOf(getSessionAttribute("mbrSn"));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getMbrSn] 회원 일련번호 조회 실패", e);
			 return "";
		 }
	 }

	/**
	  * <pre>
	  * 개요 : 참여자 아이디를 가져온다.
	  * </pre>
	  * @Author kss
	  * @Date : 2026. 01. 20.
	  * @return String
	  */
	 public static String getPrtpntId() {
		 try {
			 return String.valueOf(getSessionAttribute("prtpntId"));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getPrtpntId] 참여자 아이디 조회 실패", e);
			 return "";
		 }
	 }

	/**
	  * <pre>
	  * 개요 : 참여자 구분코드를 가져온다.
	  * </pre>
	  * @Author kss
	  * @Date : 2026. 02. 02.
	  * @return String
	  */
	 public static String getPrtpntSeCd() {
		 try {
			 return String.valueOf(getSessionAttribute("prtpntSeCd"));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getPrtpntSeCd] 참여자 구분코드 조회 실패", e);
			 return "";
		 }
	 }


	 /**
	  * <pre>
	  * 개요 : 회원명을 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2024. 12. 19.
	  * @return String
	  */
	 public static String getMbrNm() {
		 try {
			 String mbrNm = String.valueOf(getSessionAttribute("mbrNm"));
			 if(mbrNm == null || "null".equals(mbrNm) || mbrNm.isEmpty()) {
				 log.warn("[SessionUtil.getMbrNm] 회원명이 null 또는 빈 값입니다.");
				 return "";
			 }
			 if(mbrNm.length()==2) {
				 mbrNm = mbrNm.substring(0,1)+"*";
			 } else if(mbrNm.length()==3){
				 mbrNm = mbrNm.substring(0,1)+"*"+mbrNm.substring(2,3);
			 } else if(mbrNm.length() > 3) {
				 mbrNm = mbrNm.substring(0,1)+"**"+mbrNm.substring(3,mbrNm.length());
			 } else {
				 return "이름이 잘못되었습니다.";
			 }
			 return mbrNm;
		 } catch (Exception e) {
			 log.error("[SessionUtil.getMbrNm] 회원명 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 회원명을 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 3. 7.
	  * @param nameYn
	  * @return String
	  */
	 public static String getMbrNm(boolean nameYn) {
		 try {
			 if(nameYn) {
				 String mbrNm = String.valueOf(getSessionAttribute("mbrNm"));
				 if(mbrNm == null || "null".equals(mbrNm) || mbrNm.isEmpty()) {
					 log.warn("[SessionUtil.getMbrNm] 회원명이 null 또는 빈 값입니다.");
					 return "";
				 }
				 if(mbrNm.length()==2) {
					 mbrNm = mbrNm.substring(0,1)+"*";
				 } else if(mbrNm.length()==3){
					 mbrNm = mbrNm.substring(0,1)+"*"+mbrNm.substring(2,3);
				 } else if(mbrNm.length() > 3) {
					 mbrNm = mbrNm.substring(0,1)+"**"+mbrNm.substring(3,mbrNm.length());
				 } else {
					 return "이름이 잘못되었습니다.";
				 }
				 return mbrNm;
			 } else {
				 return String.valueOf(getSessionAttribute("mbrNm"));
			 }
		 } catch (Exception e) {
			 log.error("[SessionUtil.getMbrNm] 회원명 조회 실패 - nameYn: {}", nameYn, e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 로그인 여부를 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2024. 12. 12.
	  * @return boolean
	  */
	 public static boolean isLogin() {
		 try {
			 if(getSessionAttribute("isLogin") != null) {
				 return (boolean)getSessionAttribute("isLogin");
			 }
			 return false;
		 } catch (Exception e) {
			 log.error("[SessionUtil.isLogin] 로그인 여부 조회 실패", e);
			 return false;
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 로그인 여부를 세팅한다. 호출되면 세팅.
	  * </pre>
	  * @Author cwj
	  * @Date : 2024. 12. 12.
	  */
	 public static void setIsLogin(boolean bool) {
		 try {
			 setSessionAttribute("isLogin", bool);
		 } catch (Exception e) {
			 log.error("[SessionUtil.setIsLogin] 로그인 여부 세팅 실패 - bool: {}", bool, e);
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 세션 Id를 저장한다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 1. 20.
	  * @param sessId
	  */
	 public static void setSessionId(String sessId) {
		 try {
			 setSessionAttribute("sessId", sessId);
		 } catch (Exception e) {
			 log.error("[SessionUtil.setSessionId] 세션 ID 저장 실패", e);
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 세션에 저장된 Session Id를 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 1. 20.
	  * @return
	  */
	 public static String getSessionId() {
		 try {
			 return String.valueOf(getSessionAttribute("sessId"));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getSessionId] 세션 ID 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 로그인 시스템 플래그를 생성한다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 1. 20.
	  * @param loginSystemFlag
	  */
	 public static void setLoginSystemFlag(String loginSystemFlag) {
		 try {
			 setSessionAttribute("loginSystemFlag", loginSystemFlag);
		 } catch (Exception e) {
			 log.error("[SessionUtil.setLoginSystemFlag] 로그인 시스템 플래그 설정 실패", e);
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 로그인 시스템 플래그를 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 1. 20.
	  * @return String
	  */
	 public static String getLoginSystemFlag() {
		 try {
			 return String.valueOf(getSessionAttribute("loginSystemFlag"));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getLoginSystemFlag] 로그인 시스템 플래그 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 중복 로그인 flag 값을 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 1. 22.
	  * @return
	  */
	 public static String getCheckDuplFlag() {
		 try {
			 return String.valueOf(getSessionAttribute("duplCheck"));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getCheckDuplFlag] 중복 로그인 플래그 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 중복 로그인 flag 값을 삭제한다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 1. 22.
	  */
	 public static void removeCheckDuplFlag() {
		 try {
			 HttpSession session = getSession();
			 if(session != null) {
				 session.removeAttribute("duplCheck");
			 } else {
				 log.warn("[SessionUtil.removeCheckDuplFlag] Session 객체가 null입니다.");
			 }
		 } catch (Exception e) {
			 log.error("[SessionUtil.removeCheckDuplFlag] 중복 로그인 플래그 삭제 실패", e);
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 회원 닉네임을 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 1. 23.
	  * @return
	  */
	 public static String getMbrNick() {
		 try {
			 return String.valueOf(getSessionAttribute("mbrNick"));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getMbrNick] 회원 닉네임 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 회원 구분값을 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 1. 23.
	  * @return
	  */
	 public static String getMbrScd() {
		 try {
			 return String.valueOf(getSessionAttribute("mbrScd"));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getMbrScd] 회원 구분값 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 사용자 이메일을 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 2. 5.
	  * @return
	  */
	 public static String getEmail() {
		 try {
			 String emailReal = String.valueOf(getSessionAttribute("emlAddr"));
			 if(emailReal == null || "null".equals(emailReal) || emailReal.isEmpty() || !emailReal.contains("@")) {
				 log.warn("[SessionUtil.getEmail] 이메일 정보가 유효하지 않습니다.");
				 return "";
			 }
			 String emailSplit[] = emailReal.split("@");
			 String emailTail[] = emailSplit[1].split("\\.");
			 if(emailTail.length < 2) {
				 log.warn("[SessionUtil.getEmail] 이메일 도메인 형식이 유효하지 않습니다.");
				 return "";
			 }
			 String emailFirst = emailSplit[0].substring(0, 1);
			 String email = emailFirst;
			 for(int i=1;i<emailSplit[0].length();i++) {
				 email = email+"*";
			 }
			 email = email + "@" + emailSplit[1].substring(0, 1);
			 for(int i=1;i<emailTail[0].length();i++) {
				 email = email+"*";
			 }
			 email = email+"."+emailTail[1];
			 return email;
		 } catch (Exception e) {
			 log.error("[SessionUtil.getEmail] 사용자 이메일 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 사용자 이메일을 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 2. 5.
	  * @param maskYn 마스킹 여부
	  * @return
	  */
	 public static String getEmail(boolean maskYn) {
		 try {
			 if(maskYn) {
				 String emailReal = String.valueOf(getSessionAttribute("emlAddr"));
				 if(emailReal == null || "null".equals(emailReal) || emailReal.isEmpty() || !emailReal.contains("@")) {
					 log.warn("[SessionUtil.getEmail] 이메일 정보가 유효하지 않습니다.");
					 return "";
				 }
				 String emailSplit[] = emailReal.split("@");
				 String emailTail[] = emailSplit[1].split("\\.");
				 if(emailTail.length < 2) {
					 log.warn("[SessionUtil.getEmail] 이메일 도메인 형식이 유효하지 않습니다.");
					 return "";
				 }
				 String emailFirst = emailSplit[0].substring(0, 1);
				 String email = emailFirst;
				 for(int i=1;i<emailSplit[0].length();i++) {
					 email = email+"*";
				 }
				 email = email + "@" + emailSplit[1].substring(0, 1);
				 for(int i=1;i<emailTail[0].length();i++) {
					 email = email+"*";
				 }
				 email = email+"."+emailTail[1];
				 return email;
			 } else {
				 return String.valueOf(getSessionAttribute("emlAddr"));
			 }
		 } catch (Exception e) {
			 log.error("[SessionUtil.getEmail] 사용자 이메일 조회 실패 - maskYn: {}", maskYn, e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 사용자 전화번호를 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 2. 5.
	  * @return
	  */
	 public static String getTelNo() {
		 try {
			 String realTelNo = String.valueOf(getSessionAttribute("mbrTelno"));
			 if(realTelNo != null && !"null".equals(realTelNo) && !"".equals(realTelNo) && realTelNo.length() > 8) {
				 String telNo = realTelNo.substring(0, 3);
				 telNo = telNo + "****" + realTelNo.substring(7, realTelNo.length());
				 return telNo;
			 } else {
				 return "";
			 }
		 } catch (Exception e) {
			 log.error("[SessionUtil.getTelNo] 사용자 전화번호 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 사용자 전화번호를 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 2. 5.
	  * @return
	  */
	 public static String getTelNo(boolean maskYn) {
		 try {
			 if(maskYn) {
				 String realTelNo = String.valueOf(getSessionAttribute("mbrTelno"));
				 if(realTelNo != null && !"null".equals(realTelNo) && !"".equals(realTelNo) && realTelNo.length() > 8) {
					 String telNo = realTelNo.substring(0, 3);
					 telNo = telNo + "****" + realTelNo.substring(7, realTelNo.length());
					 return telNo;
				 } else {
					 return "";
				 }
			 } else {
				 return String.valueOf(getSessionAttribute("mbrTelno"));
			 }
		 } catch (Exception e) {
			 log.error("[SessionUtil.getTelNo] 사용자 전화번호 조회 실패 - maskYn: {}", maskYn, e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 기업회원 여부를 가져온다.(Y/N)
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 3. 7.
	  * @return String
	  */
	 public static String getCompanyYn() {
		 try {
			 return String.valueOf(getSessionAttribute("companyYn"));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getCompanyYn] 기업회원 여부 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 그룹일련 번호를 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 3. 7.
	  * @return String
	  */
	 public static String getMbrGroupSn() {
		 try {
			 return String.valueOf(getSessionAttribute("mbrGroupSn"));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getMbrGroupSn] 그룹 일련번호 조회 실패", e);
			 return "";
		 }
	 }

	 /**
	  * <pre>
	  * 개요 : 그룹명을 가져온다.
	  * </pre>
	  * @Author cwj
	  * @Date : 2025. 3. 11.
	  * @return String
	  */
	 public static String getMbrGroupNm() {
		 try {
			 return String.valueOf(getSessionAttribute("mbrGroupNm"));
		 } catch (Exception e) {
			 log.error("[SessionUtil.getMbrGroupNm] 그룹명 조회 실패", e);
			 return "";
		 }
	 }
}
