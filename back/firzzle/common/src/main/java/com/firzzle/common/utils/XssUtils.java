package com.firzzle.common.utils;

import com.firzzle.common.library.StringManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.util.List;

public class XssUtils {

    public final static List<String> RELAXED_LIST = List.of("p_contents", "p_content", "p_menunm", "p_grcodenm", "p_keywordNm", "p_body", "p_extr_gw_editor");
    public final static List<String> NONE_LIST = List.of("p_sco_val", "p_sco_key", "p_template_content");
    public static String cleanXss(String key, String domain, String value) {
        Safelist safelist = Safelist.basic();

        if (NONE_LIST.contains(key)) {
            return value;
        }
        if (RELAXED_LIST.contains(key)) {
            safelist = Safelist.relaxed()
                    .addTags("img", "iframe", "div", "video", "source")
                    .addAttributes("img", "align", "alt", "height", "src", "title", "width", "style")
                    .addProtocols("img", "src", "http", "https")
                    .addAttributes("iframe", "frameborder", "src", "width", "height", "class", "onload", "style") //허용할 태그의 속성 추가
                    .addAttributes("p", "style")
                    .addAttributes("span", "style")
                    .addAttributes("strike", "style")
                    .addAttributes("div", "class")
                    .addAttributes("font", "face", "color")
                    .addAttributes("video", "controls", "muted", "playsinline", "width", "height")
                    .addAttributes("source", "src", "type")
                    .addProtocols("iframe", "src", "http", "https")
                    .preserveRelativeLinks(true);
        }
        return Jsoup.clean(StringManager.htmlSpecialCharDecode(value), domain, safelist, new Document.OutputSettings().prettyPrint(false));
    }

    public static String[] cleanXss(String key, String domain, String[] values) {
        String[] result = new String[values.length];
        for (int idx = 0; idx < values.length; idx++) {
            result[idx] = cleanXss(key, domain, values[idx]);
        }

        return result;
    }
}
