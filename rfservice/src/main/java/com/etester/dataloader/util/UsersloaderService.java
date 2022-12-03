package com.etester.dataloader.util;

import com.rulefree.rfloadusersschema.GenerateRedumptionCodesRequest;
import com.rulefree.rfloadusersschema.GenerateRedumptionCodesResponse;
import com.rulefree.rfloadusersschema.GetUsersRequest;
import com.rulefree.rfloadusersschema.GetUsersResponse;
import com.rulefree.rfloadusersschema.LoadUsersRequest;
import com.rulefree.rfloadusersschema.LoadUsersResponse;

import jakarta.xml.bind.JAXBException;

public interface UsersloaderService {

	public static final int UPLOAD_SUCCESS_STATUS_CODE = 0;
	public static final String UPLOAD_SUCCESS_MESSAGE = "Success!";
	public static final int UPLOAD_UNKNOWN_FAILURE_STATUS_CODE = -1;
	public static final String UPLOAD_UNKNOWN_FAILURE_MESSAGE = "Unknown Failure.";
	
	/**
	 * This call is typically invoked by the UserloaderEndpoints class on receiving a 
	 * message that has a payload with LoadUsersRequest as a root node.  Will throw 
	 * a RfDataloadParseException if any errors occur loading users.
	 *     
	 * @param usersPayload - A LoadUsersRequest object containing users and channel subscriptions
	 * @return LoadUsersResponse - object containing a status code.  0 = success.   
	 */
	public LoadUsersResponse saveUsers(LoadUsersRequest usersPayload); 

	/**
	 * Similar to the 'saveUsers' call, except the payload in this case is a string version 
	 * of a LoadUsersRequest. 
	 *     
	 * @param usersPayloadString - A String version of LoadUsersRequest object containing users
	 * @return String 			  - Serialized string version of LoadUsersResponse object 
	 * 								containing a status.  0 = success.    
	 */
	public String saveUsers(String usersPayloadString) throws JAXBException; 

	/**
	 * This call is typically invoked by the UserloaderEndpoints class on receiving a 
	 * message that has a payload with GetUsersRequest as a root node.  Will throw 
	 * a RfDataloadParseException if any errors occur loading users.
	 *     
	 * @param getUsersPayload - A LoadUsersRequest object containing users and channel subscriptions
	 * @return LoadUsersResponse - object containing a status code.  0 = success.   
	 */
	public GetUsersResponse getUsers(GetUsersRequest getUsersPayload); 

	/**
	 * Similar to the 'getUsers' call, except the payload in this case is a string version 
	 * of a GetUsersRequest. 
	 *     
	 * @param getUsersPayloadString - A String version of GetUsersRequest object containing users
	 * @return String 			  - Serialized string version of GetUsersResponse object 
	 * 								containing a status.  0 = success.    
	 */
	public String getUsers(String getUsersPayloadString) throws JAXBException;

	/**
	 * This call is typically invoked by the UserloaderEndpoints class on receiving a 
	 * message that has a payload with GenerateRedumptionCodeRequest as a root node.  Will throw 
	 * a RfDataloadParseException if any errors occur generating redumption codes.
	 *     
	 * @param generateRedumptionCodesRequest - A GenerateRedumptionCodesRequest object containing request to create RedumptionCodes
	 * @return GenerateRedumptionCodesResponse - object containing a status code.  0 = success.   
	 */
	public GenerateRedumptionCodesResponse generateRedumptionCodes(GenerateRedumptionCodesRequest generateRedumptionCodesRequest); 

}
