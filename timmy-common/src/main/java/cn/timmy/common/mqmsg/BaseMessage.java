package cn.timmy.common.mqmsg;

import cn.timmy.common.utils.GsonHelper;

/**
 * Author zxx
 * Description 消息基础类
 * Date Created on 2018/6/30
 */
public class BaseMessage {

    private String from_uid;

    private String to_uid;

    private String content;

    private String type;

    private Long send_time;

    public BaseMessage(String from_uid, String to_uid) {
        this.from_uid = from_uid;
        this.to_uid = to_uid;
    }

    public String toString() {
        return GsonHelper.getGson().toJson(this, getClass());
    }

    public String getFrom_uid() {
        return from_uid;
    }

    public void setFrom_uid(String from_uid) {
        this.from_uid = from_uid;
    }

    public String getTo_uid() {
        return to_uid;
    }

    public void setTo_uid(String to_uid) {
        this.to_uid = to_uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSend_time() {
        return send_time;
    }

    public void setSend_time(Long send_time) {
        this.send_time = send_time;
    }
}
