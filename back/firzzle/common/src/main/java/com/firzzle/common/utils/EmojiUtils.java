package com.firzzle.common.utils;

public class EmojiUtils {

    private static final String EMOJI_REGEX = "[\\x{10000}-\\x{10FFFF}]";

    /**
     * String 4Byte 삭제
     *
     * @param input
     * @return
     */
    public static String removeEmoji(String input) {
        return input.replaceAll(EMOJI_REGEX, "");
    }

    /**
     * Xml 문자 들어가는 현상 삭제(문자열 추가 필요)
     * @param input
     * @return
     */
    public static String removeXmlString(String input) {
        return input.replace("&#xb;", "");
    }

    /**
     * String 배열 4Byte 삭제
     * @param input
     * @return
     */
    public static String[] removeEmoji(String[] input) {
        String[] result = new String[input.length];

        for (int idx = 0; idx < input.length; idx++) {
            //이모지 삭제
            result[idx] = removeEmoji(input[idx]);
            //추가 &#xb; 문자열 삭제
            result[idx] = removeXmlString(input[idx]);
        }
        return result;
    }
}