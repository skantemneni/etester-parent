package com.etester.data.domain.user;

public class UploadUserResponse {
	
	public static final int UPLOAD_USER_SUCCESS_STATUS = 0;
	public static final int UPLOAD_USER_FAIL_STATUS = -1;
	
	int userUploadStatus = UPLOAD_USER_SUCCESS_STATUS;
	String userUploadStatusMessage = null;
	boolean newUser = false;
	User user;
	
	public UploadUserResponse(String failMessage) { 
		this.userUploadStatus = UPLOAD_USER_FAIL_STATUS;
		this.userUploadStatusMessage = failMessage;
	}
	
	public UploadUserResponse(User user, boolean newUser) {
		this.userUploadStatus = UPLOAD_USER_SUCCESS_STATUS;
		this.user = user;
		this.newUser = newUser;
	}
	
	/**
	 * @return the userUploadStatus
	 */
	public int getUserUploadStatus() {
		return userUploadStatus;
	}
	/**
	 * @param userUploadStatus the userUploadStatus to set
	 */
	public void setUserUploadStatus(int userUploadStatus) {
		this.userUploadStatus = userUploadStatus;
	}
	/**
	 * @return the userUploadStatusMessage
	 */
	public String getUserUploadStatusMessage() {
		return userUploadStatusMessage;
	}
	/**
	 * @param userUploadStatusMessage the userUploadStatusMessage to set
	 */
	public void setUserUploadStatusMessage(String userUploadStatusMessage) {
		this.userUploadStatusMessage = userUploadStatusMessage;
	}
	/**
	 * @return the newUser
	 */
	public boolean isNewUser() {
		return newUser;
	}
	/**
	 * @param newUser the newUser to set
	 */
	public void setNewUser(boolean newUser) {
		this.newUser = newUser;
	}
	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	

}
