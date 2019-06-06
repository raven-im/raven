package com.raven.route.upload.wrapper;

import com.github.tobato.fastdfs.domain.MetaData;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.exception.FdfsUnsupportStorePathException;
import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.route.upload.FastDFSClientApi;
import com.raven.route.upload.bean.OutputFileInfo;
import com.raven.route.upload.bean.OutputFileMetaInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: bbpatience
 * @date: 2019/6/4
 * @description: FastDFSClientWrapper
 **/
@Slf4j
@Component
public class FastDFSClientWrapper implements FastDFSClientApi {

    private static final String META_FILE_NAME = "name";
    private static final String META_FILE_SIZE = "size";
    private static final String META_FILE_EXT = "ext";

    @Autowired
    private FastFileStorageClient storageClient;

    @Value("${fdfs.storage-port}")
    private String storagePort;

    @Value("${fdfs.res-host}")
    private String serverUrl;

    @Override
    public Result uploadFile(MultipartFile file) {
        Set<MetaData> metaSet = new HashSet<>();
        metaSet.add(new MetaData(META_FILE_NAME, file.getOriginalFilename()));
        metaSet.add(new MetaData(META_FILE_SIZE, String.valueOf(file.getSize())));
        metaSet.add(new MetaData(META_FILE_EXT, FilenameUtils.getExtension(file.getOriginalFilename())));

        try {
            StorePath storePath = storageClient
                .uploadFile(file.getInputStream(), file.getSize(), FilenameUtils
                    .getExtension(file.getOriginalFilename()), metaSet);
            log.info("storePath:" + storePath);
            OutputFileInfo fileInfo = new OutputFileInfo(file.getOriginalFilename(), file.getSize(),
                getResAccessUrl(storePath));
            return Result.success(fileInfo);
        } catch (IOException e) {
            log.error(" upload file io error: {}", e.getMessage());
        }
        return Result.failure(ResultCode.UPLOAD_FILE_UPLOAD_ERROR);
    }

    private String getResAccessUrl(StorePath storePath) {
        String fileUrl = "http://" + serverUrl + ":" + storagePort + "/" + storePath.getFullPath();
        log.info("fileUrl:" + fileUrl);
        return fileUrl;
    }

    @Override
    public Result getFileMetaData(String group, String path) {
        Set<MetaData> metaSet = storageClient.getMetadata(group, path);
        String name = null, ext = null;
        long size = 0;
        for (MetaData data : metaSet) {
            switch (data.getName()) {
                case META_FILE_NAME:
                    name = data.getValue();
                    break;
                case META_FILE_EXT:
                    ext = data.getValue();
                    break;
                case META_FILE_SIZE:
                    size = Long.parseLong(data.getValue());
                    break;
            }
        };

        OutputFileMetaInfo fileInfo = new OutputFileMetaInfo(name, size, ext);
        return Result.success(fileInfo);
    }

    @Override
    public Result deleteFile(String fileUrl) {
        try {
            StorePath storePath = StorePath.praseFromUrl(fileUrl);
            log.info("groupName:" + storePath.getGroup() + "------" + " path："+storePath.getPath());
            storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
        } catch (FdfsUnsupportStorePathException e) {
            log.warn(e.getMessage());
        }
        return Result.success();
    }

    @Override
    public InputStream downloadFile(String fileUrl) {
        try {
            StorePath storePath = StorePath.praseFromUrl(fileUrl);
            byte[] fileByte = storageClient.downloadFile(storePath.getGroup(), storePath.getPath(), new DownloadByteArray());
            return new ByteArrayInputStream(fileByte);
        } catch (Exception e) {
            log.error("Non IO Exception: Get File from Fast DFS failed", e);
        }
        return null;
    }

}
