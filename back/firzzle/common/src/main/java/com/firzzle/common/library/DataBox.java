package com.firzzle.common.library;

import com.nimbusds.oauth2.sdk.util.MapUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.*;

/**
 * @Class Name : DataBox
 * @Description : DB에서 select된 data를 관리 라이브러리
 *
 * @author 퍼스트브레인
 * @since 2014. 9. 26.
 */
public class DataBox extends HashMap<String, Object> implements Cloneable {

    /** SerialVersionUID */
    private static final long serialVersionUID = 1L;

    /** 로거 */
    private static final Logger logger = LoggerFactory.getLogger(DataBox.class);

    /** 객체명 */
    private String name = null;

    /**
     * 생성자
     */
    public DataBox() {

        super();
    }



    /**
     * 생성자
     *
     * @param name
     */
    public DataBox(String name) {

        super();
        this.name = name;
    }

    /**
     * 객체에 담긴 param value 의 String 타입으로 반환
     *
     * @param key
     * @return
     */
    public String get(String key) {

        return getString(key);
    }

    /**
     * box 객체에 담긴 param value 의 boolean 타입으로 반환
     *
     * @param key
     * @return
     */
    public boolean getBoolean(String key) {

        String value = getString(key);
        boolean isTrue = false;

        //isTrue = (new Boolean(value)).booleanValue();
        isTrue = Boolean.parseBoolean(value);
        return isTrue;
    }

    /**
     * 객체에 담긴 param value 의 double 타입으로 반환
     *
     * @param key
     * @return
     */
    public double getDouble(String key) throws Exception {

        String value = removeComma(getString(key));
        if (value.equals("")) {
            return 0.0;
        }
        //double num = Double.valueOf(value).doubleValue();
        double num = Double.parseDouble(value);
        return num;
    }

    /**
     * 객체에 담긴 param value 의 float 타입으로 반환
     *
     * @param key
     * @return
     */
    public float getFloat(String key) throws Exception{

        return (float) getDouble(key);
    }

    /**
     * 객체에 담긴 param value 의 int 타입으로 반환
     *
     * @param key
     * @return
     */
    public int getInt(String key) throws Exception{
        double value = getDouble(key);
        return (int) value;
    }

    public int getInt2(String key) {
        double value = 0;
        try {
            value = getDouble(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (int) value;
    }

    public int getInt2(String key, int defaultValue) {
        double value = 0;
        try {
            value = getDouble(key);
            if (value == 0) return defaultValue;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (int) value;
    }

    /**
     * box 객체에 담긴 param value 의 int 타입으로 반환
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public int getInt(String key, int defaultValue) throws Exception{

        double value = getDouble(key);
        if ((int) value == 0) {
            return defaultValue;
        } else {
            return (int) value;
        }
    }

    /**
     * box 객체에 담긴 param value 의 long 타입으로 반환
     *
     * @param key
     * @return
     */
    public long getLong(String key) throws Exception {

        String value = removeComma(getString(key));
        if (value.equals("")) {
            return 0L;
        }

        Long.parseLong(value);

        //long lvalue = Long.valueOf(value).longValue();
        long lvalue = Long.parseLong(value);
        return lvalue;
    }

    public long getLong2(String key) {
        try {
            String value = removeComma(getString(key));
            if (value.equals("")) {
                return 0L;
            }

            Long.parseLong(value);

            //long lvalue = Long.valueOf(value).longValue();
            long lvalue = Long.parseLong(value);
            return lvalue;
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * box 객체에 담긴 param value 의 String 타입으로 반환
     *
     * @param key
     * @return
     */
    public String getString(String key) {

        String value = null;

        Object o = super.get(key);
        if (o != null) {
            Class<? extends Object> c = o.getClass();

            if (c.isArray()) {
                int length = Array.getLength(o);
                if (length == 0) {
                    value = "";
                } else {
                    Object item = Array.get(o, 0);
                    if (item == null)
                        value = "";
                    else
                        value = item.toString();
                }
            } else {
                value = o.toString();
            }

        } else {
            value = "";
        }
        return value;
    }

    /**
     * box 객체에 담긴 param value 의 String 타입으로 반환
     *
     * @param key
     * @return
     */
    public Object getObject(String key) {
        Object value = super.get(key);
        return value;
    }

    /**
     * box 객체에 담긴 parameter value 의 String 타입으로 반환
     *
     * @param key
     * @param defstr
     * @return
     */
    public String getString(String key, String defstr) {

        return (getString(key).equals("") ? defstr : getString(key));
    }

    /**
     * 날짜 데이터에서 구분을 제거하고 반환
     *
     * @param key 키
     */
    public String getDate(String key) {

        return getDate(key, "");
    }

    /**
     * 날짜 데이터를 구분을 추가하여 반환
     *
     * @param key   키
     * @param gubun 날짜패턴
     * @return
     */
    public String getDate(String key, String gubun) {

        return getDate(key, gubun, "");
    }

    /**
     * 날짜 데이터를 구분을 추가하여 반환
     *
     * @param key   키
     * @param gubun 날짜패턴
     * @return
     */
    public String getDate(String key, String gubun, String temp) {

        if ("".equals(key) || getString(key).equals("")) {
            return temp;
        }

        String value = getString(key).replaceAll("[-\\./]", "");
        if ("".equals(gubun)) {
            return value;
        } else {

            if (value.length() < 8) {
                return StringManager.formatDate(value, gubun);
            } else {
                return StringManager.formatDate(StringManager.substring(value, 0, 8), gubun);
            }
        }
    }

    /**
     * 날짜 데이터를 구분을 추가하여 반환
     *
     * @param key   키
     * @param gubun 현재패턴
     * @param temp  새 날짜 포맷
     * @return
     */
    public String getDateFormat(String key, String gubun, String format) throws Exception {
        String value = getString(key).replaceAll("[^0-9]", "");
        if(value.length() > 7) {
            if ("".equals(gubun)) {
                // 날짜데이터에 새 포맷 적용
                String nowFormat = "yyyyMMdd";
                if (value.length() == 10) {
                    nowFormat += "HH";
                } else if (value.length() == 12) {
                    nowFormat += "HHmm";
                } else if (value.length() == 14) {
                    nowFormat += "HHmmss";
                }
                value = StringManager.dateFormat(value, nowFormat, format);
                return value;
            } else {
                value = StringManager.dateFormat(value, gubun, format);
                return value;
            }
        }else {
            return value;
        }
    }

    /**
     * 콤마를 제거
     *
     * @param str 대상문자열
     * @return
     */
    private static String removeComma(String str) throws Exception{

        if (str != null) {
            if (str.indexOf(',') != -1) {
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if (c != ',')
                        buf.append(c);
                }
                return buf.toString();
            }
        }
        return str;
    }

    /**
     * 객체 값을 출력
     *
     * @return
     */
    @Override
    public String toString() {

        String key = "";
        Object obj = "";
        StringBuffer sb = new StringBuffer();
        Iterator<String> iter = this.keySet().iterator();
        List<String> list = new ArrayList<String>();

        while (iter.hasNext()) {
            list.add(iter.next());
        }

        Collections.sort(list);
        sb.append("DataBox [name=" + name + "]\n{\n");
        for (int i = 0; i < list.size(); i++) {
            key = list.get(i);
            obj = this.get(key);
            sb.append(key);
            sb.append("=");
            sb.append(obj);
            sb.append(",\n");
        }
        sb.append("}");
        return sb.toString();
    }
//    public void encryptDataBox(String[] targets) {
//        for (String target : targets) {
//            String str = getString(target);
//            if (StringManager.isNotEmpty(str)) {
//                CubeOneItem cubeOneItem = CubeOneItem.getCubeOneItem(target);
//                put(target, AESUtil.encrypt(str, cubeOneItem));
//            }
//        }
//    }
//
//    public void decryptDataBox(String[] targets) {
//        for (String target : targets) {
//            String str = getString(target);
//            if (StringManager.isNotEmpty(str)) {
//                CubeOneItem cubeOneItem = CubeOneItem.getCubeOneItem(target);
//                put(target, AESUtil.decrypt(str, cubeOneItem));
//            }
//        }
//    }
//
//    public void decryptDataBox(String[] targets, String checkMaskingSession) {
//        for (String target : targets) {
//            String str = getString(target);
//            if (StringManager.isNotEmpty(str)) {
//                CubeOneItem cubeOneItem = CubeOneItem.getCubeOneItem(target);
//                put(target, AESUtil.decrypt(str, checkMaskingSession, cubeOneItem));
//            }
//        }
//    }

    @Override
    public DataBox clone() {
        DataBox clone = (DataBox) super.clone();
        return clone;
    }

    /**
     * databox default 처리
     * @return
     */
    public DataBox ifNullDefault() {
        return this.ifNullDefault(new DataBox());
    }

    /**
     * databox default 처리
     * @return
     */
    public DataBox ifNullDefault(DataBox dataBox) {
        return MapUtils.isEmpty(this) ? dataBox : this;
    }

    /**
     * 특정 key html 태그 삭제
     * @param key
     */
    public void deleteHtmlTag(String key) {
        this.put(key, Jsoup.clean(this.getString(key), Safelist.none()));
    }

    /**
     * 특정 key url 인코드
     * @param key
     */
    public void URLEncoderWithKey(String key) {
        this.put(key, URLEncoder.encode(this.getString(key)));
    }
}

