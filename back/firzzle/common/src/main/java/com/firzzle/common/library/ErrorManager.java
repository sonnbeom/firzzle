package com.firzzle.common.library;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @Class Name : ErrorManager
 * @Description : 에러정보관리 라이브러리
 *
 * @author 퍼스트브레인
 * @since 2014. 9. 26.
 */
public class ErrorManager {

    /** 로거 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorManager.class);


    /**
     * 에러메세지 JSP출력
     *
     * @param ex
     * @param isHtml
     * @return
     */
    public final static String getErrorStackTrace(Throwable ex, boolean isHtml) {
        String error_msg = "";
        ByteArrayOutputStream baos = null;
        PrintStream ps = null;
        try {
            if(ex != null) {
                baos = new ByteArrayOutputStream();
                ps = new PrintStream(baos,true,"UTF-8");
                //ex.printStackTrace(ps);
                error_msg = baos.toString("UTF-8");
                ////logger.error(error_msg);
                if (isHtml) {
                    error_msg = StringManager.convertHTML(error_msg);
                }
                ps.close();
                baos.close();
            }
        } catch (RuntimeException e) {
            ////logger.error("ErrorManager.getErrorStackTrace(Throwable ex) is critical error\r\n" + e.getMessage(), e);
            throw new RuntimeException();
        } catch (Exception e) {
//			e.printStackTrace();
            throw new RuntimeException();
            ////logger.error("ErrorManager.getErrorStackTrace(Throwable ex) is critical error\r\n" + e.getMessage(), e);
        }finally{
            try {
                if(baos != null) baos.close();
            } catch (Exception e2) {
                throw new RuntimeException();
                ////logger.error("ErrorManager.getErrorStackTrace(Throwable ex) is critical error\r\n" + e2.getMessage(), e2);
            }
            try {
                if(ps != null) ps.close();
            } catch (Exception e2) {
                //e2.printStackTrace();
                throw new RuntimeException();
                ////logger.error("ErrorManager.getErrorStackTrace(Throwable ex) is critical error\r\n" + e2.getMessage(), e2);
            }
        }
        return error_msg;
    }

    public static void getErrorStackTrace(Throwable ex, PrintWriter out) {

        ByteArrayOutputStream baos = null;
        PrintStream ps = null;
        try {
            baos = new ByteArrayOutputStream();
            ps = new PrintStream(baos,true,"UTF-8");
            //ex.printStackTrace(ps);
            String error_msg = baos.toString("UTF-8");
            String html="";

            if ( out != null ) {

                html +="	<h3>죄송합니다.<br />작업처리 중 오류가 발생했습니다.</h3>";
                html +="	<strong class=\"date\">["+ FormatDate.getDate("yyyy년  MM월 dd일  HH시 mm분 ss초") +"]</strong>";
                html +="    <p><textarea style=\"display:none;width:100%;height:300px;\">"+ error_msg +"</textarea></p>";
                out.println(html);
            }
        }catch ( Exception e ) {
            throw new RuntimeException();
            ////logger.error("ErrorManager.getErrorStackTrace(Throwable ex, PrintWriter out) is critical error\r\n" + e.getMessage());
        }finally{
            if ( ps != null ) { try { ps.close(); } catch ( Exception e1 ) { /*logger.debug(e1.getMessage());*/ } }
            if ( baos != null ) { try { baos.close(); } catch ( Exception e1 ) { /*logger.debug(e1.getMessage());*/ } }
            if ( out != null ) { try { out.close(); } catch ( Exception e1 ) { /*logger.debug(e1.getMessage());*/ } }
        }
    }

    public static void getErrorStackTrace(Throwable ex) {

        ByteArrayOutputStream baos = null;
        PrintStream ps = null;
        String error_msg = "";

        try {
            baos = new ByteArrayOutputStream();
            ps = new PrintStream(baos,true,"UTF-8");
            //ex.printStackTrace(ps);
            error_msg = baos.toString("UTF-8");
            ////logger.error("StackTrace : " + error_msg);
        }
        catch (Exception e) {
            throw new RuntimeException();
            ////logger.error("ErrorManager.getErrorStackTrace(Throwable ex) is critical error\r\n" + e.getMessage());
        }
    }


}
