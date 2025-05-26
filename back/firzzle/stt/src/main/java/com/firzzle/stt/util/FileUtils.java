package com.firzzle.stt.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

public class FileUtils {

    public static Path saveUploadedFile(MultipartFile file, String uuid, String dir) throws IOException {
        Path outputPath = Paths.get(dir, uuid + ".wav");
        file.transferTo(outputPath.toFile());
        return outputPath;
    }
}
