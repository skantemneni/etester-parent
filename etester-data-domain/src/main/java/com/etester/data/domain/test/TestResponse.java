package com.etester.data.domain.test;

import java.util.Date;

import lombok.Data;

@Data
public class TestResponse {

    private Long idTest;
	
    private Long idUsertest;

    private Long idUsertestresponse;
	
	// Variable used when this bean is used in the context of administering a test. This represents 
	// the status of the user instance of the test    
	private String testStatus;
	// Variable also used when this bean is used in the context of administering a test. This represents 
	// the current value of any saved response for the test instance    
	private String testResponse;

	private Date dateSaved;

}
