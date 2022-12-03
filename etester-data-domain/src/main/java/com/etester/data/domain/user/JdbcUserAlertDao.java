package com.etester.data.domain.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.etester.data.domain.test.JdbcDaoStaticHelper;
import com.etester.data.domain.util.email.EmailNotifierService;
import com.etester.data.domain.util.sms.SMSService;
import com.etester.data.domain.util.sms.Usermessage;
/**
 * Static Helper class to assist in exploding and publishing UserAlerts into the respective channelmessage and usermessage table
 * @author sesi
 *
 */
public class JdbcUserAlertDao extends NamedParameterJdbcDaoSupport implements UserAlertDao {

	private static final String DEFAULT_SERIALIZED_TARGET_CRITERIA_SEPERATOR = ":";
	private static final String DEFAULT_SERIALIZED_TARGET_ID_SEPERATOR = ",";
	private static final String DEFAULT_ALL_CRITERIA = "all";
	private static final Long DEFAULT_ID_FOR_ALL_CRITERIA = -1l;
	
	private static final int DEFAULT_MESSAGE_PRIORITY = 3;
	private static final String USER_ALERT_TARGETING_METHOD_CHANNEL = "C";
	private static final String USER_ALERT_TARGETING_METHOD_USER = "U";
	private static final String DEFAULT_ACTIVE_SUBSCRIBERS_LIST_BOX_VALUE = "A";

	// EmailNotifierService is injected via spring 
	private EmailNotifierService emailNotifierService;
    /**
	 * @return the emailNotifierService
	 */
	public EmailNotifierService getEmailNotifierService() {
		return emailNotifierService;
	}
	/**
	 * @param emailNotifierService the emailNotifierService to set
	 */
	public void setEmailNotifierService(EmailNotifierService emailNotifierService) {
		this.emailNotifierService = emailNotifierService;
	}

	// SMSService is injected via spring 
	private SMSService smsService;
	/**
	 * @return the smsService
	 */
	public SMSService getSmsService() {
		return smsService;
	}
	/**
	 * @param smsService the smsService to set
	 */
	public void setSmsService(SMSService smsService) {
		this.smsService = smsService;
	}
	
	@Override
    public void publishUserAlert(UserAlert savedUserAlert) {
		String criteriaString = savedUserAlert.getAlertTargetCriteria();
		if (criteriaString == null) {
			// nothing to do here.  return.
			return;
		} else {
			criteriaString = criteriaString.trim();
		}
		// We do have a non-null response
		String [] criteriaStringTokens = criteriaString.split(DEFAULT_SERIALIZED_TARGET_CRITERIA_SEPERATOR);
		if (criteriaStringTokens.length != 3) {
			System.err.println("Some messed up. SerializedUserAlertTargetCriteria is incorrect for UserAlert: '" + criteriaString + "'");
			return;
		}
		// get targetingMethod
		String targetingMethodPrefixString = criteriaStringTokens[0];

		// get activeSubscribers value
		String activeSubscribersListBoxString = criteriaStringTokens[1];

		// get the target ID's
		String serializedSelectedItems = criteriaStringTokens[2];
		List<Long> idList = new ArrayList<Long>();
		if (serializedSelectedItems != null) {
			String [] selectedItemStringArray = serializedSelectedItems.split(DEFAULT_SERIALIZED_TARGET_ID_SEPERATOR);
			if (selectedItemStringArray.length == 1 && DEFAULT_ALL_CRITERIA.equalsIgnoreCase(selectedItemStringArray[0].trim())) {
				// All selected
				// add "All" listing
				idList.add(DEFAULT_ID_FOR_ALL_CRITERIA);
			} else {
				for (String idString : selectedItemStringArray) {
					try {idList.add(Long.valueOf(idString));} catch (NumberFormatException nfe) {}
				}
			}
		}

		// Parsing completed.  No figure our if its a Channel Alert or a User Alert and explode...
		if (targetingMethodPrefixString != null && targetingMethodPrefixString.trim().length() > 0) {
			if (USER_ALERT_TARGETING_METHOD_USER.equalsIgnoreCase(targetingMethodPrefixString.trim())) {
				if (idList != null && idList.size() == 1 && idList.get(0) == DEFAULT_ID_FOR_ALL_CRITERIA) {
					// Send the Alert to ALL Users
					writeUsermessagesToAllUsers(savedUserAlert);
				} else {
					// Send alert to User list
					writeUsermessagesToUserList(idList, savedUserAlert);
				}
			} else if (USER_ALERT_TARGETING_METHOD_CHANNEL.equalsIgnoreCase(targetingMethodPrefixString.trim())) {
				if (idList != null && idList.size() == 1 && idList.get(0) == DEFAULT_ID_FOR_ALL_CRITERIA) {
					// Send the Alert to ALL Users
					writeUsermessagesToAllUsersInAllChannels(savedUserAlert);
				} else {
					// Send alert to User list
					writeUsermessagesToAllUsersInChannelList(idList, savedUserAlert);
				}
				
			} else {
				System.err.println("Some messed up. UserAlert TargetingMethodPrefixString is incorrect for UserAlert: '" + targetingMethodPrefixString + "'");
			}
		}
		
	}

	/**
	 * Write Usermessage to every active user in eTester (irrespective of if they have an active subscription or not)
	 * @param savedUserAlert
	 * @param namedParameterJdbcTemplate
	 */
	private void writeUsermessagesToAllUsers(UserAlert savedUserAlert) {
		// get all users (not just active)
		List<Long> idUserList = getAllActiveUsers();
		writeUsermessagesToUserList(idUserList, savedUserAlert);
	}
	
	private List<Long> getAllActiveUsers() {
		List<Long> idUserList = new ArrayList<Long>();
		String sql = "SELECT u.id_user FROM user u WHERE u.enabled = 1 ORDER BY u.id_user" ;
		Map<String, Object> args = new HashMap<String, Object>();
		idUserList = getNamedParameterJdbcTemplate().queryForList(sql, args, Long.class);
		return idUserList;
	}

	/**
	 * Write Usermessage to every active subscriber on any one of the eTester channels.
	 * @param savedUserAlert
	 * @param namedParameterJdbcTemplate
	 */
	private void writeUsermessagesToAllUsersInAllChannels(UserAlert savedUserAlert) {
		List<Long> idChannelList = getAllPublishedChannelIds();
		writeUsermessagesToAllUsersInChannelList(idChannelList, savedUserAlert);
	}

	private List<Long> getAllPublishedChannelIds() {
		List<Long> idChannelList = new ArrayList<Long>();
		String sql = "SELECT c.id_system FROM system c WHERE c.published = 1 ORDER BY c.id_system" ;
		Map<String, Object> args = new HashMap<String, Object>();
		idChannelList = getNamedParameterJdbcTemplate().queryForList(sql, args, Long.class);
		return idChannelList;
	}

	/**
	 * Write Usermessage to every active subscriber on any one of the channels on the ChannelList.
	 * @param idChannelList
	 * @param savedUserAlert
	 * @param namedParameterJdbcTemplate
	 */
	private void writeUsermessagesToAllUsersInChannelList(List<Long> idChannelList, UserAlert savedUserAlert) {
		if (idChannelList == null || idChannelList.size() == 0) {
			// none to do
			return;
		}
		List<Long> idUserList = getAllActiveUsersInChannelList(idChannelList);
		writeUsermessagesToUserList(idUserList, savedUserAlert);
	}

	/**
	 * Static method that generates a List of Active Users subscribing to any of the channels on the given Channel List
	 * @param idChannelList
	 * @param namedParameterJdbcTemplate
	 * @return
	 */
	private List<Long> getAllActiveUsersInChannelList(List<Long> idChannelList) {
		List<Long> idUserList = new ArrayList<Long>();
		StringBuffer commaSeperatedChannelListStringSB = new StringBuffer();
		boolean first = true;
		for (Long idChannel : idChannelList) {
			if (first) {
				first = false;
			} else {
				commaSeperatedChannelListStringSB.append(",");
			}
			commaSeperatedChannelListStringSB.append(idChannel.toString());
		}
		String sql = "SELECT DISTINCT cs.id_student FROM channel_subscriptions cs WHERE cs.end_date > NOW() AND id_channel in (" + commaSeperatedChannelListStringSB.toString() + ") ORDER BY cs.id_student" ;
		Map<String, Object> args = new HashMap<String, Object>();
		idUserList = getNamedParameterJdbcTemplate().queryForList(sql, args, Long.class);
		return idUserList;
	}

	
	/**
	 * This is the actual method that translates the user_alert and writes a usermessage to every one of the users list in all the formats chosen.
	 * @param idUserList
	 * @param savedUserAlert
	 * @param namedParameterJdbcTemplate
	 */
	private void writeUsermessagesToUserList(List<Long> idUserList, UserAlert savedUserAlert) {
		if (idUserList == null || idUserList.size() == 0) {
			// none to do
			return;
		} 
		// Do somethings common to all alerts
    	String alertPriorityString = "an";
    	Integer alertPriority = savedUserAlert.getAlertPriority();
    	if (alertPriority == null || alertPriority.intValue() > 5 || alertPriority.intValue() < 1) {
    		alertPriority = 3;
    	}
       	switch (alertPriority) {
            case 1:  alertPriorityString = "a Very LOW PRIORITY";
            break;
            case 2:  alertPriorityString = "a LOW PRIORITY";
            break;
            case 3:  alertPriorityString = "an";
            break;
            case 4:  alertPriorityString = "a HIGH PRIORITY";
            break;
            case 5:  alertPriorityString = "a Very HIGH PRIORITY";
            break;
            default: alertPriorityString = "an";
    	}
		
		if (savedUserAlert.getAlertModeOnline() != null && savedUserAlert.getAlertModeOnline() == 1) {
			writeOnlineUsermessagesToUserList(idUserList, savedUserAlert);
		}
		if (savedUserAlert.getAlertModeEmail() != null && savedUserAlert.getAlertModeEmail() == 1) {
			writeEmailUsermessagesToUserList(idUserList, savedUserAlert, alertPriorityString);
		}
		if (savedUserAlert.getAlertModeSms() != null && savedUserAlert.getAlertModeSms() == 1) {
			writeSMSUsermessagesToUserList(idUserList, savedUserAlert, alertPriorityString);
		}
	}

	/**
	 * This is the actual method that translates the user_alert and writes a usermessage to every one of the users.
	 * @param idUserList
	 * @param savedUserAlert
	 * @param namedParameterJdbcTemplate
	 */
	private void writeOnlineUsermessagesToUserList( List<Long> idUserList, UserAlert savedUserAlert) {
		if (idUserList == null || idUserList.size() == 0) {
			// none to do
			return;
		}

		List<SqlParameterSource> usermessageParameters = new ArrayList<SqlParameterSource>();
		for (Long idUser : idUserList) {
			Usermessage usermessage = new Usermessage();
			usermessage.setIdUser(idUser);
			usermessage.setHeading(savedUserAlert.getHeading());
			usermessage.setContent(savedUserAlert.getContent());
			usermessage.setLink(savedUserAlert.getLink());
			usermessage.setIsChannelMessage(savedUserAlert.getAlertType() != null && savedUserAlert.getAlertType().intValue() == 2 ? 1 : 0);
			usermessage.setMessagePriority(savedUserAlert.getAlertPriority());
			usermessage.setMessageExpirationDate(savedUserAlert.getAlertExpiryDate());
			usermessageParameters.add(new BeanPropertySqlParameterSource(usermessage));
		}
		getNamedParameterJdbcTemplate().batchUpdate(UserDao.insertUsermessageSQL, usermessageParameters.toArray(new SqlParameterSource[0]));
	}

	/**
	 * This is the actual method that translates the user_alert and writes and sends an email to every one of the users.
	 * @param idUserList
	 * @param savedUserAlert
	 * @param namedParameterJdbcTemplate
	 */
	private void writeEmailUsermessagesToUserList(List<Long> idUserList, UserAlert savedUserAlert, String alertPriorityString) {
		if (idUserList == null || idUserList.size() == 0) {
			// none to do
			return;
		} 
		for (Long idUser : idUserList) {
			User user = JdbcDaoStaticHelper.findUserByUserId(getNamedParameterJdbcTemplate(), idUser); 
			if (user == null || user.getEmailAddress() == null || user.getEmailAddress().trim().length() == 0) {
	        	System.out.println ("User information Missing for idUser '" + idUser + "'.  Cannot send User Alert Email Message.");
			} else {
				this.getEmailNotifierService().sendEmailMessageAlert(user, savedUserAlert.getHeading(), savedUserAlert.getContent(), savedUserAlert.getLink(), alertPriorityString, Locale.ENGLISH);
			}
		}
	}

	/**
	 * This is the actual method that translates the user_alert and sends an SMS to every one of the users.
	 * @param idUserList
	 * @param savedUserAlert
	 * @param namedParameterJdbcTemplate
	 */
	private void writeSMSUsermessagesToUserList( List<Long> idUserList, UserAlert savedUserAlert, String alertPriorityString) {
		if (idUserList == null || idUserList.size() == 0) {
			// none to do
			return;
		} 
		for (Long idUser : idUserList) {
			Webuser webuser = JdbcDaoStaticHelper.findWebuserForUserId(getNamedParameterJdbcTemplate(), idUser); 
			if (webuser == null || webuser.getPhoneNumber() == null || webuser.getPhoneNumber().trim().length() == 0) {
	        	System.out.println ("WebUser information Missing for idUser '" + idUser + "'.  Cannot send User Alert SMS Message.");
			} else {
				try {
					String messageString = "eTester - " + alertPriorityString + " - " + savedUserAlert.getHeading();
					this.getSmsService().sendSMS(webuser.getPhoneNumber(), messageString);
				} catch (Exception e) {
					System.err.println("Error Sending SMS Message to '" + webuser.getPhoneNumber() + "': " + e.getMessage());
				}
			}
		}
	}

	


}
