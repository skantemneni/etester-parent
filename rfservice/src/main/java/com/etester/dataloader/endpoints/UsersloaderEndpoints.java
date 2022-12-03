package com.etester.dataloader.endpoints;

import javax.xml.namespace.QName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.etester.dataloader.util.UsersloaderService;
import com.rulefree.rfloadusersschema.GenerateRedumptionCodesRequest;
import com.rulefree.rfloadusersschema.GenerateRedumptionCodesResponse;
import com.rulefree.rfloadusersschema.GetUsersRequest;
import com.rulefree.rfloadusersschema.GetUsersResponse;
import com.rulefree.rfloadusersschema.LoadUsersRequest;
import com.rulefree.rfloadusersschema.LoadUsersResponse;

import jakarta.xml.bind.JAXBElement;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Endpoint
public class UsersloaderEndpoints {

	private static final String SAVE_TARGET_NAMESPACE = "http://www.rulefree.com/RfLoadUsersSchema";

	@Autowired
	private UsersloaderService usersloaderService;

	@PayloadRoot(localPart = "LoadUsersRequest", namespace = SAVE_TARGET_NAMESPACE)
	public @ResponsePayload
	LoadUsersResponse loadUsers(@RequestPayload LoadUsersRequest loadUsersRequest) {
		System.out.println("Load Users...!");
		LoadUsersResponse response = usersloaderService.saveUsers(loadUsersRequest);
//		LoadUsersResponse response = new LoadUsersResponse();
//		response.setStatus("0");
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "LoadUsersResponse");
//		LoadUsersResponse response = new LoadUsersResponse();
		return response;
	}

	@PayloadRoot(localPart = "GetUsersRequest", namespace = SAVE_TARGET_NAMESPACE)
	public @ResponsePayload
	JAXBElement<GetUsersResponse> getUsers(@RequestPayload JAXBElement<GetUsersRequest> getUsersRequest) {
//		System.out.println("Get Users !");
		log.info("Get Users !");
		GetUsersResponse response = usersloaderService.getUsers(getUsersRequest.getValue());
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "GetUsersResponse");
		return new JAXBElement<GetUsersResponse> (qname, GetUsersResponse.class, response);
//		return response;
	}

	@PayloadRoot(localPart = "GenerateRedumptionCodesRequest", namespace = SAVE_TARGET_NAMESPACE)
	public @ResponsePayload
	GenerateRedumptionCodesResponse generateRedumptionCodes(@RequestPayload GenerateRedumptionCodesRequest generateRedumptionCodesRequest) {
		System.out.println("Generating Redumption Codes...!");
		GenerateRedumptionCodesResponse response = usersloaderService.generateRedumptionCodes(generateRedumptionCodesRequest);
//		LoadUsersResponse response = new LoadUsersResponse();
//		response.setStatus("0");
		QName qname = new QName(SAVE_TARGET_NAMESPACE, "GenerateRedumptionCodesResponse");
//		LoadUsersResponse response = new LoadUsersResponse();
		return response;
	}


	
	/**
	 * @param userLoaderService the userLoaderService to set
	 */
	public void setUsersloaderService(UsersloaderService usersloaderService) {
		this.usersloaderService = usersloaderService;
	}

}
