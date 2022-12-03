package com.etester.dataloader.util;

//import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
//import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;
//
//@SoapFault(faultCode = FaultCode.CLIENT)
public class RfDataloadException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RfDataloadException(String message) {
        super(message);
    }

	public RfDataloadException(String message, Throwable cause) {
        super(message, cause);
    }

}

