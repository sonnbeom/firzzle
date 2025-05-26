package com.firzzle.common.library;

import com.firzzle.common.constant.CubeOneItem;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @Class Name : MyBatisSqlSessionTemplate
 * @Description : 마이바티스작업 공통 처리 클래스
 *
 * @author 퍼스트브레인
 * @since 2014. 9. 30.
 */
public class MyBatisSqlSessionTemplate extends SqlSessionTemplate {

    /** 로거 */
    private static final Logger logger = LoggerFactory.getLogger(MyBatisSqlSessionTemplate.class);

    /**
     * 생성자
     *
     * @param sqlSessionFactory
     */
    public MyBatisSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        super(sqlSessionFactory);
    }

    public MyBatisSqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType) {
        super(sqlSessionFactory, executorType);
    }

    /**
     * DataBox 형태로 반환
     *
     * @param arg0
     * @return
     */
    public DataBox selectDataBox(String arg0) {
        //logger.debug("[QUERY ID] [ " + arg0 + " ]");
        HashMap hm = (HashMap) super.selectOne(arg0);
        return this.convertDataBox(hm, false);
    }

    /**
     * DataBox 형태로 반환
     *
     * @param arg0
     * @param arg1
     * @return
     */
    public DataBox selectDataBox(String arg0, Object arg1) {
        //logger.debug("[QUERY ID] [ " + arg0 + " ]");
        arg1 = parametersFilter(arg1);
        HashMap hm = (HashMap) super.selectOne(arg0, arg1);
        boolean isNameMasking = arg1 instanceof RequestBox
                && ((RequestBox) arg1).getString("requestURI").contains("/admin")
                && !"Y".equals(((RequestBox) arg1).getSession("masking_auth", "N"));
        return this.convertDataBox(hm, isNameMasking);
    }

    /**
     * DataBox 형태로 반환(encrypt)
     *
     * @param query
     * @param data
     * @return
     */
    public DataBox selectDataBoxEncrypt(String query, Object data, String[] targets) {
        if (ArrayUtils.isEmpty(targets)) {
            throw new IllegalArgumentException("Essential argument is required");
        }
        //logger.debug("[QUERY ID] [ " + query + " ]");
        data = parametersEncrypt(data, targets, 10);
        HashMap hm = (HashMap) super.selectOne(query, data);
        boolean isNameMasking = data instanceof RequestBox
                && ((RequestBox) data).getString("requestURI").contains("/admin")
                && !"Y".equals(((RequestBox) data).getSession("masking_auth", "N"));
        return this.convertDataBox(hm, isNameMasking);
    }

    /**
     * ArrayList<DataBox> 형태로 반환
     *
     * @param arg0
     * @return
     */
    public ArrayList<DataBox> selectDataBoxList(String arg0) {
        //logger.debug("[QUERY ID] [ " + arg0 + " ]");
        ArrayList list = (ArrayList) super.selectList(arg0);
        return this.convertArrayList(list, false);
    }

    /**
     * ArrayList<DataBox> 형태로 반환
     *
     * @param arg0
     * @param arg1
     * @return
     */
    public ArrayList<DataBox> selectDataBoxList(String arg0, Object arg1) {
        //logger.debug("[QUERY ID] [ " + arg0 + " ]");
        arg1 = parametersFilter(arg1);
        ArrayList list = (ArrayList) super.selectList(arg0, arg1);
        boolean isNameMasking = arg1 instanceof RequestBox
                && ((RequestBox) arg1).getString("requestURI").contains("/admin")
                && !"Y".equals(((RequestBox) arg1).getSession("masking_auth", "N"));
        return this.convertArrayList(list, isNameMasking);
    }

    /**
     * ArrayList<DataBox> 형태로 반환
     *
     * @param query
     * @param data
     * @return
     */
    public ArrayList<DataBox> selectDataBoxListEncrypt(String query, Object data, String[] targets) {
        if (ArrayUtils.isEmpty(targets)) {
            throw new IllegalArgumentException("Essential argument is required");
        }
        //logger.debug("[QUERY ID] [ " + query + " ]");
        data = parametersEncrypt(data, targets, 10);
        ArrayList list = (ArrayList) super.selectList(query, data);
        boolean isNameMasking = data instanceof RequestBox && ((RequestBox) data).getString("requestURI").contains("/admin");
        return this.convertArrayList(list, isNameMasking);
    }

    /**
     * ArrayList<DataBox> 형태로 반환
     *
     * @param arg0
     * @param arg1
     * @param arg3
     * @return
     */
    public ArrayList<DataBox> selectDataBoxList(String arg0, Object arg1, RowBounds arg3) {
        //logger.debug("[QUERY ID] [ " + arg0 + " ]");
        arg1 = parametersFilter(arg1);
        ArrayList list = (ArrayList) super.selectList(arg0, arg1, arg3);
        boolean isNameMasking = arg1 instanceof RequestBox
                && ((RequestBox) arg1).getString("requestURI").contains("/admin")
                && !"Y".equals(((RequestBox) arg1).getSession("masking_auth", "N"));
        return this.convertArrayList(list, isNameMasking);
    }

    /**
     * Insert 실행
     *
     * @param arg0
     * @return
     */
    @Override
    public int insert(String arg0) {
        //logger.debug("[QUERY ID] [ " + arg0 + " ]");
        return super.insert(arg0);
    }

    /**
     * Insert 실행
     *
     * @param arg0
     * @param arg1
     * @return
     */
    @Override
    public int insert(String arg0, Object arg1) {
        ////logger.debug("[QUERY ID] [ " + arg0 + " ]");
        arg1 = parametersFilter(arg1);
        return super.insert(arg0, arg1);
    }

    /**
     * Insert 실행(encrypt 특정 key값 암호화)
     *
     * @param query
     * @param data
     * @param targets
     * @return
     */
    public int insertEncrypt(String query, Object data, String[] targets) {
        if (ArrayUtils.isEmpty(targets)) {
            throw new IllegalArgumentException("Essential argument is required");
        }
        //logger.debug("[QUERY ID] [ " + query + " ]");
        data = parametersEncrypt(data, targets, 11);
        return super.insert(query, data);
    }

    /**
     * Update 실행
     *
     * @param arg0
     * @return
     */
    @Override
    public int update(String arg0) {
        //logger.debug("[QUERY ID] [ " + arg0 + " ]");
        return super.update(arg0);
    }

    /**
     * Update 실행
     *
     * @param arg0
     * @param arg1
     * @return
     */
    @Override
    public int update(String arg0, Object arg1) {
        //logger.debug("[QUERY ID] [ " + arg0 + " ]");
        arg1 = parametersFilter(arg1);
        return super.update(arg0, arg1);

    }

    /**
     * Update 실행(encrypt 특정 key값 암호화)
     *
     * @param query
     * @param data
     * @param targets
     * @return
     */
    public int updateEncrypt(String query, Object data, String[] targets) {
        if (ArrayUtils.isEmpty(targets)) {
            throw new IllegalArgumentException("Essential argument is required");
        }
        //logger.debug("[QUERY ID] [ " + query + " ]");
        data = parametersEncrypt(data, targets, 12);
        return super.update(query, data);
    }

    /**
     * Delete 실행
     *
     * @param arg0
     * @return
     */
    @Override
    public int delete(String arg0) {
        //logger.debug("[QUERY ID] [ " + arg0 + " ]");
        return super.delete(arg0);
    }

    /**
     * Delete 실행
     *
     * @param arg0
     * @param arg1
     * @return
     */
    @Override
    public int delete(String arg0, Object arg1) {
        //logger.debug("[QUERY ID] [ " + arg0 + " ]");
        arg1 = parametersFilter(arg1);
        return super.delete(arg0, arg1);
    }

    /**
     * Select 실행
     *
     * @param arg0
     * @return
     */
    @Override
    public Object selectOne(String arg0) {
        //logger.debug("[QUERY ID] [ " + arg0 + " ]");
        return super.selectOne(arg0);
    }

    /**
     * Select 실행
     *
     * @param arg0
     * @param arg1
     * @return
     */
    @Override
    public Object selectOne(String arg0, Object arg1) {
        //logger.debug("[QUERY ID] [ " + arg0 + " ]");
        arg1 = parametersFilter(arg1);
        return super.selectOne(arg0, arg1);
    }

    /**
     * HashMap을 DataBox 형태로 변환
     *
     * @param
     * @return
     */
    private ArrayList<DataBox> convertArrayList(ArrayList list, boolean isNameMasking) {

        ArrayList<DataBox> result = null;
        if (list != null) {
            result = new ArrayList<DataBox>();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                result.add(this.convertDataBox((HashMap) list.get(i), isNameMasking));
            }
        }
        return result;
    }

    /**
     * HashMap을 DataBox 형태로 변환
     *
     * @param hm
     * @return
     */
    private DataBox convertDataBox(HashMap hm, boolean isNameMasking) {

        DataBox dbox = null;
        if (hm != null) {
            dbox = new DataBox("responsebox");
            String key = "";
            Object obj = "";
            Iterator<String> iter = hm.keySet().iterator();
            while (iter.hasNext()) {

                key = iter.next();
                if (isNameMasking && "name".equals(key.toLowerCase())) {
                    obj = MaskingUtil.masking((String)hm.get(key), "name", "a*a", "*");
                } else {
                    obj = hm.get(key);
                }
                dbox.put("d_" + key.toLowerCase(), obj);
            }
        }
        return dbox;
    }

    /**
     * SQL필터링 검사
     *
     * @param arg1
     * @return
     */
    private Object parametersFilter(Object arg1) {
        if (arg1 instanceof Map || arg1 instanceof Hashtable || arg1 instanceof HashMap || arg1 instanceof RequestBox || arg1 instanceof DataBox) {

            String key = "";
            Object obj = "";
            Iterator<String> iter = ((Map) arg1).keySet().iterator();
            while (iter.hasNext()) {

                key = iter.next();
                obj = ((Map<String, Object>) arg1).get(key);
                if (obj instanceof String) {
                    obj = replaceFilter((String) obj);
                    ((Map) arg1).put(key, obj);
                }
            }
        } else if (arg1 instanceof String) {
            arg1 = replaceFilter((String) arg1);
        }
        return arg1;
    }

    /**
     * SQL파라미터 encrypt (특정 key값 필터링)
     *
     * @param data
     * @param targets
     * @return
     */
    private Object parametersEncrypt(Object data, String[] targets, int crudLog) {
        if (data instanceof Map || data instanceof Hashtable || data instanceof HashMap || data instanceof RequestBox || data instanceof DataBox) {
            ((Map<Object,Object>) data).replaceAll(((key, value) -> {
                if(this.equals((String)key, targets)){
                    try {
                        CubeOneItem cubeOneItem = CubeOneItem.getCubeOneItem((String) key);
                        return AESUtil.encrypt((String)value, cubeOneItem);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return value;
            }));
        }
        return data;
    }

    /**
     * 문자열 포함 확인
     *
     * @param key
     * @param targets
     * @return
     */
    private boolean equals(String key, String[] targets) {
        return Arrays.stream(targets)
                .anyMatch(reg -> key.equals(reg));
    }

    /**
     * SQL필터링처리
     *
     * @param param
     * @return
     */
    private String replaceFilter(String param) {

        return param;
    }
}

