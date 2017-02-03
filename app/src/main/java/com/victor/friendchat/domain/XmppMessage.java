package com.victor.friendchat.domain;

import java.io.Serializable;

/**
 * Created by Victor on 2016/1/10.
 */
public class XmppMessage implements Serializable {
    public int id;
    public String to;
    public String type;
    public XmppUser user;
    public String time;
    public String content;
    public int result;//1.未处理 0.已处理
    public String main;

    public XmppMessage(int id, String to, String type, XmppUser user, String time, String content, int result, String main) {
        this.id = id;
        this.to = to;
        this.type = type;
        this.user = user;
        this.time = time;
        this.content = content;
        this.result = result;
        this.main = main;
    }

    public XmppMessage(String to, String type, XmppUser user, String time, String content, int result, String main) {

        this.to = to;
        this.type = type;
        this.user = user;
        this.time = time;
        this.content = content;
        this.result = result;
        this.main = main;
    }

    @Override
    public String toString() {
        return "XmppMessage{" +
                "id=" + id +
                ", to='" + to + '\'' +
                ", type='" + type + '\'' +
                ", mUser=" + user +
                ", time='" + time + '\'' +
                ", content='" + content + '\'' +
                ", result='" + result + '\'' +
                ", main='" + main + '\'' +
                '}';
    }


}
