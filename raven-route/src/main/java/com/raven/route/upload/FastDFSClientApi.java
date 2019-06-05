package com.raven.route.upload;

import com.raven.common.result.Result;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

public interface FastDFSClientApi {
    Result uploadFile(MultipartFile file);
    Result getFileMetaData(String group, String path);
    Result deleteFile(String fileUrl);
    InputStream downloadFile(String fileUrl);
}
