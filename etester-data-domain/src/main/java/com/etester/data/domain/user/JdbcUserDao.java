package com.etester.data.domain.user;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.etester.data.domain.content.ChannelSubscription;
import com.etester.data.domain.test.JdbcDaoStaticHelper;
import com.etester.data.domain.test.TestConstants.UsermessageStatus;
import com.etester.data.domain.test.instance.Usertest;
import com.etester.data.domain.util.email.EmailNotifierService;
import com.etester.data.domain.util.email.SendEmailData;
import com.etester.data.domain.util.email.WebuserNotifier;
import com.etester.data.domain.util.sms.Usermessage;

@Repository
public class JdbcUserDao extends NamedParameterJdbcDaoSupport implements UserDao {
	
////    private final DataSource dataSource;
//	@Autowired
	PasswordEncoder passwordEncoder;
	
	public JdbcUserDao(DataSource dataSource, PasswordEncoder passwordEncoder) {
		super();
		setDataSource(dataSource);
//		this.dataSource = dataSource;
		this.passwordEncoder = passwordEncoder;
	}

//    @PostConstruct
//    private void initialize() {
//        setDataSource(dataSource);
//    }

    protected static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	// Password Encoder is injected via spring 
//	ShaPasswordEncoder passwordEncoder;

//	MessageDigestPasswordEncoder passwordEncoder;
	/**
	 * @return the passwordEncoder
	 */
	public PasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}
	/**
	 * @param passwordEncoder the passwordEncoder to set
	 */
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	// WebuserNotifier is injected via spring 
	private WebuserNotifier webuserNotifier;
	/**
	 * @return the webuserNotifier
	 */
	public WebuserNotifier getWebuserNotifier() {
		return webuserNotifier;
	}
	/**
	 * @param webuserNotifier the webuserNotifier to set
	 */
	public void setWebuserNotifier(WebuserNotifier webuserNotifier) {
		this.webuserNotifier = webuserNotifier;
	}

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

	// UserAlertDao is injected via spring 
	private UserAlertDao userAlertDao;
	/**
	 * @return the userAlertDao
	 */
	public UserAlertDao getUserAlertDao() {
		return userAlertDao;
	}
	/**
	 * @param userAlertDao the userAlertDao to set
	 */
	public void setUserAlertDao(UserAlertDao userAlertDao) {
		this.userAlertDao = userAlertDao;
	}

    /************************************************************/
    // Activation URL's code
	private static final String DEFAULT_SALT_CODE = null;
	private static final String DEFAULT_ACTIVATION_URL = "http://etester.com/rfservice/activateWebuser.do?";
	private static final String DEFAULT_PASSWORD_RESET_URL = "http://etester.com/rfservice/resetWebuser.do?";
	private static final String DEFAULT_MESSAGE_SUBMITTED_RECEIVER = "";
//	private static final String DEFAULT_ACTIVATION_URL = "http://localhost:8080/rfservice/activateWebuser.do?";
//	private static final String DEFAULT_PASSWORD_RESET_URL = "http://localhost:8080/rfservice/resetWebuser.do?";
	
	private String activationURL = null;
	private String passwordResetURL = null;
	private String messageSubmittedReceiver = null;
	
	private String getActivationURL() {
		if (this.activationURL == null) {
			this.activationURL = JdbcDaoStaticHelper.getSiteSetting(getNamedParameterJdbcTemplate(), "SITE_ACTIVATION_URL");
			if (this.activationURL == null) {
				this.activationURL = DEFAULT_ACTIVATION_URL;
			}
		}
		return this.activationURL;
	}
	
	private String getPasswordResetURL() {
		if (this.passwordResetURL == null) {
			this.passwordResetURL = JdbcDaoStaticHelper.getSiteSetting(getNamedParameterJdbcTemplate(), "SITE_PASSWORD_RESET_URL");
			if (this.passwordResetURL == null) {
				this.passwordResetURL = DEFAULT_PASSWORD_RESET_URL;
			}
		}
		return this.passwordResetURL;
	}

	private String getMessageSubmittedReceiver() {
		if (this.messageSubmittedReceiver == null) {
			this.messageSubmittedReceiver = JdbcDaoStaticHelper.getSiteSetting(getNamedParameterJdbcTemplate(), "MESSAGE_SUBMITTED_RECEIVER");
			if (this.messageSubmittedReceiver == null) {
				this.messageSubmittedReceiver = DEFAULT_MESSAGE_SUBMITTED_RECEIVER;
			}
		}
		return this.messageSubmittedReceiver;
	}
	
	/*******************************************************************/
	
	private static enum SaveResponse {
		SAVE_SUCCESS (0),
		SAVE_FAIL_DUPLICATE_USER (1),
		SAVE_FAIL_UNKNOWN_USER (-1),
		SAVE_FAIL_UNKNOWN_AUTHORITY (-2),
		SAVE_FAIL_UNKNOWN_PASSWORD_TRANSACTION (-3);
		
		private final int response_code;
		SaveResponse(int response_code) {
	        this.response_code = response_code;
	    }
		private int responseCode() { return response_code; }
		
		private static String responseValue (SaveResponse s) {
			for (SaveResponse sr : SaveResponse.values()) {
				if (s == sr) {
					return s.toString();
				}
			}
			return null;
		}
	}

	private static enum UpdateResponse {
		UPDATE_SUCCESS (0),
		UPDATE_FAIL_NO_USER (1),
		UPDATE_FAIL_NO_EMAIL (2),
		SAVE_FAIL_UNKNOWN_PASSWORD_TRANSACTION (-3);
		
		private final int response_code;
		UpdateResponse(int response_code) {
	        this.response_code = response_code;
	    }
		private int responseCode() { return response_code; }
	}

	@Override
	public String addUser(User user) throws Exception {
		Webuser webuser = new Webuser();
		webuser.setUsername(user.getUsername());
		webuser.setPassword(user.getPassword());
		webuser.setFirstName(user.getFirstName());
		webuser.setLastName(user.getLastName());
		webuser.setEmailAddress(user.getEmailAddress());
		webuser.setEnabled(user.isEnabled());
		
		webuser.setGender("M");
		webuser.setAddressLine1("1234 Winding Cape Way");
		webuser.setCountry("India");
		webuser.setPhoneNumber("1234567890");
		Calendar calendar = Calendar.getInstance();
		calendar.set(1990, 1, 1);
		webuser.setDob(calendar.getTime());
		webuser.setFromChannel("Manual");
		
		return addWebuser(webuser);
	}

	@Override
	public String addWebuser(Webuser user) throws Exception {
		// make the user name all lower case.  this is simply a best place to do it.
		user.setUsername(user.getUsername().trim().toLowerCase());
//        webuserNotifier.notifyNewWebuser(user.getUsername().toLowerCase(), user.getFirstName() + " " + user.getLastName(), user.getUsername().toLowerCase(), "www.etester.com");
		// check to see if the user exists already
		if (JdbcDaoStaticHelper.existsUserByUserName(getNamedParameterJdbcTemplate(), user.getUsername())) {
			// may need to throw an exception here
			return SaveResponse.responseValue(SaveResponse.SAVE_FAIL_DUPLICATE_USER);
		}
		// encode and set the password string on the user object
		// and create a transaction token: 
//		String encodedPassword = passwordEncoder.encodePassword(user.getPassword(), DEFAULT_SALT_CODE);
//		String webuserPasswordTransactionKey = passwordEncoder.encodePassword(String.valueOf(Calendar.getInstance().getTimeInMillis()), DEFAULT_SALT_CODE);
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		String webuserPasswordTransactionKey = passwordEncoder.encode(String.valueOf(Calendar.getInstance().getTimeInMillis()));
		System.out.println ("Password = '" + user.getPassword() + "', Encoded Password: ---" + encodedPassword + "---");
		System.out.println ("Password = '" + user.getPassword() + "', Transaction Key: ---" + webuserPasswordTransactionKey + "---");
		user.setPassword(encodedPassword);

		// Add the user
		SqlParameterSource userParameterSource = new BeanPropertySqlParameterSource(user);
        String insertUserSQL = insertNewUserSQL;
        try {
        	getNamedParameterJdbcTemplate().update(insertUserSQL, userParameterSource);
        } catch (Exception e) {
        	throw e;
//        	return SaveResponse.responseValue(SaveResponse.SAVE_FAIL_UNKNOWN_USER);
        }

		// Add the Authorities
        String insertAuthoritiesSql = insertNewUserAuthoritiesSql;
        try {
        	// see if you can reuse the parameterSource from before...
        	getNamedParameterJdbcTemplate().update(insertAuthoritiesSql, userParameterSource);
        } catch (Exception e) {
        	throw e;
//        	return SaveResponse.responseValue(SaveResponse.SAVE_FAIL_UNKNOWN_AUTHORITY);
        }
        
		// Add Additional user info to the webuser table
        String insertWebuserSQL = insertNewWebuserSQL;
        try {
        	// see if you can reuse the parameterSource from before...
        	getNamedParameterJdbcTemplate().update(insertWebuserSQL, userParameterSource);
        } catch (Exception e) {
        	throw e;
//        	return SaveResponse.responseValue(SaveResponse.SAVE_FAIL_UNKNOWN_USER);
        }

        // create a webuser password transaction and email the user
        WebuserPasswordTransaction webuserPasswordTransaction = new WebuserPasswordTransaction (user.getUsername(), WebuserPasswordTransaction.NEW_PASSWORD_TRANSACTION_TYPE, webuserPasswordTransactionKey, encodedPassword);
        if (!upsertWebuserPasswordTransaction(webuserPasswordTransaction)) {
        	return SaveResponse.responseValue(SaveResponse.SAVE_FAIL_UNKNOWN_PASSWORD_TRANSACTION);
        }
       
        // compose the email activation link and send it to the user....
        sendActivationEmail (user, webuserPasswordTransactionKey);
        
        // and finally return success
        return null;
        //SaveResponse.SAVE_SUCCESS.responseCode();
	}

	// send an new user activation email 
	private void sendActivationEmail(Webuser user, String webuserPasswordTransactionKey) {
        String activationLink = this.getActivationURL() + "user=" + user.getUsername() + "&token=" + webuserPasswordTransactionKey;
        System.out.println ("activationLink: " + activationLink);
//        webuserNotifier.notifyNewWebuser(user.getUsername().toLowerCase(), user.getFirstName() + " " + user.getLastName(), user.getUsername().toLowerCase(), activationLink);
		String emailAddress = user.getUsername();
        String accountOwnerName = user.getFirstName() == null ? "" : user.getFirstName() + " " + user.getLastName() == null ? "" : user.getLastName();
        accountOwnerName = accountOwnerName == null || accountOwnerName.trim().length() == 0 ? user.getUsername() : accountOwnerName;
        // send an activation email in an async thread...so the user is held loking at a dialog on his screen
		new Thread(new EmailSenderRunnable().emailNotifierService(accountOwnerName, user.getUsername(), emailAddress, activationLink, Locale.ENGLISH)).start();
//        emailNotifierService.notifyNewWebuser(accountOwnerName, user.getUsername(), emailAddress, activationLink, Locale.ENGLISH);
	}

	@Override
	public Integer resetWebuserPassword(Webuser user) {
		// make the user name all lower case.  this is simply a best place to do it.
		user.setUsername(user.getUsername().trim().toLowerCase());
		// check to see if the user exists already
		Webuser existingWebuser = findWebuserByUsername(user.getUsername());
		if (existingWebuser == null) {
			// may need to throw an exception here
			return UpdateResponse.UPDATE_FAIL_NO_USER.responseCode();
		}
		// make sure the user has a valid email address
		if (!existingWebuser.getUsername().matches(EMAIL_PATTERN) && (existingWebuser.getEmailAddress() == null || !existingWebuser.getEmailAddress().matches(EMAIL_PATTERN))) {
			return UpdateResponse.UPDATE_FAIL_NO_EMAIL.responseCode();
		}
		// encode and set the password string on the user object
		// and create a transaction token: 
//		String encodedPassword = passwordEncoder.encodePassword(user.getPassword(), DEFAULT_SALT_CODE);
//		String webuserPasswordTransactionKey = passwordEncoder.encodePassword(String.valueOf(Calendar.getInstance().getTimeInMillis()), DEFAULT_SALT_CODE);
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		String webuserPasswordTransactionKey = passwordEncoder.encode(String.valueOf(Calendar.getInstance().getTimeInMillis()));
//		System.out.println ("Password = '" + user.getPassword() + "', Encoded Password: ---" + encodedPassword + "---");
//		System.out.println ("Password = '" + user.getPassword() + "', Transaction Key: ---" + webuserPasswordTransactionKey + "---");
		user.setPassword(encodedPassword);

        // create a webuser password transaction and email the user
        WebuserPasswordTransaction webuserPasswordTransaction = new WebuserPasswordTransaction (user.getUsername(), WebuserPasswordTransaction.RESET_PASSWORD_TRANSACTION_TYPE, webuserPasswordTransactionKey, encodedPassword);
        if (!upsertWebuserPasswordTransaction(webuserPasswordTransaction)) {
        	return SaveResponse.SAVE_FAIL_UNKNOWN_PASSWORD_TRANSACTION.responseCode();
        }
       
        // compose the email activation link and send it to the user....
        sendPasswordResetEmail (existingWebuser, webuserPasswordTransactionKey);
        
        // and finally return success
        return SaveResponse.SAVE_SUCCESS.responseCode();
	}

	// send an new user activation email 
	private void sendPasswordResetEmail(Webuser user, String webuserPasswordTransactionKey) {
		// always use user name for email.  Only use email address for when the username is not a email address
		String emailAddress = user.getUsername();
		if (!emailAddress.matches(EMAIL_PATTERN)) {
			emailAddress = user.getEmailAddress();
		}
        String passwordResetLink = this.getPasswordResetURL() + "user=" + user.getUsername() + "&token=" + webuserPasswordTransactionKey;
//        System.out.println ("passwordResetLink: " + passwordResetLink);
//        webuserNotifier.notifyResetWebuser(emailAddress, user.getFirstName() == null ? "" : user.getFirstName() + " " + 
//        					user.getLastName() == null ? "" : user.getLastName(), user.getUsername().toLowerCase(), passwordResetLink);

        String accountOwnerName = user.getFirstName() == null ? "" : user.getFirstName() + " " + user.getLastName() == null ? "" : user.getLastName();
        accountOwnerName = accountOwnerName == null || accountOwnerName.trim().length() == 0 ? user.getUsername() : accountOwnerName;
        // send an activation email in an async thread...so the user is held loking at a dialog on his screen
		new Thread(new EmailSenderRunnable().notifyResetPassword(accountOwnerName, user.getUsername(), emailAddress, passwordResetLink, Locale.ENGLISH)).start();
	}

	@Override
	public int activateUser(String username, String activationToken) {
		// Only activate if the token code matches...
		if (activationToken != null && activationToken.trim().length() > 0) {
			// get the transaction code from the database
	        WebuserPasswordTransaction transaction = findWebuserPasswordTransactionByUsernameAndType (username, WebuserPasswordTransaction.NEW_PASSWORD_TRANSACTION_TYPE);
	        if (transaction != null && transaction.getTransactionKey() != null && transaction.getTransactionKey().trim().equals(activationToken.trim())) {
	        	enableWebuserWithName(username);
	        	deleteWebuserPasswordTransaction(transaction);
	        	return 0;
	        } else {
	        	return -1;
	        }
		}
		return -1;
	}
	
	@Override
	public int resetUserPassword(String username, String activationToken) {
		// Only activate if the token code matches...
		if (activationToken != null && activationToken.trim().length() > 0) {
			// get the transaction code from the database
	        WebuserPasswordTransaction transaction = findWebuserPasswordTransactionByUsernameAndType (username, WebuserPasswordTransaction.RESET_PASSWORD_TRANSACTION_TYPE);
	        try {
		        if (transaction != null && transaction.getTransactionKey() != null && transaction.getTransactionKey().trim().equals(activationToken.trim()) &&
		        		transaction.getNewPassword() != null) {
		        	updateUserPassword(username, transaction.getNewPassword());
		        	deleteWebuserPasswordTransaction(transaction);
		        	return 0;
		        } else {
		        	return -1;
		        }
	        } catch (Exception e) {
	        	// if any exceptions happen (including sql), indicate failure to the caller
	        	return -1;
	        }
		}
		return -1;
	}

	@Override
	public Integer updateEncodedPassword(String username, String newPassword) {
		try {
			if (username == null || username.trim().length() == 0 || newPassword == null || newPassword.trim().length() == 0) {
				throw new Exception("Bad Request");
			}
			String encodedPassword = passwordEncoder.encode(newPassword);
			updateUserPassword(username, encodedPassword);
	        return 0;
		} catch (Exception e) {
        	// if any exceptions happen (including sql), indicate failure to the caller
        	return -1;
		}
	}

	private void updateUserPassword(String username, String newPassword) {
        String sql = resetUserPasswordSQL;
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("username", username);
        args.put("password", newPassword);
        getNamedParameterJdbcTemplate().update(sql, args);
	}

	// private because no one out side of user context would call this method
	private boolean upsertWebuserPasswordTransaction (WebuserPasswordTransaction transaction) {
		SqlParameterSource transactionParameterSource = new BeanPropertySqlParameterSource(transaction);
		String upsertNewTransactionSql = upsertWebuserPasswordTransactionSQL;
        try {
        	getNamedParameterJdbcTemplate().update(upsertNewTransactionSql, transactionParameterSource);
        } catch (Exception e) {
        	return false;
        }
        return true;
	}
	
	// private because no one out side of user context would call this method
	private WebuserPasswordTransaction findWebuserPasswordTransactionByUsernameAndType (String username, String transactionType) {
        String sql = findWebuserPasswordTransactionByUsernameAndTypeSQL;
        BeanPropertyRowMapper<WebuserPasswordTransaction> transactionRowMapper = BeanPropertyRowMapper.newInstance(WebuserPasswordTransaction.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("username", username);
        args.put("transactionType", transactionType);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        WebuserPasswordTransaction transaction = null;
        try {
        	transaction = getNamedParameterJdbcTemplate().queryForObject(sql, args, transactionRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return transaction;
	}
	
	// private because no one out side of user context would call this method
	private boolean deleteWebuserPasswordTransaction (WebuserPasswordTransaction transaction) {
		SqlParameterSource transactionParameterSource = new BeanPropertySqlParameterSource(transaction);
		String deleteTransactionSql = deleteWebuserPasswordTransactionByUsernameAndTypeSQL;
        try {
        	getNamedParameterJdbcTemplate().update(deleteTransactionSql, transactionParameterSource);
        } catch (Exception e) {
        	return false;
        }
        return true;
	}
	
	private void activateUser(String userName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer updateWebuser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enableWebuserWithName(String username) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("enabled", new Integer(1));
        args.put("username", username);
        String sql = enableUsernameSQL;
        getNamedParameterJdbcTemplate().update(sql, args);
	}

	@Override
	public void enableWebuserWithId(Long idUser) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("enabled", new Integer(1));
        args.put("idUser", idUser);
        String sql = enableUserIdSQL;
        getNamedParameterJdbcTemplate().update(sql, args);
	}

	@Override
	public void disableWebuserWithName(String username) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("enabled", new Integer(0));
        args.put("username", username);
        String sql = enableUsernameSQL;
        getNamedParameterJdbcTemplate().update(sql, args);
	}

	@Override
	public void disableWebuserWithId(Long idUser) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("enabled", new Integer(0));
        args.put("idUser", idUser);
        String sql = enableUserIdSQL;
        getNamedParameterJdbcTemplate().update(sql, args);
	}


	@Override
	public User findByUserId(Long idUser) {
		return JdbcDaoStaticHelper.findUserByUserId(getNamedParameterJdbcTemplate(), idUser);
	}

	@Override
	public User findByUserIdWithDetails(Long idUser) {
		return JdbcDaoStaticHelper.findUserByUserId(getNamedParameterJdbcTemplate(), idUser, true);
	}

//	@Override
//	public User findByUsernameForLogin(String username) {
//		// Although this should be as simle as this, we have to go through some hoops because our User which extends 
//		// org.springframework.security.core.userdetails.User does not have a setUsername() and setPassword() methods.
////        String sql = findByUsernameForLoginSQL;
////        BeanPropertyRowMapper<User> webuserRowMapper = BeanPropertyRowMapper.newInstance(User.class);
////        Map<String, Object> args = new HashMap<String, Object>();
////        args.put("username", username);
////		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
////        User user = null;
////        try {
////        	user = getNamedParameterJdbcTemplate().queryForObject(sql, args, webuserRowMapper);
////        } catch (IncorrectResultSizeDataAccessException e) {}
////        return user;
//		User u = JdbcDaoStaticHelper.findUserByUsernameForLogin(getNamedParameterJdbcTemplate(), username);
//		return u;
//	}

	@Override
	public User findByUsername(String username) {
		User u = JdbcDaoStaticHelper.findUserByUsername(getNamedParameterJdbcTemplate(), username);
		return u;
	}

	@Override
	public User findByUsernameWithDetails(String username) {
		User u = JdbcDaoStaticHelper.findUserByUsername(getNamedParameterJdbcTemplate(), username, true);
		return u;
	}

	@Override
	public Webuser findWebuserByUsername(String username) {
        String sql = findWebuserByUsernameSQL;
        BeanPropertyRowMapper<Webuser> webuserRowMapper = BeanPropertyRowMapper.newInstance(Webuser.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("username", username);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Webuser user = null;
        try {
        	user = getNamedParameterJdbcTemplate().queryForObject(sql, args, webuserRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return user;
	}

//	@Override
//	public User findCurrentUserDetails() {
//		User user = JdbcDaoStaticHelper.findCurrentUser(getNamedParameterJdbcTemplate());
//		return user == null ? new User() : user;
//	}

	@Override
	public List<User> findAll() {
        String sql = "SELECT * FROM user ORDER BY id_user ASC";
        BeanPropertyRowMapper<User> userRowMapper = BeanPropertyRowMapper.newInstance(User.class);
        List<User> users = getNamedParameterJdbcTemplate().query(sql, userRowMapper);
        return users;
	}

	
	
	
	/**
	 * This class is used to spin off a email when a user performs an act that will require 
	 * a communication back to the user.  
	 * The reason this is a Runnable class is cause we do not want to hold up the user after 
	 * he sends the email.  We simply kick off a task thread and respond back to user.   
	 *
	 * @author sesi
	 */
	class EmailSenderRunnable implements Runnable {
		private static final String NOTIFICATION_ACTION_RESET_PASSWORD = "RESET_PASSWORD";
		private static final String NOTIFICATION_ACTION_NEW_ACCOUNT = "NEW_ACCOUNT";
		String accountOwnerName;
		String username;
		String emailAddress;
		String link;
		Locale locale;
		String action;
		
		EmailSenderRunnable notifyResetPassword (String accountOwnerName,
				String username, String emailAddress, String passwordResetLink,
				Locale locale) {
			this.accountOwnerName = accountOwnerName;
			this.username = username;
			this.emailAddress = emailAddress;
			this.link = passwordResetLink;
			this.locale = locale;
			this.action = NOTIFICATION_ACTION_RESET_PASSWORD;
			return this;
		}
		
		EmailSenderRunnable emailNotifierService(String accountOwnerName,
				String username, String emailAddress, String activationLink,
				Locale locale) {
			this.accountOwnerName = accountOwnerName;
			this.username = username;
			this.emailAddress = emailAddress;
			this.link = activationLink;
			this.locale = locale;
			this.action = NOTIFICATION_ACTION_NEW_ACCOUNT;
			return this;
		}
		
		public void run() {
			if (this.action.equals(NOTIFICATION_ACTION_RESET_PASSWORD)) {
				getEmailNotifierService().notifyResetPassword(this.accountOwnerName, this.username, this.emailAddress, this.link, this.locale);
		        // BAD stuff. delete soon
//		        emailNotifierService.notifyNewWebuser(accountOwnerName, this.username, this.emailAddress, this.link, this.locale);
			} else {
				getEmailNotifierService().notifyNewWebuser(accountOwnerName, this.username, this.emailAddress, this.link, this.locale);
			}
		}
	}

	/**
	 * This class is used to spin off a runnable thread that will send an email when a user 
	 * seeks to send an email from the ContactUs widget. 
	 * The reason this is a Runnable class is cause we do not want to hold up the user after 
	 * he sends the email.  We simply kick off a task thread and respond back to user.   
	 *
	 * @author sesi
	 */
	class EmailSubmittedRunnable implements Runnable {
		SendEmailData emailPayload;
		String recepientEmailAddresses;
		int action = 0;
		
		EmailSubmittedRunnable processSubmittedEmail (SendEmailData emailPayload, String recepientEmailAddresses) {
			this.emailPayload = emailPayload;
			this.recepientEmailAddresses = recepientEmailAddresses;
			return this;
		}
		
		public void run() {
			emailNotifierService.processSubmittedEmail(emailPayload, this.recepientEmailAddresses);
		}
	}

	
	
	/***********************************************************************************************************************************
	/ Read Site Settings stuff.  
	/***********************************************************************************************************************************/
	// Need to relocate this stuff into a Dao....started this effort (SiteSettingsDao).  But not sure how I can implement that.
	@Override
	public String getSiteSetting(String settingName) {
		return JdbcDaoStaticHelper.getSiteSetting(getNamedParameterJdbcTemplate(), settingName);
	}
	
	
	/***********************************************************************************************************************************
	/ Upload Users Stuff.  
	/***********************************************************************************************************************************/
	
	
	@Override
	public List<UploadUserResponse> uploadBatchUsers(List<User> uploadUserList) {
		// this action will be performed in the most monotonous (inefficient) fashion.....one user at a time
		if (uploadUserList == null || uploadUserList.size() == 0) {
			// nothing to do
			return null;
		}

		List<UploadUserResponse> uploadUserResponseList = new ArrayList<UploadUserResponse>();
		for (User uploadUser : uploadUserList) {
			// is user already in the database?  
			User databaseUser = findByUsername(uploadUser.getUsername().toLowerCase());
			if (databaseUser == null) {
				// New user....so we follow these steps
				// 1.) Create the new user account (and the get the new id_user)
				// 2.) Create the new Webuser (extended user attributes) record
				// 3.) Create any new Authorities records
				// 4.) Create any channel subscriptions records (set the id_user on these records before insert/update)

				// insert the user, but get the id_user back on completing insert operation
	            SqlParameterSource userParameterSource = new BeanPropertySqlParameterSource(uploadUser);
	            KeyHolder keyHolder = new GeneratedKeyHolder();
	            getNamedParameterJdbcTemplate().update(UserDao.insertNewUserSQL, userParameterSource, keyHolder, new String[]{"id_user"});
	            // id for the new user object
	            Long newIdUser = keyHolder.getKey().longValue();
	            // set the key on the uploadUser Object
	            uploadUser.setIdUser(newIdUser);
	            
	            // now insert the webuser 
	            if (uploadUser.getWebuser() != null) {
		            SqlParameterSource webuserParameterSource = new BeanPropertySqlParameterSource(uploadUser.getWebuser());
		            getNamedParameterJdbcTemplate().update(UserDao.insertNewWebuserSQL, webuserParameterSource);
	            }
	            
	            // now insert the authorities...
	            if (uploadUser.getAuthorities() != null && uploadUser.getAuthorities().size() > 0) {
		            List<SqlParameterSource> insertAuthoritiesParameters = new ArrayList<SqlParameterSource>();
		            // note the use of MapSqlParameterSource here (we usually use BeanPropertySqlParameterSource)
		            for (GrantedAuthority authority : uploadUser.getAuthorities()) {
		            	MapSqlParameterSource mapSource = new MapSqlParameterSource();
		            	mapSource.addValue("username", uploadUser.getUsername());
		            	mapSource.addValue("authority", authority.getAuthority());
		            	insertAuthoritiesParameters.add(mapSource);
		            }
		            if (insertAuthoritiesParameters.size() > 0) {
		                getNamedParameterJdbcTemplate().batchUpdate(UserDao.insertAuthoritiesSql, insertAuthoritiesParameters.toArray(new SqlParameterSource[0]));
		            }
	            }
	            
	            // now insert any channel subscriptions
	            if (uploadUser.getChannelSubscriptions() != null && uploadUser.getChannelSubscriptions().size() > 0) {
		            List<SqlParameterSource> insertChannelSubscriptionsParameters = new ArrayList<SqlParameterSource>();
		            for (ChannelSubscription channelSubscription : uploadUser.getChannelSubscriptions()) {
		            	channelSubscription.setIdStudent(newIdUser);
		            	insertChannelSubscriptionsParameters.add(new BeanPropertySqlParameterSource(channelSubscription));
		            }
		            if (insertChannelSubscriptionsParameters.size() > 0) {
		                getNamedParameterJdbcTemplate().batchUpdate(UserDao.insertStudentChannelSubscriptionsSQL, insertChannelSubscriptionsParameters.toArray(new SqlParameterSource[0]));
		            }
	            }
	            // now insert any tests
	            if (uploadUser.getTests() != null && uploadUser.getTests().size() > 0) {
	            	List<Usertest> fullUsertestList = addExamsForUser (uploadUser.getIdUser(), uploadUser.getTests(), getNamedParameterJdbcTemplate());
	            	uploadUser.setTests(fullUsertestList);
	            }
	            
	            // now insert any organization_student records
	            if (uploadUser.getIdOrganizationsList() != null && uploadUser.getIdOrganizationsList().size() > 0) {
	            	for (Long idOrganization : uploadUser.getIdOrganizationsList()) {
		            	MapSqlParameterSource mapSource = new MapSqlParameterSource();
		            	mapSource.addValue("idOrganization", idOrganization);
		            	mapSource.addValue("idStudent", newIdUser);
		                getNamedParameterJdbcTemplate().update(UserDao.insertOrganizationStudentSQL, mapSource);
	            	}
	            }
	            uploadUserResponseList.add(new UploadUserResponse(uploadUser, true));

			} else {
				// Existing user (pain in the rear end case)....so we follow these steps
				// 1.) Update metadata on the existing user record
				// 2.) Update metadata on the existing Webuser (extended user attributes) record
				// 3.) Create any new Authorities records
				// 4.) Merge any channel subscriptions records with existing ones 
				// pain in the rear end case
				uploadUser.setIdUser(databaseUser.getIdUser());
				// update user metadata 
	            SqlParameterSource userParameterSource = new BeanPropertySqlParameterSource(uploadUser);
	            getNamedParameterJdbcTemplate().update(UserDao.updateUserMetadataSQL, userParameterSource);
	            
				// update/insert webuser metadata 
				if (uploadUser.getWebuser() != null) {
					Webuser databaseWebuser = findWebuserForLoad(uploadUser.getUsername());
					if (databaseWebuser == null) {
			            SqlParameterSource webuserParameterSource = new BeanPropertySqlParameterSource(uploadUser.getWebuser());
			            getNamedParameterJdbcTemplate().update(UserDao.insertNewWebuserSQL, webuserParameterSource);
					} else {
						// do I merge the new data with existing database data?  Not doing it for now
						SqlParameterSource webuserParameterSource = new BeanPropertySqlParameterSource(uploadUser.getWebuser());
						getNamedParameterJdbcTemplate().update(UserDao.updateWebuserMetadataSQL, webuserParameterSource);
					}
				}
	            // now insert the authorities...note that we may want to merge Authorities.  We however, simply delete old ones and insert new ones in this message 
	            if (uploadUser.getAuthorities() != null && uploadUser.getAuthorities().size() > 0) {
	            	// Delete any existing authorities
	                Map<String, Object> args = new HashMap<String, Object>();
	                args.put("username", uploadUser.getUsername());
	                getNamedParameterJdbcTemplate().update(UserDao.deleteAllAuthoritiesSql, args);
	                // add the new set of authorities
		            List<SqlParameterSource> insertAuthoritiesParameters = new ArrayList<SqlParameterSource>();
		            // note the use of MapSqlParameterSource here (we usually use BeanPropertySqlParameterSource)
		            for (GrantedAuthority authority : uploadUser.getAuthorities()) {
		            	MapSqlParameterSource mapSource = new MapSqlParameterSource();
		            	mapSource.addValue("username", uploadUser.getUsername());
		            	mapSource.addValue("authority", authority.getAuthority());
		            	insertAuthoritiesParameters.add(mapSource);
		            }
		            if (insertAuthoritiesParameters.size() > 0) {
		                getNamedParameterJdbcTemplate().batchUpdate(UserDao.insertAuthoritiesSql, insertAuthoritiesParameters.toArray(new SqlParameterSource[0]));
		            }
	            }

	            // now insert any channel subscriptions
	            if (uploadUser.getChannelSubscriptions() != null && uploadUser.getChannelSubscriptions().size() > 0) {
	            	JdbcDaoStaticHelper.updateChannelSubscriptionsForUser (databaseUser.getIdUser(), uploadUser.getChannelSubscriptions(), getNamedParameterJdbcTemplate());
	            }
	            
	            // now insert any tests
	            if (uploadUser.getTests() != null && uploadUser.getTests().size() > 0) {
	            	List<Usertest> fullUsertestList = addExamsForUser (uploadUser.getIdUser(), uploadUser.getTests(), getNamedParameterJdbcTemplate());
	            	uploadUser.setTests(fullUsertestList);
	            }
	            
	            // now insert any organization_student records
	            if (uploadUser.getIdOrganizationsList() != null && uploadUser.getIdOrganizationsList().size() > 0) {
	            	for (Long idOrganization : uploadUser.getIdOrganizationsList()) {
		            	MapSqlParameterSource mapSource = new MapSqlParameterSource();
		            	mapSource.addValue("idOrganization", idOrganization);
		            	mapSource.addValue("idStudent", databaseUser.getIdUser());
		                getNamedParameterJdbcTemplate().update(UserDao.insertOrganizationStudentSQL, mapSource);
	            	}
	            }
	            uploadUserResponseList.add(new UploadUserResponse(uploadUser, false));
			}
		}
		return uploadUserResponseList;
	}
	
	/**
	 * Channel subscriptions update can come from multiple channels.  Uploader when we upload new users.  Or Web Subscriptions (Activation Code) from the frontend.  
	 * @param idUser
	 * @param channelSubscriptions
	 * @param namedParameterJdbcTemplate
	 */
	public List<Usertest> addExamsForUser(Long idUser, List<Usertest> usertests, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		List<Usertest> fullUsertestList = new ArrayList<Usertest>();
        if (usertests != null && usertests.size() > 0) {
            for (Usertest usertest : usertests) {
            	// set the student if on the new channelSubscription record
            	usertest.setIdUser(idUser);
            	// cheat here and add exams using the JdbcAbiptestresponseDao functionality...(create Usertest in with ProfileId 0)
            	Usertest fullUsertest = JdbcDaoStaticHelper.createAndGetUsertest(namedParameterJdbcTemplate, idUser, usertest.getIdTest(), usertest.getTestAssignmentDate(), 0l, false);
            	fullUsertestList.add(fullUsertest);
            }
        }
        return fullUsertestList;
	}

	private Webuser findWebuserForLoad(String username) {
        String sql = findWebuserForLoadSQL;
        BeanPropertyRowMapper<Webuser> webuserRowMapper = BeanPropertyRowMapper.newInstance(Webuser.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("username", username);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Webuser webuser = null;
        try {
        	webuser = getNamedParameterJdbcTemplate().queryForObject(sql, args, webuserRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return webuser;
	}

	/**
	 * Functionality that allows to send (submit) emails to eTester.
	 */
	@Override
	public void sendEmailMessage(SendEmailData emailPayload) {
		new Thread(new EmailSubmittedRunnable().processSubmittedEmail(emailPayload, getMessageSubmittedReceiver())).start();
	}

	/**
	 * Returns any current user messages/notifications.
	 */
	@Override
	public List<Usermessage> getAllUsermessagesForCurrentUser() {
		Long loggedinUserId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinUserId != null) {
			return getAllUsermessagesForUser(loggedinUserId);
		}
		return null;
	}

	private List<Usermessage> getAllUsermessagesForUser(Long idUser) {
        String sql = getAllUsermessagesForUserSQL;
        BeanPropertyRowMapper<Usermessage> usermessageRowMapper = BeanPropertyRowMapper.newInstance(Usermessage.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUser", idUser);
        List<Usermessage> usermessages = getNamedParameterJdbcTemplate().query(sql, args, usermessageRowMapper);
        return usermessages;
	}

	@Override
	public void updateUsermessageStatus(Long idUsermessage,
			Integer usermessageStatusInt) {
		String sql = null;
		if (usermessageStatusInt == UsermessageStatus.ACKNOWLEDGED_STATUS.intValue()) {
			sql = updateUsermessageStatusToAcknowledgedSQL;
		} else if (usermessageStatusInt == UsermessageStatus.EXPIRED_STATUS.intValue() || usermessageStatusInt == UsermessageStatus.DELETED_STATUS.intValue()) {
			sql = updateUsermessageStatusToDeletedSQL;
		} else {
			
		}
		if (sql != null) {
	        Map<String, Object> args = new HashMap<String, Object>();
	        args.put("idUsermessage", idUsermessage);
	        getNamedParameterJdbcTemplate().update(sql, args);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// User Alerts Functionality
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public List<UserAlert> getUnpublishedUserAlertsForCurrentProvider() {
		User provider = JdbcDaoStaticHelper.findCurrentUser(getNamedParameterJdbcTemplate());
		if (provider != null) {
			// see if user/provider has provider authority
			if (provider.getAuthorities() != null && provider.getAuthorities().contains (UserDao.ROLE_PROVIDER)) {
				return getUnpublishedUserAlerts(provider.getIdUser());
			}
		}
		return null;
	}

	@Override
	public List<UserAlert> getUnpublishedUserAlerts(Long idProvider) {
        String sql = "SELECT * FROM user_alert WHERE id_provider = :idProvider AND published != 1";
        BeanPropertyRowMapper<UserAlert> userAlertRowMapper = BeanPropertyRowMapper.newInstance(UserAlert.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idProvider", idProvider);
        List<UserAlert> userAlerts = getNamedParameterJdbcTemplate().query(sql, args, userAlertRowMapper);
        return userAlerts;
	}

	@Override
	public UserAlert getUserAlertForCurrentProvider(Long idUserAlert) {
		User provider = JdbcDaoStaticHelper.findCurrentUser(getNamedParameterJdbcTemplate());
		if (provider != null) {
			// see if user/provider has provider authority
			if (provider.getAuthorities() != null && provider.getAuthorities().contains (UserDao.ROLE_PROVIDER)) {
				return getUserAlert(provider.getIdUser(), idUserAlert);
			}
		}
		return null;
	}
	@Override
	public UserAlert getUserAlert(Long idProvider, Long idUserAlert) {
		if (idProvider != null) {
	        BeanPropertyRowMapper<UserAlert> userAlertRowMapper = BeanPropertyRowMapper.newInstance(UserAlert.class);
            String getUserAlertSql = "SELECT * FROM user_alert WHERE id_provider = :idProvider AND id_user_alert = :idUserAlert ";
	        Map<String, Object> args = new HashMap<String, Object>();
	        args.put("idProvider", idProvider);
	        args.put("idUserAlert", idUserAlert);
            UserAlert userAlert = null;
            try {
            	userAlert = getNamedParameterJdbcTemplate().queryForObject(getUserAlertSql, args, userAlertRowMapper);
            } catch (IncorrectResultSizeDataAccessException e) {
            	return null;
            }
            return userAlert;
		}
		return null;
	}

	@Override
	public UserAlert saveOrUpdateUserAlertForCurrentProvider (UserAlert userAlert) {
		User provider = JdbcDaoStaticHelper.findCurrentUser(getNamedParameterJdbcTemplate());
		if (provider != null) {
			// see if user/provider has provider authority
			if (provider.getAuthorities() != null && provider.getAuthorities().contains (UserDao.ROLE_ADMIN)) {
				return saveOrUpdateUserAlert(provider.getIdUser(), userAlert);
			}
		}
		return null;
	}
	@Override
	public UserAlert saveOrUpdateUserAlert(Long idProvider, UserAlert userAlert) {
		if (idProvider == null || !idProvider.equals(userAlert.getIdProvider())) {
			// provider mismatch
			return null;
		}
		userAlert.setIdProvider(idProvider);
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(userAlert);
        Long idUserAlert = null;
		if (userAlert.getIdUserAlert() == null || userAlert.getIdUserAlert().equals(0l)) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
	        getNamedParameterJdbcTemplate().update(insertUserAlertSQL, parameterSource, keyHolder, new String[]{"ID_USER_ALERT"});
            // id for the new test UserAlert
            idUserAlert = keyHolder.getKey().longValue();
		} else {
			idUserAlert = userAlert.getIdUserAlert();
	        getNamedParameterJdbcTemplate().update(upsertUserAlertSQL, parameterSource);
		}
		UserAlert savedUserAlert = getUserAlert(idProvider, idUserAlert);
		// Publish the user alert if it is selected for publishing
		if (savedUserAlert.getPublished() != null && savedUserAlert.getPublished().equals(1)) {
			this.getUserAlertDao().publishUserAlert(savedUserAlert);
		}
		return savedUserAlert; 
	}

	@Override
	public Integer deleteUserAlertForCurrentProvider(Long idUserAlert) {
		User provider = JdbcDaoStaticHelper.findCurrentUser(getNamedParameterJdbcTemplate());
		if (provider != null) {
			// see if user/provider has provider authority
			if (provider.getAuthorities() != null && provider.getAuthorities().contains (UserDao.ROLE_PROVIDER)) {
				return deleteUserAlert(provider.getIdUser(), idUserAlert);
			}
		}
		return -1;
	}

	@Override
	public Integer deleteUserAlert(Long idProvider, Long idUserAlert) {
		if (idProvider != null) {
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("idUserAlert", idUserAlert);
            args.put("idProvider", idProvider);
            String deleteUserAlertSql = "DELETE FROM user_alert WHERE id_user_alert = :idUserAlert AND id_provider = :idProvider ";
	        try {
	            getNamedParameterJdbcTemplate().update(deleteUserAlertSql, args);
	        } catch (Exception e) {
	        	return -2;
	        }
	        return 0;
		}
		return -1;
	}

	@Override
	public List<User> getUsersForCurrentProvider() {
		return findAll();
	}
	@Override
	public boolean doesUserExistByUsername(String username) {
		return JdbcDaoStaticHelper.existsUserByUserName(getNamedParameterJdbcTemplate(), username);
	}

	@Override
	public boolean doesUserExistByUserId(Long idUser) {
		return JdbcDaoStaticHelper.existsUserByUserId(getNamedParameterJdbcTemplate(), idUser);
	}
	
}
