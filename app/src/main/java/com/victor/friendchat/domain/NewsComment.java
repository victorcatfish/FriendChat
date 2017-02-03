package com.victor.friendchat.domain;

import java.io.Serializable;

/**
 * Created by jzh on 2015/9/28.
 */
public class NewsComment implements Serializable {
    private int pid;
    private int pcid;
    private User user;
    private String plocation;
    private String ptime;
    private String pcontent;
    private String pzan;
    private String ispzan;
    NewsFound mNewsFound;


    public NewsComment(int pcid, User user, String plocation, String ptime, String pcontent, String pzan, String ispzan) {
        this.pcid = pcid;
        this.user = user;
        this.plocation = plocation;
        this.ptime = ptime;
        this.pcontent = pcontent;
        this.pzan = pzan;
        this.ispzan = ispzan;
    }

    public NewsComment(int pid, int pcid, User user, String plocation, String ptime, String pcontent, String pzan, String ispzan) {
        this.pid = pid;
        this.pcid = pcid;
        this.user = user;
        this.plocation = plocation;
        this.ptime = ptime;
        this.pcontent = pcontent;
        this.pzan = pzan;
        this.ispzan = ispzan;
    }

    public NewsFound getNewsFound() {
        return mNewsFound;
    }

    public void setNewsFound(NewsFound newsFound) {
        this.mNewsFound = newsFound;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPcid() {
        return pcid;
    }

    public void setPcid(int pcid) {
        this.pcid = pcid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPlocation() {
        return plocation;
    }

    public void setPlocation(String plocation) {
        this.plocation = plocation;
    }

    public String getPtime() {
        return ptime;
    }

    public void setPtime(String ptime) {
        this.ptime = ptime;
    }

    public String getPcontent() {
        return pcontent;
    }

    public void setPcontent(String pcontent) {
        this.pcontent = pcontent;
    }

    public String getPzan() {
        return pzan;
    }

    public void setPzan(String pzan) {
        this.pzan = pzan;
    }

    public String getIspzan() {
        return ispzan;
    }

    public void setIspzan(String ispzan) {
        this.ispzan = ispzan;
    }

    public NewsComment() {
    }
}
