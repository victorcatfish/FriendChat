package com.victor.friendchat.domain;

import java.io.Serializable;

/**
 * Created by Victor on 2016/12/30.
 */
public class XmppFriend implements Serializable, Comparable<Object> {

    public User user;
    public int status;//0.在线 1.Q我吧 2.忙碌 3.勿扰 4.离开 5.隐身 6.离线

    public XmppFriend(User user, int status) {
        this.user = user;
        this.status = status;
    }

    public XmppFriend(User user) {
        this.user = user;
    }

    @Override
    public int compareTo(Object o) {

        if (this == o) {
            return 0;
        } else if (o != null && o instanceof XmppFriend) {
            XmppFriend xf = (XmppFriend) o;
            if (status <= xf.status) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return -1;
        }

    }
}
