package cn.timmy.logic.friend.bean.param;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/28
 */
public class RequestInfoOutParam {

    private List<RequestInfo> requestInfos = new ArrayList<>();

    public static class RequestInfo {

        public int id;

        public String from_uid;

        public String name;

        public String portrait_url;

        public Date create_dt;

        public List<RequestMsgInfo> msgInfos = new ArrayList<>();

    }

    public static class RequestMsgInfo {

        public String from_uid;

        public String to_uid;

        public String message;

        public Date create_dt;

    }

    public List<RequestInfo> getRequestInfos() {
        return requestInfos;
    }

    public void setRequestInfos(
        List<RequestInfo> requestInfos) {
        this.requestInfos = requestInfos;
    }
}
