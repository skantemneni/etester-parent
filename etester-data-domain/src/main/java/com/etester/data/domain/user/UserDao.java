package com.etester.data.domain.user;

import java.util.List;

import com.etester.data.domain.util.email.SendEmailData;
import com.etester.data.domain.util.sms.Usermessage;

public interface UserDao {

	// Roles
	public final static String ROLE_LOGIN = "ROLE_LOGIN"; 
	public final static String ROLE_USER = "ROLE_USER"; 
	public final static String ROLE_PROVIDER = "ROLE_PROVIDER"; 
	public final static String ROLE_ADMIN = "ROLE_ADMIN"; 

	// Permissions
	public static final String PERMISSION_RESET_CACHE = "RESET_CACHE";
	public static final String PERMISSION_DISABLE_CACHE = "DISABLE_CACHE";
	
	
	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.

	// USER SQL
	// new
	public static String insertNewUserSQL = "INSERT INTO user (username, password, enabled, email_address, first_name, last_name, middle_name) "
			+ " VALUES (:username, :password, :enabled, :emailAddress, :firstName, :lastName, :middleName)";
	// update
	public static String updateUserMetadataSQL = "UPDATE user SET first_name = :firstName, last_name = :lastName, email_address = :emailAddress, middle_name = :middleName WHERE username = :username";

	// WEBUSER SQL
	// find
	public static String findWebuserForLoadSQL = "SELECT * FROM webuser WHERE username = :username ";
	//new
	public static String insertNewWebuserSQL = "INSERT INTO webuser (username, first_name, last_name, middle_name, gender, from_channel, profession, institution, branch_year, address_line1, address_line2, country, phone_number, dob, date_created) "
			+ " VALUES (:username, :firstName, :lastName, :middleName, :gender, :fromChannel, :profession, :institution, :branchYear, :addressLine1, :addressLine2, :country, :phoneNumber, :dob, Now())";
	//update
	public static String updateWebuserMetadataSQL = "UPDATE webuser SET first_name = :firstName, last_name = :lastName, middle_name = :middleName, "
			+ "gender = :gender, from_channel = :fromChannel, profession = :profession, institution = :institution, branch_year = :branchYear, "
			+ "address_line1 = :addressLine1, address_line2 = :addressLine2, country = :country, phone_number = :phoneNumber, dob = :dob "
			+ "WHERE username = :username";

	// AUTHORITIES SQL
	// delete
	public static String deleteAllAuthoritiesSql = "DELETE FROM authorities WHERE username = :username";
	// add
	public static String insertAuthoritiesSql = "INSERT INTO authorities (username, authority) VALUES (:username, :authority)";

	// CHANNEL SUBSCRIPTIONS SQL
	// new
	public static String insertStudentChannelSubscriptionsSQL = "INSERT INTO channel_subscriptions (id_channel, id_student, start_date, end_date) "
			+ " VALUES (:idChannel, :idStudent, :startDate, :endDate)";
	// find
	public static String findStudentChannelSubscriptionsSQL = "SELECT * FROM channel_subscriptions WHERE id_channel = :idChannel AND id_student = :idStudent";
	// update
	public static String updateStudentChannelSubscriptionsSQL = "UPDATE channel_subscriptions SET start_date = :startDate, end_date = :endDate WHERE id_channel = :idChannel AND id_student = :idStudent";

	// ORGANIZATION_STUDENT
	public static String insertOrganizationStudentSQL = "INSERT IGNORE INTO organization_student (id_organization, id_student) VALUES (:idOrganization, :idStudent) ";
	
	
	
	public static String findByUsernameForLoginSQL = "SELECT * FROM user u WHERE u.username = :username "; 

	
	public static String insertNewUserAuthoritiesSql = "INSERT INTO authorities (username, authority) VALUES (:username, 'ROLE_USER')";
//	public static String insertNewUserAuthoritiesSql = "INSERT INTO authorities (username, authority) VALUES (:username, 'ROLE_LOGIN')";


//	public static String addUserAuthoritiesSql = "INSERT INTO authorities (username, authority) VALUES (:username, :authority)";

	public static String deleteUserAuthoritiesSql = "DELETE FROM authorities WHERE username = :username and authority = :authority";

	// RESET PASSWORD SQL
	public static String resetUserPasswordSQL = "UPDATE user SET password = :password WHERE username = :username";

	public static String findWebuserByUsernameSQL = "SELECT u.username AS username, wu.first_name, wu.last_name, wu.middle_name, u.email_address AS email_address, wu.gender, " 
								+ "wu.profession, wu.institution, wu.branch_year, wu.address_line1, wu.address_line2, wu.country, wu.phone_number, wu.dob "
								+ " FROM user u LEFT JOIN webuser wu ON wu.username = u.username WHERE u.username = :username "; 

	// Create user typically creates a new DISABLED user and a activation transaction to be sent to the 
	// user in an email
	public static String insertWebuserPasswordTransactionSQL = "INSERT INTO webuser_password_transaction (username, transaction_type, transaction_key, new_password, transaction_expiry_date) "
			+ " VALUES (:username, :transactionType, :transactionKey, :newPassword, Now() + INTERVAL 60 MINUTE)";

	public static String upsertWebuserPasswordTransactionSQL = "INSERT INTO webuser_password_transaction (username, transaction_type, transaction_key, new_password, transaction_expiry_date) "
			+ " VALUES (:username, :transactionType, :transactionKey, :newPassword, Now() + INTERVAL 60 MINUTE) "
			+ " ON DUPLICATE KEY UPDATE transaction_type = :transactionType, transaction_key = :transactionKey, new_password = :newPassword, transaction_expiry_date = Now() + INTERVAL 60 MINUTE ";

	public static String findWebuserPasswordTransactionByUsernameAndTypeSQL = "SELECT * FROM webuser_password_transaction WHERE username = :username and transaction_type = :transactionType and transaction_expiry_date > Now()"; 
	
	public static String deleteWebuserPasswordTransactionByUsernameAndTypeSQL = "DELETE FROM webuser_password_transaction WHERE username = :username and transaction_type = :transactionType"; 
	
//	public static String updateUserSQL = "INSERT INTO user (username, password, enabled, first_name, last_name, middle_name) "
//			+ " VALUES (:username, :password, :enabled, :firstName, :lastName, :middleName)";

	public static String enableUsernameSQL = "UPDATE user SET enabled = :enabled WHERE username = :username"; 
	
	public static String enableUserIdSQL = "UPDATE user SET enabled = :enabled WHERE id_user = :idUser"; 
	
	public static String addAuthorityUsernameSQL = "INSERT INTO authorities (username, authority) VALUES (:username, :authority)"; 
	
	public static String findByUserIdSQL = "SELECT id_user, username, password, IFNULL(enabled, 0), first_name, last_name, middle_name, email_address FROM user WHERE id_user = :id_user"; 
	
	public static String findByUserNameSQL = "SELECT id_user, username, password, IFNULL(enabled, 0), first_name, last_name, middle_name, email_address FROM user WHERE username = :username"; 
	


	
	public static final String findWebuserForUserIdSQL = 
					"SELECT u.username, u.first_name, u.last_name, u.middle_name, u.email_address, wu.gender, wu.profession, wu.institution, "
					+ "	wu.branch_year, wu.address_line1, wu.address_line2, wu.country, wu.phone_number, wu.dob, wu.from_channel, wu.date_created "
					+ " FROM user u LEFT JOIN webuser wu ON wu.username = u.username WHERE u.id_user = :idUser ";
	
	public static String findByUsernameWithOrganizationSQL = 
			" SELECT u.*, o.id_organization AS id_organization, o.`name` AS organization_name " +
			" FROM user u LEFT JOIN organization_provider op ON u.id_user = op.id_provider " +  
			" 			LEFT JOIN organization o on op.id_organization = o.id_organization " +
			" WHERE u.username = :username";

	public static String findByUserIdWithOrganizationSQL = 
			" SELECT u.*, o.id_organization AS id_organization, o.`name` AS organization_name " +
			" FROM user u LEFT JOIN organization_provider op ON u.id_user = op.id_provider " +  
			" 			LEFT JOIN organization o on op.id_organization = o.id_organization " +
			" WHERE u.id_user = :idUser";

//	public static String findChannelsByOrganizationIdSQL =  
//			" SELECT s.id_system AS id_system, s.`name` AS name " +
//			" FROM organization_channel oc LEFT JOIN system s on oc.id_channel = s.id_system " +
//			" WHERE oc.id_organization = :idOrganization ";
	
	public static String findChannelsByOrganizationIdSQL =  
			" SELECT s.* " +
			" FROM organization_channel oc LEFT JOIN `system` s on oc.id_channel = s.id_system " +
			" WHERE oc.id_organization = :idOrganization ";
	
	public static String findSubscriptionChannelsForUserIdSQL =  
			" SELECT s.* FROM `system` s LEFT JOIN channel_subscriptions cs ON s.id_system = cs.id_channel "
			+ " WHERE cs.id_student = :idUser AND cs.start_date <= NOW() AND cs.end_date >= NOW() ";
	
	public static String findAllAuthoritiesByUsernameSQL = "SELECT * FROM authorities WHERE username = :username"; 
	
	public static String findAllPermissionsByUsernameSQL = "SELECT * FROM permissions WHERE username = :username"; 
	
	public static String deleteByUsernameSQL = "DELETE FROM user WHERE username = :username"; 

//	public static String deleteAuthoritiesUsernameSQL = "DELETE FROM authorities WHERE username = :username and authority = :authority"; 
//	
//	public static String deleteAllAuthoritiesUsernameSQL = "DELETE FROM authorities WHERE username = :username"; 
	

	public String addWebuser(Webuser user) throws Exception;

	public String addUser(User user) throws Exception;

	public Integer resetWebuserPassword(Webuser user);

	// TODO:  SESI - Temporary only here so I can call using REST Controllers world for now.  Will update soon.  
	public Integer updateEncodedPassword(String username, String newPassword);

	public Integer updateWebuser(User user);

	public void enableWebuserWithName(String username);

	public void enableWebuserWithId(Long idUser);

	public void disableWebuserWithName(String username);

	public void disableWebuserWithId(Long idUser);

    public User findByUserId(Long idUser);

    public User findByUserIdWithDetails(Long idUser);

    public User findByUsername(String username);
    
    public User findByUsernameWithDetails(String username);
    
//    public User findByUsernameForLogin(String username);
    
    public boolean doesUserExistByUsername(String username);

    public boolean doesUserExistByUserId(Long idUser);

//    public User findCurrentUserDetails();

    public List<User> findAll();

    // used to activate users.  This call comes from the RfService web app.
	public int activateUser(String username, String activationToken);

    // user to reset password for user.  This call comes from the RfService web app.
	public int resetUserPassword(String username, String activationToken);

	public Webuser findWebuserByUsername(String username);

	/**
	 * Allows for programmatically loading users into the database
	 * @param userDaoList
	 * @return 
	 */
	public List<UploadUserResponse> uploadBatchUsers(List<User> userDaoList);
	
	
	/**
	 * Functionality that allows to send (submit) emails to eTester. 
	 */
	public void sendEmailMessage(SendEmailData emailPayload);

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// Usermessage Section
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String insertUsermessageSQL = "INSERT INTO usermessage (id_user, heading, content, link, is_channel_message, message_priority, message_creation_date, message_expiration_date) VALUES "
			+ "(:idUser, :heading, :content, :link, :isChannelMessage, :messagePriority, NOW(), :messageExpirationDate)";
	public static final String getAllUsermessagesForUserSQL = "SELECT * FROM usermessage WHERE message_expired != 1 AND id_user = :idUser ";
	public static final String updateUsermessageStatusToAcknowledgedSQL = "UPDATE usermessage SET message_acknowledged = 1, message_acknowledged_date = NOW() WHERE id_usermessage = :idUsermessage ";	
	public static final String updateUsermessageStatusToDeletedSQL = "UPDATE usermessage SET message_expired = 1, message_expiration_date = NOW() WHERE id_usermessage = :idUsermessage ";	
	
	/**
	 * Functionality that allows to send (submit) emails to eTester. 
	 */
	public List<Usermessage> getAllUsermessagesForCurrentUser();
	/**
	 * Update Usermessage status
	 * @param idUsermessage
	 * @param usermessageStatusInt
	 * @return
	 */
	public void updateUsermessageStatus(Long idUsermessage, Integer usermessageStatusInt);

	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// User Alerts Section
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String insertUserAlertSQL = "INSERT INTO user_alert (id_provider, name, description, alert_target_criteria, heading, content, link, alert_type, alert_priority, alert_creation_date, alert_expiry_date, "
			+ " alert_mode_online, alert_mode_email, alert_mode_sms, published) VALUES "
			+ "(:idProvider, :name, :description, :alertTargetCriteria, :heading, :content, :link, :alertType, :alertPriority, NOW(), :alertExpiryDate, :alertModeOnline, :alertModeEmail, :alertModeSms, :published)";
	public static final String upsertUserAlertSQL = "INSERT INTO user_alert (id_user_alert, id_provider, name, description, alert_target_criteria, heading, content, link, alert_type, alert_priority, alert_creation_date, alert_expiry_date, "
			+ " alert_mode_online, alert_mode_email, alert_mode_sms, published) "
			+ " VALUES (:idUserAlert, :idProvider, :name, :description, :alertTargetCriteria, :heading, :content, :link, :alertType, :alertPriority, NOW(), :alertExpiryDate, :alertModeOnline, :alertModeEmail, :alertModeSms, :published) "
			+ " ON DUPLICATE KEY "
			+ " UPDATE name = :name, description = :description, alert_target_criteria = :alertTargetCriteria, heading = :heading, content = :content, link = :link, alert_type = :alertType, alert_priority = :alertPriority, "
			+ " alert_expiry_date = :alertExpiryDate, alert_mode_online = :alertModeOnline , alert_mode_email = :alertModeEmail , alert_mode_sms = :alertModeSms , published = :published";
	
	public List<UserAlert> getUnpublishedUserAlertsForCurrentProvider();
	public List<UserAlert> getUnpublishedUserAlerts(Long idProvider);
	public UserAlert getUserAlertForCurrentProvider(Long idUserAlert);
	public UserAlert getUserAlert(Long idProvider, Long idUserAlert);
	public UserAlert saveOrUpdateUserAlertForCurrentProvider(UserAlert userAlert);
	public UserAlert saveOrUpdateUserAlert(Long idProvider, UserAlert userAlert);
	public Integer deleteUserAlertForCurrentProvider(Long idUserAlert);
	public Integer deleteUserAlert(Long idProvider, Long idUserAlert);
	// Associated methods for UserAlerts
	public List<User> getUsersForCurrentProvider();

	// Read Site Settings from remote applications...
	public String getSiteSetting(String settingName);
	
}

