package kr.or.nia.dpg.vpos.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;

/**
 * 날짜/시간 유틸리티 클래스
 *
 *  - 모든 메소드는 Asia/Seoul(KST) 타임존을 기준으로 동작함.
 *  - LocalDateTime.now()는 JVM의 시스템 타임존을 따르기 때문에, 서버 환경에 따라 UTC로 동작할 수 있음.
 *  - 이로 인해 외부 API 연동 시 시간 기반 유효성 검증에 실패할 수 있으므로, ZoneId를 명시하여 타임존을 고정함.
 *
 *  [변경이력]
 *  - 2026-04-14 : LocalDateTime.now() -> LocalDateTime.now(KST) 변경
 *  (개발 서버 JVM 타임존이 UTC롤 설정되어 외부 API(LG) 연동 시 trdno 불일치 오류 발생)
 */

@Slf4j
public class DateUtil {

	/**
	 * 인스턴스 생성 방지
	 */
	private DateUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * 기준 타임존 (Asia/Seoul, KST)
	 * 서버 환경에 관계 없이 항상 한국 표준시 기준으로 동작하도록 고정
	 */
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static String getTodayString(String format) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return formatter.format(LocalDate.now(KST));
        } catch (Exception e) {
            log.error("[DateUtil.getTodayString] 날짜 변환 실패 - format: {}", format, e);
            return "";
        }
    }
    public static String getTodayDashString() {
        return getTodayString("yyyy-MM-dd");
    }

    public static String getNowStringWithMilliSecond() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
            return LocalDateTime.now(KST).format(formatter);
        } catch (Exception e) {
            log.error("[DateUtil.getNowStringWithMilliSecond] 날짜 변환 실패", e);
            return "";
        }
    }

    public static String getNowStringWithSecond() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            return LocalDateTime.now(KST).format(formatter);
        } catch (Exception e) {
            log.error("[DateUtil.getNowStringWithSecond] 날짜 변환 실패", e);
            return "";
        }
    }

    public static String getNowStringWithMinute() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            return LocalDateTime.now(KST).format(formatter);
        } catch (Exception e) {
            log.error("[DateUtil.getNowStringWithMinute] 날짜 변환 실패", e);
            return "";
        }
    }

    /**
     * LG U+ 거래번호 생성을 위한 메소드 추가
     * yyyyMMddHHmmssSSS + 랜덤 3자리
     * @return 20자리 문자열
     */
    public static String generateTrdNo() {
        try {
            String timestamp = getNowStringWithMilliSecond();
            String randomNum = String.format("%03d", new SecureRandom().nextInt(1000));
            return timestamp + randomNum;
        } catch (Exception e) {
            log.error("[DateUtil.generateTrdNo] 거래번호 생성 실패", e);
            return "";
        }
    }
}
