package com.etester.data.domain.test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.etester.data.domain.admin.Authority;
import com.etester.data.domain.admin.Providerstudent;
import com.etester.data.domain.admin.SiteSettings;
import com.etester.data.domain.content.ChannelSubscription;
import com.etester.data.domain.content.DerivedSectionQuestion;
import com.etester.data.domain.content.additional.wordlist.WlPassage;
import com.etester.data.domain.content.additional.wordlist.WlWord;
import com.etester.data.domain.content.additional.wordlist.WlWordlist;
import com.etester.data.domain.content.additional.wordlist.WlWordlistDao;
import com.etester.data.domain.content.core.Answer;
import com.etester.data.domain.content.core.Channel;
import com.etester.data.domain.content.core.Level;
import com.etester.data.domain.content.core.LevelDao;
import com.etester.data.domain.content.core.Question;
import com.etester.data.domain.content.core.QuestionDao;
import com.etester.data.domain.content.core.Questionset;
import com.etester.data.domain.content.core.Section;
import com.etester.data.domain.content.core.SectionDao;
import com.etester.data.domain.content.core.Skill;
import com.etester.data.domain.content.core.SkillDao;
import com.etester.data.domain.content.core.Topic;
import com.etester.data.domain.content.core.TopicDao;
import com.etester.data.domain.profile.ProfileDao;
import com.etester.data.domain.profile.Profilesegment;
import com.etester.data.domain.profile.Profiletest;
import com.etester.data.domain.profile.Userprofile;
import com.etester.data.domain.test.instance.Usertest;
import com.etester.data.domain.test.instance.UsertestDao;
import com.etester.data.domain.user.Gradeskill;
import com.etester.data.domain.user.Mysection;
import com.etester.data.domain.user.Permission;
import com.etester.data.domain.user.User;
import com.etester.data.domain.user.UserDao;
import com.etester.data.domain.user.Usergroup;
import com.etester.data.domain.user.UsergroupDao;
import com.etester.data.domain.user.Usergroupmember;
import com.etester.data.domain.user.Webuser;
import com.etester.data.domain.util.QuestionCompareConstants;
import com.etester.data.domain.util.UpdateStatusBean;

public class JdbcDaoStaticHelper {
	
	static final Logger logger = Logger.getAnonymousLogger(); 	
	
	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.

	public static String ANONYMOUS_USER_NAME = "anonymousUser";
	public static Long ANONYMOUS_USER_ID = 999999l;
	
	/**************************************************************************************************************
	 * I will be gathering all the methods I am seeing being used below this line
	 *************************************************************************************************************/
	/**
	 * A method to retrieve User from the database.  Note that I am no longer using User to mean the Security Authentication user as per 
	 * Spring Standards, hence, no longer extend "org.springframework.security.core.userdetails.User"
	 * @param namedParameterJdbcTemplate
	 * @param username
	 * @return
	 */
	public static User findUserByUsername(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String username) {
		return findUserByUsername(namedParameterJdbcTemplate, username, false);
	}
	public static User findUserByUsername(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String username, boolean withDetails) {
		User user = findUserByUsernameForLogin(namedParameterJdbcTemplate, username);
		if (user != null && withDetails) {
			// Set Authorities (Roles)
			user.setAuthorities(getAuthoritiesForUser (namedParameterJdbcTemplate, user.getUsername()));
			// Set Permissions (in a traditional sense, this is an authority.  but since I already use authority to mean role....here)
			user.setPermissions(getPermissionsForUser (namedParameterJdbcTemplate, user.getUsername()));
			// If the user has an Organization, then associate the Organization's channle's to the user.  Typically providers are associated 
			// with Organizations and hence will have this populated
			if (user.getIdOrganization() != null) {
				user.setChannels(getChannelsForOrganization (namedParameterJdbcTemplate, user.getIdOrganization()));
			}
			// Does the user have any subscriptions for student channels.  Typical for a Student.  
			user.setSubscriptions(getSubscriptionChannelsForUser (namedParameterJdbcTemplate, user.getIdUser()));
			// Does the user have any user tests assigned?
			user.setTests(findAllUsertestsForUsername(namedParameterJdbcTemplate, user.getUsername()));		}
        return user;
	}

	public static List<Usertest> findAllUsertestsForUsername(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String username) {
        String sql = UsertestDao.findAllUsertestsForUserNameSQL;
        BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("username", username);
        List<Usertest> tests = namedParameterJdbcTemplate.query(sql, args, usertestRowMapper);
        return tests;
	}
	
	public static User findUserByUserId(NamedParameterJdbcTemplate namedParameterJdbcTemplate, Long idUser) {
		return findUserByUserId(namedParameterJdbcTemplate, idUser, false);
	}
	public static User findUserByUserId(NamedParameterJdbcTemplate namedParameterJdbcTemplate, Long idUser, boolean withDetails) {
		User user = findUserByUserIdForLogin(namedParameterJdbcTemplate, idUser);
		if (user != null && withDetails) {
			// Set Authorities (Roles)
			user.setAuthorities(getAuthoritiesForUser (namedParameterJdbcTemplate, user.getUsername()));
			// Set Permissions (in a traditional sense, this is an authority.  but since I already use authority to mean role....here)
			user.setPermissions(getPermissionsForUser (namedParameterJdbcTemplate, user.getUsername()));
			// If the user has an Organization, then associate the Organization's channle's to the user.  Typically providers are associated 
			// with Organizations and hence will have this populated
			if (user.getIdOrganization() != null) {
				user.setChannels(getChannelsForOrganization (namedParameterJdbcTemplate, user.getIdOrganization()));
			}
			// Does the user have any subscriptions for student channels.  Typical for a Student.  
			user.setSubscriptions(getSubscriptionChannelsForUser (namedParameterJdbcTemplate, user.getIdUser()));
		}
        return user;
	}

	/**
	 * findUserByUsernameForLogin returns a User with a metadata filled in.  See the UserDao.findByUsernameWithOrganizationSQL for details.
	 * @param namedParameterJdbcTemplate
	 * @param username
	 * @return
	 */
	public static User findUserByUsernameForLogin(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String username) {
        String sql = UserDao.findByUsernameWithOrganizationSQL;
        BeanPropertyRowMapper<User> userRowMapper = BeanPropertyRowMapper.newInstance(User.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("username", username);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        User user = null;
        try {
        	user = namedParameterJdbcTemplate.queryForObject(sql, args, userRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return user;
	}

	/**
	 * findUserByUsernameForLogin returns a User with a metadata filled in.  See the UserDao.findByUsernameWithOrganizationSQL for details.
	 * @param namedParameterJdbcTemplate
	 * @param username
	 * @return
	 */
	public static User findUserByUserIdForLogin(NamedParameterJdbcTemplate namedParameterJdbcTemplate, Long idUser) {
        String sql = UserDao.findByUserIdWithOrganizationSQL;
        BeanPropertyRowMapper<User> userRowMapper = BeanPropertyRowMapper.newInstance(User.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUser", idUser);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        User user = null;
        try {
        	user = namedParameterJdbcTemplate.queryForObject(sql, args, userRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return user;
	}

	/**
	 * Get the Authorities (roles) for the User
	 * @param namedParameterJdbcTemplate
	 * @param username
	 * @return
	 */
	private static List<Authority> getAuthoritiesForUser(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String username) {
		String sql = UserDao.findAllAuthoritiesByUsernameSQL;
		BeanPropertyRowMapper<Authority> authorityRowMapper = BeanPropertyRowMapper.newInstance(Authority.class);
		Map<String, Object> args = new HashMap<String, Object>();
        args.put("username", username);
		List<Authority> authorities = namedParameterJdbcTemplate.query(sql, args, authorityRowMapper);
		return authorities;
	}

	/**
	 * Get the Permissions (roles) for the User
	 * @param namedParameterJdbcTemplate
	 * @param username
	 * @return
	 */
	private static List<String> getPermissionsForUser(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String username) {
		ArrayList<String> permissionsList = new ArrayList<String>();
		String sql = UserDao.findAllPermissionsByUsernameSQL;
		BeanPropertyRowMapper<Permission> permissionRowMapper = BeanPropertyRowMapper.newInstance(Permission.class);
		Map<String, Object> args = new HashMap<String, Object>();
        args.put("username", username);
		List<Permission> permissions = namedParameterJdbcTemplate.query(sql, args, permissionRowMapper);
		if (permissions != null) {
			for (Permission permission : permissions) {
				permissionsList.add(permission.getPrivilege());
			}
		}
		return permissionsList;
	}

	/**
	 * Get the Channel subscriptions for user
	 * @param namedParameterJdbcTemplate
	 * @param idUser
	 * @return
	 */
	private static List<Channel> getSubscriptionChannelsForUser(NamedParameterJdbcTemplate namedParameterJdbcTemplate, Long idUser) {
		List<Channel> channels= null;
		String sql = UserDao.findSubscriptionChannelsForUserIdSQL;
		BeanPropertyRowMapper<Channel> channelRowMapper = BeanPropertyRowMapper.newInstance(Channel.class);
		Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUser", idUser);
		channels = namedParameterJdbcTemplate.query(sql, args, channelRowMapper);
		return channels;
	}

	/**
	 * Get the Channels associated with the Organization.
	 * @param namedParameterJdbcTemplate
	 * @param idOrganization
	 * @return
	 */
	private static List<Channel> getChannelsForOrganization(NamedParameterJdbcTemplate namedParameterJdbcTemplate, Long idOrganization) {
		List<Channel> channels= null;
		String sql = UserDao.findChannelsByOrganizationIdSQL;
		BeanPropertyRowMapper<Channel> channelRowMapper = BeanPropertyRowMapper.newInstance(Channel.class);
		Map<String, Object> args = new HashMap<String, Object>();
        args.put("idOrganization", idOrganization);
		channels = namedParameterJdbcTemplate.query(sql, args, channelRowMapper);
		return channels;
	}

//	private static User setUserPermissions (NamedParameterJdbcTemplate namedParameterJdbcTemplate, User user) {
//    	if (user != null) {
//    		// TODO: SESI - WTF happened here 
////    		user.builder().authorities(getAuthoritiesForUser (namedParameterJdbcTemplate, user.getUsername()));
//    		user.setPermissions(getPermissionsForUser (namedParameterJdbcTemplate, user.getUsername()));
//    		if (user.getIdOrganization() != null) {
//    			user.setChannels(getChannelsForOrganization (namedParameterJdbcTemplate, user.getIdOrganization()));
//    		}
//    		user.setSubscriptions(getSubscriptionChannelsForUser (namedParameterJdbcTemplate, user.getIdUser()));
//    	}
//		return user;
//	}
//	

	
	/**************************************************************************************************************
	 * I will be gathering all the methods I am seeing being used above this line
	 *************************************************************************************************************/

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static List<Long> getMySectionIdList(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		List<Long> myList = new ArrayList<Long>();
		// It is absolutely essential we be logged in and have a user id to execute this function
		Long idProvider = JdbcDaoStaticHelper.getCurrentUserId(namedParameterJdbcTemplate);
		if (idProvider == null) {
			// can't do much.  return.
			return myList;
		}
		String sql = "SELECT id_section FROM mysection WHERE id_provider = :idProvider ORDER BY id_section";
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idProvider", idProvider);
		myList = namedParameterJdbcTemplate.queryForList(sql, args, Long.class);
		return myList;
	}

	public static void insertSectionsToMyList (List<Long> sectionIdList, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
		if (sectionIdList == null || sectionIdList.size() == 0) {
			// nothing to do here
			return;
		}
		// It is absolutely essential we be logged in and have a user id to execute this function
		Long idProvider = JdbcDaoStaticHelper.getCurrentUserId(namedParameterJdbcTemplate);
		if (idProvider == null) {
			// can't do much.  return.
			return;
		}

		if (reload) {
			// delete all my sections for user
			String sql = "DELETE FROM mysection WHERE id_provider = :idProvider";
	        Map<String, Object> args = new HashMap<String, Object>();
	        args.put("idProvider", idProvider);
	        namedParameterJdbcTemplate.update(sql, args);
		} else {
			String deleteMysection = "DELETE FROM mysection WHERE id_provider = :idProvider AND id_section = :idSection ";
			for (int i = 0; i < sectionIdList.size(); i++) {
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("idProvider", idProvider);
				args.put("idSection", sectionIdList.get(i));
		        namedParameterJdbcTemplate.update(deleteMysection, args);
			}
		}
		// now add all mysections
		// create lists for SqlParameterSource objects for each of sections, questions and answers
		// save the user id on each mysection object
		//create a list Mysections to use
		List<Mysection> mysections = new ArrayList<Mysection>();
		for (int i = 0; i < sectionIdList.size(); i++) {
			Mysection mysection = new Mysection();
			mysection.setIdSection(sectionIdList.get(i));
			mysection.setIdProvider(idProvider);
			mysections.add(mysection);
		}
		List<SqlParameterSource> mysectionParameters = new ArrayList<SqlParameterSource>();
		for (Mysection mysection : mysections) {
			mysectionParameters.add(new BeanPropertySqlParameterSource(mysection));
		}
		namedParameterJdbcTemplate.batchUpdate(SectionDao.insertMysectionSQL, mysectionParameters.toArray(new SqlParameterSource[0]));
	}


	public static void insertMysectionBatch (List<Mysection> mysections, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
		if (mysections == null || mysections.size() == 0) {
			// nothing to do here
			return;
		}
		// It is absolutely essential we be logged in and have a user id to execute this function
		Long idProvider = JdbcDaoStaticHelper.getCurrentUserId(namedParameterJdbcTemplate);
		if (idProvider == null) {
			// can't do much.  return.
			return;
		}

		if (reload) {
			// delete all my sections for user
			String sql = "DELETE FROM mysection WHERE id_provider = :idProvider";
	        Map<String, Object> args = new HashMap<String, Object>();
	        args.put("idProvider", idProvider);
	        namedParameterJdbcTemplate.update(sql, args);
		} else {
			String deleteMysection = "DELETE FROM mysection WHERE id_provider = :idProvider AND id_section = :idSection ";
			for (int i = 0; i < mysections.size(); i++) {
				if (mysections.get(i).getIdSection() != null) {
					Map<String, Object> args = new HashMap<String, Object>();
					args.put("idProvider", idProvider);
					args.put("idSection", mysections.get(i).getIdSection());
			        namedParameterJdbcTemplate.update(deleteMysection, args);
				}
			}
		}
		// now add all mysections
		// create lists for SqlParameterSource objects for each of sections, questions and answers
		// save the user id on each mysection object
		for (int i = 0; i < mysections.size(); i++) {
			mysections.get(i).setIdProvider(idProvider);
		}
		List<SqlParameterSource> mysectionParameters = new ArrayList<SqlParameterSource>();
		for (Mysection mysection : mysections) {
			mysectionParameters.add(new BeanPropertySqlParameterSource(mysection));
		}
		namedParameterJdbcTemplate.batchUpdate(SectionDao.insertMysectionSQL, mysectionParameters.toArray(new SqlParameterSource[0]));
	}


	public static void insertGradeskillBatch (List<Gradeskill> gradeskills, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
		// create lists for SqlParameterSource objects for each of sections, questions and answers
		List<SqlParameterSource> gradeskillParameters = new ArrayList<SqlParameterSource>();
		// iterate through all sections and create the necessary SqlParameterSource objects for each of sections, questions and answers
		// this way we can simply execute batch updates for each set - done within 3 sql statements.
		for (Gradeskill gradeskill : gradeskills) {
			gradeskillParameters.add(new BeanPropertySqlParameterSource(gradeskill));
		}
		
		// if reload flag is set, delete the data prior to loading again
		if (reload) {
			// time to update.  
			// all with in the scope of a transaction -- before all that though, delete the existing data in 
			// database for these gradeskills.
// no gradeskill delete.
//			for (Gradeskill gradeskill : gradeskills) {
//				namedParameterJdbcTemplate.update("call rulefree.delete_gradeskill(:idGradeskill)", new MapSqlParameterSource().
//						addValue("idGradeskill", gradeskill.getIdGradeskill(), Types.NUMERIC));
//			}
		}
		
		namedParameterJdbcTemplate.batchUpdate(SkillDao.insertGradeskillSQL, gradeskillParameters.toArray(new SqlParameterSource[0]));
	}


	/**
	 * This method uploads sections into the database.  Called from the Services layer to ingest content into the database.
	 * Note that this method may also be used to upload (any previously downloaded) "DerivedSections".   
	 * @param sections - The sections to be uploaded
	 * @param namedParameterJdbcTemplate
	 * @param reload - reload indicates if we would like to delete content before uploading or otherwise
	 */
	public static List<UpdateStatusBean> insertSectionBatch (List<Section> sections, NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate, boolean reload) {
		List<UpdateStatusBean> returnStatuses = new ArrayList<UpdateStatusBean>();
		if (sections != null && sections.size() > 0) {
			for (Section section : sections) {
				if (Section.DERIVED_SECTION_TYPE.equals(section.getSectionType())) {
					UpdateStatusBean returnStatus = insertDerivedSection(section, namedParameterJdbcTemplate, jdbcTemplate, reload);
					returnStatuses.add(returnStatus);
				} else {
					UpdateStatusBean returnStatus = insertSection(section, namedParameterJdbcTemplate, jdbcTemplate, reload);
					returnStatuses.add(returnStatus);
				}
			}
		}
		return returnStatuses;
	}
		
	/**
	 * This method uploads sections into the database.  Called from the Services layer to ingest content into the database.
	 * Note that this method may also be used to upload (any previously downloaded) "DerivedSections".   
	 * @param sections - The sections to be uploaded
	 * @param namedParameterJdbcTemplate
	 * @param reload - reload indicates if we would like to delete content before uploading or otherwise
	 */
	private static UpdateStatusBean insertDerivedSection (Section section, NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate, boolean reload) {
		// create BeanPropertySqlParameterSource objects for section
		BeanPropertySqlParameterSource sectionParameters = new BeanPropertySqlParameterSource(section);
		Map<String, Object> argsSection = new HashMap<String, Object>();
		argsSection.put("idSection", section.getIdSection());
		
		// create the derivedSectionQuestion - to be used for insert...
		List<SqlParameterSource> questionParameters = new ArrayList<SqlParameterSource>();
		for (DerivedSectionQuestion derivedSectionQuestion : section.getDerivedSectionQuestions()) {
			questionParameters.add(new BeanPropertySqlParameterSource(derivedSectionQuestion));
		}

		// see if we need to delete section before adding it again
		int deleteStatusCode = 0;
		String deleteStatusMessage = null;
		if (reload) {
//			namedParameterJdbcTemplate.update("call rulefree.delete_section(:idDerivedSection)", new MapSqlParameterSource().
//						addValue("idDerivedSection", section.getIdSection(), Types.NUMERIC));
			SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withCatalogName("rulefree").withProcedureName("delete_derived_section");
	        SqlParameterSource in = new MapSqlParameterSource().addValue("idDerivedSection", section.getIdSection(), Types.NUMERIC);
	        Map out = simpleJdbcCall.execute(in);
	        deleteStatusCode = (Integer) out.get("status_code");
	        deleteStatusMessage = (String) out.get("status_message");
		    System.out.println("statusCode: " + deleteStatusCode);
		    System.out.println("statusMessage: " + deleteStatusMessage);
		}
		
		if (deleteStatusCode == 0) {
			try {
				// add section
				namedParameterJdbcTemplate.update(SectionDao.insertSectionSQL, sectionParameters);
				// Add questions 
				if (section.getDerivedSectionQuestions() != null && section.getDerivedSectionQuestions().size() > 0) {
					namedParameterJdbcTemplate.batchUpdate(SectionDao.insertDerivedSectionQuestionSQL, questionParameters.toArray(new SqlParameterSource[0]));
				}
				return new UpdateStatusBean(section.getIdSection(), 0, "Success!");
			} catch (DataAccessException dae) {
				System.out.println("Error updating Section: " + dae.getMessage());
				logger.info("Error updating Section: " + dae.getMessage());
				return new UpdateStatusBean(section.getIdSection(), -1, "Unable to Update Section: " + deleteStatusMessage);
			}
		} else {
			// We cannot delete the section or one of its questions....so we do the following:
			// 1.) Delete and insert any derived questions.
			// 2.) Upcert the section
			// 3.) Insert any derived questions.
			
			// 1.) Delete any derived questions.
			String sqlDeleteDerivedSectionQuestions = "DELETE FROM derived_section_question WHERE id_section = :idSection ";
			try {
				namedParameterJdbcTemplate.update(sqlDeleteDerivedSectionQuestions, argsSection);
			} catch (Exception e) {
				// cannot delete  derived_section_question for some goddam reason.  Figure it out. 
				// This situation cannot be remedied automatically.  Throw a fit and Exit Immediately.
				return new UpdateStatusBean(section.getIdSection(), -1, "Unable to Delete derived_section_questions: " + e.getMessage());
			}
			// 2.) We update the section info
			if (sectionParameters != null) {
				namedParameterJdbcTemplate.update(SectionDao.updateSectionSQL, sectionParameters);
			}
			// 3.) Insert any derived questions.
			if (section.getDerivedSectionQuestions() != null && section.getDerivedSectionQuestions().size() > 0) {
				namedParameterJdbcTemplate.batchUpdate(SectionDao.insertDerivedSectionQuestionSQL, questionParameters.toArray(new SqlParameterSource[0]));
			}

			// return success response
			return new UpdateStatusBean(section.getIdSection(), 0, "Success!");
		}
	}

	
	/**
	 * This method uploads sections into the database.  Called from the Services layer to ingest content into the database.
	 * Note that this method may also be used to upload (any previously downloaded) "DerivedSections".   
	 * @param sections - The sections to be uploaded
	 * @param namedParameterJdbcTemplate
	 * @param reload - reload indicates if we would like to delete content before uploading or otherwise
	 */
	private static UpdateStatusBean insertSection (Section section, NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate, boolean reload) {
		// create BeanPropertySqlParameterSource objects for section
		BeanPropertySqlParameterSource sectionParameters = new BeanPropertySqlParameterSource(section);
		Map<String, Object> argsSection = new HashMap<String, Object>();
		argsSection.put("idSection", section.getIdSection());
		
		// create lists for SqlParameterSource objects for each of questionsets, questions and answers
		List<SqlParameterSource> questionsetParameters = new ArrayList<SqlParameterSource>();
		List<SqlParameterSource> questionParameters = new ArrayList<SqlParameterSource>();
		List<SqlParameterSource> answerParameters = new ArrayList<SqlParameterSource>();
		// iterate through all sections and create the necessary SqlParameterSource objects for each of sections, questions and answers
		// this way we can simply execute batch updates for each set - done within 3 sql statements.
		if (section.getQuestions() != null && section.getQuestions().size() > 0) {
			for (Question question : section.getQuestions()) {
				questionParameters.add(new BeanPropertySqlParameterSource(question)); 
				if (question.getAnswers() != null && question.getAnswers().size() > 0) {
					List<Answer> answers = question.getAnswers();
					for (Answer answer : answers) {
						answerParameters.add(new BeanPropertySqlParameterSource(compileAnswerCompareAddlInfoOnAnswer(answer))); // idQuestion is not set
					}						
				}
			}
			if (section.getQuestionsets() != null && section.getQuestionsets().size() > 0) {
				List<Questionset> questionsets = section.getQuestionsets();
				for (Questionset questionset : questionsets) {
					questionsetParameters.add(new BeanPropertySqlParameterSource(questionset)); 
				}
			}
		}

		// if reload flag is set, delete the data prior to loading again
		int deleteStatusCode = 0;
//		String deleteStatusMessage = null;
		if (reload) {
			// delete the section if one already exists
//			JdbcTemplate simpleJdbcTemplate = new JdbcTemplate(dataSource);
			SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withCatalogName("rulefree").withProcedureName("delete_section");
	        SqlParameterSource in = new MapSqlParameterSource().addValue("idSection", section.getIdSection(), Types.NUMERIC);
	        Map out = simpleJdbcCall.execute(in);
	        deleteStatusCode = (Integer) out.get("status_code");
//	        deleteStatusMessage = (String) out.get("status_message");
		    System.out.println("Could Not Delete Section: '" + section.getIdSection() + "'.  statusCode: '" + deleteStatusCode + "'  statusMessage: " + out.get("status_message"));
		    System.out.println("Will update the section inplace! ");
		}
		if (deleteStatusCode == 0) {
			try {
				if (sectionParameters != null) {
					namedParameterJdbcTemplate.update(SectionDao.insertSectionSQL, sectionParameters);
				}
				if (questionsetParameters != null && questionsetParameters.size() > 0) {
					namedParameterJdbcTemplate.batchUpdate(SectionDao.insertQuestionsetSQL, questionsetParameters.toArray(new SqlParameterSource[0]));
				}
				if (questionParameters != null && questionParameters.size() > 0) {
					namedParameterJdbcTemplate.batchUpdate(QuestionDao.insertQuestionSQL, questionParameters.toArray(new SqlParameterSource[0]));
				}
				if (answerParameters != null && answerParameters.size() > 0) {
					namedParameterJdbcTemplate.batchUpdate(QuestionDao.insertAnswerSQL, answerParameters.toArray(new SqlParameterSource[0]));
				}
				return new UpdateStatusBean(section.getIdSection(), 0, "Success!");
			} catch (DataAccessException dae) {
				System.out.println("Error updating Section: " + dae.getMessage());
				logger.info("Error updating Section: " + dae.getMessage());
				return new UpdateStatusBean(section.getIdSection(), -1, "Unable to Update Section: " + dae.getMessage());
			}
		} else {
			// this is a tricky situation.  We cannot delete the section or one of its questions....
			// so we do the following:
			// 1.) we delete all questionsets for the section (again, questionsets do not have fk issues)
			// 2.) We delete all answers for all questions for the section (since answers do not have referencial integrity)
			// 3.) We try to delete Any questions that are NOT in the new set (delete all questions in database minus the incoming ones)
			//	   Note that we may fail in this if one of the to-be-Deleted questions is part of a Derived section.  That situation will  
			//	   mandate an independent inquiry. (most likely cause probably the only cause for failure)
			// 4.) We update the section info
			// 5.) we upcert the question(s) info
			// 6.) we insert the question sets
			// 7.) we insert the answers (for section) 
			try {
				// 1.) we delete all questionsets for the section (again, questionsets do not have fk issues)
				String sqlDeleteQuestionset = "DELETE FROM questionset WHERE id_section = :idSection ";
				try {
					namedParameterJdbcTemplate.update(sqlDeleteQuestionset, argsSection);
				} catch (Exception e) {
					// cannot delete  Questionsets for some goddam reason.  Figure it out. 
					// This situation cannot be remedied automatically.  Throw a fit and Exit Immediately.
					return new UpdateStatusBean(section.getIdSection(), -1, "Unable to Delete Questionsets: " + e.getMessage());
				}
				
				// 2.) We delete all answers for all questions for the section (since answers do not have referencial integrity)
				String sqlDeleteAnswers = "DELETE FROM answer WHERE id_question in (SELECT id_question FROM question q LEFT JOIN section s ON q.id_section = s.id_section WHERE s.id_section = :idSection) ";
				try {
					namedParameterJdbcTemplate.update(sqlDeleteAnswers, argsSection);
				} catch (Exception e) {
					// cannot delete  Answers for some goddam reason.  Figure it out. 
					// This situation cannot be remedied automatically.  Throw a fit and Exit Immediately.
					return new UpdateStatusBean(section.getIdSection(), -1, "Unable to Delete Answers: " + e.getMessage());
				}

				// 3.) delete questions (id's) in the database that are not part of the incoming set (example: database has 40 questions, the new set has 38, then identify and delete the 2 (or more)
				// First split the incoming set into existing and new usertests
				StringBuffer shouldExistQuestionsTestsSB = new StringBuffer();
				boolean isFirst = true;
				for (Question question : section.getQuestions()) {
					if (isFirst) {
						isFirst = false;
					} else {
						shouldExistQuestionsTestsSB.append(",");
					}
					shouldExistQuestionsTestsSB.append(question.getIdQuestion());
				}
				
				String sqlDeleteQuestions = "DELETE FROM question WHERE id_section = :idSection ";
				if (shouldExistQuestionsTestsSB.length() > 0)  {
					sqlDeleteQuestions = sqlDeleteQuestions + " AND id_question NOT IN (" + shouldExistQuestionsTestsSB + ")" ;
				}
				try {
					namedParameterJdbcTemplate.update(sqlDeleteQuestions, argsSection);
				} catch (DataIntegrityViolationException dive) {
					// cannot delete a Question since its referenced in a FK.  The only FK is to a "Composed Section" 
					// This situation cannot be remedied automatically.  Throw a fit and Exit Immediately.
					return new UpdateStatusBean(section.getIdSection(), -1, "A question being DELETED is referenced in a 'Composed Section': " + dive.getMessage());
				} catch (Exception e) {
					// cannot delete a Question for some goddam reason.  Figure it out. 
					// This situation cannot be remedied automatically.  Throw a fit and Exit Immediately.
					return new UpdateStatusBean(section.getIdSection(), -1, "A question being DELETED is causing a Problem: " + e.getMessage());
				}

				// 4.) We update the section info
				if (sectionParameters != null) {
					namedParameterJdbcTemplate.update(SectionDao.updateSectionSQL, sectionParameters);
				}
				// 5.) we upcert the question(s) info
				if (questionParameters != null && questionParameters.size() > 0) {
					namedParameterJdbcTemplate.batchUpdate(QuestionDao.upsertQuestionSQL, questionParameters.toArray(new SqlParameterSource[0]));
				}
				// 5.) we insert the questionset info
				if (questionsetParameters != null && questionsetParameters.size() > 0) {
					namedParameterJdbcTemplate.batchUpdate(SectionDao.insertQuestionsetSQL, questionsetParameters.toArray(new SqlParameterSource[0]));
				}
				if (answerParameters != null && answerParameters.size() > 0) {
					namedParameterJdbcTemplate.batchUpdate(QuestionDao.insertAnswerSQL, answerParameters.toArray(new SqlParameterSource[0]));
				}
				return new UpdateStatusBean(section.getIdSection(), 0, "Success!");
			} catch (DataAccessException dae) {
				System.out.println("Error updating Section: " + dae.getMessage());
				logger.info("Error updating Section: " + dae.getMessage());
				return new UpdateStatusBean(section.getIdSection(), -1, "Unable to Update Section: " + dae.getMessage());
			}
//			return new UpdateStatusBean(section.getIdSection(), deleteStatusCode, "Unable to Delete before Re-insert Section: " + deleteStatusMessage);
		}
	}


	public static void insertSkillBatch (List<Skill> skills, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
		// check to make sure we have some skills to work with
		if (skills == null || skills.size() == 0) {
			// nothing to do here.  simply return
			return;
		}
		
        List<SqlParameterSource> skillParameters = new ArrayList<SqlParameterSource>();
		List<Section> combinedSectionList = new ArrayList<Section>();
		List<Gradeskill> combinedGradeskillList = new ArrayList<Gradeskill>();
        for (Skill skill : skills) {
        	skillParameters.add(new BeanPropertySqlParameterSource(skill));
        	List<Section> sections = skill.getSections();
			if (sections != null && sections.size() > 0) {
				combinedSectionList.addAll(sections);
			}
        	List<Gradeskill> gradeskills = skill.getGradeskills();
			if (gradeskills != null && gradeskills.size() > 0) {
				combinedGradeskillList.addAll(gradeskills);
			}
        }
        
        // Here is a little something we do if a "Skill" has a "Referenced Skill" (user in Channel-skill Overlays) but 
        // does not have have a name (entered via xml as override_name) or a description (entered via xml as override_description) 
        // we will need to grab it from the "referenced-skill"
        // Also validate the existance of referenced skills....note that we already dor this with a FK in the database.  This is 
        // done to simplify uploads with a more contextual message...
        
        for (Skill skill : skills) {
        	Skill referencedSkill = null;
        	if (skill.getIdSkillReference() != null && skill.getIdSkillReference() != 0l) {
        		referencedSkill = getReferencedSkill(skill.getIdSkillReference(), namedParameterJdbcTemplate);
            	if (referencedSkill == null) {
            		throw new RuntimeException("Referenced Skill with ID: '" + skill.getIdSkillReference() + "' does not exist."); 
            	}

        		if (skill.getName() == null || skill.getName().trim().length() == 0) {
        			skill.setName(referencedSkill.getName());
        		}
        		if (skill.getDescription() == null || skill.getDescription().trim().length() == 0) {
        			skill.setDescription(referencedSkill.getDescription());
        		}
        	}
        }

		// if reload flag is set, delete the data prior to loading again
		if (reload) {
			// time to update.  because of FK rules, first update sections, then questions and then answers
			// all with in the scope of a transaction -- before all that though, delete the existing data in 
			// database for these sections.
			for (Skill skill : skills) {
				namedParameterJdbcTemplate.update("call rulefree.delete_skill(:idSkill)", new MapSqlParameterSource().
						addValue("idSkill", skill.getIdSkill(), Types.NUMERIC));
			}
		}
		
        // first add the skills
        namedParameterJdbcTemplate.batchUpdate(SkillDao.insertSkillSQL, skillParameters.toArray(new SqlParameterSource[0]));
        // not call method to add any sections
        // note that sections have already been deleted
//		insertSectionBatch(combinedSectionList, namedParameterJdbcTemplate, false);
        // not call method to add any sections
        // note that sections have already been deleted
		insertGradeskillBatch(combinedGradeskillList, namedParameterJdbcTemplate, false);
	}

	// used in an action to upload channels with "referenced skills".  Those referenced skills may nay have names or descriptions.
	private static Skill getReferencedSkill(Long idSkillReference, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		Skill skill = null;
        String sql = SkillDao.findBySkillIdSQL;
        BeanPropertyRowMapper<Skill> skillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idSkill", idSkillReference);
        try {
        	skill = namedParameterJdbcTemplate.queryForObject(sql, args, skillRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return skill;
	}

	// used in an action to upload channels with "referenced topics".  Those referenced topics may not have names or descriptions.
	private static Topic getReferencedTopic(Long idTopicReference, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		Topic topic = null;
        String sql = TopicDao.findByTopicIdSQL;
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTopic", idTopicReference);
        try {
        	topic = namedParameterJdbcTemplate.queryForObject(sql, args, topicRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return topic;
	}

	// used in an action to upload channels with "referenced levels".  Those referenced levels may not have names or descriptions.
	private static Level getReferencedLevel(Long idLevelReference, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		Level level = null;
        String sql = LevelDao.findByLevelIdSQL;
        BeanPropertyRowMapper<Level> levelRowMapper = BeanPropertyRowMapper.newInstance(Level.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevelReference);
        try {
        	level = namedParameterJdbcTemplate.queryForObject(sql, args, levelRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return level;
	}

	public static void insertTopicBatch (List<Topic> topics, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
		// check to make sure we have some skills to work with
		if (topics == null || topics.size() == 0) {
			// nothing to do here.  simply return
			return;
		}
		
        // Here is a little something we do if a "Topic" has a "Referenced Topic" (user in Channel-Topic Overlays) but 
        // does not have have a name (entered via xml as override_name) or a description (entered via xml as override_description) 
        // we will need to grab it from the "referenced-topic"
        // Also validate the existance of referenced topics....note that we already dor this with a FK in the database.  This is 
        // done to simplify uploads with a more contextual message...
        
        for (Topic topic : topics) {
        	Topic referencedTopic = null;
        	if (topic.getIdTopicReference() != null && topic.getIdTopicReference() != 0l) {
        		referencedTopic = getReferencedTopic(topic.getIdTopicReference(), namedParameterJdbcTemplate);
            	if (referencedTopic == null) {
            		throw new RuntimeException("Referenced Topic with ID: '" + topic.getIdTopicReference() + "' does not exist."); 
            	}
        		if (topic.getSubject() == null || topic.getSubject().trim().length() == 0) {
        			topic.setSubject(referencedTopic.getSubject());
        		}
        		if (topic.getName() == null || topic.getName().trim().length() == 0) {
        			topic.setName(referencedTopic.getName());
        		}
        		if (topic.getDescription() == null || topic.getDescription().trim().length() == 0) {
        			topic.setDescription(referencedTopic.getDescription());
        		}
        	}
        }

        List<SqlParameterSource> topicParameters = new ArrayList<SqlParameterSource>();
		List<Skill> combinedSkillList = new ArrayList<Skill>();
        for (Topic topic : topics) {
        	topicParameters.add(new BeanPropertySqlParameterSource(topic));
        	List<Skill> skills = topic.getSkills();
			if (skills != null && skills.size() > 0) {
				combinedSkillList.addAll(skills);
			}
        }

        // if reload flag is set, delete the data prior to loading again
		if (reload) {
			// time to update.  because of FK rules, first update sections, then questions and then answers
			// all with in the scope of a transaction -- before all that though, delete the existing data in 
			// database for these sections.
			for (Topic topic : topics) {
				namedParameterJdbcTemplate.update("call rulefree.delete_topic(:idTopic)", new MapSqlParameterSource().
						addValue("idTopic", topic.getIdTopic(), Types.NUMERIC));
			}
		}
		
        // first add the skills
        namedParameterJdbcTemplate.batchUpdate(TopicDao.insertTopicSQL, topicParameters.toArray(new SqlParameterSource[0]));
        // not call method to add any sections
        // note that skills have already been deleted
        insertSkillBatch(combinedSkillList, namedParameterJdbcTemplate, false);
	}

	public static void insertLevelBatch (List<Level> levels, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
		// check to make sure we have some Levels to work with
		if (levels == null || levels.size() == 0) {
			// nothing to do here.  simply return
			return;
		}
		
        // Here is a little something we do if a "Level" has a "Referenced Level" (used in Channel-Level Overlays) but 
        // does not have have a name (entered via xml as override_name) or a description (entered via xml as override_description) 
		// or a subject (entered via xml as override_subject), we will need to grab it from the "referenced-level"
        // Also validate the existence of referenced levels....note that we already do this with a FK in the database.  This is 
        // done to simplify uploads with a more contextual message...
        
        for (Level level : levels) {
        	Level referencedLevel = null;
        	if (level.getIdLevelReference() != null && level.getIdLevelReference() != 0l) {
        		referencedLevel = getReferencedLevel(level.getIdLevelReference(), namedParameterJdbcTemplate);
            	if (referencedLevel == null) {
            		throw new RuntimeException("Referenced Level with ID: '" + level.getIdLevelReference() + "' does not exist."); 
            	}
        		if (level.getSubject() == null || level.getSubject().trim().length() == 0) {
        			level.setSubject(referencedLevel.getSubject());
        		}
        		if (level.getSubjectHeader() == null || level.getSubjectHeader().trim().length() == 0) {
        			level.setSubjectHeader(referencedLevel.getSubjectHeader());
        		}
        		if (level.getName() == null || level.getName().trim().length() == 0) {
        			level.setName(referencedLevel.getName());
        		}
        		if (level.getDescription() == null || level.getDescription().trim().length() == 0) {
        			level.setDescription(referencedLevel.getDescription());
        		}
        	}
        }

        List<SqlParameterSource> levelParameters = new ArrayList<SqlParameterSource>();
		List<Topic> combinedTopicList = new ArrayList<Topic>();
        for (Level level : levels) {
        	levelParameters.add(new BeanPropertySqlParameterSource(level));
        	List<Topic> topics = level.getTopics();
			if (topics != null && topics.size() > 0) {
				combinedTopicList.addAll(topics);
			}
        }

        // if reload flag is set, delete the data prior to loading again
		if (reload) {
			// time to update.  because of FK rules, first update sections, then questions and then answers
			// all with in the scope of a transaction -- before all that though, delete the existing data in 
			// database for these sections.
			for (Level level : levels) {
				namedParameterJdbcTemplate.update("call rulefree.delete_level(:idLevel)", new MapSqlParameterSource().
						addValue("idLevel", level.getIdLevel(), Types.NUMERIC));
			}
		}
		
        // first add the skills
        namedParameterJdbcTemplate.batchUpdate(LevelDao.insertLevelSQL, levelParameters.toArray(new SqlParameterSource[0]));
        // not call method to add any sections
        // note that topics have already been deleted
        if (combinedTopicList != null && combinedTopicList.size() > 0) {
        	insertTopicBatch(combinedTopicList, namedParameterJdbcTemplate, false);
        }
	}

	/**
	 * insertWordlistBatch Procedure
	 * @param wordlists
	 * @param namedParameterJdbcTemplate
	 * @param reload
	 */
	public static void insertWordlistBatch (List<WlWordlist> wordlists, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
		// check to make sure we have some Levels to work with
		if (wordlists == null || wordlists.size() == 0) {
			// nothing to do here.  simply return
			return;
		}
		
        List<SqlParameterSource> wordlistParameters = new ArrayList<SqlParameterSource>();
		List<SqlParameterSource> combinedDummyParentSectionParameters = new ArrayList<SqlParameterSource>();
		List<WlWord> combinedWordsList = new ArrayList<WlWord>();
		List<WlPassage> combinedPassagesList = new ArrayList<WlPassage>();
		List<Question> combinedQuestionsList = new ArrayList<Question>();
        for (WlWordlist wordlist : wordlists) {
        	wordlistParameters.add(new BeanPropertySqlParameterSource(wordlist));
        	Section parentSection = wordlist.getParentSection();
        	if (parentSection != null) {
        		combinedDummyParentSectionParameters.add(new BeanPropertySqlParameterSource(parentSection));
        	}
        	List<WlWord> words = wordlist.getWords();
			if (words != null && words.size() > 0) {
				combinedWordsList.addAll(words);
			}
        	List<WlPassage> passages = wordlist.getPassages();
			if (passages != null && passages.size() > 0) {
				combinedPassagesList.addAll(passages);
			}
        	List<Question> questions = wordlist.getQuestions();
			if (questions != null && questions.size() > 0) {
				combinedQuestionsList.addAll(questions);
			}
        }

        // if reload flag is set, delete the data prior to loading again
		if (reload) {
			// time to update.  because of FK rules, first update sections, then questions and then answers
			// all with in the scope of a transaction -- before all that though, delete the existing data in 
			// database for these sections.
	        for (WlWordlist wordlist : wordlists) {
				namedParameterJdbcTemplate.update("call rulefree.delete_wordlist(:idWordlist)", new MapSqlParameterSource().
						addValue("idWordlist", wordlist.getIdWordlist(), Types.NUMERIC));
			}
		}
		
        // first add the dummy parent sections
        namedParameterJdbcTemplate.batchUpdate(SectionDao.insertSectionSQL, combinedDummyParentSectionParameters.toArray(new SqlParameterSource[0]));

        // now add the wordlists - note that wordlists have a 1-1 FK relation with the Parent Section.
        namedParameterJdbcTemplate.batchUpdate(WlWordlistDao.insertWlWordlistSQL, wordlistParameters.toArray(new SqlParameterSource[0]));

        // not call method to add any words, passages and questions
        // insert questions
        List<SqlParameterSource> questionParameters = new ArrayList<SqlParameterSource>();
		List<Answer> combinedAnswersList = new ArrayList<Answer>();
        for (Question question : combinedQuestionsList) {
        	questionParameters.add(new BeanPropertySqlParameterSource(question));
        	List<Answer> answers = question.getAnswers();
			if (answers != null && answers.size() > 0) {
				combinedAnswersList.addAll(compileAnswerCompareAddlInfoOnAnswers(answers));
			}
        }
        namedParameterJdbcTemplate.batchUpdate(QuestionDao.insertQuestionSQL, questionParameters.toArray(new SqlParameterSource[0]));

        // insert answers
        List<SqlParameterSource> answerParameters = new ArrayList<SqlParameterSource>();
        for (Answer answer : combinedAnswersList) {
        	answerParameters.add(new BeanPropertySqlParameterSource(answer));
        }
        namedParameterJdbcTemplate.batchUpdate(QuestionDao.insertAnswerSQL, answerParameters.toArray(new SqlParameterSource[0]));

        // insert passages
        List<SqlParameterSource> passageParameters = new ArrayList<SqlParameterSource>();
        for (WlPassage passage : combinedPassagesList) {
        	passageParameters.add(new BeanPropertySqlParameterSource(passage));
        }
        namedParameterJdbcTemplate.batchUpdate(WlWordlistDao.insertWlPassageSQL, passageParameters.toArray(new SqlParameterSource[0]));

        // insert words
        List<SqlParameterSource> wordParameters = new ArrayList<SqlParameterSource>();
        for (WlWord word : combinedWordsList) {
        	wordParameters.add(new BeanPropertySqlParameterSource(word));
        }
        namedParameterJdbcTemplate.batchUpdate(WlWordlistDao.insertWlWordSQL, wordParameters.toArray(new SqlParameterSource[0]));
	}

	// This takes a list of Answers and maps the AnswerCompareAddlInfo from the individual flags on each answer.
	public static List<Answer> compileAnswerCompareAddlInfoOnAnswers(List<Answer> answers) {
		if (answers != null && answers.size() > 0) {
			for (Answer answer : answers) {
				compileAnswerCompareAddlInfoOnAnswer(answer);
			}
		}
		return answers;
	}

	// This takes a single Answer and maps the AnswerCompareAddlInfo from the individual flags on it:
	// Calculate and set the answerDao.setAnswerCompareAddlInfo to a string that represents criteria to use when comparing strings 3 characters long
    // a.) 1st char: 0=case insensitive, 1= case sensitive
    // b.) 2nd char: 0=do not trim surrounding white space, 1 = trim surrounding white space
    // c.) 3rd char: 0= trim interior white spaces to one white space, 1 = leave interior white spaces alone
	public static Answer compileAnswerCompareAddlInfoOnAnswer(Answer answer) {
		if (answer != null && answer.getAnswerCompareType() != null) {
			if (answer.getAnswerCompareType().equals(QuestionCompareConstants.QuestionCompareTypes.TEXT_COMPARE.compareType())) {
				StringBuffer answerCompareAddlInfoBuffer = new StringBuffer(3);
				answerCompareAddlInfoBuffer.append(answer.isCaseSensitive() ? "1" : "0");
				answerCompareAddlInfoBuffer.append(answer.isTrimOuterSpaces() ? "1" : "0");
				answerCompareAddlInfoBuffer.append(answer.isTrimExtraInnerSpaces() ? "1" : "0");
				// now set the CompareCriteria additional info from the buffer
				answer.setAnswerCompareAddlInfo(answerCompareAddlInfoBuffer.toString());
			}
		}
		return answer;
	}

	// This takes a list of Answers and maps the AnswerCompareAddlInfo from the individual flags on each answer.
	public static List<Answer> parseAnswerCompareAddlInfoOnAnswers(List<Answer> answers) {
		if (answers != null && answers.size() > 0) {
			for (Answer answer : answers) {
				parseAnswerCompareAddlInfoOnAnswer(answer);
			}
		}
		return answers;
	}

	// This takes a single Answer and maps the AnswerCompareAddlInfo from the individual flags on it:
	// Calculate and set the answerDao.setAnswerCompareAddlInfo to a string that represents criteria to use when comparing strings 3 characters long
    // a.) 1st char: 0=case insensitive, 1= case sensitive
    // b.) 2nd char: 0=do not trim surrounding white space, 1 = trim surrounding white space
    // c.) 3rd char: 0= trim interior white spaces to one white space, 1 = leave interior white spaces alone
	public static Answer parseAnswerCompareAddlInfoOnAnswer(Answer answer) {
		if (answer != null && answer.getAnswerCompareType() != null) {
			if (answer.getAnswerCompareType().equals(QuestionCompareConstants.QuestionCompareTypes.TEXT_COMPARE.compareType())) {
				// now set the CompareCriteria additional info from the buffer
				String answerCompareAddlInfo = answer.getAnswerCompareAddlInfo();
				if (answerCompareAddlInfo != null && answerCompareAddlInfo.trim().length() == 3) {
					answer.setCaseSensitive(answerCompareAddlInfo.substring(0,1).equals("1"));
					answer.setTrimOuterSpaces(answerCompareAddlInfo.substring(1,2).equals("1"));
					answer.setTrimExtraInnerSpaces(answerCompareAddlInfo.substring(2,3).equals("1"));
				} else {
					// set to defaults
					answer.setCaseSensitive(false);
					answer.setTrimOuterSpaces(true);
					answer.setTrimExtraInnerSpaces(true);
				}
			} else if (answer.getAnswerCompareType().equals(QuestionCompareConstants.QuestionCompareTypes.DECIMAL_COMPARE.compareType())) {
				int precisionDigits = 2;
				try {
					precisionDigits = Integer.parseInt(answer.getAnswerCompareAddlInfo() == null ? "2" : answer.getAnswerCompareAddlInfo());
				} catch (NumberFormatException nfe) { 
					//nothing to do
				}
				answer.setPrecisionDigits(precisionDigits);
			}
		}
		return answer;
	}


	public static void insertTestsegmentBatch (List<Testsegment> testsegments, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
		// check to make sure we have some Testsegments to work with
		if (testsegments == null || testsegments.size() == 0) {
			// nothing to do here.  simply return
			return;
		}
		
		if (reload) {
	        for (Testsegment testsegment : testsegments) {
				namedParameterJdbcTemplate.update("call rulefree.delete_testsegment(:idTestsegment)", new MapSqlParameterSource().
						addValue("idTestsegment", testsegment.getIdTestsegment(), Types.NUMERIC));
			}
		}
		// Cannot do a batch update here since I need to retrieve the testsegment id key to add testSections.  Will have to iterate through the 
		// list of testsegments
		//        // first add the testsegments
		//        namedParameterJdbcTemplate.batchUpdate(TestsegmentDao.insertTestsegmentSQL, testParameters.toArray(new SqlParameterSource[0]));
        for (Testsegment testsegment : testsegments) {
            SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(testsegment);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(TestsegmentDao.insertTestsegmentSQL, parameterSource, keyHolder, new String[]{"ID_TESTSEGMENT"});
            // id for the new test object
            Long idTestsegment = keyHolder.getKey().longValue();
            // now insert the testsections
            if (testsegment.getTestsections() != null && testsegment.getTestsections().size() > 0) {
                List<SqlParameterSource> testsectionParameters = new ArrayList<SqlParameterSource>();
                for (Testsection testsection : testsegment.getTestsections()) {
                	testsection.setIdTestsegment(idTestsegment);
                	testsectionParameters.add(new BeanPropertySqlParameterSource(testsection));
                }
                // Now add the new list of testsections
                namedParameterJdbcTemplate.batchUpdate(TestsegmentDao.insertTestsectionSQL, testsectionParameters.toArray(new SqlParameterSource[0]));
            }
        }
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void insertTestsegmentsForTest (List<Testsegment> testsegments, Long idTest, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		// Delete any existing test segments for the test
		namedParameterJdbcTemplate.update("call rulefree.delete_testsegments_for_test(:idTest)", new MapSqlParameterSource().
				addValue("idTest", idTest, Types.NUMERIC));

		// check to make sure we have some Testsegments to work with
		if (testsegments == null || testsegments.size() == 0) {
			// nothing to do here.  simply return
			return;
		}
		
		// Cannot do a batch update here since I need to retrieve the keys so I can add testSections.  Will have to iterate through the 
		// loop of testsegments
		for (int testsegmentCounter = 0; testsegmentCounter < testsegments.size(); testsegmentCounter++) {
			Testsegment testsegment = testsegments.get(testsegmentCounter);
        	// set the testId on each testsegment, just in case its not set
			Long idTestsegment = (idTest *  100) + testsegmentCounter + 1;
        	testsegment.setIdTest(idTest);
        	testsegment.setIdTestsegment(idTestsegment);
        	SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(testsegment);
            namedParameterJdbcTemplate.update(TestsegmentDao.insertTestsegmentSQL, parameterSource);
            // now insert/overwrite the testsections
            if (testsegment.getTestsections() != null && testsegment.getTestsections().size() > 0) {
                List<SqlParameterSource> testsectionParameters = new ArrayList<SqlParameterSource>();
        		for (int testsectionCounter = 0; testsectionCounter < testsegment.getTestsections().size(); testsectionCounter++) {
        			Testsection testsection = testsegment.getTestsections().get(testsectionCounter);
                	// set the newly generated testsegmentID on testsection
//        			Long idTestsection = (idTestsegment *  100) + testsectionCounter + 1;
        			Long idTestsection = (idTestsegment *  100) + testsection.getSeq();
                	testsection.setIdTestsegment(idTestsegment);
                	testsection.setIdTestsection(idTestsection);
                	testsectionParameters.add(new BeanPropertySqlParameterSource(testsection));
                }
        		// Now add the new list of testsections
                namedParameterJdbcTemplate.batchUpdate(TestsegmentDao.insertTestsectionSQL, testsectionParameters.toArray(new SqlParameterSource[0]));
            }
            // now insert/overwrite the testsynopsislinks
            if (testsegment.getTestsynopsislinks() != null && testsegment.getTestsynopsislinks().size() > 0) {
                List<SqlParameterSource> testsynopsislinkParameters = new ArrayList<SqlParameterSource>();
        		for (int testsynopsislinkCounter = 0; testsynopsislinkCounter < testsegment.getTestsynopsislinks().size(); testsynopsislinkCounter++) {
        			Testsynopsislink testsynopsislink = testsegment.getTestsynopsislinks().get(testsynopsislinkCounter);
                	// set the newly generated testsegmentID on testsection
//        			Long idTestsynopsislink = (idTestsegment *  100) + testsynopsislinkCounter + 1;
        			Long idTestsynopsislink = (idTestsegment *  100) + testsynopsislink.getSeq();
        			testsynopsislink.setIdTestsegment(idTestsegment);
        			testsynopsislink.setIdTestsynopsislink(idTestsynopsislink);
        			testsynopsislinkParameters.add(new BeanPropertySqlParameterSource(testsynopsislink));
                }
        		// Now add the new list of testsections
                namedParameterJdbcTemplate.batchUpdate(TestsegmentDao.insertTestsynopsislinkSQL, testsynopsislinkParameters.toArray(new SqlParameterSource[0]));
            }
        }
	}
	
	public static String insertProfilesegmentsForProfile (List<Profilesegment> profilesegments, Long idProfile, NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate) {
		// Delete any existing test segments for the test
		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withCatalogName("rulefree").withProcedureName("delete_profilesegments_for_profile");
        SqlParameterSource in = new MapSqlParameterSource().addValue("idProfile", idProfile, Types.NUMERIC);
        Map out = simpleJdbcCall.execute(in);
        int deleteStatusCode = (Integer) out.get("status_code");
        if (deleteStatusCode != 0) {
        	return ("Delete ProfileSegments Failed for Profile: '" + idProfile + "'.  " + out.get("status_message"));
        }

        // check to make sure we have some Testsegments to work with
		if (profilesegments == null || profilesegments.size() == 0) {
			// nothing to do here.  simply return
			return null;
		}
		
		// Cannot do a batch update here since I need to retrieve the keys so I can add testSections.  Will have to iterate through the 
		// loop of testsegments
		for (int profilesegmentCounter = 0; profilesegmentCounter < profilesegments.size(); profilesegmentCounter++) {
			Profilesegment profilesegment = profilesegments.get(profilesegmentCounter);
        	// set the profileId on each profilesegment, just in case its not set
			Long idProfilesegment = (idProfile *  100) + profilesegmentCounter + 1;
			profilesegment.setIdProfile(idProfile);
			profilesegment.setIdProfilesegment(idProfilesegment);
        	SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(profilesegment);
            namedParameterJdbcTemplate.update(ProfileDao.insertProfilesegmentSQL, parameterSource);
            // now insert/overwrite the testsections
            if (profilesegment.getProfiletests() != null && profilesegment.getProfiletests().size() > 0) {
                List<SqlParameterSource> profiletestParameters = new ArrayList<SqlParameterSource>();
        		for (int profiletestCounter = 0; profiletestCounter < profilesegment.getProfiletests().size(); profiletestCounter++) {
        			Profiletest profiletest = profilesegment.getProfiletests().get(profiletestCounter);
                	// set the newly generated profilesegmentID on profiletest
//        			Long idTestsection = (idTestsegment *  100) + testsectionCounter + 1;
        			profiletest.setIdProfile(idProfile);
        			Long idProfiletest = (idProfilesegment *  100) + profiletest.getSeq();
        			profiletest.setIdProfilesegment(idProfilesegment);
        			profiletest.setIdProfiletest(idProfiletest);
        			profiletestParameters.add(new BeanPropertySqlParameterSource(profiletest));
                }
        		// Now add the new list of testsections
                namedParameterJdbcTemplate.batchUpdate(ProfileDao.insertProfiletestSQL, profiletestParameters.toArray(new SqlParameterSource[0]));
            }
        }
		return null;
	}
	
	/**
	 * Delete all user groups for current user and reload the groups in the list under the current user
	 * @param usergroups
	 * @param namedParameterJdbcTemplate
	 * @param reload
	 */
	public static void insertUsergroupBatchForCurrentUser (List<Usergroup> usergroups, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
		insertUsergroupBatchForUser(getCurrentUserName(), usergroups, namedParameterJdbcTemplate, reload);
	}
		// check to make sure we have some Testsegments to work with

	
	public static void insertUsergroupBatchForUser (String username, List<Usergroup> usergroups, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
		// check to make sure there is a valid user
		if (username == null || username.trim().length() == 0) {
			// nothing to do here.  simply return
			return;
		}
		// check to make sure we have some Testsegments to work with
		if (usergroups == null || usergroups.size() == 0) {
			// nothing to do here.  simply return
			return;
		}
		
		// all user groups are reloaded for the current username only
		if (reload) {
			namedParameterJdbcTemplate.update("call rulefree.delete_usergroup(:username)", new MapSqlParameterSource().
					addValue("username", username, Types.VARCHAR));
		}
		// Cannot do a batch update here since I need to retrieve the testsegment id key to add testSections.  Will have to iterate through the 
		// list of testsegments
		//        // first add the testsegments
		//        namedParameterJdbcTemplate.batchUpdate(TestsegmentDao.insertTestsegmentSQL, testParameters.toArray(new SqlParameterSource[0]));
        for (Usergroup usergroup : usergroups) {
        	// hard wire group to the user name for whom the user groups are being created.  So the logged in user can only add groups for himself 
        	usergroup.setProviderUsername(username);
        	
            SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(usergroup);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(UsergroupDao.insertUsergroupSQL, parameterSource, keyHolder, new String[]{"ID_USERGROUP"});
            // id for the new test object
            Long idUsergroup = keyHolder.getKey().longValue();
            // now insert the usergroupmembers
            if (usergroup.getUsers() != null && usergroup.getUsers().size() > 0) {
                List<SqlParameterSource> usergroupmemberParameters = new ArrayList<SqlParameterSource>();
                for (User user : usergroup.getUsers()) {
                	usergroupmemberParameters.add(new BeanPropertySqlParameterSource(new Usergroupmember(idUsergroup, user.getUsername())));
                }
                // Now add the new list of testsections
                namedParameterJdbcTemplate.batchUpdate(UsergroupDao.insertUsergroupmemberSQL, usergroupmemberParameters.toArray(new SqlParameterSource[0]));
            }
        }
	}

	/**
	 * Delete all user providerstudents for current provider and reload them
	 * @param usergroups
	 * @param namedParameterJdbcTemplate
	 * @param reload
	 */
	public static void insertUsersForCurrentProvider (List<User> allUsersList, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
		insertUsersForProvider(getCurrentUserName(), allUsersList, namedParameterJdbcTemplate, reload);
	}
	
	public static void insertUsersForProvider (String providerName, List<User> allUsersList, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
		// check to make sure there is a valid user
		if (providerName == null || providerName.trim().length() == 0) {
			// nothing to do here.  simply return
			return;
		}
		// delete all users from providerstudent table if reload is marked true
		if (reload) {
			String deleteSql = UsergroupDao.deleteUsersForProviderSQL;
	        Map<String, Object> args = new HashMap<String, Object>();
	        args.put("providerName", providerName);
	        namedParameterJdbcTemplate.update(deleteSql, args);

		}
		// now add the new providerstudent records 
		// check to make sure we have some Users to work with
		if (allUsersList == null || allUsersList.size() == 0) {
			// nothing to do here.  simply return
			return;
		}
		// first create providerstudent objects corresponding to the User objects
		List<Providerstudent> providerstudentList = new ArrayList<Providerstudent>();
		for (User u : allUsersList) {
			providerstudentList.add(new Providerstudent(providerName, u.getUsername()));
		}
		// next, insert any new providerstudent records
        List<SqlParameterSource> insertProviderstudentParameters = new ArrayList<SqlParameterSource>();
        for (Providerstudent ps : providerstudentList) {
        	insertProviderstudentParameters.add(new BeanPropertySqlParameterSource(ps));
        }
        if (insertProviderstudentParameters.size() > 0) {
			String insertSql = UsergroupDao.insertUsersForProviderSQL; 
            namedParameterJdbcTemplate.batchUpdate(insertSql, insertProviderstudentParameters.toArray(new SqlParameterSource[0]));
        }
	}

	/**
	 * Delete all user groups for current user and reload the groups in the list under the current user
	 * @param usergroups
	 * @param namedParameterJdbcTemplate
	 * @param reload
	 */
	public static void insertUsertestBatchForCurrentUser (List<Usertest> usertests, NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate, boolean reload) {
		insertUsertestBatchForUser(getCurrentUserId(namedParameterJdbcTemplate), usertests, namedParameterJdbcTemplate, jdbcTemplate, reload);
	}
		// check to make sure we have some Testsegments to work with

	
	public static void insertUsertestBatchForUser (Long idProvider, List<Usertest> usertests, NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate, boolean reload) {
		// check to make sure we have some Testsegments to work with

		// Incorrect.  We can have a empty/null usertests (in case all user tests are DELETED).
//		if (usertests == null || usertests.size() == 0) {
//			// nothing to do here.  simply return
//			return;
//		}
		if (usertests == null) {
			// so we do not throw a null pointer exception
			usertests = new ArrayList<Usertest>();
			// nothing to do here.  simply return
			return;
		}
		
//		// all user test assignments are reloaded for the current username only
//		if (reload) {
//			namedParameterJdbcTemplate.update("call rulefree.delete_usertest(:idProvider)", new MapSqlParameterSource().
//					addValue("idProvider", idProvider, Types.NUMERIC));
//		}
		
		// we cannot do blanket deletes anymore.  
		// We need to update the userTest data in 3 steps...
		// 1.) Identify existing usertest records that have been deleted and are obsolete otherwise and blow them away from the database.
		//     However, make sure not to delete usertests that users have started working on (status of anything other than 'assigned')
		// 2.) Insert any new usertests (where there is no id_usertest)
		// 3.) update any existing usertests (where there is infact a id_usertest)
		
		// First split the incoming set into existing and new usertests
		List<Usertest> newUsertests = new ArrayList<Usertest>();
		List<Usertest> existingUsertests = new ArrayList<Usertest>();
		StringBuffer existingUserTestsSB = new StringBuffer();
		boolean isFirst = true;
		for (Usertest usertest : usertests) {
			if (usertest.getIdUsertest() == null || usertest.getIdUsertest() == 0l) {
				newUsertests.add(usertest);
			} else {
				existingUsertests.add(usertest);
				if (isFirst) {
					isFirst = false;
				} else {
					existingUserTestsSB.append(",");
				}
				existingUserTestsSB.append(usertest.getIdUsertest());
			}
		}
		
		// first delete any user tests from the database that have been deleted on the front end
		// weird stuff, I had to get a separate template for this...
		//*** ERROR: This deletes ALL user tester assigned by ANY provider.  FIX this - 09/14/2014
//		String sql = "DELETE FROM usertest WHERE test_status = 'assigned' ";
//		if (existingUserTestsSB.length() > 0)  {
//			sql = sql + " AND id_usertest NOT IN (" + existingUserTestsSB + ")" ;
//		}
//		jdbcTemplate.execute(sql);
		
		// ONLY DELETE NON-PROFILED usertests 
		String sql = "DELETE FROM usertest WHERE id_provider = " + idProvider + " AND test_status = 'assigned' AND id_profile = 0";
		if (existingUserTestsSB.length() > 0)  {
			sql = sql + " AND id_usertest NOT IN (" + existingUserTestsSB + ")" ;
		}
		jdbcTemplate.execute(sql);
		
		// next, insert any new usertests
        List<SqlParameterSource> insertUsertestParameters = new ArrayList<SqlParameterSource>();
        for (Usertest usertest : newUsertests) {
        	// hard wire group to the user name for whom the user groups are being created.  So the logged in user can only add groups for himself 
        	usertest.setIdProvider(idProvider);
        	insertUsertestParameters.add(new BeanPropertySqlParameterSource(usertest));
        }
        if (insertUsertestParameters.size() > 0) {
            namedParameterJdbcTemplate.batchUpdate(UsertestDao.insertUsertestSQL, insertUsertestParameters.toArray(new SqlParameterSource[0]));
        }
        
        // next update any existing usertests
        List<SqlParameterSource> updateUsertestParameters = new ArrayList<SqlParameterSource>();
        for (Usertest usertest : existingUsertests) {
        	if (usertest.getTestStatus() == null || usertest.getTestStatus().equals(TestConstants.TEST_STATUS_ASSIGNED)) {
        		usertest.setIdProvider(idProvider);
        	}
        	updateUsertestParameters.add(new BeanPropertySqlParameterSource(usertest));
        }
        if (updateUsertestParameters.size() > 0) {
            namedParameterJdbcTemplate.batchUpdate(UsertestDao.updateUsertestSQL, updateUsertestParameters.toArray(new SqlParameterSource[0]));
        }
        
	}

	/**
	 * Function used to create a UserTest for the user in the profile.  Usually called while Loading Users 
	 * from the SOAP frontend or Automatically creating usertests for OMR-ABIP exams from (JdbcAbiptestresponse)
	 * @param namedParameterJdbcTemplate
	 * @param idUser
	 * @param idTest
	 * @param assignmentDate
	 * @param idProfile
	 * @return
	 */
	public static Usertest createAndGetUsertest(NamedParameterJdbcTemplate namedParameterJdbcTemplate, Long idUser, Long idTest, Date assignmentDate, Long idProfile, boolean isAdministeredOffline) {
		if (idUser == null || idUser == 0l || idTest == null || idTest == 0l) {
			return null;
		}
		Test test = getTestByTestId(namedParameterJdbcTemplate, idTest);
        String sql = "SELECT * FROM usertest WHERE id_user = :idUser AND id_test = :idTest AND id_profile = :idProfile";
		BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUser", idUser);
        args.put("idTest", idTest);
        args.put("idProfile", idProfile);
		Usertest usertest = null;
		try {
			usertest = namedParameterJdbcTemplate
					.queryForObject(sql, args, usertestRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		if (usertest == null) {
			usertest = new Usertest();
			usertest.setIdProvider(test.getIdProvider());
			usertest.setIdTest(idTest);
			usertest.setIdUser(idUser);
			usertest.setAutoGrade(test.getAutoGrade());
			usertest.setAutoPublishResults(test.getAutoPublishResults());
			usertest.setTestType(TestConstants.TEST);
			usertest.setUserType(1);
			usertest.setTestStatus(TestConstants.TEST_STATUS_ASSIGNED);
			usertest.setName(test.getName());
			usertest.setDescription(test.getDescription());
			usertest.setTestAssignmentDate(assignmentDate == null ? new Date() : assignmentDate);
			usertest.setIsReportAvailableToViewByStudent(test.getAutoPublishResults());
			// mark this as an Offline-administered exam (only ABIP reports) 
			usertest.setAdministeredOffline(isAdministeredOffline ? 1 : 0);
			// mark this as an Offline-administered exam (only ABIP reports) 
			usertest.setIdProfile(idProfile);
			// update/insert testinstance into the database
	        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(usertest);
	        KeyHolder keyHolder = new GeneratedKeyHolder();
	        namedParameterJdbcTemplate.update(UsertestDao.insertUsertestSQL, parameterSource, keyHolder, new String[]{"id_usertest"});
	        // id for the new usertest object
	        Long idUsertest = keyHolder.getKey().longValue();
	        
	        // set the id on testinstance so it will be used in reporting email
	        usertest.setIdUsertest(idUsertest);
		}
		
		return usertest;

	}

	/**
	 * Simple function used to create a test object corresponding to idTest
	 * @param namedParameterJdbcTemplate
	 * @param idTest
	 * @return
	 */
	public static Test getTestByTestId(NamedParameterJdbcTemplate namedParameterJdbcTemplate, Long idTest) {
        String sql = TestDao.getTestByTestIdSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTest", idTest);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Test test = null;
        try {
        	test = namedParameterJdbcTemplate.queryForObject(sql, args, testRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return test;
	}

	/**
	 * Delete all userprofiles for current provider and reload 
	 * @param usergroups
	 * @param namedParameterJdbcTemplate
	 * @param reload
	 */
	public static void insertUserprofileBatchForCurrentUser (List<Userprofile> userprofiles, NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate, boolean reload) {
		insertUserprofileBatchForUser(getCurrentUserId(namedParameterJdbcTemplate), userprofiles, namedParameterJdbcTemplate, jdbcTemplate, reload);
	}
		// check to make sure we have some Testsegments to work with

	
	public static void insertUserprofileBatchForUser (Long idProvider, List<Userprofile> userprofiles, NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate, boolean reload) {
		if (userprofiles == null) {
			// so we do not throw a null pointer exception
			userprofiles = new ArrayList<Userprofile>();
			// nothing to do here.  simply return
			return;
		}
		
		// we cannot do blanket deletes anymore.  
		// We need to update the userProfile data in 3 steps...
		// 1.) Identify existing userprofile records that have been deleted and are obsolete otherwise and blow them away from the database.
		// 2.) Insert any new userprofiles 
		// 3.) update any existing userprofiles (where there is infact a id_userprofile)
		
		// First split the incoming set into existing and new usertests
		List<Userprofile> newUserprofiles = new ArrayList<Userprofile>();
		List<Userprofile> existingUserprofiles = new ArrayList<Userprofile>();
		StringBuffer existingUserprofilesSB = new StringBuffer();
		boolean isFirst = true;
		for (Userprofile userprofile : userprofiles) {
			if (userprofile.getIdUserprofile() == null || userprofile.getIdUserprofile() == 0l) {
				newUserprofiles.add(userprofile);
			} else {
				existingUserprofiles.add(userprofile);
				if (isFirst) {
					isFirst = false;
				} else {
					existingUserprofilesSB.append(",");
				}
				existingUserprofilesSB.append(userprofile.getIdUserprofile());
			}
		}
		
		// first delete any user profiles from the database that have been deleted on the front end
		// Note that this delete statement also deletes from "userprofiletest" table with FK cascading.
		String sql = "DELETE FROM userprofile WHERE id_provider = " + idProvider;
		if (existingUserprofilesSB.length() > 0)  {
			sql = sql + " AND id_userprofile NOT IN (" + existingUserprofilesSB + ")" ;
		}
		jdbcTemplate.execute(sql);
		
		// next, insert any new usertests
        for (Userprofile userprofile : newUserprofiles) {
        	// hard wire group to the user name for whom the user groups are being created.  So the logged in user can only add groups for himself 
        	userprofile.setIdProvider(idProvider);
        	// set the assignment date to current date
        	userprofile.setProfileAssignmentDate(new Date());
        	
        	// Insert one Userprofile at a time so we can get the "Generated idUserprofile"
            SqlParameterSource insertUserprofileParameters = new BeanPropertySqlParameterSource(userprofile);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(ProfileDao.insertUserprofileSQL, insertUserprofileParameters, keyHolder, new String[]{"ID_USERPROFILE"});
            // id for the new userprofile object
            Long idUserprofile = keyHolder.getKey().longValue();
            
        	// Call a stored procedure to update the userprofiletest table with relevant records....by passing in the newly minted idUserprofile
			SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withCatalogName("rulefree").withProcedureName("update_user_profile_test");
	        SqlParameterSource in = new MapSqlParameterSource().addValue("in_idUserprofile", idUserprofile, Types.NUMERIC).addValue("in_action", 1, Types.NUMERIC);
	        Map out = simpleJdbcCall.execute(in);
	        if ((Integer) out.get("out_status_code") != 0) {
			    System.out.println("Error Message: " + (String) out.get("out_status_message"));
	        }
            
        }
//        if (insertUserprofileParameters.size() > 0) {
//            namedParameterJdbcTemplate.batchUpdate(ProfileDao.insertUserprofileSQL, insertUserprofileParameters.toArray(new SqlParameterSource[0]));
//        }
        
        // next update any existing userprofiles
        List<SqlParameterSource> updateUserprofileParameters = new ArrayList<SqlParameterSource>();
        for (Userprofile userprofile : existingUserprofiles) {
        	userprofile.setIdProvider(idProvider);
        	updateUserprofileParameters.add(new BeanPropertySqlParameterSource(userprofile));
        }
        if (updateUserprofileParameters.size() > 0) {
            namedParameterJdbcTemplate.batchUpdate(ProfileDao.updateUserprofileSQL, updateUserprofileParameters.toArray(new SqlParameterSource[0]));
        }
        
    	// Call a stored procedure to update the userprofiletest table with relevant records....
        for (Userprofile userprofile : existingUserprofiles) {
			SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withCatalogName("rulefree").withProcedureName("update_user_profile_test");
	        SqlParameterSource in = new MapSqlParameterSource().addValue("in_idUserprofile", userprofile.getIdUserprofile(), Types.NUMERIC).addValue("in_action", 3, Types.NUMERIC);
	        Map out = simpleJdbcCall.execute(in);
	        if ((Integer) out.get("out_status_code") != 0) {
			    System.out.println("Error Message: " + (String) out.get("out_status_message"));
	        }
        }

        
	}

        

//	public static void insertUsergroupBatchForAnyUser (List<Usergroup> usergroups, NamedParameterJdbcTemplate namedParameterJdbcTemplate, boolean reload) {
//		// check to make sure we have some Testsegments to work with
//		if (usergroups == null || usergroups.size() == 0) {
//			// nothing to do here.  simply return
//			return;
//		}
//		
//		// all user groups are reloaded for the current username only
//		if (reload) {
//			namedParameterJdbcTemplate.update("call rulefree.delete_usergroup(:username)", new MapSqlParameterSource().
//					addValue("username", username, Types.VARCHAR));
//		}
//		// Cannot do a batch update here since I need to retrieve the testsegment id key to add testSections.  Will have to iterate through the 
//		// list of testsegments
//		//        // first add the testsegments
//		//        namedParameterJdbcTemplate.batchUpdate(TestsegmentDao.insertTestsegmentSQL, testParameters.toArray(new SqlParameterSource[0]));
//        for (Usergroup usergroup : usergroups) {
//        	// hard wire group to the user name for whom the user groups are being created.  So the logged in user can only add groups for himself 
//        	usergroup.setProviderUsername(username);
//        	
//            SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(usergroup);
//            KeyHolder keyHolder = new GeneratedKeyHolder();
//            namedParameterJdbcTemplate.update(UsergroupDao.insertUsergroupSQL, parameterSource, keyHolder, new String[]{"ID_USERGROUP"});
//            // id for the new test object
//            Long idUsergroup = keyHolder.getKey().longValue();
//            // now insert the usergroupmembers
//            if (usergroup.getUsers() != null && usergroup.getUsers().size() > 0) {
//                List<SqlParameterSource> usergroupmemberParameters = new ArrayList<SqlParameterSource>();
//                for (User user : usergroup.getUsers()) {
//                	usergroupmemberParameters.add(new BeanPropertySqlParameterSource(new Usergroupmember(idUsergroup, user.getUsername())));
//                }
//                // Now add the new list of testsections
//                namedParameterJdbcTemplate.batchUpdate(UsergroupDao.insertUsergroupmemberSQL, usergroupmemberParameters.toArray(new SqlParameterSource[0]));
//            }
//        }
//	}
	
	/**
	 * Channel subscriptions update can come from multiple channels.  Uploader when we upload new users.  Or Web Subscriptions (Activation Code) from the frontend.  
	 * @param channelSubscriptions
	 * @param namedParameterJdbcTemplate
	 */
	public static void updateChannelSubscriptionsForCurrentUser (List<ChannelSubscription> channelSubscriptions, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		updateChannelSubscriptionsForUser(getCurrentUserId(namedParameterJdbcTemplate), channelSubscriptions, namedParameterJdbcTemplate);
	}

	/**
	 * Channel subscriptions update can come from multiple channels.  Uploader when we upload new users.  Or Web Subscriptions (Activation Code) from the frontend.  
	 * @param idUser
	 * @param channelSubscriptions
	 * @param namedParameterJdbcTemplate
	 */
	public static void updateChannelSubscriptionsForUser(Long idUser, List<ChannelSubscription> channelSubscriptions, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        if (channelSubscriptions != null && channelSubscriptions.size() > 0) {
            List<SqlParameterSource> insertChannelSubscriptionsParameters = new ArrayList<SqlParameterSource>();
            List<SqlParameterSource> updateChannelSubscriptionsParameters = new ArrayList<SqlParameterSource>();
            for (ChannelSubscription channelSubscription : channelSubscriptions) {
            	// set the student if on the new channelSubscription record
            	channelSubscription.setIdStudent(idUser);
            	// See if there is an existing subscription for this channel 
            	ChannelSubscription databaseChannelSubscription = findStudentChannelSubscription(idUser, channelSubscription.getIdChannel(), namedParameterJdbcTemplate);
            	if (databaseChannelSubscription == null) {
            		// insert a new channel subscription
	            	insertChannelSubscriptionsParameters.add(new BeanPropertySqlParameterSource(channelSubscription));
            	} else {
            		// merge and update channel subscription
            		if (channelSubscription.getStartDate().before(databaseChannelSubscription.getStartDate()) || (channelSubscription.getEndDate().after(databaseChannelSubscription.getEndDate()))) {
	            		if (channelSubscription.getStartDate().before(databaseChannelSubscription.getStartDate())) {
	            			databaseChannelSubscription.setStartDate(channelSubscription.getStartDate());
	            		}
	            		if (channelSubscription.getEndDate().after(databaseChannelSubscription.getEndDate())) {
	            			databaseChannelSubscription.setEndDate(channelSubscription.getEndDate());
	            		}
	            		// update the new channel subscription
	            		updateChannelSubscriptionsParameters.add(new BeanPropertySqlParameterSource(databaseChannelSubscription));
            		}
            	}
            }
            if (insertChannelSubscriptionsParameters.size() > 0) {
            	namedParameterJdbcTemplate.batchUpdate(UserDao.insertStudentChannelSubscriptionsSQL, insertChannelSubscriptionsParameters.toArray(new SqlParameterSource[0]));
            }
            if (updateChannelSubscriptionsParameters.size() > 0) {
            	namedParameterJdbcTemplate.batchUpdate(UserDao.updateStudentChannelSubscriptionsSQL, updateChannelSubscriptionsParameters.toArray(new SqlParameterSource[0]));
            }
        }
	}

	private static ChannelSubscription findStudentChannelSubscription(Long idStudent, Long idChannel, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        String sql = UserDao.findStudentChannelSubscriptionsSQL;
        BeanPropertyRowMapper<ChannelSubscription> channelSubscriptionRowMapper = BeanPropertyRowMapper.newInstance(ChannelSubscription.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idStudent", idStudent);
        args.put("idChannel", idChannel);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        ChannelSubscription channelSubscription = null;
        try {
        	channelSubscription = namedParameterJdbcTemplate.queryForObject(sql, args, channelSubscriptionRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return channelSubscription;
	}

	public static void updateDerivedSection(Section derivedSection, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(derivedSection);
        namedParameterJdbcTemplate.update(SectionDao.updateSectionSQL, parameterSource);
        insertQuestionsForderivedSection(derivedSection.getIdSection(), derivedSection.getDerivedSectionQuestions(), namedParameterJdbcTemplate);
	}

	public static void insertDerivedSection(Section derivedSection, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		// first order of business, settle on a new Section ID.  
		// Section ID is determined by taking the skill ID, adding 500 to it and funding an available number between 5000 and 9999 more than the (skill ID * 1000).
		// Find max section ID in skill...
		Long idSection = null;
		Long idSkill = derivedSection.getIdSkill();
		if (idSkill == null || idSkill.equals(0l)) {
			// Some effed up.
			return;
		}
		Long idSectionLow = idSkill * 10000 + 5000;
		Long idSectionHigh = idSkill * 10000 + 9999;
		
		String currentMaxDerivedSectionIdForSkillSQL = "SELECT IFNULL(MAX(id_section), 0) FROM section WHERE id_section >= :idSectionLow AND id_section <= :idSectionHigh";
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idSectionLow", idSectionLow);
        args.put("idSectionHigh", idSectionHigh);
        Long currentMaxDerivedSectionIdForSkill = namedParameterJdbcTemplate.queryForObject(currentMaxDerivedSectionIdForSkillSQL, args, Long.class);
        if (currentMaxDerivedSectionIdForSkill == 0) { 
        	idSection = idSectionLow;
        } else {
    		idSection = ++currentMaxDerivedSectionIdForSkill;
        	if (idSection > idSectionHigh) {
        		// too many derived sections
        		idSection = null;
        	} 
        }
        if (idSection == null) {
        	// Some effed up again
        	return;
        }
        derivedSection.setIdSection(idSection);
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(derivedSection);
        namedParameterJdbcTemplate.update(SectionDao.insertSectionSQL, parameterSource);
        insertQuestionsForderivedSection(derivedSection.getIdSection(), derivedSection.getDerivedSectionQuestions(), namedParameterJdbcTemplate);
	}

	public static void insertQuestionsForderivedSection (Long idSection, List<DerivedSectionQuestion> derivedSectionQuestionList, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		if (derivedSectionQuestionList == null) {
			// nothing to do here
			return;
		}

		// Delete any existing Derived Section Question for the DerivedSection
		namedParameterJdbcTemplate.update("call rulefree.delete_questions_for_derivedsection(:idSection)", new MapSqlParameterSource().
				addValue("idSection", idSection, Types.NUMERIC));

		// Add questions 
        List<SqlParameterSource> questionParameters = new ArrayList<SqlParameterSource>();
		for (DerivedSectionQuestion question : derivedSectionQuestionList) {
			question.setIdSection(idSection);
			questionParameters.add(new BeanPropertySqlParameterSource(question));
		}
        namedParameterJdbcTemplate.batchUpdate(SectionDao.insertDerivedSectionQuestionSQL, questionParameters.toArray(new SqlParameterSource[0]));
		
	}
	

	/*************************************************************************************************************************************************
	 * 
	 * Find Any User and Logged-In User information
	 * 
	 ************************************************************************************************************************************************/
//	public static String getCurrentUserName() {
//		Object principal = SecurityContextHolder.getContext()
//				.getAuthentication().getPrincipal();
//		UserDetails userDetails = null;
//		if (principal != null && ANONYMOUS_USER_NAME.equalsIgnoreCase(principal.toString())) {
//			return ANONYMOUS_USER_NAME;
//		}
//		if (principal instanceof UserDetails) {
//			userDetails = (UserDetails) principal;
//		}
//		String userName = userDetails.getUsername();
//		return userName;
//	}

	public static String getCurrentUserName() {
		Object principal = SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		if (principal == null || ANONYMOUS_USER_NAME.equalsIgnoreCase(principal.toString())) {
			return null;
		}
		String userName = null;
		if (principal instanceof UserDetails) {
			userName = ((UserDetails) principal).getUsername();
			if (userName != null && ANONYMOUS_USER_NAME.equalsIgnoreCase(userName.trim())) {
				userName = null;
			}
		} else {
			userName = SecurityContextHolder.getContext()
					.getAuthentication().getName();
		}
		return userName;
	}

	public static Long getCurrentUserId(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		String username = getCurrentUserName();
 		if (username == null || username.trim().length() == 0) {
			return null;
		}
 		if (username.equalsIgnoreCase(ANONYMOUS_USER_NAME)) {
 			return ANONYMOUS_USER_ID;
 		}
		String sql = UserDao.findByUserNameSQL;
		BeanPropertyRowMapper<User> userRowMapper = BeanPropertyRowMapper.newInstance(User.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("username", username);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		User user = null;
		try {
			user = namedParameterJdbcTemplate.queryForObject(sql, args, userRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		return (user == null || user.getIdUser() == null) ? null : user.getIdUser();
	}

	public static User findCurrentUser(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		String username = getCurrentUserName();
		if (username == null || username.equalsIgnoreCase(ANONYMOUS_USER_NAME)) {
			return null;
		}
		return findUserByUsername(namedParameterJdbcTemplate, username);
	}

	public static Webuser findWebuserForUserId(NamedParameterJdbcTemplate namedParameterJdbcTemplate, Long idUser) {
		String sql = UserDao.findWebuserForUserIdSQL;
		BeanPropertyRowMapper<Webuser> webuserRowMapper = BeanPropertyRowMapper.newInstance(Webuser.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idUser", idUser);
    	Webuser webuser = null;
		try {
			webuser = namedParameterJdbcTemplate.queryForObject(sql, args, webuserRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		return webuser;
	}

	public static boolean existsUserByUserId(NamedParameterJdbcTemplate namedParameterJdbcTemplate, Long idUser) {
        String sql = UserDao.findByUserIdSQL;
        BeanPropertyRowMapper<User> userRowMapper = BeanPropertyRowMapper.newInstance(User.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("id_user", idUser);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        User user = null;
        try {
        	user = namedParameterJdbcTemplate.queryForObject(sql, args, userRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return user != null ? true : false;
	}

	public static boolean existsUserByUserName(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String username) {
        String sql = UserDao.findByUserNameSQL;
        BeanPropertyRowMapper<User> userRowMapper = BeanPropertyRowMapper.newInstance(User.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("username", username);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        User user = null;
        try {
        	user = namedParameterJdbcTemplate.queryForObject(sql, args, userRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {
        	e.getStackTrace();
        } catch (Exception e) {
        	e.getStackTrace();
        }
        
        return user != null ? true : false;
	}

	/**
	 * Function created to check if the logged in user is an Admin user 
	 * @param namedParameterJdbcTemplate
	 * @return
	 */
	public static boolean isCurrentUserAnAdminUser(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		// Locate the logged in user
		User user = JdbcDaoStaticHelper.findCurrentUser(namedParameterJdbcTemplate);
		// problem if the user is missing
		if (user == null) {
			return false;
		}
		// problem if the user is not a admin 
		if (user.getAuthorities() != null && user.getAuthorities().contains (UserDao.ROLE_ADMIN)) {
			return true;
		}
		return false;
	}

	/**
	 * Function created to check if the logged in user is an Admin user and has the stated permission
	 * @param namedParameterJdbcTemplate
	 * @param permission
	 * @return
	 */
	public static boolean isCurrentUserAnAdminUserWithPermission(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String permission) {
		// Locate the logged in user
		User user = JdbcDaoStaticHelper.findCurrentUser(namedParameterJdbcTemplate);
		// problem if the user is missing
		if (user == null) {
			return false;
		}
		// problem if the user is not a admin 
		if ((user.getAuthorities() != null && user.getAuthorities().contains (UserDao.ROLE_ADMIN)) &&
			(user.getPermissions() != null && user.getPermissions().contains (permission)))
		{
			return true;
		} 
		
		return false;
	}



	/***********************************************************************************************************************************
	/ Read Site Settings stuff.  
	/***********************************************************************************************************************************/
	// Need to relocate this stuff into a Dao....started this effort (SiteSettingsDao).  But not sure how I can implement that.
	public static String getSiteSetting(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String settingName) {
        String sql = "SELECT * FROM site_settings WHERE setting_name = :settingName";
        BeanPropertyRowMapper<SiteSettings> siteSettingsRowMapper = BeanPropertyRowMapper.newInstance(SiteSettings.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("settingName", settingName);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        SiteSettings siteSetting = null;
        try {
        	siteSetting = namedParameterJdbcTemplate.queryForObject(sql, args, siteSettingsRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return siteSetting == null || siteSetting.getSettingValue() == null || siteSetting.getSettingValue().trim().length() == 0 ? null : siteSetting.getSettingValue().trim();
	}
	
	

}
