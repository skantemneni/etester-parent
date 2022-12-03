package com.etester.data.domain.util.sms;

public interface SMSService {
	public void sendSMS(String mobileNo, String message) throws Exception;
}
