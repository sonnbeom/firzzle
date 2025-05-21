package com.firzzle.common.library;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @Class Name : StringManager
 * @Description : String 관리 라이브러리
 *
 * @author 퍼스트브레인
 * @since 2014. 9. 30.
 */
public class StringManager {

	private static final Logger logger = LoggerFactory.getLogger(StringManager.class);

	// YouTube ID 추출 정규식 패턴
	private static final Pattern YOUTUBE_ID_PATTERN =
			Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})");

	/**
	 * YouTube URL에서 ID 추출
	 *
	 * @param youtubeUrl - YouTube URL
	 * @return String - YouTube ID
	 */
	public static String extractYoutubeId(String youtubeUrl) {
		if (!org.springframework.util.StringUtils.hasText(youtubeUrl)) {
			return null;
		}

		Matcher matcher = YOUTUBE_ID_PATTERN.matcher(youtubeUrl);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

	/**
	 * 해당 문자열에서 older String 을 newer String 으로 교체한다.
	 *
	 * @param original 전체 String
	 * @param older    전체 String 중 교체 전 문자 String
	 * @param newer    전체 String 중 교체 후 문자 String
	 * @return result 교체된 문자열을 반환함
	 */
	public static String replace(String original, String older, String newer) {
		String result = original;

		if (original != null) {
			int idx = result.indexOf(older);
			int newLength = newer.length();

			while (idx >= 0) {
				if (idx == 0) {
					result = newer + result.substring(older.length());
				} else {
					result = result.substring(0, idx) + newer + result.substring(idx + older.length());
				}
				idx = result.indexOf(older, idx + newLength);
			}
		}
		return result;
	}

	/**
	 * java.lang.String 패키지의 trim() 메소드와 기능은 동일, null 체크만 함
	 *
	 * @param str 전체 문자열
	 * @return result trim 된 문자열을 반환함
	 */
	public static String trim(String str) throws Exception {
		String result = "";

		if (str != null)
			result = str.trim();

		return result;
	}

	/**
	 * java.lang.String 패키지의 trim() 메소드와 기능은 동일, 디폴트 값 출력
	 *
	 * @param str 전체 문자열
	 * @return result trim 된 문자열을 반환함
	 */
	public static String trim(String str, String defstr) throws Exception {
		String result = "";

		if (str != null)
			result = str.trim();

		return (result.equals("") ? defstr : result);
	}

	/**
	 * java.lang.String 패키지의 substring() 메소드와 기능은 동일, null 체크만 함
	 *
	 * @param str        전체 문자열
	 * @param beginIndex
	 * @param endIndex
	 * @return result substring 된 문자열을 반환함
	 */
	public static String substring(String str, int beginIndex, int endIndex) {
		String result = "";

		if (str != null)
			result = str.substring(beginIndex, endIndex);

		return result;
	}

	/**
	 * java.lang.String 패키지의 substring() 메소드와 기능은 동일, null 체크만 함
	 *
	 * @param str        전체 문자열
	 * @param beginIndex
	 * @return result substring 된 문자열을 반환함
	 */
	public static String substring(String str, int beginIndex) {
		String result = "";

		if (str != null)
			result = str.substring(beginIndex);

		return result;
	}

	/**
	 * java.lang.String 패키지의 substring() 메소드와 기능은 동일한데 오른쪽 문자끝부터 count 를 해서 자름
	 *
	 * @param str   전체 문자열
	 * @param count 오른쪽 문자끝(1) 부터 count 까지
	 * @return result substring 된 문자열을 반환함
	 */
	public static String rightstring(String str, int count) throws Exception {
		if (str == null)
			return null;

		String result = null;
		if (count == 0) // 갯수가 0 이면 공백을
			result = "";
		else if (count > str.length()) // 문자열 길이보다 크면 문자열 전체를
			result = str;
		else
			result = str.substring(str.length() - count, str.length()); // 오른쪽 count 만큼 리턴
		return result;
	}

	/**
	 * null 체크
	 *
	 * @param str 전체 문자열
	 * @return str null 인경우 "" 을, 아니면 원래의 문자열을 반환한다.
	 */
	public static String chkNull(String str) {
		if (str == null)
			return "";
		else
			return str;
	}

	/**
	 * 빈문자열의 경우 &nbsp로 붙여준다.
	 *
	 * @param str 전체 문자열
	 * @return str null이나 빈문자 인경우 &nbsp를, 아니면 원래의 문자열을 반환한다.
	 */
	public static String toWSpace(String str) {
		if (str == null || str.length() == 0)
			return "&nbsp";
		else
			return str;
	}

	/**
	 * String 형을 int 형으로 변환, null 및 "" 체크
	 *
	 * @param str 전체 문자열
	 * @return null 및 "" 일 경우 0 반환
	 */
	public static int toInt(String str) {
		if (str == null || str.equals(""))
			return 0;
		else
			return Integer.parseInt(str);
	}

	/**
	 * String 형을 Double 형으로 변환, null 및 "" 체크
	 *
	 * @param str 전체 문자열
	 * @return null 및 "" 일 경우 0 반환
	 */
	public static double toDouble(String str) {
		if (str == null || str.equals(""))
			return 0;
		else
			return Double.parseDouble(str);
	}



	/**
	 * URLEncoder 로 암호화
	 * @param str 전체 문자열
	 * @return URLEncoder로 암호화된 문자열
	 */
	public static String URLEncode(String str) throws Exception {
		String result = "";
		if (str != null) {
			result = URLEncoder.encode(str, "UTF-8");
		}
		return result;
	}

	/**
	 * URLDecode 로 암호화
	 * @param str 전체 문자열
	 * @return URLDecode로 복호화된 문자열
	 */
	public static String URLDecode(String str) throws Exception{
		String result = "";
		if (str != null) {
			try {
				result = URLDecoder.decode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw e;
			}
		}
		return result;
	}


	public String encode(String str, String charset) throws Exception  {
		StringBuilder sb = new StringBuilder();
		byte[] key_source = str.getBytes(charset);
		for (byte b : key_source) {
			String hex = String.format("%02x", b).toUpperCase();
			sb.append("%");
			sb.append(hex);
		}
		return sb.toString();
	}

	public String decode(String hex, String charset)  throws Exception {
		byte[] bytes = new byte[hex.length() / 3];
		int len = hex.length();


		int i = 0;
		while(i < len) {
			int pos = hex.substring(i).indexOf("%");
			if (pos == 0) {
				String hex_code = hex.substring(i + 1, i + 3);
				bytes[i / 3] = (byte) Integer.parseInt(hex_code, 16);
				i += 3;
			} else {
				i += pos;
			}
		}

		/*
		for (int i = 0; i < len;) {
			int pos = hex.substring(i).indexOf("%");
			if (pos == 0) {
				String hex_code = hex.substring(i + 1, i + 3);
				bytes[i / 3] = (byte) Integer.parseInt(hex_code, 16);
				i += 3;
			} else {
				i += pos;
			}
		}
		*/
		return new String(bytes, charset);
	}

	public static String korEncode(String str) throws Exception {
		if (str == null)
			return null;
		return new String(str.getBytes("8859_1"), "KSC5601");
	}

	public static String engEncode(String str) throws Exception {
		if (str == null)
			return null;
		return new String(str.getBytes("KSC5601"), "8859_1");
	}

	/**
	 * SQL Query 문에서 value 값의 ' ' 를 만들어 주기 위한 메소드
	 *
	 * @param str ' ' 안에 들어갈 변수 값
	 * @return 'str' 로 리턴됨
	 */
	public static String makeSQL(String str) {
		String result = "";
		if (str != null)
			result = "'" + chkNull(replace(str, "'", "")) + "'";
		return result;
	}

	/**
	 * 제목을 보여줄때 제한된 길이를 초과하면 뒷부분을 짜르고 "..." 으로 대치한다.
	 *
	 * @param title(제목등의 문자열), max(최대길이)
	 * @return title(변경된 문자열)
	 */
	public static String formatTitle(String title, int max) {
		if (title == null)
			return null;

		int totbyte = 0;

		char[] string = title.toCharArray();
		String retitle = "";
		for (int j = 0; j < string.length; j++) {
			if (string[j] >= '\uAC00' && string[j] <= '\uD7A3') {
				totbyte++;
				totbyte++;
			} else {
				totbyte++;
			}

			if (totbyte <= max) {
				retitle += string[j];
			}

		}

		if (totbyte <= max) {
			return retitle;
		} else {
			return retitle + "...";
		}
	}

	/**
	 * 제목을 보여줄때 제한된 길이를 초과하면 뒷부분을 짜르고 "..." 으로 대치한다.
	 *
	 * @param title(제목등의 문자열), max(최대길이)
	 * @return title(변경된 문자열)
	 */
	public static String cutZero(String seq) {
		String result = Integer.parseInt(seq) + "";
		return result;
	}

	/**
	 * Html 변환
	 *
	 * @param title(제목등의 문자열), max(최대길이)
	 * @return title(변경된 문자열)
	 */
	public static String convertHTML(String _text) {

		StringBuffer html = new StringBuffer();
		String text = _text;

		int startIndex = 0;
		int endIndex = 0;

		while ((endIndex = text.indexOf('\n', startIndex)) > -1) {
			html.append(text.substring(startIndex, endIndex));
			html.append("<br />");
			startIndex = endIndex + 1;
		}
		html.append(text.substring(startIndex, text.length()));
		return html.toString();

	}

	/**
	 * 두개의 날짜를 비교해서 크다,같다,작다를 리턴한다. 단, 에러가 나는 경우에는 물음표를 리턴한다.
	 *
	 * @author sjkang
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static String compareDate(String date1, String date2) {
		date1 = date1.trim().replaceAll("[^0-9]", "");
		date2 = date2.trim().replaceAll("[^0-9]", "");

		int date1Int = 0;
		int date2Int = 0;
		String result = "?";
		date1Int = Integer.parseInt(date1);
		date2Int = Integer.parseInt(date2);

		if (date1Int > date2Int) {
			result =  ">";
		}
		if (date1Int < date2Int) {
			result =  "<";
		}
		if (date1Int == date2Int) {
			result =  "=";
		}


		return result;
	}

	public static String formatDate(String strDate, String gubun) {
		if (strDate == null)
			return "";

		strDate = strDate.trim();
		if (strDate.length() == 6) {

			return strDate.substring(0, 2) + gubun + strDate.substring(2, 4) + gubun + strDate.substring(4, 6);
		} else if (strDate.length() >= 8) {

			return strDate.substring(0, 4) + gubun + strDate.substring(4, 6) + gubun + strDate.substring(6);
		} else {

			return "";
		}
	}

	/**
	 * String date를 Date객체로 parsing하여 return하는 method.
	 *
	 * @param date    날짜를 나타내는 String 객체
	 * @param format Date의 format
	 * @return String 날짜를 parsing한 Date객체.
	 */
	public static Date dateParse(String date, String format) throws Exception {
		if (date == null)
			return null;

		return new SimpleDateFormat(format).parse(date);
	}

	/**
	 * Date를 format에 맞는 String으로 return하는 method.
	 *
	 * @param date    Date객체
	 * @param format Date의 format
	 * @return format에 맞는 날짜 String.
	 */
	public static String dateFormat(Date date, String format) throws Exception{
		if (date == null)
			return "";

		return new SimpleDateFormat(format).format(date);
	}

	/**
	 * 오늘 날자를 반환
	 *
	 * @param year  년수
	 * @param month 개월수
	 * @param day   일수
	 * @return yyyyMMdd 형식의 String
	 */
	public static String getTodayStr() throws Exception {
		return dateFormat(getToday(), "yyyyMMdd");
	}

	/**
	 * 오늘 날자를 반환
	 *
	 * @param year  년수
	 * @param month 개월수
	 * @param day   일수
	 * @return yyyyMMdd 형식의 String
	 */
	public static String getTimeAfterHour() throws Exception {
		return dateFormat(afterDate(0, 0, 0, 1), "yyyyMMddHH");
	}

	/**
	 * 입력 년, 월, 일 이후의 날짜를 가져옴.
	 *
	 * @param year  년수
	 * @param month 개월수
	 * @param day   일수
	 * @return yyyyMMdd 형식의 String
	 */
	public static String getAfterDate(int year, int month, int day) throws Exception {
		GregorianCalendar aCal = new GregorianCalendar();
		aCal.add(Calendar.YEAR, year);
		aCal.add(Calendar.MONTH, month);
		aCal.add(Calendar.DATE, day);

		return dateFormat(new java.sql.Date(aCal.getTimeInMillis()), "yyyyMMdd");
	}

	/**
	 * 입력 년, 월, 일 이전의 날짜를 가져옴.
	 *
	 * @param year  년수
	 * @param month 개월수
	 * @param day   일수
	 * @return yyyyMMdd 형식의 String
	 */
	public static String getBeforeDate(int year, int month, int day) throws Exception {
		return dateFormat(afterDate(-year, -month, -day), "yyyyMMdd");
	}

	/**
	 * 입력 년, 월, 일 이후의 날짜를 가져옴.
	 *
	 * @param year  년수
	 * @param month 개월수
	 * @param day   일수
	 * @return 입력 년, 월, 일이 더해진 날짜
	 */
	public static Date afterDate(int year, int month, int day) {
		GregorianCalendar aCal = new GregorianCalendar();
		aCal.add(Calendar.YEAR, year);
		aCal.add(Calendar.MONTH, month);
		aCal.add(Calendar.DATE, day);

		return new java.sql.Date(aCal.getTimeInMillis());
	}

	/**
	 * 입력 년, 월, 일 이후의 날짜를 가져옴.
	 *
	 * @param year  년수
	 * @param month 개월수
	 * @param day   일수
	 * @return 입력 년, 월, 일이 더해진 날짜
	 */
	public static Date afterDate(int year, int month, int day, int hour) {
		GregorianCalendar aCal = new GregorianCalendar();
		aCal.add(Calendar.YEAR, year);
		aCal.add(Calendar.MONTH, month);
		aCal.add(Calendar.DATE, day);
		aCal.add(Calendar.HOUR, hour);

		return new java.sql.Date(aCal.getTimeInMillis());
	}

	/**
	 * 입력 년, 월, 일 이전의 날짜를 가져옴.
	 *
	 * @param year  년수
	 * @param month 개월수
	 * @param day   일수
	 * @return 입력 년, 월, 일이 빠진 날짜
	 */
	public static Date beforeDate(int year, int month, int day) {
		return afterDate(-year, -month, -day);
	}

	/**
	 * String Date를 parsing하여 새로운 format에 맞는 Date String을 return하는 method.
	 *
	 * @param date    String의 날짜
	 * @param parse   String 날짜의 현재 format
	 * @param format 새로운 Date의 format
	 *
	 * @return format에 맞는 날짜 String.<br>
	 */
	public static String dateFormat(String date, String parse, String format) throws Exception{
		String result = "";
		if (date != null && !date.equals("")) {
			try {
				result =  new SimpleDateFormat(format).format(new SimpleDateFormat(parse).parse(date));
			} catch (Exception e) {
				//logger.error("error", e);
			}
		}
		return result;
	}

	/**
	 * 오늘 날짜를 Date타입으로 return
	 *
	 * @return
	 */
	public static Date getToday() {
		Calendar cd = Calendar.getInstance();
		Date dt = cd.getTime();
		return dt;
	}

	/**
	 * 기준자(',')로 문자열을 자른후 Strign 배열로 리턴
	 *
	 * @param tokenVal String 대상문자열
	 * @return
	 */
	public static String[] getSplitStrToArray(String tokenVal) {
		String strVal[] = null;
		strVal = tokenVal.split(",");
		return strVal;
	}

	/**
	 * Number를 지정한 format의 String으로변환
	 *
	 * @param num    long 대상숫자
	 * @param format String 형식
	 * @return
	 */
	public static String numberFormat(long num, String format) {
		return new DecimalFormat(format).format(num);
	}

	/**
	 * Number를 지정한 format의 String으로변환
	 *
	 * @param num    double 대상숫자
	 * @param format String 형식
	 * @return
	 */
	public static String numberFormatDouble(double num, String format) {
		return new DecimalFormat(format).format(num);
	}

	/**
	 * 제한적으로 보여줘야될 string인 경우 특정 길이 이상이면 [...]로 대체하는 메소드(한글의 경우는 compByte()를 사용해야 됨)
	 *
	 * @param str String 대상문자열
	 * @param i   int 문자열길이
	 * @return
	 */
	public static String setTitle(String str, int i) {
		if (str == null)
			return "";
		String tmp = str;
		if (tmp.length() > i)
			tmp = tmp.substring(0, i) + "...";
		return tmp;
	}

	/**
	 * 날짜 형식 변경
	 *
	 * @param date          String 날짜
	 * @param orignalformat String 원래 날짜 형식
	 * @param wantformat    String 변경을 원하는 날짜 형식
	 * @return
	 */
	public static String getFormatDate(String date, String orignalformat, String wantformat) {
		String day = "";
		SimpleDateFormat dd = new SimpleDateFormat(orignalformat, Locale.US);
		ParsePosition parse = new ParsePosition(0);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dd.parse(date, parse));
		SimpleDateFormat sdf = new SimpleDateFormat(wantformat, Locale.US);
		day = sdf.format(cal.getTime());
		return day;
	}

	/**
	 * 특수문자를 HTML로치환
	 *
	 * @param str
	 * @return
	 */
	public static String htmlSpecialChar(String str) {
		if (str == null)
			return "";

		str = str.replaceAll("&", "&amp;");
		str = str.replaceAll("·", "&middot;");

		str = str.replaceAll("<", "&lt;");
		str = str.replaceAll(">", "&gt;");
		str = str.replaceAll("\'", "&apos;");
		str = str.replaceAll("\"", "&quot");
		str = str.replaceAll("\n", "<br/>");
		str = str.replaceAll("\r\n", "<br/>");

		return str;
	}

	/**
	 * 특수문자를 HTML로치환
	 *
	 * @param str
	 * @return
	 */
	public static String htmlSpecialCharDecode(String str) {
		if (str == null)
			return "";

		str = str.replaceAll("&amp;", "&");
		str = str.replaceAll("&middot;", "·");

		str = str.replaceAll("&lt;", "<");
		str = str.replaceAll("&gt;", ">");
		str = str.replaceAll("&apos;", "\'");
		str = str.replaceAll("&#39;", "\'");
		str = str.replaceAll("&lsquo;", "\'");
		str = str.replaceAll("&rsquo;", "\'");
		str = str.replaceAll("&quot;", "\"");
		str = str.replaceAll("<br/>", "\n");
		str = str.replaceAll("<br/>", "\r\n");
		str = str.replaceAll("&nbsp;", " ");
		str = str.replaceAll("&times;", "×");
		str = str.replaceAll("&ndash;", "–");

		return str;
	}

	/**
	 * NULL검사
	 *
	 * @param str
	 * @param Defaultvalue
	 * @return
	 * @throws Exception
	 */
	public static String nullcheck(String str, String Defaultvalue) throws Exception {
		String ReturnDefault = "";
		if (str == null || str.equals("")) {
			ReturnDefault = Defaultvalue;
		} else {
			ReturnDefault = str;
		}
		return ReturnDefault;
	}

	/**
	 * 앞에 자리수만금 숫자 채우기
	 *
	 * @param chkNumber
	 * @param chkLen
	 * @return
	 */
	public static String addZero(int chkNumber, int chkLen) {
		String temp = null;
		temp = String.valueOf(chkNumber);
		int len = temp.length();

		if (len < chkLen) {
			for (int i = 1; i <= (chkLen - len); i++) {
				temp = "0" + temp;
			}
		}
		return temp;
	}

	/**
	 * 천단위콤마
	 *
	 * @param chkNumber
	 * @param chkLen
	 * @return
	 */
	public static String setComma(double num) {

		if( num > 0 ) {
			DecimalFormat df = new DecimalFormat("#,##0");
			String temp = df.format(num);
			return temp;
		} else {
			return "";
		}

	}

	/**
	 * 천단위콤마 / default 0
	 *
	 * @param chkNumber
	 * @param chkLen
	 * @return
	 */
	public static String setCommaDefaultZero(double num) {

		if( num > 0 ) {
			DecimalFormat df = new DecimalFormat("#,##0");
			String temp = df.format(num);
			return temp;
		} else {
			return "0";
		}

	}
	// 문자 배열 길이의 값을 랜덤으로 length를 뽑아 구문을 작성함
	public static String getTempPassword(int length){
		char[] charSet = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
				'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
				'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
		String str = "";
		int idx = 0;
		SecureRandom sr = new SecureRandom();
		for (int i = 0; i < length; i++) {
			idx = (int) (charSet.length * sr.nextDouble());
			str += charSet[idx];
		}
		return str;
	}
	/**
	 * 임시비밀번호 생성
	 *
	 * @param length
	 * @return
	 */
	public static String randomPassword(int length) {
		int index = 0;
		char[] charSet = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
				'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
				'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

		StringBuffer sb = new StringBuffer();
		SecureRandom r = new SecureRandom();
		for (int i = 0; i < length; i++) {
			r.setSeed(new Date().getTime());
			index = charSet.length * r.nextInt();
			sb.append(charSet[index]);
		}

		return sb.toString();

	}

	/**
	 * 임시비밀번호 생성
	 *
	 * @param length
	 * @return
	 */
	public static String randomPassword() {
		return randomPassword(8);
	}

	/**
	 * MD5 얻어오기
	 *
	 * @param input
	 * @return
	 */
	@Deprecated
	public static String getMD5(String input) throws Exception{
		return input;
		/*
		byte[] source;
		source = input.getBytes("UTF-8");
		source = input.getBytes("UTF-8");
		String result = null;
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(source);
		byte temp[] = md.digest();
		char str[] = new char[16 * 2];
		int k = 0;
		for (int i = 0; i < 16; i++) {
			byte byte0 = temp[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf];
		}
		result = new String(str);
		return result;
		*/
	}

	/**
	 * 정규식 문자열 추출
	 *
	 * @param text:  문자열
	 * @param start: 시작문자열
	 * @param end:   종료문자열
	 * @return
	 */
	public static String getPatternMatch(String text, String start, String end) {

		String result = "";
		String regex = start + "(.*?)" + end;
		Pattern pt = Pattern.compile(regex);
		Matcher mt = pt.matcher(text);

		if (mt.find()) {
			result = mt.group().replaceAll(start, "").replaceAll(end, "");
		} else {
			result = text;
		}
		return result;
	}

	/**
	 * 전화번호 타입 변경
	 *
	 * @param number: ex)010-1234-5678, 02-123-4567, 02-1234-5678...
	 * @param type:   ex)"-", "."...
	 * @return
	 */
	public static String phoneNumber(String number, String type) {
		String convert_b = "";

		if (StringManager.isNotEmpty(number)) {
			if (number.contains("*")) {
				//마스킹 처리 되있는경우 그냥 노출
				return number;
			}
			// 1.숫자만 추출
			String convert_a = number.replaceAll("[^0-9]", "");

			// 2.전화번호 형태 변경
			convert_b = String.valueOf(convert_a).replaceFirst("(^02.{0}|^01.{1}|\\d{2,3})(\\d{3,4})(\\d{4}$)", "$1" + type + "$2" + type + "$3");
		}

		return convert_b;
	}

	/**
	 * 주민번호 타입 변경
	 *
	 * @param number: ex)123456-1234567...
	 * @param type:   ex)"-", "."...
	 * @return
	 */
	public static String rrnNumber(String number, String type) {

		//마스킹 처리 되있는경우 그냥 노출
		if (number.contains("*")) {
			return number;
		}

		// 1.숫자만 추출
		String convert_a = number.replaceAll("[^0-9]", "");

		// 2.주민번호 형태 변경
		String convert_b = String.valueOf(convert_a).replaceFirst("(\\d{6})(\\d{7}$)", "$1" + type + "$2" );

		return convert_b;
	}

	/**
	 * 이메일 정규식 유효성 체크
	 *
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email) {

		boolean result = false;
		String str = email.trim();

		if (str != null && !"".equals(str)) {
			result = Pattern.matches("[0-9a-zA-Z]+(.[_a-zA-Z0-9-]+)*@(?:[_\\w-]+\\.)+\\w+$", str);
		} else {
			result = true;
		}
		return result;
	}

	/**
	 * 휴대폰번호 정규식 유효성 체크
	 *
	 * @param cell
	 * @return
	 */
	public static boolean isCell(String cell) {

		boolean result = false;
		String str = cell.replaceAll("[^0-9]", "");

		if (str != null && !"".equals(str)) {
			result = Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", str);
		} else {
			result = true;
		}
		return result;
	}

	/**
	 * 전화번호 정규식 유효성 체크
	 *
	 * @param tel
	 * @return
	 */
	public static boolean isTel(String tel) {

		boolean result = false;
		String str = tel.replaceAll("[^0-9]", "");

		if (str != null && !"".equals(str)) {
			result = Pattern.matches("^\\d{2,3}\\d{3,4}\\d{4}$", str);
		} else {
			result = true;
		}
		return result;
	}

	/**
	 * 첨부파일 파일명 인코딩
	 *
	 * @param filename
	 * @return
	 */
	public static String fileEncode(String filename) {

		String result = "";
		String v_filename = "";
		if (!"".equals(filename)) {
			v_filename = filename;

			v_filename = v_filename.replace("!", "%21");
			v_filename = v_filename.replace("\"", "%22");
			v_filename = v_filename.replace("#", "%23");
			v_filename = v_filename.replace("$", "%24");
			v_filename = v_filename.replace("%", "%25");
			v_filename = v_filename.replace("&", "%26");
			v_filename = v_filename.replace("\'", "%27");
			v_filename = v_filename.replace("(", "%28");
			v_filename = v_filename.replace(")", "%29");
			v_filename = v_filename.replace("*", "%2A");
			v_filename = v_filename.replace("+", "%2B");
			v_filename = v_filename.replace(",", "%2C");
			v_filename = v_filename.replace("-", "%2D");
			v_filename = v_filename.replace("?", "%3F");
			v_filename = v_filename.replace("=", "%3D");
			v_filename = v_filename.replace("/", "%2F");
			v_filename = v_filename.replace("\n", "%0A");

		} else {
			v_filename = filename;
		}
		result = v_filename;
		return result;
	}

	/**
	 * 첨부파일 파일명 디코딩
	 *
	 * @param filename
	 * @return
	 */
	public static String fileDecode(String filename) {

		String result = "";
		String v_filename = "";
		if (!"".equals(filename)) {
			v_filename = filename;

			v_filename = v_filename.replace("%21", "!");
			v_filename = v_filename.replace("%22", "\"");
			v_filename = v_filename.replace("%23", "#");
			v_filename = v_filename.replace("%24", "$");
			v_filename = v_filename.replace("%25", "%");
			v_filename = v_filename.replace("%26", "&");
			v_filename = v_filename.replace("%27", "\'");
			v_filename = v_filename.replace("%28", "(");
			v_filename = v_filename.replace("%29", ")");
			v_filename = v_filename.replace("%2A", "*");
			v_filename = v_filename.replace("%2B", "+");
			v_filename = v_filename.replace("%2C", ",");
			v_filename = v_filename.replace("%2D", "-");
			v_filename = v_filename.replace("%3F", "?");
			v_filename = v_filename.replace("%3D", "=");
			v_filename = v_filename.replace("%2F", "/");
			v_filename = v_filename.replace("%0A", "\n");

		} else {
			v_filename = filename;
		}
		result = v_filename;
		return result;
	}

	/**
	 * 문자열의 byte를 구함
	 *
	 * @param str
	 * @return
	 */
	public static int getByteLength(String str) {

		int strlen = 0;
		char tempChar[] = new char[str.length()];

		for (int i = 0; i < tempChar.length; i++) {
			tempChar[i] = str.charAt(i);

			if (tempChar[i] < 128) {
				strlen += 1;
			} else {
				strlen += 2;
			}
		}
		return strlen;
	}

	/**
	 * byte단위로 문자열 자르기
	 *
	 * @param str:    문자열
	 * @param maxLen: 자를 byte 수
	 * @return
	 */
	public static String getMaxByteString(String str, int maxLen) {
		StringBuilder sb = new StringBuilder();
		int curLen = 0;
		String curChar;

		for (int i = 0; i < str.length(); i++) {
			curChar = str.substring(i, (i + 1));
			try {
				curLen += curChar.getBytes("UTF-8").length;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (curLen > maxLen) {
				break;
			} else {
				sb.append(curChar);
			}
		}
		return sb.toString();
	}

	/**
	 * 문자 null 체크
	 *
	 * @param email
	 * @return
	 */
	public static boolean isNotEmpty(String str) {
		if (str == null || str.equals(""))
			return false;
		else
			return true;
	}

	/**
	 * xss 점검
	 * @param str
	 * @return
	 */
	public static String cleanXSS(String str){
		StringBuffer sb = null;
		String[] checkStr_arr = {
				"<script>","</script>",
						"&lt;script&gt;","&lt;/script&gt;",
				"<javascript>","</javascript>",
						"&lt;javascript&gt;", "&lt;/javascript&gt;",
				"<vbscript>","</vbscript>", "onerror", "onclick", "onmouseover"
	    };

		for(String checkStr : checkStr_arr){
			while(str.indexOf(checkStr)!=-1){
				str = str.replaceAll(checkStr, "");
			}
			while(str.toLowerCase().indexOf(checkStr)!=-1){
				sb = new StringBuffer(str);
				sb = sb.replace(str.toLowerCase().indexOf(checkStr),str.toLowerCase().indexOf(checkStr)+ checkStr.length(), "");
				str = sb.toString();
			}

		}

		str = str.replaceAll("eval\\((.*)\\)", "");
		str = str.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
		return str;
	}

	/**
	 * xss 파일 패스 점검
	 * @param path
	 * @return
	 */
	public static String clearPath(String path){
		if(path != null && !"".equals(path)){
			path = path.replaceAll("/","");
			path = path.replaceAll("\\\\","");
			path = path.replaceAll("\\.","");
			path = path.replaceAll("&","");
		}else {
			path = "";
		}

		return path;
	}


	public static String lpadZero(Integer chkNumber, Integer chkLen) {
		//return String.format("%0"+chkLen+"d", chkNumber);
		return StringUtils.leftPad(chkNumber+"", chkLen, "0");
	}


	/**
	 * 랜덤 int 생성 함수
	 * @param size
	 * @return
	 */
	public static int makeSecureRandomInt(int size) throws Exception {
		//logger.debug("random size : " + size);
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		int result = random.nextInt(size);
		return result;
	}


	/**
	 * 파일 사이즈 표현
	 * @param size
	 * @return
	 */
	public static String sizeToString(Integer size) {

		long unit = 1024;
		if(size < unit) {
			return String.format("(%d B)", size);
		}else {

			int exp = (int) (Math.log(size) / Math.log(unit));
			return String.format("(%.0f %sB)", size / Math.pow(unit, exp), "KMGTPE".charAt(exp-1));
		}
	}

	/**
	 * LPAD
	 * @param strContext
	 * @param iLen
	 * @param strChar
	 * @return
	 */
	public static String setLpad(String strContext, int iLen, String strChar) {
		String strResult = "";
		StringBuilder sbAddChar = new StringBuilder();
		for( int i = strContext.length(); i < iLen; i++ ) {
			sbAddChar.append( strChar );
		}
		strResult = sbAddChar + strContext;
		return strResult;
	}


	/**
	 * 날짜유효성 체크
	 * @param dateFormat
	 * @param dateCheck
	 * @return
	 */
	public static boolean dateValidation(String dateFormat, String dateCheck) {

		try {
			SimpleDateFormat df = new SimpleDateFormat(dateFormat);
			df.setLenient(false);
			df.parse(dateCheck);
			return true;

		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * 비밀번호 유효성 체크 (from validator.js)
	 *
	 * @param pwd
	 * @param loginId
	 * @param phone
	 * @return
	 */
	public static boolean passwordValidationCheck(String pwd, String loginId, String phone, String birthday) {
		//0. 공백 체크
		if(pwd == null || "".equals(pwd)){
			//logger.error("passwordValidationCheck :: 비밀번호 입력값 없음");
			return false;
		}

		//1. 8~12자리의 영문+숫자+특수문자(~,!,@,#,$,%,^,&,*,-,_,.) 조합
		String pattern_pwd = "^(?=.*?[A-Za-z])(?=.*?[0-9])(?=.*?[-~!@#$%^&*_.])[A-Za-z0-9-~!@#$%^&*_.]{8,12}$";
		Matcher match = Pattern.compile(pattern_pwd).matcher(pwd);

		if(!match.find()) {
			//logger.error("passwordValidationCheck :: 1 조합 오류");
			return false;
		}

		// 2. 전화번호, 생년월일의 숫자 4자리 이상 연속 불가 => 생년월일은 저장하지 않음 / 제외
		List phonebirthStrs = new ArrayList<String>();

		if(phone.length() > 0){
			phone = phone.replaceAll("-", "");
			phonebirthStrs.add(phone.substring(3,7));
			phonebirthStrs.add(phone.substring(7,11));
		}

		if (birthday.length() >= 4) {
			 phonebirthStrs.add(birthday.substring(birthday.length()-4, birthday.length())); // 마지막 4자리 ____####
		}

		if (birthday.length() >= 8) {
			phonebirthStrs.add(birthday.substring(birthday.length()-8, birthday.length()-4)); // 마지막 4자리 ____####
		}

		if(phonebirthStrs.size() > 0){
			String phonePattern = String.join("|",phonebirthStrs);
			match = Pattern.compile(phonePattern).matcher(pwd);

			if(match.find()) {
				//logger.error("passwordValidationCheck :: 2 휴대폰번호 포함");
				return false;
			}
		}

		// 3. 4자리 이상 동일 문자 반복 불가
		String continuePattern = "([\\w\\d])\\1\\1\\1";
		match = Pattern.compile(continuePattern).matcher(pwd);

		if(match.find()) {
			//logger.error("passwordValidationCheck :: 3 4자리 이상 동일 문자 반복");
			return false;
		}

		// 4. 키보드상 나란히 있는 문자열 4자리 이상 연속 불가
		String qwerPattern = "(?i)qwer|wert|erty|rtyu|tyui|yuio|uiop|asdf|sdfg|dfgh|fghj|ghjk|hjkl|zxcv|xcvb|cvbn|vbnm";
		match = Pattern.compile(qwerPattern).matcher(pwd);

		if(match.find()) {
			//logger.error("passwordValidationCheck :: 4 키보드상 나란히 있는 문자열");
			return false;
		}

		// 5. 일련된 숫자, 문자 4자리 이상 연속 불가
		String seriesPattern = "(?i)1234|2345|3456|4567|5678|6789|0123|abcd|bcde|cdef|defg|efgh|fghi|ghij|hijk|ijkl|jklm|klmn|lmno|mnop|nopq|opqr|pqrs|qrst|rstu|stuv|tuvw|uvwx|vwxy|wxyz";
		match = Pattern.compile(seriesPattern).matcher(pwd);

		if(match.find()){
			//logger.error("passwordValidationCheck :: 5 일련된 숫자/문자");
			return false;
		}

		// 6.입력된 아이디가 포함된 경우
		if (loginId.length() > 0) {
			if (pwd.toLowerCase().indexOf(loginId.toLowerCase()) >= 0) {
				//logger.error("passwordValidationCheck :: 6 아이디와 유사한 비밀번호");
				return false;
			}
		}

		return true;
	}


}
