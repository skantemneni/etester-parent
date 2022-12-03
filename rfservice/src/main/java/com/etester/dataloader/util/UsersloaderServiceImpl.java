package com.etester.dataloader.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etester.data.domain.admin.Authority;
import com.etester.data.domain.content.ChannelSubscription;
import com.etester.data.domain.content.core.ChannelDao;
import com.etester.data.domain.test.JdbcDaoStaticHelper;
import com.etester.data.domain.test.instance.Usertest;
import com.etester.data.domain.user.UploadUserResponse;
import com.etester.data.domain.user.User;
import com.etester.data.domain.user.UserDao;
import com.etester.data.domain.user.Webuser;
import com.etester.data.domain.util.RedumptionCodeWebRequest;
import com.rulefree.rfloadusersschema.ExamType;
import com.rulefree.rfloadusersschema.GenerateRedumptionCodesRequest;
import com.rulefree.rfloadusersschema.GenerateRedumptionCodesResponse;
import com.rulefree.rfloadusersschema.GetUsersRequest;
import com.rulefree.rfloadusersschema.GetUsersResponse;
import com.rulefree.rfloadusersschema.LoadUsersRequest;
import com.rulefree.rfloadusersschema.LoadUsersResponse;
import com.rulefree.rfloadusersschema.RedumptionCodeRequestType;
import com.rulefree.rfloadusersschema.RolesType;
import com.rulefree.rfloadusersschema.SubscriptionType;
import com.rulefree.rfloadusersschema.UserType;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UsersloaderServiceImpl implements UsersloaderService {

	private static final Long CHANNEL_ADMINISTRATOR = 100l;
	private static final Long SYSTEM_ADMINISTRATOR = 0l;
	private static final String DEFAULT_CONTENT_LOADER_PROVIDER = "channeladmin";
	
	// roles in eTester
	private static final String USER_ROLE_LOGIN = "ROLE_LOGIN";
	private static final String USER_ROLE_USER = "ROLE_USER";
	private static final String USER_ROLE_PROVIDER = "ROLE_PROVIDER";
	private static final String USER_ROLE_ADMIN = "ROLE_ADMIN";
	private static final String USER_ROLE_SYSTEM_ADMIN = "ROLE_SYSTEM";
	
	private static final String DEFAULT_NEW_USER_PASSWORD = "0fd205965ce169b5c023282bb5fa2e239b6716726db5defaa8ceff225be805dc";
	
//	static final Logger logger = Logger.getLogger(DataloaderServiceImpl.class);  
	private static final String UPLOAD_PATH = "C:\\rffiles";
	
	private static final String MULTI_LINE_INPUT_SEPERATOR = "$$$$";
	
	@Autowired
	private UserDao userDao;
	private ChannelDao channelDao;
	
	public UsersloaderServiceImpl (UserDao userDao, ChannelDao channelDao) {
		this.userDao = userDao;
		this.channelDao = channelDao;
		init();
	}

	private void init() {
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	private Unmarshaller getLoadUsersUnmarshaller() throws JAXBException {
		JAXBContext context = JAXBContext
				.newInstance(LoadUsersRequest.class);
		Unmarshaller um = context.createUnmarshaller();
		return um;
	}

	private Marshaller getLoadUsersMarshaller() throws JAXBException {
		JAXBContext context = JAXBContext
				.newInstance(LoadUsersResponse.class);
		Marshaller m = context.createMarshaller();
		return m;
	}

	/***********************************************************************************************************************************
	// Users Stuff.  Will need to be relocated to a different webservice SOON...............
	/***********************************************************************************************************************************/
	@Override
	public String saveUsers(String usersPayloadString) throws JAXBException {
		// unmarshall to get the payload request object -
		// LoadUsersRequest
		StringReader usersPayloadStringReader = new StringReader(usersPayloadString);
		LoadUsersRequest usersPayload = (LoadUsersRequest) getLoadUsersUnmarshaller()
				.unmarshal(usersPayloadStringReader);

		// call the method to process the payload
		LoadUsersResponse response = saveUsers(usersPayload);

		// marshall to create the payload response object -
		// LoadPracticesectionsResponse
		Writer writer = new StringWriter();
		getLoadUsersMarshaller().marshal(response, writer);

		return writer.toString();
	}

	
	@Override
	public LoadUsersResponse saveUsers(LoadUsersRequest usersPayload) {
		String statusString = null;
		// the following function performs the check to ensure a channel is capable of ingesting content.  For that, a channel should be marked "editable".
		if (!loggedInProviderIsAdmin()) {
			throw new RfDataloadException("The logged in user is NOT a Channel Administrator.  Only Channel Administrator's can Validate and Load user data.");
		}
		
		// dump the payload...
//		System.out.println ("LoadUsersRequest: " + usersPayload);

		int messageResponseSatatus = UPLOAD_SUCCESS_STATUS_CODE;
		// parse user data into users and webusers
		if (usersPayload.getUserArray() == null || usersPayload.getUserArray().getUser() == null || usersPayload.getUserArray().getUser().size() == 0) {
			statusString = "No users in the payload.  Nothing to load.";
		} else {
			List<User> uploadUserList = parseUserTypeListToUsers(usersPayload.getUserArray().getUser());
			// dump the uploadUserList...
//			System.out.println ("uploadUserList: " + uploadUserList);
			
			// now save them to a database
			// Even administrators need a special ROLE to upload users into the database programmatically - System Admin
			if (!loggedInProviderIsSystemAdmin()) {
				throw new RfDataloadException("The LoggedIn Channel Administrator cannot Load Users because he does not have the System Administrator Role.");
			}
			// finally, load the users
			if (!usersPayload.isValidateOnly()) {
				List<UploadUserResponse> uploadUserResponseList = userDao.uploadBatchUsers(uploadUserList);
				StringBuffer sbresponse = new StringBuffer();
				for (UploadUserResponse uploadUserResponse : uploadUserResponseList) {
					if (uploadUserResponse.getUserUploadStatus() == UploadUserResponse.UPLOAD_USER_SUCCESS_STATUS) {
						sbresponse.append ("Username: " + (uploadUserResponse.getUser() == null ? "" : uploadUserResponse.getUser().getUsername()) + ",  ");
						sbresponse.append ("User ID: " + (uploadUserResponse.getUser() == null ? "" : uploadUserResponse.getUser().getIdUser()) + ",  ");
						sbresponse.append ("New User: " + uploadUserResponse.isNewUser());
					} else {
						messageResponseSatatus = UPLOAD_UNKNOWN_FAILURE_STATUS_CODE;
						sbresponse.append ("Status: " + uploadUserResponse.getUserUploadStatus() + ",  ");
						sbresponse.append ("Error Message: " + uploadUserResponse.getUserUploadStatusMessage());
					}
					sbresponse.append ("\n");
				}
				statusString = sbresponse.toString();
			} else {
				statusString = "Users Payload Validation Successful";
			}
		}

		// create the response
		LoadUsersResponse response = new LoadUsersResponse();
		response.setStatus(messageResponseSatatus);
		if (statusString != null) {
			response.setMessage(statusString);
		} else {
			response.setMessage(UPLOAD_SUCCESS_MESSAGE);
		}

		return response;
	}
	
	private List<User> parseUserTypeListToUsers(List<UserType> userTypeList) {
		List<User> databaseUserList = null;
		if (userTypeList == null || userTypeList.size() == 0) {
			return null;
		} else {
			// create a response list
			databaseUserList = new ArrayList<User>();
			for (UserType userType : userTypeList) {
				User databaseUser = new User();
				
				// set the most important qualities
				String userName = userType.getUsername();
				databaseUser.setUsername(userName);
				
				databaseUser.setEmailAddress(userType.getEmailAddress());
				
				// if password = "0" the set it to DEFAULT_NEW_USER_PASSWORD (p@ssword)
				databaseUser.setPassword(userType.getPassword() == null || "0".equals(userType.getPassword().trim()) ? DEFAULT_NEW_USER_PASSWORD : userType.getPassword().trim());
				
				String firstName = userType.getFirstName();
				databaseUser.setFirstName(firstName);
				
				String lastName = userType.getLastName();
				databaseUser.setLastName(lastName);
				
				String middleName = userType.getMiddleName();
				databaseUser.setMiddleName(middleName);
				
				// set the new user as enabled
				databaseUser.setEnabled(userType.isEnabled() == null || userType.isEnabled() == true ? true : false);
				
				// now create the webuser fields
				Webuser webuser = new Webuser();
				webuser.setUsername(userName);
				webuser.setFirstName(firstName);
				webuser.setLastName(lastName);
				webuser.setMiddleName(middleName);
				webuser.setGender(userType.getGender() == null || userType.getGender().trim().length() == 0 ? "N" : userType.getGender().trim().substring(0, 1).toUpperCase());
				webuser.setProfession(userType.getProfession());
				webuser.setInstitution(userType.getInstitution());
				webuser.setBranchYear((userType.getBranch() == null ? "" : (userType.getBranch().trim() + " - ")) + (userType.getYear() == null ? "" : userType.getYear().toString()));
				webuser.setAddressLine1(userType.getAddressLine1());
				webuser.setAddressLine2(userType.getAddressLine2());
				webuser.setCountry(userType.getCountry());
				webuser.setPhoneNumber(userType.getPhoneNumber());
				webuser.setDob(userType.getDateOfBirth().toGregorianCalendar().getTime());
				webuser.setFromChannel(userType.getSourceChannel() == null ? "" : userType.getSourceChannel().toString());
				
				// now set the webuser on the user
				databaseUser.setWebuser(webuser);
				
				// set the user's authorities
				databaseUser.setAuthorities(mapRolesToAuthorities(userType.getRoles(), userName));

				// now see if the user has any subscriptions
				if (userType.getSubscriptions() != null && userType.getSubscriptions().getSubscription() != null && userType.getSubscriptions().getSubscription().size() > 0) {
					databaseUser.setChannelSubscriptions(mapChannalSubscriptions(userType.getSubscriptions().getSubscription()));
				}

				// now see if the user has any subscriptions
				if (userType.getExams() != null && userType.getExams().getExam() != null && userType.getExams().getExam().size() > 0) {
					databaseUser.setTests(mapExams(userType.getExams().getExam()));
				}

				// set the user's organizations to add (tried using xs:long in the schema.  some effed up for sone god**** reason)

				// if (userType.getOrganizations() == null || userType.getOrganizations().getIdOrganization() == null || userType.getOrganizations().getIdOrganization().size() > 0) {
				// SESI: 03/08/2018:  This line above is wrong.  Fixed it as below.  However, its not deployed ... as of today.  
				if (userType.getOrganizations() != null && userType.getOrganizations().getIdOrganization() != null && userType.getOrganizations().getIdOrganization().size() > 0) {
					List<Long> idOrganizaionList = new ArrayList<Long>();
					for (Integer orgId : userType.getOrganizations().getIdOrganization()) {
						idOrganizaionList.add(new Long(orgId));
					}
					databaseUser.setIdOrganizationsList(idOrganizaionList);
				}

				// now add to the list of database users being created
				databaseUserList.add(databaseUser);
			}
		}
		return databaseUserList;
	}
	
	
	private List<Authority> mapRolesToAuthorities(RolesType roles, String username) {
		List<Authority> authoritiesList = new ArrayList<Authority>();
		if (roles == null) {
			// default roles
			authoritiesList.add(new Authority(username, USER_ROLE_LOGIN));
			authoritiesList.add(new Authority(username, USER_ROLE_USER));
		} else {
			if (roles.isUser() == null || roles.isUser() == true) {
				authoritiesList.add(new Authority(username, USER_ROLE_LOGIN));
			}
			if (roles.isStudent() == null || roles.isStudent() == true) {
				authoritiesList.add(new Authority(username, USER_ROLE_USER));
			}
			if (roles.isTeacher() != null && roles.isTeacher() == true) {
				authoritiesList.add(new Authority(username, USER_ROLE_PROVIDER));
			}
		}
		return authoritiesList;
	}
	
//	private List<String> mapRolesToAuthorities(RolesType roles) {
//		List<String> authoritiesList = new ArrayList<String>();
//		if (roles == null) {
//			// default roles
//			authoritiesList.add(USER_ROLE_LOGIN);
//			authoritiesList.add(USER_ROLE_USER);
//		} else {
//			if (roles.isUser() == null || roles.isUser() == true) {
//				authoritiesList.add(USER_ROLE_LOGIN);
//			}
//			if (roles.isStudent() == null || roles.isStudent() == true) {
//				authoritiesList.add(USER_ROLE_USER);
//			}
//			if (roles.isTeacher() != null && roles.isTeacher() == true) {
//				authoritiesList.add(USER_ROLE_PROVIDER);
//			}
//		}
//		return authoritiesList;
//	}
	
	
	private List<Usertest> mapExams(List<ExamType> examTypeList) {
		if (examTypeList == null || examTypeList.size() == 0) {
			return null;
		} 
		List<Usertest> usertestList = new ArrayList<Usertest>();
		for (ExamType examType : examTypeList) {
			Usertest usertestDao = new Usertest();
			usertestDao.setIdTest(examType.getIdExam());
			usertestDao.setTestAssignmentDate(examType.getExamDate() == null ? null : examType.getExamDate().toGregorianCalendar().getTime());
			usertestList.add(usertestDao);
		}
		return usertestList;
	}
	
	private List<ChannelSubscription> mapChannalSubscriptions(List<SubscriptionType> subscriptionTypeList) {
		if (subscriptionTypeList == null || subscriptionTypeList.size() == 0) {
			return null;
		} 
		List<ChannelSubscription> channelSubscriptionList = new ArrayList<ChannelSubscription>();
		for (SubscriptionType subscriptionType : subscriptionTypeList) {
			ChannelSubscription channelSubscriptionDao = new ChannelSubscription();
			channelSubscriptionDao.setIdChannel(subscriptionType.getIdChannel() == null ? 0l : subscriptionType.getIdChannel().longValue());
			// set the student id to 0. since we do not have it yet.
			channelSubscriptionDao.setIdStudent(0l);
			if (subscriptionType.getDuration() != null) {
				if (subscriptionType.getDuration().getExactEnddate() != null) {
					channelSubscriptionDao.setStartDate(subscriptionType.getDuration().getExactEnddate().getStartDate() == null ? new Date() : subscriptionType.getDuration().getExactEnddate().getStartDate().toGregorianCalendar().getTime());
					channelSubscriptionDao.setEndDate(subscriptionType.getDuration().getExactEnddate().getEndDate() == null ? new Date() : subscriptionType.getDuration().getExactEnddate().getEndDate().toGregorianCalendar().getTime());
					// add it to the list
					channelSubscriptionList.add(channelSubscriptionDao);
				} else if (subscriptionType.getDuration().getExactDuration() != null) {
					Date startDate = subscriptionType.getDuration().getExactDuration().getStartDate() == null ? new Date() : subscriptionType.getDuration().getExactDuration().getStartDate().toGregorianCalendar().getTime();
					channelSubscriptionDao.setStartDate(startDate);
					// set the initial value of end date to start date....
					Date endDate = subscriptionType.getDuration().getExactDuration().getStartDate() == null ? new Date() : subscriptionType.getDuration().getExactDuration().getStartDate().toGregorianCalendar().getTime();
					// now add the duration
					Duration period = subscriptionType.getDuration().getExactDuration().getPeriod();
					period.addTo(endDate);
					channelSubscriptionDao.setEndDate(endDate);
					// add it to the list
					channelSubscriptionList.add(channelSubscriptionDao);
				} else {
					// no dates means, do not add the subscription to the subscription list
				}
			}
		}
		return channelSubscriptionList;
	}
	
	private boolean loggedInProviderIsAdmin() {
		
		String loggedInUserName = JdbcDaoStaticHelper.getCurrentUserName();
		User loggedInUser = userDao.findByUsername(loggedInUserName);
		// user has to be logged in
		if (loggedInUser == null) {
			throw new RfDataloadException("Invaid User ID.  Exiting immediately.");
		}
		// logged in user has to be an administrator or provider
		if (loggedInUser.getAuthorities().contains(USER_ROLE_ADMIN)) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean loggedInProviderIsSystemAdmin() {
		String loggedInUserName = JdbcDaoStaticHelper.getCurrentUserName();
		User loggedInUser = userDao.findByUsername(loggedInUserName);
		// user has to be logged in
		if (loggedInUser == null) {
			throw new RfDataloadException("Invaid User ID.  Exiting immediately.");
		}
		// logged in user has to be an administrator or provider
		if (loggedInUser.getAuthorities().contains(USER_ROLE_SYSTEM_ADMIN)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public GetUsersResponse getUsers(GetUsersRequest getUsersPayload) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getUsers(String getUsersPayloadString) throws JAXBException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenerateRedumptionCodesResponse generateRedumptionCodes(
			GenerateRedumptionCodesRequest generateRedumptionCodesRequest) {
		String statusString = null;
		// the following function performs the check to ensure a channel is capable of ingesting content.  For that, a channel should be marked "editable".
		if (!loggedInProviderIsAdmin()) {
			throw new RfDataloadException("The logged in user is NOT a Channel Administrator.  Only Channel Administrator's can generate Redumption Codes.");
		}
		
		// dump the payload...
		System.out.println ("generateRedumptionCodesRequest: " + generateRedumptionCodesRequest);

		// parse user data into users and webusers
		if (generateRedumptionCodesRequest.getRedumptionCodesRequestArray() == null || generateRedumptionCodesRequest.getRedumptionCodesRequestArray().getRedumptionCodeRequest() == null || 
				generateRedumptionCodesRequest.getRedumptionCodesRequestArray().getRedumptionCodeRequest().size() == 0) {
			statusString = "No users in the payload.  No redumption codes to generate.";
		} else if (generateRedumptionCodesRequest.getIdCrcTypeCode() == 0) {
			statusString = "No IdCrcTypeCode in the payload.  No redumption codes to generate.";
		} else {
			List<RedumptionCodeWebRequest> redumptionCodeRequestList = parseRedumptionCodesRequestTypeListToRedumptionCodeWebRequests(generateRedumptionCodesRequest.getIdCrcTypeCode(), 
					generateRedumptionCodesRequest.getPurchaserMessage(),
					generateRedumptionCodesRequest.getRedumptionCodesRequestArray().getRedumptionCodeRequest());
			// dump the uploadUserList...
			System.out.println ("redumptionCodeRequestList: " + redumptionCodeRequestList);
			
			// now kick of a server method to generate the requisite redumption codes
			// Even administrators need a special ROLE to upload users into the database programmatically - System Admin
			// finally, load the users
			statusString = channelDao.generateRedumptionCodes(redumptionCodeRequestList);
		}

		// create the response
		GenerateRedumptionCodesResponse response = new GenerateRedumptionCodesResponse();
		if (statusString == null) {
			response.setStatus(UPLOAD_SUCCESS_STATUS_CODE);
			response.setMessage(UPLOAD_SUCCESS_MESSAGE);
		} else {
			response.setStatus(UPLOAD_UNKNOWN_FAILURE_STATUS_CODE);
			response.setMessage(statusString);
		}

		return response;
	}

	private List<RedumptionCodeWebRequest> parseRedumptionCodesRequestTypeListToRedumptionCodeWebRequests (
			Integer idCrcTypeCode, String purchaserMessage, List<RedumptionCodeRequestType> redumptionCodeRequestTypes) {
		List<RedumptionCodeWebRequest> redumptionCodeRequestList = new ArrayList<RedumptionCodeWebRequest>();
		if (redumptionCodeRequestTypes != null && redumptionCodeRequestTypes.size() > 0) {
			for (RedumptionCodeRequestType redumptionCodeRequestType : redumptionCodeRequestTypes) {
				RedumptionCodeWebRequest redumptionCodeWebRequest = new RedumptionCodeWebRequest();
				redumptionCodeWebRequest.setIdCrcTypeCode(idCrcTypeCode);
				redumptionCodeWebRequest.setEmailAddress(redumptionCodeRequestType.getEmailAddress());
				redumptionCodeWebRequest.setFirstName(redumptionCodeRequestType.getFirstName());
				redumptionCodeWebRequest.setLastName(redumptionCodeRequestType.getLastName());
				redumptionCodeWebRequest.setMiddleName(redumptionCodeRequestType.getMiddleName());
				redumptionCodeWebRequest.setPurchaserMessage(purchaserMessage);
				redumptionCodeRequestList.add(redumptionCodeWebRequest);
			}
		}
		return redumptionCodeRequestList;
	}
	
	
	
	
	
}
