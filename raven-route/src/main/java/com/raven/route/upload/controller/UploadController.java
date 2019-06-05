package com.raven.route.upload.controller;

import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.route.upload.wrapper.FastDFSClientWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping(path="/upload")
public class UploadController {

    private FastDFSClientWrapper client;

    @Autowired
    public UploadController(FastDFSClientWrapper clientWrapper) {
        this.client = clientWrapper;
    }

    @PostMapping
    public @ResponseBody
    Result uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("user upload");

        if (file.isEmpty()) {
            return Result.failure(ResultCode.UPLOAD_FILE_EMPTY);
        }
        return client.uploadFile(file);
    }

    @GetMapping(path="/meta")
    public @ResponseBody
    Result getFileMeta(@RequestParam("group") String group,
        @RequestParam("path") String path) {
        log.info("get file meta data.");

        if (StringUtils.isEmpty(group) || StringUtils.isEmpty(path)) {
            return Result.failure(ResultCode.UPLOAD_FILE_UPLOAD_PARAMETER_ERROR);
        }
        return client.getFileMetaData(group, path);
    }
}
