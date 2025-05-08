package com.firzzle.common.library;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @Class Name : ConfigSet.java
 * @Description : 시스템 설정정보 값을 실시간로드 라이브러리
 *
 * @author 퍼스트브레인
 * @since 2019. 8. 13.
 */
@Slf4j
public class ConfigSet {

    /** 설정프러퍼티 */
    protected static Properties props;

    /**
     * 생성자
     *
     * @throws Exception
     */
    public ConfigSet() throws Exception {

        initialize();
    }

    /**
     * 프러퍼티 geeter
     *
     * @return
     */
    public Properties getProperties() {
        return ConfigSet.props;
    }

    /**
     * 시스템 설정값 RealTime Loding
     *
     * @throws Exception
     */
    protected void initialize() throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        ConfigurableEnvironment env = context.getEnvironment();

        String profiles = "default";

        if(env != null && env.getActiveProfiles() != null && env.getActiveProfiles().length > 0){
            profiles = env.getActiveProfiles()[0];
        }

        if (ConfigSet.props != null) return;
        InputStream inputStream = null;
        BufferedInputStream bi = null;
        try {
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            Resource systemPropsResource = resourceLoader.getResource("classpath:config/system.properties");
            if("production".equals(profiles)){
                systemPropsResource = resourceLoader.getResource("classpath:config/system-prd.properties");
            }else if("dev".equals(profiles)){
                systemPropsResource = resourceLoader.getResource("classpath:config/system-dev.properties");
            } else if("ssg-dev".equals(profiles)) {
                systemPropsResource = resourceLoader.getResource("classpath:config/system-ssg-dev.properties");
            } else if("ssg-stg".equals(profiles)) {
                systemPropsResource = resourceLoader.getResource("classpath:config/system-ssg-stg.properties");
            } else if("ssg-prd".equals(profiles)) {
                systemPropsResource = resourceLoader.getResource("classpath:config/system-ssg-prd.properties");
            }
            inputStream = systemPropsResource.getInputStream();
            bi = new BufferedInputStream(inputStream);
            ConfigSet.props = new Properties();
            ConfigSet.props.load(bi);
        } catch (Exception ex) {
            //log.error(this.getClass().getName() + " - 시스템 설정정보 로딩 실패: " + ex.getMessage());
            throw ex;
        } finally {
            if(bi != null) bi.close();
            if(inputStream != null) inputStream.close();
        }
    }

    /**
     * 시스템 설정값을 boolean type으로 반환
     *
     * @param key
     * @return
     */
    public boolean getBoolean(String key) {
        boolean value = Boolean.parseBoolean(props.getProperty(key));
        return value;
    }

    /**
     * 시스템 설정값을 int type으로 반환
     *
     * @param key
     * @return
     */
    public int getInt(String key) {

        int value = Integer.parseInt(props.getProperty(key));
        return value;
    }

    /**
     * 시스템 설정값을 String type으로 반환
     *
     * @param key
     * @return
     */
    public String getProperty(String key) {

        String value = null;
        value = props.getProperty(key);
        if (value == null) {
            value = "";
        }
        return value.trim();
    }

    /**
     * 시스템 설정값중에서 파일이 업로드되는 디렉토리명을 String type으로 반환
     *
     * @param key
     * @param dir
     * @return
     */
    public String getDir(String key, String dir) {
        String dirKey = "";
        StringTokenizer st = new StringTokenizer(key, ";");
        while (st.hasMoreElements()) {
            String token = st.nextToken();
            int isDir = token.indexOf(dir.toLowerCase());
            if (isDir > -1) {
                dirKey = token;
                return dirKey;
            } else {
                dirKey = "default";
            }
        }
        return dirKey;
    }

}

