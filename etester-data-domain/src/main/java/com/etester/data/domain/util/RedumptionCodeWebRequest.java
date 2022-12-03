package com.etester.data.domain.util;

import lombok.Data;

/**
 * Bean class used to communicate RedumptionCode Web Request or RedumptionCode Soap Request to the server 
 * @author sesi
 *
 */
@Data
public class RedumptionCodeWebRequest {

	private Integer idCrcTypeCode;

	private String emailAddress;

	private String firstName;

	private String lastName;

	private String middleName;

	private String purchaserMessage;

	private String redumptionCode;

}
