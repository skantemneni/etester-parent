package com.etester.data.domain.test.instance;

import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.etester.data.dao.JdbcDataDaoParent;
import com.etester.data.domain.content.core.Section;
import com.etester.data.domain.content.instance.Testinstance;
import com.etester.data.domain.test.JdbcDaoStaticHelper;
import com.etester.data.domain.test.TestConstants;
import com.etester.data.domain.test.TestConstants.TestresponseSaveResponse;
//import com.rf.web.notifications.jms.EtesterMessageSender;
//import com.rf.web.shared.util.test.TestResponse;
//import com.rf.web.shared.util.test.TestsectionResponse;

@Repository
public class JdbcUsertestresponseDao extends JdbcDataDaoParent
		implements UsertestresponseDao {

	public JdbcUsertestresponseDao(DataSource dataSource) {
		super(dataSource);
	}

	private static final String DEFAULT_REPORTS_URL = "http://etester.com/#viewreport:";
	private String reportsURL = null;
	private String getReportsURL() {
		if (this.reportsURL == null) {
			this.reportsURL = JdbcDaoStaticHelper.getSiteSetting(getNamedParameterJdbcTemplate(), "REPORTS_URL");
			if (this.reportsURL == null) {
				this.reportsURL = DEFAULT_REPORTS_URL;
			}
		}
		return this.reportsURL;
	}


	public static final String QUESTION_STATUS_CORRECT = "C";
	public static final String QUESTION_STATUS_WRONG = "W";
	public static final String QUESTION_STATUS_UNANSWERED = "N";
	
//	EtesterMessageSender etesterMessageSender = null;

	
//	/**
//	 * @return the etesterMessageSender
//	 */
//	public EtesterMessageSender getEtesterMessageSender() {
//		return etesterMessageSender;
//	}
//
//	/**
//	 * @param etesterMessageSender the etesterMessageSender to set
//	 */
//	public void setEtesterMessageSender(EtesterMessageSender etesterMessageSender) {
//		this.etesterMessageSender = etesterMessageSender;
//	}

	private Usertest findUsertestByUsertestId(Long idUsertest) {
        BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUsertest", idUsertest);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Usertest usertest = null;
        try {
        	usertest = getNamedParameterJdbcTemplate().queryForObject(UsertestDao.findByUsertestIdSQL, args, usertestRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return usertest;
	}

	@Override
	public Usertestresponse findByUsertestresponseId(Long idUsertestresponse) {
		// Although a user is not needed in theory, I will make this a logged in user function.
		// Test response is dependent on the logged in user.  If a logged in user is not 
		// detected, the response will be null 
		String username = JdbcDaoStaticHelper.getCurrentUserName();
		if (username == null || username.trim().length() == 0) {
			return null;
		}

//		String sql = "SELECT * FROM usertestresponse WHERE id_testresponse = :idTestresponse";
		String sql = "SELECT utr.* FROM usertestresponse utr INNER JOIN usertest ut ON utr.id_usertest = ut.id_usertest	INNER JOIN user u ON ut.id_user = u.id_user	WHERE u.username = :username AND utr.id_usertestresponse = :idUsertestresponse";
		BeanPropertyRowMapper<Usertestresponse> usertestresponseRowMapper = BeanPropertyRowMapper.newInstance(Usertestresponse.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("username", username);
		args.put("idUsertestresponse", idUsertestresponse);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		Usertestresponse usertestresponse = null;
		try {
			usertestresponse = getNamedParameterJdbcTemplate()
					.queryForObject(sql, args, usertestresponseRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		return usertestresponse;
	}

	@Override
	public Usertestresponse findUsertestresponseForUsertest(Long idUsertest) {
		// Test response if wholly dependent on the logged in user.  If a logged in 
		// user is not detected, the response will be null 
		String username = JdbcDaoStaticHelper.getCurrentUserName();
		if (username == null || username.trim().length() == 0) {
			return null;
		}

		String sql = "SELECT utr.* FROM usertestresponse utr INNER JOIN usertest ut ON utr.id_usertest = ut.id_usertest	INNER JOIN user u ON ut.id_user = u.id_user	WHERE u.username = :username AND utr.id_usertest = :idUsertest";
		BeanPropertyRowMapper<Usertestresponse> usertestresponseRowMapper = BeanPropertyRowMapper.newInstance(Usertestresponse.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("username", username);
		args.put("idUsertest", idUsertest);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		Usertestresponse usertestresponse = null;
		try {
			usertestresponse = getNamedParameterJdbcTemplate()
					.queryForObject(sql, args, usertestresponseRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		return usertestresponse;
	}

    /**
     * Invoked by a User/Student to save and submit test responses.
     * Note that saveTestResponse does a lot of stuff.  Here is what happens...
     * 1.) Replace the Student Response with the New one - after checking a bunch of conditions (expected)
     * 2.) If the response has been "SUBMITTED" (usertestresponse is marked complete), mark the corresponding Usertest as "TEST_STATUS_SUBMITTED" else 
     * 2a.) If NOT Submitted, Mark the corresponding Usertest as "TEST_STATUS_STARTED"
     * 3.) If the response has been "SUBMITTED" and the Test has been "Purchased" (not assigned), then AUTO_APPROVE the test.  This is done as follows
     * 		a.) Mark the Usertest as "TEST_STATUS_CORRECTIONS" - meaning the Null Provider has APPROVED the user response
     * 		b.) Archive the Response into Testinstance and such...
     * @param usertestresponse
     * @return
     */
	@Override
	public Integer saveTestResponse(Usertestresponse usertestresponse) {
		Integer responseCode = replaceSavedResponse (usertestresponse);
		if (responseCode == TestresponseSaveResponse.SAVE_SUCCESS.responseCode()) {
	        // if the usertestresponse is marked complete, set the user test as complete also.
	        if (usertestresponse.isCompleted()) {
	        	// See if the usertest is marked auto-grade.
	        	// User test can be auto-graded when it has NO QUESTIONS that need to be "explicitly graded" AND under the following conditions
	        	//  (for now, no tests have Questions that cannot be auto-graded)
	        	// 1.) Test is Purchased on the web site (not provider-assigned) OR
	        	// 2.) Usertest (or Test) is Marked auto-grade

        		Usertest usertest = findUsertestByUsertestId(usertestresponse.getIdUsertest());
        		if (usertest != null) {
	        		if (usertest.getIdProvider() == null || usertest.getIdProvider().equals(0l) || (usertest.getAutoGrade() != null && usertest.getAutoGrade() == 1)) {
	        			responseCode = markUsertestAsGraded(usertestresponse.getIdUsertest());
	        			if (responseCode != TestresponseSaveResponse.SAVE_SUCCESS.responseCode()) {
	        				return responseCode;
	        			}
	        			// We will publish results - no matter, however, we will pass information to say if the results will be visible to users (note that providers can always see them)
	        			if (usertest.getAutoPublishResults() != null && usertest.getAutoPublishResults() == 1) {
	    	    			// run archive test in a thread
	        				// archiveTest(usertestresponse);
	    	    			new Thread(new AnalyticsRunnable().setUsertestresponse(usertestresponse, true)).start();
	        			} else {
	    	    			// run archive test in a thread
	        				// archiveTest(usertestresponse);
	    	    			new Thread(new AnalyticsRunnable().setUsertestresponse(usertestresponse, false)).start();
	        			}
        			} else {
        				markUsertestSubmitted(usertestresponse.getIdUsertest(), TestConstants.TEST_STATUS_SUBMITTED);
        			}
        		}
        	} else {
    			markUsertestWithStatus(usertestresponse.getIdUsertest(), TestConstants.TEST_STATUS_STARTED);
    		}
		}
//		// Write Student Email
//		sendStudentEmailMessage();
		return responseCode;
	}

	private Integer replaceSavedResponse(Usertestresponse usertestresponse) {
		// Test response if wholly dependent on the logged in user.  If a logged in 
		// user is not detected, the response will be null 
		Long idUser = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (idUser == null) {
			// may need to throw an exception here
			return TestresponseSaveResponse.SAVE_FAIL_NO_USER.responseCode();
		}
		// get a hold of the usertest to make sure it is in fact owned by the current user and is not in submitted or archived state.
		Usertest usertest = getUsertest(usertestresponse.getIdUsertest());
		
		if (usertest != null) {
			if (usertest.getIdUser() == null || !usertest.getIdUser().equals(idUser)) {
				return TestresponseSaveResponse.SAVE_FAIL_NOT_OWNED.responseCode();
			}
			if (usertest.getTestStatus() != null && (usertest.getTestStatus().equalsIgnoreCase(TestConstants.TEST_STATUS_SUBMITTED) || usertest.getTestStatus().equalsIgnoreCase(TestConstants.TEST_STATUS_ARCHIVED))) {
				return TestresponseSaveResponse.SAVE_FAIL_PREVIOUSLY_SUBMITTED.responseCode();
			}
			// delete before update
			deleteForUsertest(usertestresponse.getIdUsertest());
			// insert a date if its missing
			if (usertestresponse.getDateSaved() == null) {
				usertestresponse.setDateSaved(new Date());
			}
			// now do the necessary
			SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(usertestresponse);
	        getNamedParameterJdbcTemplate().update(insertUsertestresponseSQL, parameterSource);
	        return TestresponseSaveResponse.SAVE_SUCCESS.responseCode();
		} else {
			return TestresponseSaveResponse.SAVE_FAIL_NO_TEST.responseCode();
		}
	}

	@Override
	public Integer rejectTestResponse(Usertestresponse usertestresponse) {
		markUsertestWithStatus(usertestresponse.getIdUsertest(), TestConstants.TEST_STATUS_STARTED);
		return TestresponseSaveResponse.SAVE_SUCCESS.responseCode();
	}

	@Override
	public Integer archiveTestResponse(Usertestresponse usertestresponse) {
		markUsertestWithStatus(usertestresponse.getIdUsertest(), TestConstants.TEST_STATUS_ARCHIVED);
		return TestresponseSaveResponse.SAVE_SUCCESS.responseCode();
	}

	@Override
	public Integer archiveTestResponse(Long idUsertest) {
		markUsertestWithStatus(idUsertest, TestConstants.TEST_STATUS_ARCHIVED);
		return TestresponseSaveResponse.SAVE_SUCCESS.responseCode();
	}

	@Override
	public Integer approveTestResponse(Usertestresponse usertestresponse) {
		Integer reponseCode = replaceWithApprovedResponse (usertestresponse);
		if (reponseCode == TestresponseSaveResponse.SAVE_SUCCESS.responseCode()) {
//			markUsertestAsGraded(usertestresponse.getIdUsertest(), TestConstants.TEST_STATUS_CORRECTIONS);
			reponseCode = markUsertestAsGraded(usertestresponse.getIdUsertest());
			if (reponseCode != TestresponseSaveResponse.SAVE_SUCCESS.responseCode()) {
				return reponseCode;
			}
			// run archive test in a thread
//			archiveTest(usertestresponse);
			new Thread(new AnalyticsRunnable().setUsertestresponse(usertestresponse, true)).start();
		}
		return reponseCode;
	}

	@Override
	public Integer approveTestResponse(Long idUsertest) {
		// make sure to get the provider name (current logged in user should be a provider (to perform "Approve UserTest action")
		String providerName = JdbcDaoStaticHelper.getCurrentUserName();
		if (providerName == null || providerName.trim().length() == 0) {
			return TestresponseSaveResponse.SAVE_FAIL_NO_USER.responseCode();
		}

		String sql = "SELECT utr.* FROM usertestresponse utr INNER JOIN usertest ut ON utr.id_usertest = ut.id_usertest	INNER JOIN user p ON ut.id_provider = p.id_user WHERE p.username = :providerName AND utr.id_usertest = :idUsertest";
		BeanPropertyRowMapper<Usertestresponse> usertestresponseRowMapper = BeanPropertyRowMapper.newInstance(Usertestresponse.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("providerName", providerName);
		args.put("idUsertest", idUsertest);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		Usertestresponse usertestresponse = null;
		try {
			usertestresponse = getNamedParameterJdbcTemplate()
					.queryForObject(sql, args, usertestresponseRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		if (usertestresponse != null) {
			return (approveTestResponse(usertestresponse));
		} else {
			return TestresponseSaveResponse.SAVE_FAIL_NO_TEST.responseCode();
		}
//		// unknown error
//		return TestresponseSaveResponse.SAVE_FAIL_UNKNOWN.responseCode();
	}

	
//	Fifth ATTEMPT (Feb 04th, 2015) - Attempt Quality stuff included 
	// here is how we define attempt quality...
	//	perfect attempt - intime and correct = 4
	//	inefficient attempt - out of time and correct = 3
	//	bad attempt - out of time and incorrect = 2
	//	bad attempt - out of time and unanswered = 2
	//	wasted attempt - intime and incorrect = 1
	//	wasted attempt - intime and unanswered = 1

	private TestresponseSaveResponse archiveTest(Usertestresponse usertestresponse, boolean publishToStudent) {
			return TestresponseSaveResponse.SAVE_SUCCESS;
//		// Get the test time to answer
//		// lets first try to create a Testresponse object
//		TestResponse testResponse = new TestResponse(usertestresponse.getResponse());
//		
//		// delete any previous instances of the id_usertest in the testinstance table 
//		getNamedParameterJdbcTemplate().update("call rulefree.delete_testinstance(:idUsertest)", new MapSqlParameterSource().
//				addValue("idUsertest", usertestresponse.getIdUsertest(), Types.NUMERIC));
//
//		// create the Testinstance object;
//		String sql = testinstanceSQL;
//		BeanPropertyRowMapper<Testinstance> testinstanceRowMapper = BeanPropertyRowMapper.newInstance(Testinstance.class);
//		Map<String, Object> args = new HashMap<String, Object>();
//		args.put("idUsertest", usertestresponse.getIdUsertest());
//		Testinstance testinstance = null;
//		try {
//			testinstance = getNamedParameterJdbcTemplate()
//					.queryForObject(sql, args, testinstanceRowMapper);
//		} catch (IncorrectResultSizeDataAccessException e) {}
//		if (testinstance == null) {
//			return TestresponseSaveResponse.SAVE_FAIL_CANNOT_ARCHIVE;
//		}
//		
//		boolean isTest = testinstance.getTestType() != null && testinstance.getTestType().equals(TestConstants.TEST_TYPE_TEST);
//		// insert the Testinstance object and get the primary key (id_testinstance)
//		// first enhance the testinstance object with counts
//		testinstance.setCorrectCount(testResponse.getCorrectCount());
//		testinstance.setUnansweredCount(testResponse.getUnansweredCount());
//		testinstance.setWrongCount(testResponse.getWrongCount());
//		testinstance.setUserPoints(testResponse.getCorrectPoints());
//		int realTimeToAnswer = getTimeToAnswerForUsertest (usertestresponse, testResponse.getTimeToAnswer());
//		testinstance.setTimeInSeconds(realTimeToAnswer);
//		// set the archived flag to NO (or 0).
//		testinstance.setArchived(0);
//		// set the isReportAvailableToViewByStudent flag on testInstance to publishToStudent
//		testinstance.setIsReportAvailableToViewByStudent(publishToStudent ? 1 : 0);
//		
//		// update/insert testinstance into the database
//        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(testinstance);
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//        getNamedParameterJdbcTemplate().update(createTestinstanceSQL, parameterSource, keyHolder, new String[]{"id_testinstance"});
//        // id for the new test instance object
//        Long idTestinstance = keyHolder.getKey().longValue();
//        
//        // set the id on testinstance so it will be used in reporting email
//        testinstance.setIdTestinstance(idTestinstance);
//
//        // now look to insert testinstance_section and testinstance_detail records
//        
//        // First insert the testinstance_section records to get their ID's.  Then insert the testinstance_detail using that info
//        List<SqlParameterSource> testinstanceDetailParameters = new ArrayList<SqlParameterSource>();
//        if (testResponse.getTestsectionIdList() != null && testResponse.getTestsectionIdList().size() > 0) {
//        	for (Long idSection : testResponse.getTestsectionIdList()) {
//        		// now create a testsection instance record with base testsection data
//        		// create the Testinstance object;
//        		String sqlTestinstanceSection = testsectioninstanceSQL;
//        		BeanPropertyRowMapper<TestinstanceSection> testinstanceSectionRowMapper = BeanPropertyRowMapper.newInstance(TestinstanceSection.class);
//        		Map<String, Object> argsTestinstanceSection = new HashMap<String, Object>();
//        		argsTestinstanceSection.put("idUsertest", usertestresponse.getIdUsertest());
//        		argsTestinstanceSection.put("idSection", idSection);
//        		TestinstanceSection testinstanceSection = null;
//        		try {
//        			testinstanceSection = getNamedParameterJdbcTemplate()
//        					.queryForObject(sqlTestinstanceSection, argsTestinstanceSection, testinstanceSectionRowMapper);
//        		} catch (IncorrectResultSizeDataAccessException e) {}
//        		if (testinstanceSection == null) {
//        			return TestresponseSaveResponse.SAVE_FAIL_CANNOT_ARCHIVE;
//        		}
//        		// set the idTestinstance
//    			testinstanceSection.setIdTestinstance(idTestinstance);
//    			// set the rest
//        		TestsectionResponse testsectionResponse = testResponse.getTestsectionResponse(idSection);
//        		if (testsectionResponse != null) {
//        			testinstanceSection.setIdSection(idSection);
//        			testinstanceSection.setCorrectCount(testsectionResponse.getCorrectCount());
//        			testinstanceSection.setUserPoints(testsectionResponse.getCorrectPoints());
//        			testinstanceSection.setWrongCount(testsectionResponse.getWrongCount());
//        			// I know this is confusing.  and hence, this comment
//        			// note that the "TimeToAnswer" on the testinstanceSection is set from the "testsectioninstanceSQL" statement when creating the testinstanceSection object
//        			// the testsectionResponse.getTimeToAnswer() is the actual time that was consumed by the section - and hence is set in the setTimeInSeconds field.
//        			testinstanceSection.setTimeInSeconds(testsectionResponse.getTimeToAnswer());
//        			testinstanceSection.setUnansweredCount(testsectionResponse.getUnansweredCount());
////        			testinstanceSection.setReportSubject(???);
////        			testinstanceSection.setPointsPerQuestion(???);
////        			testinstanceSection.setNegativePointsPerQuestion(???);
////        			testinstanceSection.setUnansweredPointsPerQuestion(???);
////        			testinstanceSection.setQuestionStartIndex(???);
////        			testinstanceSection.setSeq(???);
////        			testinstanceSection.setDistributedScoring()
//        			
//        		}
//        		
//        		int expectedTimeInSecondsPerQuestion = testinstanceSection.getTimeToAnswer() * 60 / testinstanceSection.getQuestionCount();
//        		
//        		// update/insert testinstanceSection into the database
//                SqlParameterSource parameterSourceTestsection = new BeanPropertySqlParameterSource(testinstanceSection);
//                KeyHolder keyHolderTestsection = new GeneratedKeyHolder();
//                getNamedParameterJdbcTemplate().update(createTestinstanceSectionSQL, parameterSourceTestsection, keyHolderTestsection, new String[]{"id_testinstance_section"});
//                // id for the new test instance object
//                Long idTestinstanceSection = keyHolderTestsection.getKey().longValue();
//    			// Note that we do not have question id's in the response (may be we should put them in there)
//    			if (isTest) {
//        			// testinstance_detail must only be stored for TESTS - but for now, we allow for everything
////        			List<Long> questionIdList = getQuestionIdListForSection(idSection);
////        			if (questionIdList != null && questionIdList.size() == testsectionResponse.getAnswerSetSize()) {
////        				for (int i = 0; i < questionIdList.size(); i++) {
////        					String questionStatus = testsectionResponse.getQuestionStatus(i) != null ? testsectionResponse.getQuestionStatus(i).toString().substring(0, 1) : "";
////        					String answerText = testsectionResponse.getAnswer(i);
////        					int questionTimeInSeconds = testsectionResponse.getQuestionTime(i);
////        					int attemptQuality = 1;
////        					if (questionTimeInSeconds < expectedTimeInSecondsPerQuestion) {
////        						if (questionStatus.equalsIgnoreCase(QUESTION_STATUS_CORRECT)) {
////        							attemptQuality = 4;
////        						} else { // (questionStatus.equalsIgnoreCase(QUESTION_STATUS_WRONG) || questionStatus.equalsIgnoreCase(QUESTION_STATUS_UNANSWERED)) 
////        							attemptQuality = 1;
////        						}
////        					} else {
////        						if (questionStatus.equalsIgnoreCase(QUESTION_STATUS_CORRECT)) {
////        							attemptQuality = 3;
////        						} else { // (questionStatus.equalsIgnoreCase(QUESTION_STATUS_WRONG) || questionStatus.equalsIgnoreCase(QUESTION_STATUS_UNANSWERED)) 
////        							attemptQuality = 2;
////        						}
////        					}
////        					testinstanceDetailParameters.add(new BeanPropertySqlParameterSource(new TestinstanceDetail (idTestinstance, idTestinstanceSection, idSection, questionIdList.get(i), questionStatus, answerText, questionTimeInSeconds, attemptQuality)));
////        				}
////        			}
//    				
//    				
//    				
//    				List<Long> questionIdList = testsectionResponse.getQuestionIdList();
//        			if (questionIdList != null && questionIdList.size() > 0) {
//        				for (Long idQuestion : questionIdList) {
//        					String questionStatus = testsectionResponse.getQuestionStatus(idQuestion) != null ? testsectionResponse.getQuestionStatus(idQuestion).toString().substring(0, 1) : "";
//        					String answerText = testsectionResponse.getAnswer(idQuestion);
//        					Float userPoints = testsectionResponse.getPointsScored(idQuestion) == null ? 0.0f : testsectionResponse.getPointsScored(idQuestion);
//        					int questionTimeInSeconds = testsectionResponse.getQuestionTime(idQuestion);
//        					int attemptQuality = 1;
//        					if (questionTimeInSeconds < expectedTimeInSecondsPerQuestion) {
//        						if (questionStatus.equalsIgnoreCase(QUESTION_STATUS_CORRECT)) {
//        							attemptQuality = 4;
//        						} else { // (questionStatus.equalsIgnoreCase(QUESTION_STATUS_WRONG) || questionStatus.equalsIgnoreCase(QUESTION_STATUS_UNANSWERED)) 
//        							attemptQuality = 1;
//        						}
//        					} else {
//        						if (questionStatus.equalsIgnoreCase(QUESTION_STATUS_CORRECT)) {
//        							attemptQuality = 3;
//        						} else { // (questionStatus.equalsIgnoreCase(QUESTION_STATUS_WRONG) || questionStatus.equalsIgnoreCase(QUESTION_STATUS_UNANSWERED)) 
//        							attemptQuality = 2;
//        						}
//        					}
//        					testinstanceDetailParameters.add(new BeanPropertySqlParameterSource(new TestinstanceDetail (idTestinstance, idTestinstanceSection, idSection, idQuestion, questionStatus, answerText, userPoints, questionTimeInSeconds, attemptQuality)));
//        				}        				
//        			}
//    			}
//    		}
//    	}
//               
//        // now insert the testinstance_detail records
//        if (testinstanceDetailParameters.size() > 0) {
//        	getNamedParameterJdbcTemplate().batchUpdate(createTestinstanceDetailSQL, testinstanceDetailParameters.toArray(new SqlParameterSource[0]));
//        }
//		
//        // now that we are all done, update attempt quality counts on testinstance and testinstance_section also
//		// call a stored proc to update question and point counts on the test - this will happen on 
//		// every create or update of a test.
//		getNamedParameterJdbcTemplate().update("call rulefree.update_attempt_quality_for_testinstance(:v_in_idTestinstance)", new MapSqlParameterSource().
//				addValue("v_in_idTestinstance", idTestinstance, Types.NUMERIC));
//
//        // now that we are all done, update anal_*_data tables with various statistics...(not the anal_*_rollups)
//		getNamedParameterJdbcTemplate().update("call rulefree.update_anal_data_stats_for_usertest(:v_in_idUsertest)", new MapSqlParameterSource().
//				addValue("v_in_idUsertest", usertestresponse.getIdUsertest(), Types.NUMERIC));
//
//        // Update Percentiles for test....
//        // Note that "Test Percentiles" should have been previously calculated for a selected sample set".  This procedure simply updates the 
//        // percentiles wrt. the sample set. 
//		getNamedParameterJdbcTemplate().update("call rulefree.set_all_percentiles_for_usertest(:v_in_idUsertest)", new MapSqlParameterSource().
//							addValue("v_in_idUsertest", usertestresponse.getIdUsertest(), Types.NUMERIC));
//        
//		// Write a notification message to JMS for delivery to user
//		if (publishToStudent) {
//			sendStudentTestReportEmailMessage(testinstance);
//		}
//
//		return TestresponseSaveResponse.SAVE_SUCCESS;
	}
	
	/** 
	 * Note that this is a kludgy solution for now to calculate the actual time taken to answer a test.
	 * This becomes import testresponses usually save "timeLeft" for "Timed" tests.  So we have to calculate 
	 * TimeTaken by subtracting the "TimeLeft" from the TotalTestTime (only for Timed tests).
	 * @param usertestresponse usertestresponse to the test
	 * @param timeToAnswer time taken value from the testResponse string (correct for Un-Timed test).
	 * @return
	 */
	private int getTimeToAnswerForUsertest(Usertestresponse usertestresponse, int timeToAnswer) {
		// One magical SQL that gives us what we are looking for (TotalTestTime - in minute) for TimedTests and -1 for Un-Timed Tests 
		String sql = usertestTimeToAnswerSQL;
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUsertest", usertestresponse.getIdUsertest());
        int timeInMinutes = -1;
        try {
        	timeInMinutes = getNamedParameterJdbcTemplate().queryForObject(sql, args, Integer.class);
        } catch (IncorrectResultSizeDataAccessException e) {
        	// happens when the left join in the query fails to find a test with the id on the usertast.  should never happen...but stranger things have happened
        	timeInMinutes = -1;
        }
        if (timeInMinutes > 0) {
        	return timeInMinutes * 60 - timeToAnswer;
        } else {
        	return timeToAnswer;
        }
	}

	private List<Long> getQuestionIdListForSection(Long idSection) {
		// first we have to figure out if the sectio is derived
		String sqlSection = "SELECT * FROM section WHERE id_section = :idSection";
		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper.newInstance(Section.class);
		Map<String, Object> argsSection = new HashMap<String, Object>();
		argsSection.put("idSection", idSection);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		Section section = null;
		try {
			section = getNamedParameterJdbcTemplate().queryForObject(sqlSection, argsSection, sectionRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		if (section == null) {
			return null;
		}
		String sql = null;
		if (Section.DERIVED_SECTION_TYPE.equalsIgnoreCase(section.getSectionType())) {
			sql = questionIdListForDerivedSection;
		} else {
			sql = questionIdListForSection;
		}
		Map<String, Long> args = new HashMap<String, Long>();
		args.put("idSection", idSection);
		List<Long> results = getNamedParameterJdbcTemplate().queryForList(sql, args, Long.class);
		return results;
	}

	private Integer replaceWithApprovedResponse (Usertestresponse usertestresponse) {
		// Test response if wholly dependent on the logged in user.  If a logged in 
		// user is not detected, the response will be null 
		Long idProvider = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (idProvider == null) {
			// may need to throw an exception here
			return TestresponseSaveResponse.SAVE_FAIL_NO_USER.responseCode();
		}
		// get a hold of the usertest to make sure it is in fact owned by the current user and is not in submitted or archived state.
		Usertest usertest = getUsertest(usertestresponse.getIdUsertest());
		if (usertest != null) {
			if (usertest.getTestStatus().equalsIgnoreCase(TestConstants.TEST_STATUS_CORRECTIONS)) {
				// simply set the usertest so the reports are ready for view by user - so they do not show as provider approval needed.  look at  UsertestDao.findSubmittedUsertestTypesByProviderUsernameSQL and UsertestDao.findAllSubmittedUsertestsByProviderUsernameSQL
				markUsertestAsAvailableForUserProgressReports(usertestresponse.getIdUsertest());
				return TestresponseSaveResponse.SAVE_SUCCESS.responseCode();
			} else {
				// simply set the usertest so the reports are ready for view by user - so they do not show as provider approval needed.  look at  UsertestDao.findSubmittedUsertestTypesByProviderUsernameSQL and UsertestDao.findAllSubmittedUsertestsByProviderUsernameSQL
				markUsertestAsAvailableForUserProgressReports(usertestresponse.getIdUsertest());

				if (usertest.getIdProvider() == null || !usertest.getIdProvider().equals(idProvider)) {
					return TestresponseSaveResponse.SAVE_FAIL_NOT_PROVIDER.responseCode();
				}
				if (usertest.getTestStatus() == null || !usertest.getTestStatus().equalsIgnoreCase(TestConstants.TEST_STATUS_SUBMITTED)) {
					return TestresponseSaveResponse.SAVE_FAIL_NOT_SUBMITTED.responseCode();
				}
				// delete before update
				deleteForUsertest(usertestresponse.getIdUsertest());
				// now do the necessary
				SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(usertestresponse);
		        getNamedParameterJdbcTemplate().update(insertUsertestresponseSQL, parameterSource);
		        return TestresponseSaveResponse.SAVE_SUCCESS.responseCode();
			}
		} else {
			return TestresponseSaveResponse.SAVE_FAIL_NO_TEST.responseCode();
		}
	}

	private Usertest getUsertest(Long idUsertest) {
		if (idUsertest == null) {
			return null;
		}
        String sql = "SELECT * FROM usertest WHERE id_usertest = :idUsertest";
		BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUsertest", idUsertest);
		Usertest usertest = null;
		try {
			usertest = getNamedParameterJdbcTemplate()
					.queryForObject(sql, args, usertestRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		return usertest;
	}

	private void deleteForUsertest(Long idUsertest) {
        String sql = "DELETE FROM usertestresponse WHERE id_usertest = :idUsertest";
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUsertest", idUsertest);
        getNamedParameterJdbcTemplate().update(sql, args);
	}

	// This will happen in a Stored Procedure so we can take of the special case when the usertest is part of a sequence Profile series 
	private Integer markUsertestAsGraded(Long idUsertest) {
//        String sql = "UPDATE usertest set test_status = :teststatus WHERE id_usertest = :idUsertest";
//        Map<String, Object> args = new HashMap<String, Object>();
//        args.put("teststatus", teststatus);
//        args.put("idUsertest", idUsertest);
//        getNamedParameterJdbcTemplate().update(sql, args);
		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("rulefree").withProcedureName("mark_usertest_as_graded");
        SqlParameterSource in = new MapSqlParameterSource().addValue("in_idUsertest", idUsertest, Types.NUMERIC);
        Map out = simpleJdbcCall.execute(in);
        if ((Integer) out.get("out_status_code") != 0) {
		    System.out.println("Error Message: " + (String) out.get("out_status_message"));
		    return TestresponseSaveResponse.SAVE_FAIL_UNKNOWN.responseCode();
        }
        return TestresponseSaveResponse.SAVE_SUCCESS.responseCode();
	}

	private void markUsertestWithStatus(Long idUsertest, String teststatus) {
        String sql = "UPDATE usertest set test_status = :teststatus WHERE id_usertest = :idUsertest";
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("teststatus", teststatus);
        args.put("idUsertest", idUsertest);
        getNamedParameterJdbcTemplate().update(sql, args);
	}

	private void markUsertestSubmitted(Long idUsertest, String teststatus) {
        String sql = "UPDATE usertest SET test_completion_date = :testCompletionDate, test_status = :teststatus WHERE id_usertest = :idUsertest";
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("testCompletionDate", new Date());
        args.put("teststatus", teststatus);
        args.put("idUsertest", idUsertest);
        getNamedParameterJdbcTemplate().update(sql, args);
	}
	
	/**
	 * Set the usertest so the reports are ready for view by user - so they do not show as provider approval needed.  look at  UsertestDao.findSubmittedUsertestTypesByProviderUsernameSQL and UsertestDao.findAllSubmittedUsertestsByProviderUsernameSQL
	 * is_report_available_to_view_by_student will be updated to 1
	 * @param idUsertest
	 */
	private void markUsertestAsAvailableForUserProgressReports(Long idUsertest) {
        String sql = "UPDATE usertest SET is_report_available_to_view_by_student = 1 WHERE id_usertest = :idUsertest";
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUsertest", idUsertest);
        getNamedParameterJdbcTemplate().update(sql, args);
	}
	
	private Testinstance getTestinstanceForMessaging (Long idTestinstance) {
		if (idTestinstance == null) {
			return null;
		}
        String sql = "SELECT * FROM testinstance WHERE id_testinstance = :idTestinstance";
		BeanPropertyRowMapper<Testinstance> testinstanceRowMapper = BeanPropertyRowMapper.newInstance(Testinstance.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTestinstance", idTestinstance);
        Testinstance testinstance = null;
		try {
			testinstance = getNamedParameterJdbcTemplate()
					.queryForObject(sql, args, testinstanceRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		return testinstance;
	}


	class AnalyticsRunnable implements Runnable {
		Usertestresponse usertestresponse;
		boolean publishToStudent = false;
//		AnalyticsRunnable setUsertestresponse(Usertestresponse usertestresponse) {
//			this.usertestresponse = usertestresponse;
//			this.publishToStudent = true;
//			return this;
//		}
		AnalyticsRunnable setUsertestresponse(Usertestresponse usertestresponse, boolean publishToStudent) {
			this.usertestresponse = usertestresponse;
			this.publishToStudent = publishToStudent;
			return this;
		}
	    public void run() {
	    	System.out.println("Starting Archive Usertestresponse for '" + this.usertestresponse.getIdUsertest() + "' in the AnalyticsRunnable Thread!");
	    	archiveTest(this.usertestresponse, this.publishToStudent);
	        System.out.println("Completed Usertestresponse for '" + this.usertestresponse.getIdUsertest() + "' in the AnalyticsRunnable Thread!");
	    }
	}
	
	
	private void sendStudentTestReportEmailMessage(Testinstance testinstance) {
//		// get a new copy of the testinstance from the database...
//		Testinstance testinstanceFromDb = getTestinstanceForMessaging (testinstance.getIdTestinstance());
//		if (testinstanceFromDb == null) {
//			// nothing to do.
//			return;
//		}
//		// get user email address
//		User student = JdbcDaoStaticHelper.findUserByUserId(getNamedParameterJdbcTemplate(), testinstanceFromDb.getIdUser());
//		if (student == null || student.getEmailAddress() == null) {
//			// nothing to do.
//			return;
//		}
//		EmailMessage emailMessage = new EmailMessage();
//		emailMessage.setRecepientName(student.getFirstName());
//		emailMessage.setTestName(testinstanceFromDb.getName());
//		emailMessage.setEmailAddress(student.getEmailAddress().trim());
//		emailMessage.setEmailType(NotificationEmailType.STUDENT_TEST_REPORT_EMAIL);
//		emailMessage.setSubject("Test Report: " + testinstanceFromDb.getName());
//		emailMessage.setLink(getReportsURL() + testinstanceFromDb.getIdTestinstance());
//		Map<String, String> testReportAttributes = new HashMap<String, String>();
//		testReportAttributes.put("submittedDate", testinstanceFromDb.getTestCompletionDate().toString());
//		testReportAttributes.put("marks", testinstanceFromDb.getUserPoints().toString() + " / " + testinstanceFromDb.getPointCount().toString());
//		testReportAttributes.put("correct", testinstanceFromDb.getCorrectCount().toString());
//		testReportAttributes.put("wrong", testinstanceFromDb.getWrongCount().toString());
//		testReportAttributes.put("unanswered", testinstanceFromDb.getUnansweredCount().toString());
//		int timeInMinutes = (testinstanceFromDb.getTimeInSeconds() == null ? 0 : testinstanceFromDb.getTimeInSeconds()) / 60;
//		String timeInHours = timeInMinutes/60 + " Hour(s) and " + timeInMinutes % 60 + " Minute(s)."; 
//		testReportAttributes.put("time", timeInHours);
//		emailMessage.setMessageDetails(testReportAttributes);
//		try {
//			etesterMessageSender.sendEmail(emailMessage);
//		} catch (JMSException e) {
//			System.out.println ("JMS Exception writing Student Report Availability Message: " + e.getMessage());
//		} catch (JmsException e) {
//			System.out.println ("Spring JMS Exception writing Student Report Availability Message: " + e.getMessage());
//		} catch (RuntimeException e) {
//			System.out.println ("Other Runtime Exception writing Student Report Availability Message: " + e.getMessage());
//		}
//		System.out.println ("Completed writing Student Report Availability Message!");
//	}

}
}
