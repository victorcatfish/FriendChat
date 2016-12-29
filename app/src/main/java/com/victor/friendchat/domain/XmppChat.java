package com.victor.friendchat.domain;

import java.io.Serializable;

/**
 * Created by Victor on 2016/12/26.
 */
public class XmppChat implements Serializable {

    public String main;//当前登录的用户名
    public String user;//发送人-用户名
    public String nickname;//发送人-昵称
    public String icon;//发送人-头像
    public int type;//1.发送 2.接收
    public String content;//发送人-消息内容
    public String sex;//发送人-性别
    public String too;//接收人
    public int viewType;//消息类型 1.普通带表情消息 2.图片 3.语音 4.视频
    public long time;//消息时间
    
    public XmppChat(String mian, String user, String nickname, String icon, int type, String content, String sex, String too, int viewType, long time) {
        this.main = mian.toUpperCase();
        this.user = user.toUpperCase();
        this.nickname = nickname;
        this.icon = icon;
        this.type = type;
        this.content = content;
        this.sex = sex;
        this.too = too.toUpperCase();
        this.viewType = viewType;
        this.time = time;
    }

    public XmppChat() {
    }
}
