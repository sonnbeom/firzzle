package com.firzzle.common.library;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @Class Name : FormatDate
 * @Description : 날짜관련 라이브러리
 *
 * @author 퍼스트브레인
 * @since 2014. 9. 30.
 */
public class FormatDate {

    /** 로거 */
    private static final Logger logger = LoggerFactory.getLogger(FormatDate.class);

    /**
     * 학습일 제한
     *
     * @param edusdate    : 시작일
     * @param eduedate    : 종료일
     * @param gubun       : 변경할 구분(ex: 년(year), 월(month), 일(date)
     * @param convertDate : 변경 날짜
     * @return
     * @throws Exception
     */
    public static String eduDateLimit(String edusdate, String eduedate, String gubun, int convertDate) throws Exception {

        String result = "";


        String v_edusdate = edusdate.replaceAll("[^0-9]", "");
        String v_eduedate = eduedate.replaceAll("[^0-9]", "");

        if ("".equals(v_edusdate) && "".equals(v_eduedate)) {
            v_edusdate = getDate("yyyy") + "0101";
            v_eduedate = getDate("yyyy") + "1231";

        } else {
            v_edusdate = getFormatDate(StringUtils.rightPad(v_edusdate, 6, "0"), "yyyyMMdd");
            v_eduedate = getFormatDate(StringUtils.rightPad(v_eduedate, 6, "0"), "yyyyMMdd");

            if ("".equals(v_edusdate) && !"".equals(v_eduedate)) {
                v_edusdate = convertAddDate("yyyyMMdd", v_eduedate, gubun, -convertDate);
            }
            if (!"".equals(v_edusdate) && "".equals(v_eduedate)) {
                v_eduedate = convertAddDate("yyyyMMdd", v_edusdate, gubun, convertDate);
            }
        }

        if (datediff("date", v_edusdate, v_eduedate) > 365) {
            v_eduedate = convertAddDate("yyyyMMdd", v_edusdate, gubun, convertDate);
        }

        result = v_edusdate + "_" + v_eduedate;

        return result;
    }

    /**
     * 날짜 더하기
     *
     * @param type      : 날짜형태(ex: yyyyMMddHHmmss, yyyyMMdd, yyyy.MM.dd ...)
     * @param inputdate : 기준 날짜
     * @param gubun     : 변경할 구분(ex: 년(year), 월(month), 일(date)
     * @param addDate   : 더할 날짜
     * @return
     * @throws Exception
     */
    public static String convertAddDate(String type, String inputdate, String gubun, Integer addDate) throws Exception {

        String v_convertdate = "";
        String gubunUpper = gubun.toUpperCase();
        String result = "";


        if (!"".equals(type) && !"".equals(inputdate) && !"".equals(gubun)) {

            v_convertdate = getFormatDate(inputdate, type); // 입력된 날짜형태로 변경

            // 날짜 계산
            DateFormat df = new SimpleDateFormat(type);
            Date date = df.parse(v_convertdate);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            if ("DATE".equals(gubunUpper)) {
                // day
                cal.add(Calendar.DATE, addDate);

            } else if ("WEEK".equals(gubunUpper)) {
                // week
                cal.add(Calendar.WEDNESDAY, addDate);

            } else if ("MONTH".equals(gubunUpper)) {
                // month
                cal.add(Calendar.MONTH, addDate);

            } else if ("YEAR".equals(gubunUpper)) {
                // year
                cal.add(Calendar.YEAR, addDate);

            }
            result = df.format(cal.getTime());
        }

        return result;
    }

    /**
     * 주간 시작과 끝날짜 가져오기
     *
     * @param cal   : 현재 날짜가 셋팅된 Calendar
     * @param gubun : 마지막 요일, 0(Sunday), 1(Monday), 2(Thuesday), 3(Wednesday), 4(Thursday), 5(Friday), 6(Saturday)
     * @return
     */
    public static String getWeeksFirLastDate(Calendar cal, int gubun) {

        String result = "";
        String sdate = "";
        String edate = "";
        sdate = cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_WEEK, gubun);
        edate = cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH);

        result = sdate + "_" + edate;
        return result;
    }

    /**
     * 0 = Sunday, 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 = Friday, 6 = Saturday) 특정일(yyyyMMdd) 에서 주어진 일자만큼 더한 날짜를 계산한다.
     *
     * @param date 특정일(yyyyMMdd)
     * @param rday 주어진 일자
     * @return result 더한 날짜를 계산하여 반환함
     */
    public static String getRelativeDate(String date, int rday) throws Exception {
        if (date == null)
            return null;
        if (date.length() < 8)
            return ""; // 최소 8 자리
        String time = "";

        TimeZone kst = TimeZone.getTimeZone("JST");
        TimeZone.setDefault(kst);

        Calendar calendar = Calendar.getInstance(kst);

        int yyyy = Integer.parseInt(date.substring(0, 4));
        int mm = Integer.parseInt(date.substring(4, 6));
        int dd = Integer.parseInt(date.substring(6, 8));

        calendar.set(yyyy, mm - 1, dd);
        calendar.add(calendar.DATE, rday);

        time = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());

        return time;
    }

    /**
     * 현재 System시간으로 부터 정해진 날짜를 구한다.
     *
     * @param tab 상대적으로 구할 날짜 (-3 : 3일전, 100 : 100일후)
     * @return 날짜 8자리
     */
    public static String getRelativeDate(int tab) {
        Calendar today = Calendar.getInstance();
        today.add(Calendar.DATE, tab);
        Date targetDate = today.getTime();
        String sDate = (targetDate.getYear() + 1900) + "" + (targetDate.getMonth() + 1) + "";

        if (sDate.length() == 5)
            sDate = sDate.substring(0, 4) + "0" + sDate.substring(4);
        sDate += targetDate.getDate();
        if (sDate.length() == 7) {
            sDate = sDate.substring(0, 6) + "0" + sDate.substring(6);
        }
        return sDate;
    }

    /**
     * 시간을 스트링으로 받어서 type 형태로 리턴한다. 예) getFormatTime("1200","HH:mm") -> "12:00" getFormatTime("1200","HH:mm:ss") -> "12:00:00"
     * getFormatTime("120003","HH:mm") -> "12:00" getFormatTime("120003","HH:mm ss") -> "12:00 03"
     *
     * @param time 시간
     * @param type 시간타입
     * @return result 변경된 시간타입을 반환함
     */
    public static String getFormatTime(String time, String type) throws Exception {
        if (time == null || type == null)
            return null;

        String result = "";
        int hour = 0, min = 0, sec = 0;//, length = time.length();

        hour = Integer.parseInt(time.substring(0, 2));
        min = Integer.parseInt(time.substring(2, 4));
        sec = Integer.parseInt(time.substring(4, 6));
        Calendar calendar = Calendar.getInstance();
        calendar.set(0, 0, 0, hour, min, sec);
        result = (new SimpleDateFormat(type)).format(calendar.getTime());

        return result;
    }

    /**
     * 날짜(+시간)을 스트링으로 받어서 type 형태로 리턴한다. 예) getFormatDate("19991201","yyyy/MM/dd") -> "1999/12/01" getFormatDate("19991201","yyyy-MM-dd") ->
     * "1999-12-01" getFormatDate("1999120112","yyyy-MM-dd HH") -> "1999-12-01 12" getFormatDate("199912011200","yyyy-MM-dd HH:mm ss") -> "1999-12-01
     * 12:00 00" getFormatDate("19991231115959","yyyy-MM-dd-HH-mm-ss") -> "1999-12-31-11-59-59"
     *
     * @param date 날짜
     * @param type 날짜타입
     * @return result 변경된 날짜타입을 반환함
     */
    public static String getFormatDate(String date, String type) throws Exception {

        String result = "";
        String v_year = "";
        String v_month = "";
        String v_day = "";
        String v_hour = "";
        String v_min = "";
        String v_sec = "";


        if (!"".equals(date) && !"".equals(type)) {

            String v_date = StringUtils.rightPad(date.replaceAll("[^0-9]", ""), 14, "0");
            if (v_date.length() == 14) {

                v_year = v_date.substring(0, 4);
                v_month = v_date.substring(4, 6);
                v_day = v_date.substring(6, 8);
                v_hour = v_date.substring(8, 10);
                v_min = v_date.substring(10, 12);
                v_sec = v_date.substring(12, 14);

                if ("0000".equals(v_year)) {
                    v_year = new SimpleDateFormat("yyyy").format(new Date());
                }
                if ("00".equals(v_month)) {
                    v_month = "01";
                } else if (12 < Integer.parseInt(v_month)) {
                    v_month = "12";
                }
                if ("00".equals(v_day)) {
                    v_day = "01";
                } else if (31 < Integer.parseInt(v_day)) {
                    v_day = "31";
                }
                Calendar calendar = Calendar.getInstance();
                calendar.set(Integer.parseInt(v_year), Integer.parseInt(v_month) - 1, Integer.parseInt(v_day), Integer.parseInt(v_hour),
                        Integer.parseInt(v_min), Integer.parseInt(v_sec));
                result = (new SimpleDateFormat(type)).format(calendar.getTime());
//				result = (DateFormat.getDateInstance(DateFormat.FULL, LocaleContextHolder.getLocale())).format(calendar.getTime());
            }
        }

        return result;
    }

    /**
     * 날짜를 여러 타입으로 리턴한다. 예) getDate("yyyyMMdd"); getDate("yyyyMMddHHmmss"); getDate("yyyyMMddHHmmssSSS"); getDate("yyyy/MM/dd HH:mm:ss");
     * getDate("yyyy/MM/dd"); getDate("HHmm");
     *
     * @param type 날짜타입
     * @return result 변경된 날짜타입을 반환함
     */
    public static String getDate(String type) throws Exception {
        if (type == null)
            return null;

        String s = new SimpleDateFormat(type).format(new Date());
        return s;
    }

    /**
     * 해당날짜의 요일을 계산한다. (년월일(6자리)을 지정하는데 지정되지 않으면 default 값을 사용한다. 2000.2) 예) getDayOfWeek("2000") -> 토 (2000/1/1) getDayOfWeek("200002") -> 화
     * (2000/2/1) getDayOfWeek("20000225") -> 금 (2000/2/25)
     *
     * @param date 날짜타입
     * @return result 변경된 날짜타입을 반환함
     */
    public static String getDayOfWeek(String date) throws Exception{
        if (date == null)
            return null;

        int yyyy = 0;
        int MM = 1;
        int dd = 1;
        int day_of_week;

        String days[] = {
                RenderMessageManager.getMessage("MSG_0000010108",""),//ORG_MSG MSG_0000010108::일
                RenderMessageManager.getMessage("MSG_0000001718",""),//ORG_MSG MSG_0000001718::월
                RenderMessageManager.getMessage("MSG_0000001719",""),//ORG_MSG MSG_0000001719::화
                RenderMessageManager.getMessage("MSG_0000001720",""),//ORG_MSG MSG_0000001720::수
                RenderMessageManager.getMessage("MSG_0000001721",""),//ORG_MSG MSG_0000001721::목
                RenderMessageManager.getMessage("MSG_0000001722",""),//ORG_MSG MSG_0000001722::금
                RenderMessageManager.getMessage("MSG_0000001723","")//ORG_MSG MSG_0000001723::토
        };

        yyyy = Integer.parseInt(date.substring(0, 4));
        MM = Integer.parseInt(date.substring(4, 6));
        dd = Integer.parseInt(date.substring(6, 8));

        Calendar calendar = Calendar.getInstance();
        calendar.set(yyyy, MM - 1, dd);
        day_of_week = calendar.get(Calendar.DAY_OF_WEEK);

        return days[day_of_week - 1];
    }

    /**
     * 해당날짜의 요일을 계산한다. (년월일(6자리)을 지정하는데 지정되지 않으면 default 값을 사용한다. 2000.2) 예) getDayOfWeek("2000") -> 토 (2000/1/1) getDayOfWeek("200002") -> 화
     * (2000/2/1) getDayOfWeek("20000225") -> 금 (2000/2/25)
     *
     * @param date 날짜타입
     * @return result 변경된 날짜타입을 반환함
     */
    public static int getDayOfWeekCode(String date)  throws Exception{
        if (date == null)
            return -1;

        int yyyy = 0;
        int MM = 1;
        int dd = 1;
        int day_of_week;

        yyyy = Integer.parseInt(date.substring(0, 4));
        MM = Integer.parseInt(date.substring(4, 6));
        dd = Integer.parseInt(date.substring(6, 8));

        Calendar calendar = Calendar.getInstance();
        calendar.set(yyyy, MM - 1, dd);
        day_of_week = calendar.get(Calendar.DAY_OF_WEEK);

        return day_of_week;
    }

    /**
     * 오늘의 요일을 계산한다.
     *
     * @return 오늘의 요일을 반환함
     */
    public static String getDayOfWeek() throws Exception {
        return getDayOfWeek(getDate("yyyyMMdd"));
    }

    /**
     * 오늘의 요일을 계산한다.
     *
     * @return 오늘의 요일을 반환함
     */
    public static int getDayOfWeekCode() throws Exception {
        return getDayOfWeekCode(getDate("yyyyMMdd"));
    }

    /**
     * 두 시간의 차이를 분으로 계산한다. 처음 파라메터가 작은 날짜인데 만약 더 큰날짜를 처음으로 주면 음수를리턴. 예) getMinDifference("20000302","20000303") --> 3600
     * getMinDifference("2000030210","2000030211") --> 60 getMinDifference("200003021020","200003021021") --> 1
     * getMinDifference("20000302102000","20000302102130") --> 1
     *
     * @return 두시간의 차를 분으로 반환함
     */
    public static int getMinDifference(String s_start, String s_end) throws Exception {
        long l_gap = getTimeDifference(s_start, s_end);

        return (int) (l_gap / (1000 * 60));
    }

    /**
     * 두 시간의 차이를 초로 계산한다.. 처음 파라메터가 작은 날짜인데 만약 더 큰날짜를 처음으로 주면 음수를리턴.
     *
     * @return 두시간의 차를 초로 반환함
     */
    public static int getSecDifference(String s_start, String s_end) throws Exception {
        long l_gap = getTimeDifference(s_start, s_end);

        return (int) (l_gap / (1000));
    }

    /**
     * 두시간의 차를 밀리초로 반환함
     *
     * @param s_start
     * @param s_end
     * @return
     * @throws Exception
     */
    public static int getMilliSecDifference(String s_start, String s_end) throws Exception {
        long l_gap = getTimeDifference(s_start, s_end);

        return (int) l_gap;
    }

    /**
     * 두시간의 차를 밀리초로 반환함
     *
     * @param s_start
     * @param s_end
     * @return
     * @throws Exception
     */
    public static long getTimeDifference(String s_start, String s_end) throws Exception {
        long l_start, l_end, l_gap;

        int i_start_year = 0, i_start_month = 1, i_start_day = 1, i_start_hour = 0, i_start_min = 0, i_start_sec = 0, i_start_msec = 0;
        int i_end_year = 0, i_end_month = 1, i_end_day = 1, i_end_hour = 0, i_end_min = 0, i_end_sec = 0, i_end_msec = 0;


        i_start_year = Integer.parseInt(s_start.substring(0, 4));
        i_start_month = Integer.parseInt(s_start.substring(4, 6));
        i_start_day = Integer.parseInt(s_start.substring(6, 8));
        i_start_hour = Integer.parseInt(s_start.substring(8, 10));
        i_start_min = Integer.parseInt(s_start.substring(10, 12));
        if (s_start.length() > 12) {
            i_start_sec = Integer.parseInt(s_start.substring(12, 14));
        }
        if (s_start.length() > 14) {
            i_start_msec = Integer.parseInt(s_start.substring(14, 17));
        }
        i_end_year = Integer.parseInt(s_end.substring(0, 4));
        i_end_month = Integer.parseInt(s_end.substring(4, 6));
        i_end_day = Integer.parseInt(s_end.substring(6, 8));
        i_end_hour = Integer.parseInt(s_end.substring(8, 10));
        i_end_min = Integer.parseInt(s_end.substring(10, 12));
        if (s_end.length() > 12) {
            i_end_sec = Integer.parseInt(s_end.substring(12, 14));
        }
        if (s_end.length() > 14) {
            i_end_msec = Integer.parseInt(s_end.substring(14, 17));
        }


        Calendar calendar = Calendar.getInstance();

        calendar.set(i_start_year, i_start_month - 1, i_start_day, i_start_hour, i_start_min, i_start_sec);
        calendar.set(Calendar.MILLISECOND, i_start_msec);

        l_start = calendar.getTime().getTime();

        calendar.set(i_end_year, i_end_month - 1, i_end_day, i_end_hour, i_end_min, i_end_sec);
        calendar.set(Calendar.MILLISECOND, i_end_msec);
        l_end = calendar.getTime().getTime();

        l_gap = l_end - l_start;

        return l_gap;
    }

    /**
     * 년,월,일,시,분등과 관련된 HTML <option> 을 출력한다.
     *
     * @param start 시작시간
     * @param end   종료시간
     * @return getDateOptions(start,end,-1);
     */
    public static String getDateOptions(int start, int end) {
        return getDateOptions(start, end, -1);
    }

    /**
     * 년,월,일,시,분등과 관련된 HTML <option> 을 출력한다.
     *
     * @param start    시작시간
     * @param end      종료시간
     * @param nDefault default 값이 선택됨
     * @return HTML <option> 을 출력
     */
    public static String getDateOptions(int start, int end, int nDefault) {
        String result = "";

        for (int i = start; i <= end; i++) {
            if (i < 100) {
                String temp = "";
                temp = String.valueOf(i + 100);
                temp = temp.substring(1);

                if (i == nDefault) {
                    result += "<option value='" + temp + "' selected>" + temp;
                } else {
                    result += "<option value='" + temp + "'>" + temp;
                }
            } else {
                if (i == nDefault) {
                    result += "<option value='" + i + "' selected>" + i;
                } else {
                    result += "<option value='" + i + "'>" + i;
                }
            }
        }
        return result;
    }

    /**
     * 해당 날짜의 요일을 출력한다.
     *
     * @param date YYYYMMDD
     * @return 요일 리턴
     */
    public static int weekday(String date) {
        if (date == null)
            return -1;

        int yyyy = 0, MM = 1, dd = 1, day_of_week; // default

        yyyy = Integer.parseInt(date.substring(0, 4));
        MM = Integer.parseInt(date.substring(4, 6));
        dd = Integer.parseInt(date.substring(6, 8));

        Calendar calendar = Calendar.getInstance();
        calendar.set(yyyy, MM - 1, dd);
        day_of_week = calendar.get(Calendar.DAY_OF_WEEK);

        return day_of_week;
    }

    /**
     * 해당 날짜의 요일을 출력한다.
     *
     * @param date YYYYMMDD
     * @return 요일 리턴
     */
    public static String getWeekday(String date) {
        if (date == null) {
            return "";
        }

        int yyyy = 0, MM = 1, dd = 1, day_of_week; // default

        yyyy = Integer.parseInt(date.substring(0, 4));
        MM = Integer.parseInt(date.substring(4, 6));
        dd = Integer.parseInt(date.substring(6, 8));

        Calendar calendar = Calendar.getInstance();
        calendar.set(yyyy, MM - 1, dd);
        day_of_week = calendar.get(Calendar.DAY_OF_WEEK);

        String day = "";
        if(day_of_week > 0 && day_of_week < 8) {

            if( day_of_week == 1 ) {
                day = RenderMessageManager.getMessage("MSG_0000010108","");//ORG_MSG MSG_0000010108::일
            } else if( day_of_week == 2 ) {
                day = RenderMessageManager.getMessage("MSG_0000001718","");//ORG_MSG MSG_0000001718::월
            } else if( day_of_week == 3 ) {
                day = RenderMessageManager.getMessage("MSG_0000001719","");//ORG_MSG MSG_0000001719::화
            } else if( day_of_week == 4 ) {
                day = RenderMessageManager.getMessage("MSG_0000001720","");//ORG_MSG MSG_0000001720::수
            } else if( day_of_week == 5 ) {
                day = RenderMessageManager.getMessage("MSG_0000001721","");//ORG_MSG MSG_0000001721::목
            } else if( day_of_week == 6 ) {
                day = RenderMessageManager.getMessage("MSG_0000001722","");//ORG_MSG MSG_0000001722::금
            } else if( day_of_week == 7 ) {
                day = RenderMessageManager.getMessage("MSG_0000001723","");//ORG_MSG MSG_0000001723::토
            }
        }
        return day;
    }



    /**
     * 월 차이의 달수 구함. 예) datediff("20010101", "20000501"); 달의 차
     *
     * @param firstdate
     * @param lastdate
     * @return 더하거나 뺀 월을 리턴
     */
    public static int datediff(String firstdate, String lastdate) throws Exception {
        int returnValue = 0;
        int year = 0, month = 0, year1 = 0, month1 = 0;
        int year2 = 0, month2 = 0;

        if (firstdate == null || firstdate.equals(""))
            return returnValue;
        if (lastdate == null || lastdate.equals(""))
            return returnValue;

        year = Integer.parseInt(firstdate.substring(0, 4));
        month = Integer.parseInt(firstdate.substring(4, 6));
        year1 = Integer.parseInt(lastdate.substring(0, 4));
        month1 = Integer.parseInt(lastdate.substring(4, 6));
        year2 = (year - year1) * 12;
        month2 = month - month1;
        returnValue = year2 + month2 + 1;
        return returnValue;
    }

    /**
     * 일짜 차이의 일수, 월 차이의 달수 구함. 예) datediff("d", "20000101", "20010501RenderMessageManager.getMessage("MSG_0000001673","")20010101", "20000501"); 달의 차-작은 날이 뒤에//ORG_MSG MSG_0000001673::); 일의 차 - 작은 날이 앞에, datediff(
     *
     * @param gubn      월, 일 중 하나를 세팅한다.(월 = "monthRenderMessageManager.getMessage("MSG_0000001726","")"date")//ORG_MSG MSG_0000001726::, 일 =
     * @param firstdate
     * @param lastdate
     * @return 더하거나 뺀 월, 일을 리턴
     */
    public static int datediff(String gubn, String firstdate, String lastdate) throws Exception {
        int returnValue = 0;
        long temp = 0;
        int year = 0;
        int month = 0;
        int day = 0;
        int year1 = 0;
        int month1 = 0;
        int day1 = 0;
        int year2 = 0;
        int month2 = 0;

        if (firstdate == null || firstdate.equals(""))
            return returnValue;
        if (lastdate == null || lastdate.equals(""))
            return returnValue;

        year = Integer.parseInt(firstdate.substring(0, 4));
        month = Integer.parseInt(firstdate.substring(4, 6));
        day = Integer.parseInt(firstdate.substring(6, 8));

        year1 = Integer.parseInt(lastdate.substring(0, 4));
        month1 = Integer.parseInt(lastdate.substring(4, 6));
        day1 = Integer.parseInt(lastdate.substring(6, 8));

        if (gubn.equals("date")) {
            TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");
            Calendar calendar = Calendar.getInstance(tz);

            calendar.set((year - 1900), (month - 1), day);

            Calendar cal2 = Calendar.getInstance(tz);
            cal2.set((year1 - 1900), (month1 - 1), day1);

            Date temp1 = calendar.getTime();
            Date temp2 = cal2.getTime();

            temp = temp2.getTime() - temp1.getTime();

            if ((temp % 10) < 5) {

                temp = temp - (temp % 10);
            } else {

                temp = temp + (10 - (temp % 10));
            }
            returnValue = (int) (temp / (1000 * 60 * 60 * 24));
            if (returnValue == 0) {
                returnValue = 1;
            }
        } else {
            year2 = (year - year1) * 12;
            month2 = month - month1;
            returnValue = year2 + month2;
        }
        return returnValue;
    }


    /**
     * String 형식의 YYYYMMDDHHMISS 를 Date 객체로 리턴한다.
     *
     * @param v_datestr YYYYMMDDHHMISS
     * @return Date 객체 리턴
     */
    public static Date getDate2(String v_datestr) {
        Date d = null;

        int v_year = 0;
        int v_month = 1;
        int v_date = 0;
        int v_hrs = 0;
        int v_min = 0;
        int v_sec = 0;

        if (v_datestr.length() >= 4) {
            v_year = Integer.parseInt(v_datestr.substring(0, 4));
        }

        if (v_datestr.length() >= 6) {
            v_month = Integer.parseInt(v_datestr.substring(4, 6));
        }

        if (v_datestr.length() >= 8) {
            v_date = Integer.parseInt(v_datestr.substring(6, 8));
        }

        if (v_datestr.length() >= 10) {
            v_hrs = Integer.parseInt(v_datestr.substring(8, 10));
        }

        if (v_datestr.length() >= 12) {
            v_min = Integer.parseInt(v_datestr.substring(10, 12));
        }

        if (v_datestr.length() >= 14) {
            v_sec = Integer.parseInt(v_datestr.substring(12, 14));
        }

        d = (new GregorianCalendar(v_year, v_month - 1, v_date, v_hrs, v_min, v_sec)).getTime();

        return d;
    }

    /**
     * 요일을 한글 값으로 가져온다
     *
     * @param week
     * @return
     */
    public static String getDay(int week) {
        String day = "";
        switch (week) {
            case 1:
                day = RenderMessageManager.getMessage("MSG_0000010108","");//ORG_MSG MSG_0000010108::일
                break;
            case 2:
                day = RenderMessageManager.getMessage("MSG_0000001718","");//ORG_MSG MSG_0000001718::월
                break;
            case 3:
                day = RenderMessageManager.getMessage("MSG_0000001719","");//ORG_MSG MSG_0000001719::화
                break;
            case 4:
                day = RenderMessageManager.getMessage("MSG_0000001720","");//ORG_MSG MSG_0000001720::수
                break;
            case 5:
                day = RenderMessageManager.getMessage("MSG_0000001721","");//ORG_MSG MSG_0000001721::목
                break;
            case 6:
                day = RenderMessageManager.getMessage("MSG_0000001722","");//ORG_MSG MSG_0000001722::금
                break;
            case 7:
                day = RenderMessageManager.getMessage("MSG_0000001723","");//ORG_MSG MSG_0000001723::토
                break;
            default:
                //logger.error("날짜형식이 아님");
                break;
        }

        return day;
    }

    /**
     * 달력 데이터 만들기
     *
     * @param year:  년도
     * @param month: 월
     * @return: 2차원 배열 형식의 달력 데이터
     */
    public Object[][] getCalendarTable(int year, int month) {

        int daycount = 1;
        int nextdaycount = 1;

        String v_month = "";
        String v_premonth = "";
        String v_nextmonth = "";

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 2, 1);
        int prelastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        cal.set(year, month - 1, 1);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDay = cal.get(Calendar.DAY_OF_WEEK);
        Object temp[][] = new Object[6][7];
        prelastDay = prelastDay - firstDay + 2;

        if (month == 1) {
            v_premonth = (year - 1) + "12";

        } else if ((month - 1) < 10) {
            v_premonth = year + "0" + (month - 1);

        } else {
            v_premonth = year + "" + (month - 1);
        }

        if (month < 10) {
            v_month = year + "0" + month;
        } else {
            v_month = year + "" + month;
        }

        if (month == 12) {
            v_nextmonth = (year + 1) + "01";

        } else if ((month + 1) < 10) {
            v_nextmonth = year + "0" + (month + 1);

        } else {
            v_nextmonth = year + "" + (month + 1);
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                if (firstDay - 1 > 0) {
                    temp[i][j] = String.valueOf(v_premonth + prelastDay);
                    prelastDay++;
                    firstDay--;
                    continue;

                } else if (daycount > lastDay) {
                    if (nextdaycount < 10) {
                        temp[i][j] = String.valueOf(v_nextmonth + "0" + nextdaycount);
                    } else {
                        temp[i][j] = String.valueOf(v_nextmonth + nextdaycount);
                    }

                    nextdaycount++;
                    firstDay--;
                    continue;

                } else {
                    if (daycount < 10) {
                        temp[i][j] = String.valueOf(v_month + "0" + daycount);
                    } else {
                        temp[i][j] = String.valueOf(v_month + daycount);
                    }
                    daycount++;
                }
            }
        }
        return temp;
    }

    public static String getTimeStamp() {

        String rtnStr = null;

        // 문자열로 변환하기 위한 패턴 설정(년도-월-일 시:분:초:초(자정이후 초))
        String pattern = "yyyyMMddhhmmssSSS";

        SimpleDateFormat sdfCurrent = new SimpleDateFormat(pattern, Locale.KOREA);
        Timestamp ts = new Timestamp(System.currentTimeMillis());

        rtnStr = sdfCurrent.format(ts.getTime());
        return rtnStr;
    }


    /**
     * 날짜 유효성 체크
     * @param checkDate
     * @return
     */
    public static boolean validationDate(String checkDate, String format) {
        try {
            SimpleDateFormat dateFormat = new  SimpleDateFormat(format, Locale.KOREA);

            dateFormat.setLenient(false);
            dateFormat.parse(checkDate);
            return true;

        } catch (ParseException e) {
            // TODO: handle exception
            return false;
        }
    }

    /**
     * 초를 입력 받아 hh:mm:ss 형태로 출력
     * @param sec
     * @return
     */
    public static String formatHourMinSec(int sec) {
        String result = "";

        if(sec < 1) {
            result = "00:00:00";
            return result;
        }

        int h = 0;
        int m = 0;
        int s = 0;

        h= sec / 3600;
        m=(sec % 3600) / 60;
        s= sec % 60;


        String h_txt = "00";
        String m_txt = "00";
        String s_txt = "00";


        if(h < 10 && h > -10) {
            h_txt = "0" + String.valueOf(h);
        }else {
            h_txt = String.valueOf(h);
        }

        if(m < 10 && m > -10) {
            m_txt = "0" + String.valueOf(m);
        }else {
            m_txt = String.valueOf(m);
        }

        if(s < 10 && s > -10) {
            s_txt = "0" + String.valueOf(s);
        }else {
            s_txt = String.valueOf(s);
        }


        result = h_txt + ":" +  m_txt + ":" + s_txt;
        return result;
    }

    /**
     * 시간차의 년월일시분초 별로 문구 출력 "yyyyMMddhhmmss" 형식 맞춤
     *
     * @param indate
     * @return 화면 출력 시간차이별 문구
     */
    public static String datediffText(String indate) throws Exception {
        String diffText = "";
        String logFormat = "%s-%s-%sT%s:%s:%s";
        String sdate = "";
        LocalDateTime sdateTime = null;
        LocalDateTime nowTime = LocalDateTime.now();

        if(!(indate.isEmpty() || "".equals(indate))) {
            if(indate.length() == 14) {
                sdate = indate;
            }
        }

        if(sdate != "") {
            String d_year = sdate.substring(0, 4);
            String d_month = sdate.substring(4, 6);
            String d_day = sdate.substring(6, 8);
            String d_hour = sdate.substring(8, 10);
            String d_minute = sdate.substring(10, 12);
            String d_second = sdate.substring(12, 14);

            sdateTime = LocalDateTime.parse(String.format(logFormat, d_year, d_month, d_day, d_hour, d_minute, d_second));

            //long diff_year = ChronoUnit.YEARS.between(sdateTime, nowTime);
            long diff_month = ChronoUnit.MONTHS.between(sdateTime, nowTime);
            long diff_day = ChronoUnit.DAYS.between(sdateTime, nowTime);
            long diff_hour = ChronoUnit.HOURS.between(sdateTime, nowTime);
            long diff_minute = ChronoUnit.MINUTES.between(sdateTime, nowTime);
            long diff_second = ChronoUnit.SECONDS.between(sdateTime, nowTime);


            if(diff_month >= 12) {
                diffText = d_year + "." + d_month + "." + d_day;
            }else if(diff_day >= 36) {
                diffText = RenderMessageManager.getMessage("MSG_0000001727","",diff_month);//ORG_MSG MSG_0000001727::개월 전
            }else if(diff_day <= 35 && diff_day >= 29) {
                diffText = RenderMessageManager.getMessage("MSG_0000001728","",4);//ORG_MSG MSG_0000001728::4주 전
            }else if(diff_day <= 28 && diff_day >= 22) {
                diffText = RenderMessageManager.getMessage("MSG_0000001728","",3);//ORG_MSG MSG_0000001728::3주 전
            }else if(diff_day <= 21 && diff_day >= 15) {
                diffText = RenderMessageManager.getMessage("MSG_0000001728","",2);//ORG_MSG MSG_0000001728::2주 전
            }else if(diff_day <= 14 && diff_day >= 8) {
                diffText = RenderMessageManager.getMessage("MSG_0000001728","",1);//ORG_MSG MSG_0000001728::1주 전
            }else if(diff_day <= 7 && diff_day >= 1) {
                diffText = diff_day + RenderMessageManager.getMessage("MSG_0000001657","");//ORG_MSG MSG_0000001657::일 전
            }else if(diff_hour <= 23 && diff_hour >= 1) {
                diffText = diff_hour + RenderMessageManager.getMessage("MSG_0000001656","");//ORG_MSG MSG_0000001656::시간 전
            }else if(diff_minute <= 59 && diff_minute >= 1) {
                diffText = diff_minute + RenderMessageManager.getMessage("MSG_0000001655","");//ORG_MSG MSG_0000001655::분 전
            }else if(diff_second <= 60) {
                diffText = diff_second + RenderMessageManager.getMessage("MSG_0000001654","");//ORG_MSG MSG_0000001654::초 전
            }
        }

        return diffText;
    }

    public static String addDaysWithFormatter(String srcDateStr, String srcFormat, int addDays) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(srcFormat);
        LocalDate date = LocalDate.parse(srcDateStr, formatter);

        LocalDate plusDays = date.plusDays(addDays);

        return plusDays.format(formatter);
    }

    public static String convertDateWithFormatter(String srcDateStr, String srcFormat, String destFormat) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(srcFormat);
        LocalDate date = LocalDate.parse(srcDateStr, formatter);
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern(destFormat);
        return date.format(formatter2);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(FormatDate.convertDateWithFormatter("20230925", "yyyyMMdd", "yyyy-MM-dd"));
    }

    /**
     * dateTime이 startDateTime과 endDateTime 사이에 있는지 검사
     *
     * @param dateTime      현재일시
     * @param startDateTime 시작일시
     * @param endDateTime   종료일시
     * @return
     */
    public static boolean isWithinRange(String dateTime, String startDateTime, String endDateTime) {
        if (dateTime.length() < 12 || startDateTime.length() < 12 || endDateTime.length() < 12) {
            return false;
        }

        LocalDateTime localdate = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        LocalDateTime startLocalDate = LocalDateTime.parse(startDateTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        LocalDateTime endLocalDate = LocalDateTime.parse(endDateTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return (!localdate.isBefore(startLocalDate)) && (localdate.isBefore(endLocalDate));
    }
}

