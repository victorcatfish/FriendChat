package com.victor.friendchat.global;


import com.victor.friendchat.uitl.LogUtils;

/** 全局的静态常量
 * Created by Victor on 2016/7/5.
 */
public class Constant {

    public static final int DEBUGLEVEL = LogUtils.LEVEL_ALL;


    public class URL {

        public static final String BASE_URL = "http://192.168.2.101:8080/FriendChatServer";

        public static final String DO_GET_URSER = BASE_URL + "/DoGetUser";

    }

}
