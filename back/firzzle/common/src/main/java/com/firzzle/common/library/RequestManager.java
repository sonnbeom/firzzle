package com.firzzle.common.library;

import com.firzzle.common.utils.EmojiUtils;
import com.firzzle.common.utils.XssUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.util.UrlPathHelper;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * @Class Name : RequestManager
 * @Description : RequestBox 관리
 *
 * @author 퍼스트브레인
 * @since 2014. 9. 30.
 */
public class RequestManager {

    /** 로거 */
    private static final Logger logger = LoggerFactory.getLogger(RequestManager.class);

    /**
     * 인터셉터에서 사용하는 RequestBox 생성
     *
     * @param request
     * @return
     */
    public static RequestBox getInterceptorBox(HttpServletRequest request) throws Exception {
        RequestBox box = new RequestBox("requestbox");

        //파라미터 정보 담기
        //String key = "";
        //String[] values = null;
        Enumeration<?> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            //key = (String) enumeration.nextElement();
            //values = request.getParameterValues(key);
            //if(values != null){
            //	box.put(key, (values.length > 1) ? values:values[0] );
            //}

            String key = (String) enumeration.nextElement();
            String[] values = request.getParameterValues(key);
            if (values != null && values.length > 0) {
                String sanitizedValue = sanitizeParameterValue(values[0]);
                box.put(key, sanitizedValue);
            }
        }


        // 공통파라미터 정보
        box.put("request", request);
        box.put("session", request.getSession(true));
        box.put("ndate", FormatDate.getDate("yyyyMMddHHmmss"));
        box.put("userip", getClientIP(request));
        box.put("requestURL", request.getRequestURL());
        box.put("requestURI", request.getRequestURI());
        box.put("contextPath", request.getContextPath());
        box.put("requestDomain", request.getServerName());
        box.put("userAgent", StringManager.htmlSpecialChar(request.getHeader("User-Agent")));
        //box.put("u_locale", box.getSession("u_locale", "ko"));

        //회원사용언어 제공
        HttpSession session = box.getHttpSession();
        Locale locales = null;
        locales = new Locale("ko");
        session.setAttribute("u_locale", locales.toString());
        session.setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locales);

        if("".equals(box.getSession("u_grcode", ""))
                && box.getString("requestDomain").indexOf("www.learninghrd.co.kr") > -1) {
            box.put("u_grcode", "G999459");
            box.put("u_comp", "A10459");

        }else {
            //사용자 공통파라미터 정보
            box.put("u_userid", box.getSession("u_userid"));
            box.put("u_comp", box.getSession("u_comp"));
            box.put("u_grcode", box.getSession("u_grcode"));
        }


        // 관리자 공통파라미터 정보
        box.put("userid", box.getSession("userid"));
        box.put("grcode", box.getSession("grcode"));
        box.put("gadmin", box.getSession("gadmin"));
        box.put("gauthid", box.getSession("gauthid"));
        if(!box.getString("gadmin").equals("") && box.getString("gadmin").length() >= 2) {
            box.put("mgadmin", box.getString("gadmin"));
        }
        return box;
    }

    private static String sanitizeParameterValue(String value) {
        return StringEscapeUtils.escapeHtml4(value);
    }

    private static boolean isValidLocale(Locale locale) {
        return locale != null && Arrays.asList(Locale.getAvailableLocales()).contains(locale);
    }

    private static boolean isValidDomain(String domain) {
        return domain != null && domain.contains("www.learninghrd.co.kr");
    }

    /** 문자열이 숫자인지 아닌지 확인
     * @param input
     * @return Boolean
     */

    public static boolean isNumeric(String input) {
        try {
            Double.parseDouble(input);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }



    /**
     * ContentType 확인
     *
     * @param request
     * @return
     */
    public static boolean isMultipartForm(HttpServletRequest request) {
        String contentType = "";

        if (request.getContentType() != null) {
            contentType = request.getContentType();
        }
        return contentType.indexOf("multipart/form-data") >= 0;
    }

    public static RequestBox getBox(HttpServletRequest request) throws Exception {
        RequestBox box = new RequestBox("requestbox");

        // 인증 정보 처리 (HeaderAuthenticationFilter에서 설정한 속성 사용)
        Object uuid = request.getAttribute("uuid");
        if (uuid != null) {
            box.put("uuid", uuid.toString());
        } else {
            // 헤더에서 시도
            String headerUuid = request.getHeader("X-User-UUID");
            if (headerUuid != null && !headerUuid.isEmpty()) {
                box.put("uuid", headerUuid);
            }
        }

        //파라미터 정보 담기
        String key = "";
        String[] values = null;
        Enumeration<?> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            key = (String) enumeration.nextElement();
            // 들어오는 key 로깅
            logger.debug("들어오는 key: {}", key);

            values = request.getParameterValues(key);
            if(values != null){

                /**멀티파트 폼의 XSS처리를 추가 */
                // TODO Xss 처리 추가할 것!
//				if (isMultipartForm(request)) {
//
//					for (int index = 0; index < values.length; index++) {
//						values[index] = XssEscapeFilter.getInstance().doFilter(request.getRequestURI(), key, values[index]);
//					}
//				}

                logger.debug("key: {}, values: {}", key, Arrays.toString(values));

                values = XssUtils.cleanXss(key, "https" + "://" + request.getServerName(), values);
                values = EmojiUtils.removeEmoji(values);

                box.put(key, (values.length > 1) ? values:values[0] );
            }
        }

        //멀티파트 폼처리시 파일 업로드및 결과 반환
        if (isMultipartForm(request)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Map<String, MultipartFile> files = multiRequest.getFileMap();
            if(files != null && files.size() > 0){

                ConfigSet conf = new ConfigSet();

                //파일업로드 경로
                String uriPath = getUriPath(request);

                if("piece".equals(uriPath)) {
//					//logger.debug("강제 변경 >> piece 는 contentspool 과 같이 쓰려고 함!!!! ");
                    uriPath = "contentspool";
                }

                // 어학 신청 추가
                if("propose".equals(uriPath)) {
                    uriPath = "langpropose";
                }

                if (isNumeric(uriPath) || "homework".equals(uriPath) || "modifRecognitionTime".equals(uriPath) || "insRecognitionTime".equals(uriPath)) {
                    uriPath = "default";
                }

                String urlUploadRoot = conf.getProperty("url.upload");
                String urlUpload = conf.getProperty("dir.upload." + uriPath);

                // 파일사이즈구하기
                String fileSizeKey = conf.getDir(conf.getProperty("file.size"), uriPath);
                int fileSize = conf.getInt("file.size." + fileSizeKey);
                // 파일확장자타입구하기
                String fileTypekey = conf.getDir(conf.getProperty("file.type"), uriPath);
                String fileType = conf.getProperty("file.type." + fileTypekey);

                box.put("uriPath", uriPath);
                box.put("urlUploadRoot", urlUploadRoot);
                box.put("urlUpload", urlUpload);
                box.put("fileSize", fileSize);
                box.put("fileType", fileType);

                //첨부파일 저장
                box = MultipartRequestManager.readFile(files, box);
            }
        }


        // 공통파라미터 정보
        box.put("request", request);
        box.put("session", request.getSession(true));
        box.put("ndate", FormatDate.getDate("yyyyMMddHHmmss"));
        box.put("userip", getClientIP(request));
        box.put("requestURL", request.getRequestURL());
        box.put("requestURI", request.getRequestURI());
        box.put("contextPath", request.getContextPath());
        box.put("requestDomain", request.getServerName());
        box.put("userAgent", StringManager.htmlSpecialChar(request.getHeader("User-Agent")));
        //box.put("u_locale", box.getSession("u_locale", "ko"));
        box.put("referer", StringManager.htmlSpecialChar(request.getHeader("referer"))); // 학습창 뒤로가기를 위해서 추가함.

        //회원사용언어 제공
        HttpSession session = box.getHttpSession();
        Locale locales = null;
        locales = new Locale("ko");
        session.setAttribute("u_locale", locales.toString());
        session.setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locales);

        if("".equals(box.getSession("u_grcode", ""))
                && box.getString("requestDomain").indexOf("learninghrd.co.kr") > -1) {
            box.put("u_grcode", "G999459");
            box.put("u_comp", "A10459");

        }else {
            //사용자 공통파라미터 정보

            // admin에서 컨텐츠 바로 확인할때 ( 사용자화면 로그인 안한상태에서 )
            if( box.getString("p_adminUserid") != null && !box.getString("p_adminUserid").equals("") ){
                box.setSession("u_userid", box.getString("p_adminUserid"));
                box.setSession("u_grcode", box.getString("p_adminGrcode"));
            }

            box.put("u_userid", box.getSession("u_userid"));
            box.put("u_grcode", box.getSession("u_grcode"));
            box.put("u_comp", box.getSession("u_comp"));
            box.put("u_global_cdn_use_yn", box.getSession("u_global_cdn_use_yn"));
        }

        //관리자 공통파라미터 정보
        box.put("userid", box.getSession("userid", box.getSession("u_userid")));
        box.put("grcode", box.getSession("grcode", box.getSession("u_grcode")));
        box.put("global_cdn_use_yn", box.getSession("global_cdn_use_yn"));
        box.put("gadmin", box.getSession("gadmin"));
        box.put("comp", box.getSession("comp", box.getSession("u_comp")));
        box.put("gauthid", box.getSession("gauthid"));
        if(!box.getString("gadmin").equals("") && box.getString("gadmin").length() >= 2) {
            box.put("mgadmin", box.getString("gadmin"));
        }

        //FLEX 추가 공통파라미터
        if ("Y".equals(box.getSession("u_flex_gr_yn"))) {
            box.put("s_flex_comp_yn", "Y");
            box.put("u_flex_gr_yn", "Y");
        }
        box.put("deptSeq", box.getSession("deptSeq"));
        box.put("periodSeq", box.getSession("periodSeq"));
        box.put("startYmd", box.getSession("startYmd"));
        box.put("endYmd", box.getSession("endYmd"));

        // 엑셀 업로드 세션 관리
//		String v_useSession = box.getString("p_useSession", "");
//		if(!"Y".equals(v_useSession)) {
//
//			if( session.getAttribute("session_subjseq_excelList") != null ) {
//				session.removeAttribute("session_subjseq_excelList");
//			}
//			if( session.getAttribute("session_subjSul_excelList") != null ) {
//				session.removeAttribute("session_subjSul_excelList");
//			}
//			if( session.getAttribute("session_contentsPoolExam_excelList") != null ) {
//				session.removeAttribute("session_contentsPoolExam_excelList");// 컨텐츠풀 시험 미리보기 등록을 위해 생성한 세션을 삭제한다
//			}
//			if( session.getAttribute("session_contentsPoolSul_excelList") != null ) {
//				session.removeAttribute("session_contentsPoolSul_excelList");// 컨텐츠풀 설문문항 미리보기 등록을 위해 생성한 세션을 삭제한다
//			}
//			if( session.getAttribute("session_contentsPoolSulTarget_excelList") != null ) {
//				session.removeAttribute("session_contentsPoolSulTarget_excelList");// 컨텐츠풀 설문 대상자 미리보기 등록을 위해 생성한 세션을 삭제한다
//			}
//			if( session.getAttribute("session_subjExam_excelList") != null ) {
//				session.removeAttribute("session_subjExam_excelList");// 과정 시험 미리보기 등록을 위해 생성한 세션을 삭제한다
//			}
//
//			if( session.getAttribute("session_tutor_excelList") != null ) {
//				session.removeAttribute("session_tutor_excelList");// 강사/튜터 미리보기 등록을 위해 생성한 세션을 삭제한다
//			}
//
//			if( session.getAttribute("session_langStold_excelList") != null ) {
//				session.removeAttribute("session_langStold_excelList");// 어학 결과 업로드 미리보기 등록을 위해 생성한 세션을 삭제한다
//			}
//
//			if( session.getAttribute("session_langSchedule_excelList") != null ) {
//				session.removeAttribute("session_langSchedule_excelList");// 어학검정 개설 미리보기 등록을 위해 생성한 세션을 삭제한다
//			}
//
//			if( session.getAttribute("session_ge_excelList") != null ) {
//				session.removeAttribute("session_ge_excelList");// ge 미리보기 등록을 위해 생성한 세션을 삭제한다
//			}
//
//		}

        ////logger.debug(box.toString());
        ////logger.debug(box.sessionToString());

        return box;
    }

    /**
     * 호출 URL 에서 key 정보를 추출 한다.
     *
     * @param request
     * @return
     */
    public static String getUriPath(HttpServletRequest request) throws Exception{
        String uriPath = "";
        // 호출 URL 정보 얻어오기
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        uriPath = urlPathHelper.getOriginatingRequestUri(request);

        String[] servletPaths = uriPath.split("/");
        if (servletPaths != null && servletPaths.length > 2) {
            if (uriPath.lastIndexOf(".do") > -1) {
                uriPath = servletPaths[servletPaths.length - 2];
            }
        } else {
            if (uriPath.lastIndexOf(".do") > -1) {
                uriPath = uriPath.substring(uriPath.lastIndexOf('/') + 1, uriPath.lastIndexOf(".do"));
            }
        }

        return uriPath;
    }

    /**
     * 호출 URL 에서 key 정보를 추출 한다.
     * @param box
     * @return
     */
    public static String getUriPath(RequestBox box) throws Exception {
        return getUriPath(box.getHttpServletRequest());
    }


    /**
     * Client IP 가져오기
     * @param request
     * @return
     */
    public static String getClientIP(HttpServletRequest request) {

        String v_ip = request.getHeader("X-Real-IP");

        if (v_ip == null) {
            v_ip = request.getHeader("X-Forwarded-For");
        }
        if(v_ip == null) {
            v_ip = request.getHeader("Proxy-Client-IP");
        }
        if(v_ip == null) {
            v_ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(v_ip == null) {
            v_ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if(v_ip == null) {
            v_ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if(v_ip == null) {
            v_ip = request.getRemoteAddr();
        }
        return v_ip;
    }





}

