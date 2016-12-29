package com.victor.friendchat.domain;

import java.io.Serializable;

/** 用户信息javabean
 * Created by Victor on 2016/12/26.
 */
public class User implements Serializable {
    public int id;
    public String user;// 用户名
    public String password;// 密码
    public String qq;// qq号码
    public String icon;// 头像地址
    public String nickname;// 昵称
    public String city;// 城市
    public String sex;// 性别
    public String years;// 年龄
    public String location;// 注册地址
    public String qianming;// 个性签名

    public User() {

    }

    public User(String user, String password, String qq, String icon,
                String nickname, String city, String sex, String years,
                String location, String qianming) {
        super();
        this.user = user;
        this.password = password;
        this.qq = qq;
        this.icon = icon;
        this.nickname = nickname;
        this.city = city;
        this.sex = sex;
        this.years = years;
        this.location = location;
        this.qianming = qianming;
    }

    public User(int id, String user, String password, String qq, String icon,
                String nickname, String city, String sex, String years,
                String location, String qianming) {
        super();
        this.id = id;
        this.user = user;
        this.password = password;
        this.qq = qq;
        this.icon = icon;
        this.nickname = nickname;
        this.city = city;
        this.sex = sex;
        this.years = years;
        this.location = location;
        this.qianming = qianming;
    }
}
