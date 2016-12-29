package com.victor.friendchat.domain;

import java.io.Serializable;

public class XmppUser implements Serializable {
	
	public String userName;
	public String name;


	@Override
	public String toString() {
		return "User [userName=" + userName + ", name=" + name + "]";
	}

	public XmppUser(String userName, String name) {
		this.userName = userName;
		this.name = name;
	}
}
