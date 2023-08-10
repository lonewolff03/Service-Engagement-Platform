package com.anushka.serviceengagementplatform.model;

public class ForgotPwd {
	String userId;
	
	public ForgotPwd() {
	}

	public ForgotPwd(String userId) {
		super();
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
