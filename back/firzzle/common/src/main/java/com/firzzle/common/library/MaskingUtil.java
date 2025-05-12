package com.firzzle.common.library;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Slf4j
public class MaskingUtil {

    /**
     * 패턴에 맞춰 마스킹(이름, 이메일, 폰번호)
     * TODO: 이메일, 폰번호 마스킹
     * @param target 바꾸려는 데이터
     * @param field name, phone, email
     * @param pattern a*: 두 번째 글자만 가림,
     *                a*a: 가운데 다 가림,
     *                a**: 첫 글자 제외 다 가림
     * @param mask 마스킹 대체텍스트
     * @return string
     * @throws Exception
     * */
//    public static String masking(DataBox box, String field, String pattern) throws Exception {
    public static String masking(String target, String field, String pattern, String mask) {
        String result = target;

        if (StringUtils.isEmpty(target)) {
            return "";
        }

        if("name".equals(field)) {
            result = nameMasking(target, pattern, mask);
        } else if("phone".equals(field)) {
            // TODO : 폰번호 마스킹-가운데 3,4자리수 체크
        } else if ("email".equals(field)) {
            // TODO : 이메일 마스킹-공백체크
        } else if ("normal".equals(field)) {
            result = normalMasking(target, pattern, mask);
        }

        return result;
    }

    /**
     * 일반 마스킹
     * @param id 마스킹 대상 아이디
     * @param pattern 마스킹 패턴
     * @param mask 마스킹 대체 텍스트
     * @return
     */
    private static String normalMasking(String id, String pattern, String mask) {
        int length = id.length();
        if(length > 2) {
            if("a*a".equals(pattern)) { // 가운데 다 가림
                id = id.substring(0, 1).concat(mask.repeat(length - 2)).concat(id.substring(length-1, length));
            } else if("a**".equals(pattern)) { // 첫 글자 빼고 다 가림
                id = id.substring(0,1).concat(mask.repeat(length - 1));
            } else if("**".equals(pattern)) {
                id = mask.repeat(length);
            } else {
                log.debug("마스킹 패턴 형식이 잘못되었습니다.");
            }
        }
        return id;
    }

    /**
     * 이름 마스킹
     * @param name 이름데이터
     * @param pattern 패턴타입
     * @param mask 마스킹 대체텍스트
     *
     */
    public static String nameMasking(String name, String pattern, String mask) {
        int length = name.length();
        int last = name.length();
        String middleMask = "";

        if(length > 2) {
            if("a*".equals(pattern)) { // 두 번째만 가림
                middleMask = name.substring(1, 2);
                last = 2;
            } else if("a*a".equals(pattern)) { // 가운데 다 가림
                middleMask = name.substring(1, length-1);
                last = length - 1;
            } else if("a**".equals(pattern)) { // 첫 글자 뺴고 다 가림
                middleMask = name.substring(1, length);
                last = 1;
            } else {
                log.debug("마스킹 패턴 형식이 잘못되었습니다.");
                return name;
            }
        } else { // 이름이 외자
            middleMask = name.substring(1, length);
        }

        String dot = "";
        for (int i = 0; i < middleMask.length(); i++) {
            dot += mask;
        }

        if(length > 2 && last > 1) {
            return name.substring(0, 1) + middleMask.replace(middleMask, dot) + name.substring(last, length);
        } else { // 외자와 첫 글자 빼고 다 가림
            return name.substring(0, 1) + middleMask.replace(middleMask, dot);
        }
    }

    public static void listMasking(List<DataBox> target, String field, String pattern, String mask, String[] maskingColumns) {
        target.stream().forEach(item -> {
            for (String maskingColumn : maskingColumns) {
                item.put(maskingColumn, masking(item.getString(maskingColumn), field, pattern, mask));
            }
        });
    }
}
