package com.victor.friendchat.uitl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

    /**给指定字符串按照md5算法加密
     * @param pwd 需要加密的字符串
     * @return 返回MD5加密之后的字符串，加密失败返回空字符串
     */
    public static String md5Encode(String pwd) {
        
        try {
            // 1. 指定算法类型
            MessageDigest digest = MessageDigest.getInstance("MD5");
            // 2. 将需要的加密的字符串转化为byte类型的数组,然后进行随机哈希
            byte[] bs = digest.digest(pwd.getBytes());
            StringBuffer sb = new StringBuffer();
            for (byte b : bs) {
                int i = b & 0xff;
                String hexString = Integer.toHexString(i);
                if (hexString.length() < 2) {
                    hexString = "0" + hexString;
                }
                sb.append(hexString);
            }
            String md5Pwd = sb.toString();
            return md5Pwd;
            //System.out.println(md5Pwd);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
