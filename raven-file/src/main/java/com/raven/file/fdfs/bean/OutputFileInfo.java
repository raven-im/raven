package com.raven.file.fdfs.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutputFileInfo {

    @JsonProperty("name")
    private String name;

    @JsonProperty("size")
    private Long size;

    @JsonProperty("url")
    private String url;

}
