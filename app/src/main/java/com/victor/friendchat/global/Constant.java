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

        public static final String ICON = BASE_URL + "/DoGetIcon";
        public static final String DO_GET_LUNTAN = BASE_URL + "/DoGetLunTan";
    }

    public class RequestParamNames {
        // "user", mPhoneNum);
        // "nickname", mNickName)
        //         "password", mPwd);
        // "sex", mGender);
        // "icon", "");
        // "city", city);
        // "location", position);
        // "years", "");
        // "qq", "");
        // "action", "save");

        public static final String user = "user";
        public static final String nickname = "nickname";
        public static final String password = "password";
        public static final String sex = "sex";
        public static final String icon = "icon";
        public static final String city = "city";
        public static final String location = "location";
        public static final String years = "years";
        public static final String qq = "qq";
        public static final String action = "action";
    }

    /**
     * SP的一些配置文件的name
     */
    public class SP {
        public static final String SYSTEM_SETTING = "SYSTEM_SETTING";
    }
}
