package com.anushka.serviceengagementplatform.model;

public class EnterNewPwd {
	String newPwd;
	String userId;

	public EnterNewPwd(String userId, String newPwd) {
		super();
		this.newPwd = newPwd;
		this.userId = userId;
	}

	public EnterNewPwd() {
		super();
		this.userId = null;
		this.newPwd = null;
	}

	public String getNewPwd() {
		return newPwd;
	}

	public void setNewPwd(String newPwd) {
		this.newPwd = newPwd;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	
}
