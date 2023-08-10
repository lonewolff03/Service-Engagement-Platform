package com.anushka.serviceengagementplatform.model;

public class UserLogin {
	private String userId;
	private String password;
	
	public UserLogin() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserLogin(String userId, String password) {
		super();
		this.userId = userId;
		this.password = password;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
