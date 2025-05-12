package com.firzzle.common.library;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Class Name : RanderMessageManager.java
 * @Description : 다국어 메시지 반환
 *
 * @author 퍼스트브레인
 * @since 2019. 8. 13.
 */
@Slf4j
public class RenderMessageManager {

    /**로거*/
    private static final Logger logger = LoggerFactory.getLogger(RenderMessageManager.class);

    /**
     * MessageSourceAccessor
     */
    private static MessageSourceAccessor msAcc = null;


    public void setMessageSourceAccessor(MessageSourceAccessor msAcc) {
        RenderMessageManager.msAcc = msAcc;
    }

    /**
     * KEY에 해당하는 메세지 반환
     *
     * @param key
     * @return
     */
    public static String getMessage(String key, @Nullable Object[] args,String defaultStr) {
        String result = msAcc.getMessage(key, args, defaultStr);
        if(result == null || result.equals("")) {
            result = defaultStr;
        }
        return result;
    }

    private static Object[] getMessageFromDatas(Object... codes) {
        return Arrays.stream(codes)
                .map(code -> {
                    if (code instanceof String && ((String) code).startsWith("MSG_")) {
                        return RenderMessageManager.getMessage(String.valueOf(code));
                    }
                    return code;
                })
                .collect(Collectors.toList())
                .toArray(Object[]::new);
    }

    public static String getMessage(String key, String defaultStr, Object... datas) {
        Object[] arguments = getMessageFromDatas(datas);
        return StringUtils.defaultString(msAcc.getMessage(key, arguments), defaultStr);
    }

    public static String getMessageWithDelimeter(String delimeter, Object... datas) {
        String[] args = Arrays.stream(getMessageFromDatas(datas)).map(Object::toString).toArray(String[]::new);
        return String.join(delimeter, args);
    }

    /**
     * KEY에 해당하는 메세지 반환
     *
     * @param key
     * @return
     */
    public static String getMessage(String key, String defaultStr) {
        String result = msAcc.getMessage(key, defaultStr);
        if(result == null || result.equals("")) {
            result = defaultStr;
        }
        //logger.debug(key +  " ==> " + result);
        return result;
    }

    /**
     * KEY에 해당하는 메세지 반환
     *
     * @param key
     * @return
     */
    public static String getMessage(String key) {
        return getMessage(key, "");
    }

    public static RequestBox createMessagesToBox(@NotNull String[] keys, @NotNull RequestBox box) {
        RequestBox clone = box.clone();

        Arrays.stream(keys)
                .forEach(key -> clone.put(key, RenderMessageManager.getMessage(key)));

        return clone;
    }

    public static DataBox createMessagesToBox(@NotNull String[] keys, @NotNull DataBox box) {
        DataBox clone = box.clone();

        Arrays.stream(keys)
                .forEach(key -> clone.put(key, RenderMessageManager.getMessage(key)));

        return clone;
    }

}

