package com.victor.friendchat.domain;

import java.io.Serializable;

public class NewsFound implements Serializable {
    public int lid;
    public User user;
    public String content;
    public String image;
    public String time;
    public String pinglun;
    public String location;
    
    public NewsFound() {
        super();

    }

}
