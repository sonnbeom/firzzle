package com.firzzle.common.library;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @Class Name : MultipartRequestManager
 * @Description : MultipartFormData 를 처리하는 라이브러리
 *
 * @author 퍼스트브레인
 * @since 2014. 9. 26.
 */
public class MultipartRequestManager {

    /** 로거 */
    private static final Logger logger = LoggerFactory.getLogger(MultipartRequestManager.class);

    /**
     * 첨부파일 저장후 읽어드리기
     * @return
     * @throws Exception
     */
    public static RequestBox readFile(Map<String, MultipartFile> files, RequestBox box) throws Exception {

        ConfigSet conf = new ConfigSet();

        String profile = conf.getProperty("project.deploy");

        //logger.debug("profile-readFile  ::::::" + profile);

        String attachID = box.getString("uriPath");
        String attachRoot = box.getString("urlUploadRoot");
        String attachDir = box.getString("urlUpload");
        String attachExt = box.getString("fileType");
        long attachSize = box.getInt("fileSize");

        attachDir = attachDir.replace("//", "/");
        attachDir = attachDir.replace("\\", File.separator);
        attachDir = attachDir.replace("/", File.separator);


        Iterator<Entry<String, MultipartFile>> itr = files.entrySet().iterator();
        MultipartFile file = null;
        int fileIdx = 0;
        while (itr.hasNext()) {

            Entry<String, MultipartFile> entry = itr.next();
            String key = entry.getKey();
            file = entry.getValue();

            //실제파일명
            String realFile = file.getOriginalFilename();


            //파일이 없는경우
            if (!"".equals(realFile)) {

                //파일위치
                fileIdx++;

                //저장파일명
                String saveFile = "";

                //파일확장자
                String fileExt = "";
                int idx = realFile.lastIndexOf('.');
                if(idx > -1){
                    fileExt = realFile.substring(idx + 1);
                    fileExt = fileExt.toLowerCase();
                    saveFile = attachID + "_" + getTimeStamp() + "_" + fileIdx + "." + fileExt;
                }else{
                    saveFile = attachID + "_" +  getTimeStamp() + "_" + fileIdx;
                }

                //파일사이즈
                long fileSize = file.getSize();
                //파일경로
                String filePath = attachRoot + attachDir + saveFile;

                //디렉토리 생성
                dirMake(attachRoot + attachDir);

                //파일저장유효성 검사
                if(fileSize > (attachSize * 1000000)){
                    //logger.debug("파일 용량 초과되어 안됨!!!");
                    box.put(key + "_isupload", "N");
                    continue;
                }

                //파일확장자 검사
                if(attachExt.indexOf(fileExt) < 0){
                    //logger.debug("파일 확장자 불량으로 안됨!!!");
                    box.put(key + "_isupload", "N");
                    continue;
                }


                try {
                    //파일저장
                    //Path target; //Paths.get(attachDir).resolve(FilenameUtils.getName(saveFile));

//					if("PRD".equals(profile)){
                    Path realPath = Paths.get(attachRoot + attachDir + saveFile);//.resolve(FilenameUtils.getName(saveFile));
                    //if(realPath.toFile().exists()) throw new RuntimeException();
                    Path target = realPath.toAbsolutePath();
                    //logger.debug("PRD path-readFile :::::::::"+target);
//					}

//					File newFile = new File(filePathBlackList(filePath));
                    Files.copy(file.getInputStream(), target);


                    //이미지파일 DRM해지
                    if(
                            fileExt.equals("png") ||
                                    fileExt.equals("jpg") ||
                                    fileExt.equals("gif") ||
                                    fileExt.equals("jpeg")
                    ) {

                        saveFile = saveFile;
                        ////logger.debug("DRM 해지파일 : " + saveFile);

                        filePath = attachRoot + attachDir + saveFile;
                        ////logger.debug("DRM 해지파일 PATH: " + filePath);
                    }



                    //첨부파일 정보 저장
//					box.put(key + "_filePath", filePath);


                    box.put(key + "_real", replaceFileName(realFile));
                    box.put(key + "_new", replaceFileName(saveFile));
                    box.put(key + "_size", fileSize);
                    box.put(key + "_isupload", "Y");
                }catch (Exception e) {
                    box.put(key + "_isupload", "N");
                    ErrorManager.getErrorStackTrace(e);
                }

            }else{
                box.put(key + "_isupload", "N");
            }

        }
        box.remove("urlUploadRoot");
        box.remove("urlUpload");

        return box;
    }


    /**
     * 파일명에 / \\ . & 등 특수문자 제거
     * @param fileName
     * @return
     */
    private static String replaceFileName(String fileName) {
        boolean flag = false;
        int idx = -1;
        String fileExt = "";

        if(StringUtils.isNotEmpty(fileName)) {
            idx = fileName.lastIndexOf('.');
            if(idx > -1){
                flag = true;
                fileExt = fileName.substring(idx + 1);	// 확장자
                fileName = fileName.substring(0, idx);	// 확장명

                fileName = fileName.replaceAll("/","");
                fileName = fileName.replaceAll("\\\\","");
                fileName = fileName.replaceAll("\\.","");
                fileName = fileName.replaceAll("&","");
            }
        }
        if(flag) {
            return fileName+"."+fileExt;
        }else {
            return fileName;
        }
    }

    /**
     * 디렉토리 생성
     * @param targetDir
     */
    public static void dirMake(String targetDir) throws Exception{
        //ConfigSet conf = new ConfigSet();
        File d = new File(targetDir);
        if(!d.isDirectory()){
            if(!d.mkdirs()){
                //logger.error("디렉토리 생성 실패 [" + targetDir + "]" );
            }
        }
    }

    /**
     * 파일구분자 XSS 제거
     * @param value
     * @return
     */
    private static String filePathBlackList(String value) {
        String returnValue = value;
        if (returnValue == null || returnValue.trim().equals("")) {
            return "";
        }
        returnValue = returnValue.replaceAll("\\.\\./", ""); // ../
        returnValue = returnValue.replaceAll("\\.\\.\\\\", ""); // ..\
        return returnValue;
    }


    /**
     * @return Timestamp 값
     * @see
     */
    private static String getTimeStamp() {

        String rtnStr = null;

        // 문자열로 변환하기 위한 패턴 설정(년도-월-일 시:분:초:초(자정이후 초))
        String pattern = "yyyyMMddhhmmssSSS";

        SimpleDateFormat sdfCurrent = new SimpleDateFormat(pattern, Locale.KOREA);
        Timestamp ts = new Timestamp(System.currentTimeMillis());

        rtnStr = sdfCurrent.format(ts.getTime());
        return rtnStr;
    }
}
