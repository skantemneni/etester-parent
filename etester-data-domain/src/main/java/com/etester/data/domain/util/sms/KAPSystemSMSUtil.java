package com.etester.data.domain.util.sms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class KAPSystemSMSUtil implements SMSService {

//	<bean id="smsService"
//				class="com.rf.web.xqee.server.sms.KAPSystemSMSUtil">
//		<property name="username" value="kapbulk"/>
//		<property name="password" value="kapbulk@user!123"/>
//		<property name="senderID" value="KAPMSG"/>
//		<property name="serverURL" value="http://sms2.kapsystem.com"/>
//	</bean>

		
	private String username = "kapbulk";
	private String password = "kapbulk@user!123";
	private String senderID = "KAPMSG";
//	private String serverURL = "http://sms2.kapsystem.com"; 
	private String serverURL = "http://123.63.33.43/blank/sms/user/urlsmstemp.php"; 
	

	public KAPSystemSMSUtil() {};
	
	@Override
	public void sendSMS(String mobileNo, String message) throws Exception {
		// give all Parameters In String
		if (mobileNo == null) {
			// siva's phone
			mobileNo = "9666464158";
		}
		if (message == null) {
			message = "Test message from java code";
		}
		String postData = "";
		postData += "username=" + this.username + "&pass=" + this.password + "&dest_mobileno="
				+ mobileNo + "&senderid=" + this.senderID + "&message=" + message;

		String retval = "";
		URL url = new URL(serverURL);
		HttpURLConnection urlconnection = (HttpURLConnection) url
				.openConnection();
		urlconnection.setRequestMethod("POST");
		urlconnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		urlconnection.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(
				urlconnection.getOutputStream());
		out.write(postData);
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				urlconnection.getInputStream()));
		String decodedString;
		while ((decodedString = in.readLine()) != null) {
			retval += decodedString;
		}
		in.close();
		System.out.println(retval);
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the senderID
	 */
	public String getSenderID() {
		return senderID;
	}

	/**
	 * @param senderID the senderID to set
	 */
	public void setSenderID(String senderID) {
		this.senderID = senderID;
	}

	/**
	 * @return the serverURL
	 */
	public String getServerURL() {
		return serverURL;
	}

	/**
	 * @param serverURL the serverURL to set
	 */
	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}
}
// - See more at:
// http://bulksms-services.kapsystem.com/tag/bulk-sms-api/#sthash.plY1uHGY.dpuf