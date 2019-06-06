package com.raven.route.upload.controller;

import static com.raven.common.utils.Constants.AUTH_TOKEN;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.route.upload.wrapper.FastDFSClientWrapper;
import com.raven.route.validator.TokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping(path="/upload")
public class UploadController {

    private static final String REQ_FILE = "file";
    private static final String REQ_GROUP = "group";
    private static final String REQ_PATH = "path";

    @Autowired
    private FastDFSClientWrapper client;

    @Autowired
    private TokenValidator tokenValidator;

    @PostMapping
    public @ResponseBody
    Result uploadFile(@RequestHeader(AUTH_TOKEN) String token, @RequestParam(REQ_FILE) MultipartFile file) {
        log.info("user upload");

        if (file.isEmpty()) {
            return Result.failure(ResultCode.UPLOAD_FILE_EMPTY);
        }
        if (!tokenValidator.validate(token)) {
            return Result.failure(tokenValidator.errorCode());
        }

        return client.uploadFile(file);
    }

    @GetMapping(path="/meta")
    public @ResponseBody
    Result getFileMeta(@RequestHeader(AUTH_TOKEN) String token,
        @RequestParam(REQ_GROUP) String group,
        @RequestParam(REQ_PATH) String path) {
        log.info("get file meta data.");

        if (!tokenValidator.validate(token)) {
            return Result.failure(tokenValidator.errorCode());
        }
        return client.getFileMetaData(group, path);
    }
}
