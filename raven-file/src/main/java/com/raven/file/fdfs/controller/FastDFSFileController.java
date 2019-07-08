package com.raven.file.fdfs.controller;

import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.file.fdfs.service.FastDFSFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping(path = "/fdfs")
public class FastDFSFileController {

    @Autowired
    private FastDFSFileService fastDFSFileService;

    @PostMapping
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.failure(ResultCode.UPLOAD_FILE_EMPTY);
        }
        return fastDFSFileService.uploadFile(file);
    }

    @GetMapping(path = "/meta")
    public Result getFileMeta(@RequestParam("group") String group,
        @RequestParam("path") String path) {
        return fastDFSFileService.getFileMetaData(group, path);
    }
}
