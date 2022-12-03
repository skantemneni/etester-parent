package com.etester.data.domain.test;

import java.io.IOException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.etester.data.dao.JdbcDataDaoParent;
import com.etester.data.domain.content.core.Channel;
import com.etester.data.domain.content.core.JdbcSectionDao;
import com.etester.data.domain.content.core.Section;
import com.etester.data.domain.user.User;
import com.etester.data.domain.user.UserDao;
import com.etester.data.domain.util.RedumptionCode;
import com.etester.data.domain.util.UpdateStatusBean;
import com.etester.data.domain.util.cache.EtesterCacheController;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class JdbcTestDao extends JdbcDataDaoParent implements TestDao {

	public JdbcTestDao(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public Test findByTestId(Long idTest) {
		boolean withSectionDetails = false;
		return findByTestId(idTest, withSectionDetails);
	}

	@Override
	public Test findCompleteTestById(Long idTest) {
		// This may be an expensive opetation.  Falling back to some we use for administration
		//		boolean withSectionDetails = true;
		//		return findByTestId(idTest, withSectionDetails);
		
		// note that this uses a state of Etester at time of query.  If its caching, info comes out of cache.  Otherwise the  test is recreated.
        return locateTestInSystem(idTest);

	}

	private Test findByTestId(Long idTest, boolean withSectionDetails) {
        String sql = findByTestIdSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTest", idTest);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Test test = null;
        try {
        	test = getNamedParameterJdbcTemplate().queryForObject(sql, args, testRowMapper);
        	// massage the published and public attributes
        } catch (IncorrectResultSizeDataAccessException e) {}
        // set testsegments
        if (test != null) {
        	test.setTestsegments(findTestsegmentsForTest(test.getIdTest(), withSectionDetails));
        }
        return test;
	}

	@Override
	public List<Test> findTestsByTestName(String name) {
        String sql = findTestsByTestNameSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("name", name);
        List<Test> tests = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        // get the testsegments associated with all the tests
		if (tests != null && tests.size() > 0) {
			for (int i = 0; i < tests.size(); i++) {
				tests.get(i).setTestsegments(findTestsegmentsForTest(tests.get(i).getIdTest()));
			}
		}
        return tests;
	}

	private Test findCompleteTestByTestId(Long idTest) {
        String sql = findCompleteTestByTestIdSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTest", idTest);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Test test = null;
        try {
        	test = getNamedParameterJdbcTemplate().queryForObject(sql, args, testRowMapper);
        	// massage the published and public attributes
        } catch (IncorrectResultSizeDataAccessException e) {}
        if (test != null) {
        	// set testsegments - the true below states that we need sections, questions and answers also in the response
        	boolean withSectionDetails = true;
        	test.setTestsegments(findTestsegmentsForTest(test.getIdTest(), withSectionDetails));
        }
        return test;
	}

	@Override
	public List<Test> findAllTests() {
//        String sql = "SELECT * FROM test ORDER BY id_test ASC";
        String sql = findAllTestsSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        List<Test> tests = getNamedParameterJdbcTemplate().query(sql, testRowMapper);
        // get the testsegments associated with all the tests
        // NOPE.  Test Segments and Test Sections not needed for this call 
//		if (tests != null && tests.size() > 0) {
//			for (int i = 0; i < tests.size(); i++) {
//				tests.get(i).setTestsegments(findTestsegmentsForTest(tests.get(i).getIdTest()));
//			}
//		}
        return tests;
	}

	@Override
	public List<Test> findAllTestsInChannel(Long idChannel) {
        String sql = findAllTestsInChannelSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idChannel", idChannel);
        List<Test> tests = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        return tests;
	}

	@Override
	public List<Test> findAllTestsInChannelByType(Long idChannel, String testType) {
		if (testType == null || testType.equalsIgnoreCase(TestConstants.TEST_TYPE_ALL)) {
			return this.findAllTestsInChannel(idChannel);
		}
        String sql = findAllTestsInChannelByTypeSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idChannel", idChannel);
        args.put("testType", testType);
        List<Test> tests = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        return tests;
	}

	/*****************************************************************************************************************
	 * Provider-Owned Test queries
	 ****************************************************************************************************************/
	@Override
	public List<Test> findAllEditableTestsOwnedByCurrentProvider() {
		List<Test> resultList = null;
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinProviderId != null) {
			resultList = findAllEditableTestsOwnedByProvider(loggedinProviderId);
		}
		return resultList;
	}

	
	@Override
	public List<Test> findAllEditableTestsOwnedByProvider(Long idProvider) {
//        String sql = "SELECT * FROM test WHERE id_provider = :idProvider ORDER BY id_test ASC";
        String sql = findAllTestsOwnedByProviderSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idProvider", idProvider);
        List<Test> tests = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        // get the testsegments associated with all the tests
        // NOPE.  Test Segments and Test Sections not needed for this call 
//		if (tests != null && tests.size() > 0) {
//			for (int i = 0; i < tests.size(); i++) {
//				tests.get(i).setTestsegments(findTestsegmentsForTest(tests.get(i).getIdTest()));
//			}
//		}
        return tests;
	}

	@Override
	public List<Test> findEditableTestsOwnedByCurrentProviderByType(String testType) {
		List<Test> resultList = null;
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinProviderId != null) {
			resultList = findEditableTestsOwnedByProviderByType(loggedinProviderId, testType);
		}
		return resultList;
	}

	@Override
	public List<Test> findEditableTestsOwnedByProviderByType(Long idProvider, String testType) {
        String sql = findTestsOwnedByProviderByTypeSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idProvider", idProvider);
        args.put("testType", testType);
        List<Test> tests = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        return tests;
	}
	
	/*****************************************************************************************************************
	 * Provider-Owned or Provider's Organization owned Test queries
	 ****************************************************************************************************************/
	@Override
	public List<Test> findAllEditableTestsOwnedByCurrentProviderOrOrganization() {
		List<Test> resultList = null;
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinProviderId != null) {
			resultList = findAllEditableTestsOwnedByProviderOrOrganization(loggedinProviderId);
		}
		return resultList;
	}

	
	@Override
	public List<Test> findAllEditableTestsOwnedByProviderOrOrganization(Long idProvider) {
        String sql = findAllTestsOwnedByProviderOrOrganizationSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idProvider", idProvider);
        List<Test> tests = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        return tests;
	}

	@Override
	public List<Test> findEditableTestsOwnedByCurrentProviderOrOrganizationByType(String testType) {
		List<Test> resultList = null;
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinProviderId != null) {
			resultList = findEditableTestsOwnedByProviderOrOrganizationByType(loggedinProviderId, testType);
		}
		return resultList;
	}

	@Override
	public List<Test> findEditableTestsOwnedByProviderOrOrganizationByType(Long idProvider, String testType) {
        String sql = findTestsOwnedByProviderOrOrganizationByTypeSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idProvider", idProvider);
        args.put("testType", testType);
        List<Test> tests = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        return tests;
	}
	
	/*****************************************************************************************************************
	 * Provider Assignable Test queries
	 ****************************************************************************************************************/
	@Override
	public List<Test> findAllTestsAvailableToAssignForCurrentProvider() {
		List<Test> resultList = null;
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinProviderId != null) {
			resultList = findAllTestsAvailableToAssignForProvider(loggedinProviderId);
		}
		return resultList;
	}

	@Override
	public List<Test> findAllTestsAvailableToAssignForProvider(Long idProvider) {
        String sql = findAllTestsAvailableToAssignForProviderSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idProvider", idProvider);
        args.put("accessLevelPublic", TestConstants.AccessLevelVisibility.PUBLIC.visibility());
        args.put("accessLevelOrganization", TestConstants.AccessLevelVisibility.ORGANIZATION.visibility());
        List<Test> tests = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        // get the testsegments associated with all the tests
        // NOPE.  Test Segments and Test Sections not needed for this call 
//		if (tests != null && tests.size() > 0) {
//			for (int i = 0; i < tests.size(); i++) {
//				tests.get(i).setTestsegments(findTestsegmentsForTest(tests.get(i).getIdTest()));
//			}
//		}
        return tests;
	}

	@Override
	public List<Test> findTestsAvailableToAssignForCurrentProviderByType(String testType) {
		List<Test> resultList = null;
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinProviderId != null) {
			resultList = findTestsAvailableToAssignForProviderByType(loggedinProviderId, testType);
		}
		return resultList;
	}

	@Override
	public List<Test> findTestsAvailableToAssignForProviderByType(Long idProvider, String testType) {
        String sql = findTestsAvailableToAssignForProviderByTypeSQL;
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("testType", testType);
        args.put("idProvider", idProvider);
        args.put("accessLevelPublic", TestConstants.AccessLevelVisibility.PUBLIC.visibility());
        args.put("accessLevelOrganization", TestConstants.AccessLevelVisibility.ORGANIZATION.visibility());
        List<Test> tests = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        // get the testsegments associated with all the tests
        // NOPE.  Test Segments and Test Sections not needed for this call 
//		if (tests != null && tests.size() > 0) {
//			for (int i = 0; i < tests.size(); i++) {
//				tests.get(i).setTestsegments(findTestsegmentsForTest(tests.get(i).getIdTest()));
//			}
//		}
        return tests;
	}

//	@Override
//	public List<Test> findTestsForCurrentUser() {
//		List<Test> resultList = null;
//		String loggedinUsername = JdbcDaoStaticHelper.getCurrentUserName();
//		if (loggedinUsername != null && loggedinUsername.trim().length() > 0) {
//			resultList = findTestsForUsername(loggedinUsername);
//		}
//		return resultList;
//	}

//	@Override
//	public List<Test> findTestsForUsername(String username) {
//        String sql = findTestsForUserNameSQL;
//        BeanPropertyRowMapper<Test> usertestRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
//        Map<String, Object> args = new HashMap<String, Object>();
//        args.put("username", username);
//        List<Test> tests = getNamedParameterJdbcTemplate().query(sql, args, usertestRowMapper);
//        // get the testsegments associated with all the tests
//		if (tests != null && tests.size() > 0) {
//			for (int i = 0; i < tests.size(); i++) {
//				tests.get(i).setTestsegments(findTestsegmentsForTest(tests.get(i).getIdTest()));
//			}
//		}
//        return tests;
//	}

//	@Override
//	public List<Test> findTestsForCurrentUser(String testType) {
//		List<Test> resultList = null;
//		String loggedinUsername = JdbcDaoStaticHelper.getCurrentUserName();
//		if (loggedinUsername != null && loggedinUsername.trim().length() > 0) {
//			resultList = findTestsForUsername(loggedinUsername, testType);
//		}
//		return resultList;
//	}

	/**
	 * Note that test can be created when the provider (logged in user) has the following:
	 * 1.) ROLE_PROVIDER
	 * 2.) Is Associated with an Organization (has an entry in organization_channel) table
	 * 3.) Has UPDATE_TEST or UPDATE_ANY_TEST Permission
	 * 
	 * @param test
	 * @param loggedinProviderId
	 * @return
	 */
//	private Test insert(Test test) {
//		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
//		if (loggedinProviderId == null) {
//			throw new RuntimeException ("Need to be Logged in to Insert or Update Test!");
//		}
//		String permissionsFailure = checkPermissionsForInsertAction(test, loggedinProviderId);
//		if (permissionsFailure != null) {
//			throw new RuntimeException (permissionsFailure);
//		}
//		// set the provider id on the test before getting started - fiorce it to the current login ID
//		test.setIdProvider(loggedinProviderId);
//
//		// TestConstants.TEST_TYPE_TEST are NOT published by default.  They need to be approved.
//		// Note that only published content can be assigned.
//		if (test.getTestType() != null && test.getTestType().equalsIgnoreCase(TestConstants.TEST_TYPE_TEST)) {
//			test.setPublished(TestConstants.AssignableStatus.NOT_PUBLISHED.assignableStatus());
//		} else {
//			test.setPublished(TestConstants.AssignableStatus.PUBLISHED.assignableStatus());
//		}
//		
//		// Note that all Tests and Assignments are created with AccessLevelVisibility.PRIVATE.  Meaning, only 
//		// the owner can see and assign tests to students.  
//		test.setAccessLevel(TestConstants.AccessLevelVisibility.PRIVATE.visibility());
//		
//		// id_test is not a AUTO INCREMENT...call a function to get a new test id
//		String sql = "SELECT get_new_test_id()";
//		Long idTest = getNamedParameterJdbcTemplate().queryForObject(sql, new HashMap<String, Object>(), Long.class);
//		// set the id on the test object and send it on its merry way
//		test.setIdTest(idTest);
//		// call to create the test
//        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(test);
//        if (TestConstants.TEST_TYPE_TEST.equals(test.getTestType())) {
//            getNamedParameterJdbcTemplate().update(insertTestSQL, parameterSource);
//        } else {
//            getNamedParameterJdbcTemplate().update(insertAssignmentSQL, parameterSource);
//        }
//        // now insert/overwrite the testsegments
//        // delete any existing test segments
//        if (test.getTestsegments() != null && test.getTestsegments().size() > 0) {
//        	JdbcDaoStaticHelper.insertTestsegmentsForTest(test.getTestsegments(), idTest, getNamedParameterJdbcTemplate());
//        }
//		// call a stored proc to update question and point counts on the test - this will happen on 
//		// every create or update of a test.
//		getNamedParameterJdbcTemplate().update("call rulefree.update_question_point_counts_time_for_test(:idTest)", new MapSqlParameterSource().
//				addValue("idTest", idTest, Types.NUMERIC));
//		
//        // return the database row post insert
//		return findByTestId (idTest);
//	}

	/**
	 * Note that test can be updated when the provider (logged in user) has the following:
	 * 1.) ROLE_PROVIDER
	 * 2.) Is Associated with an Organization (has an entry in organization_channel) table
	 * 3.) Has UPDATE_TEST or UPDATE_ANY_TEST Permission
	 * 
	 * @param test
	 * @param loggedinProviderId
	 * @return
	 */
	@Override
	public Test updateTest(Test test) {
		return this.updateTest(test, JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate()));
	}
	/**
	 * Note that test can be updated when the provider (logged in user) has the following:
	 * 1.) ROLE_PROVIDER
	 * 2.) Is Associated with an Organization (has an entry in organization_channel) table
	 * 3.) Has UPDATE_TEST or UPDATE_ANY_TEST Permission
	 * 
	 * @param test
	 * @param loggedinProviderId
	 * @return
	 */
	@Override
	public Test updateTest(Test test, Long loggedInProviderId) {
		// make sure the user is logged in.  
		if (loggedInProviderId == null) {
			throw new RuntimeException ("Need to be Logged in to Insert or Update Test!");
		}
		String permissionsFailure = checkPermissionsForUpdateAction(test, loggedInProviderId);
		if (permissionsFailure != null) {
			throw new RuntimeException (permissionsFailure);
		}
		return updateTestInternal(test, loggedInProviderId);
	}
		
	private Test updateTestInternal(Test test, Long loggedInProviderId) {
		// now get the current state of the test....if it exists & set some flags indicating newTest or published test
		boolean isTest = test.getTestType() != null && test.getTestType().equalsIgnoreCase(TestConstants.TEST_TYPE_TEST);
		boolean isPublished = false;
		Test databaseTest = null;
		if (test.getIdTest() != null && test.getIdTest().longValue() != 0l) {
			databaseTest = findByTestId(test.getIdTest());
		}
		if (databaseTest == null) {
			isPublished = false;
		} else {
			isPublished = databaseTest.getPublished() != null && databaseTest.getPublished() == 1;
		}

		
		// TestConstants.TEST_TYPE_TEST are NOT published by default.  They need to be approved.
		// Note that only published content can be assigned.
		// note this is done in SQL where "upsertTestSQL" does not update Published flag
//		if (test.getTestType() != null && test.getTestType().equalsIgnoreCase(TestConstants.TEST_TYPE_TEST)) {
//			test.setPublished(TestConstants.AssignableStatus.NOT_PUBLISHED.assignableStatus());
//		}
//		else {
//			test.setPublished(TestConstants.AssignableStatus.PUBLISHED.assignableStatus());
//		}
		
		// Note that all Tests and Assignments are created with AccessLevelVisibility.PRIVATE.  Meaning, only 
		// the owner can see and assign tests to students.  
		// Also note that AccessLevel cannot be controlled from the front end.  Tests are created "Private Access" and cannot be updated from the front end. (this is done in "upsertTestSQL" and "upsertAssignmentSQL") 
//		test.setAccessLevel(TestConstants.AccessLevelVisibility.PRIVATE.visibility());
		
		// if this is an insert operation (IdTest is null or 0) call the insert operation
		Long idTest = null;
		if (test.getIdTest() == null || test.getIdTest().longValue() == 0l) {
			// id_test is not a AUTO INCREMENT...call a function to get a new test id
			String sql = "SELECT get_new_test_id()";
			idTest = getNamedParameterJdbcTemplate().queryForObject(sql, new HashMap<String, Object>(), Long.class);
			// set the id on the test object and send it on its merry way
			test.setIdTest(idTest);
			// set the provider id on the test to the currently logged in provider
			if (test.getIdProvider() == null || test.getIdProvider() == 0l) {
				test.setIdProvider(loggedInProviderId);
			}
		} else {
			idTest = test.getIdTest();
		}

		// update test metadata
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(test);
        if (TestConstants.TEST_TYPE_TEST.equals(test.getTestType())) {
	        getNamedParameterJdbcTemplate().update(upsertTestSQL, parameterSource);
        } else {
	        getNamedParameterJdbcTemplate().update(upsertAssignmentSQL, parameterSource);
        }
        
        // now insert/overwrite the testsegments
        // delete any existing test segments
		// Only unpublished tests can be updated with new Testsegments
        String cannotUpdateTestSegmentsReason = null;
        // TESTS have to be marked unpublished to be updated.  Assignments do not have to be. (all other conditions apply the same for Tests and Assignments - hasUserTests, hasTestinstances etc)
		if (isTest && isPublished) {
			cannotUpdateTestSegmentsReason = "The test is marked Published.  Only test Meta Data was updated.  Please unpublish the test for a FULL Update.";
		}
		// see if test has been assigned somewhere
		if (cannotUpdateTestSegmentsReason == null) {
			// see if the test has been assigned (has some usertests)
			boolean hasUsertests = databaseTest == null ? false : testHasUsertests(idTest);
			if (hasUsertests) {
				cannotUpdateTestSegmentsReason = "Only test Meta Data of the test was updated.  The test has been assigned to users.  Please delete the Assignments before updating the full test";
			}
		}
		if (cannotUpdateTestSegmentsReason == null) {
			// see if the test has any testinstances (has some testinstances)
			boolean hasTestinstances = databaseTest == null ? false : testHasTestinstances(test.getIdTest());
			if (hasTestinstances) {
				cannotUpdateTestSegmentsReason = "Only test Meta Data of the test was updated.  The test has some testinstances.  Please delete the testinstances before updating the full test";
			}
		}
		if (cannotUpdateTestSegmentsReason == null) {
	        if (test.getTestsegments() != null && test.getTestsegments().size() > 0) {
	        	JdbcDaoStaticHelper.insertTestsegmentsForTest(test.getTestsegments(), test.getIdTest(), getNamedParameterJdbcTemplate());
	        }
		} else {
			throw new RuntimeException (cannotUpdateTestSegmentsReason);
		}
		
		// call a stored proc to update question and point counts on the test - this will happen on 
		// every create or update of a test.
		getNamedParameterJdbcTemplate().update("call rulefree.update_question_point_counts_time_for_test(:idTest)", new MapSqlParameterSource().
				addValue("idTest", idTest, Types.NUMERIC));
        // return the database row post update
        return findByTestId (idTest);
	}

	private boolean testHasTestinstances(Long idTest) {
		String sql = "SELECT count(*) FROM testinstance WHERE id_test = :idTest ";
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idTest", idTest);
		int existsCount = getNamedParameterJdbcTemplate().queryForObject(sql, args, Integer.class);
		if (existsCount > 0) {
			return true;
		} else {
			return false;
		}
	}


	private boolean testHasUsertests(Long idTest) {
		String sql = "SELECT count(*) FROM usertest WHERE id_test = :idTest ";
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idTest", idTest);
		int existsCount = getNamedParameterJdbcTemplate().queryForObject(sql, args, Integer.class);
		if (existsCount > 0) {
			return true;
		} else {
			return false;
		}
	}

//	private String checkPermissionsForInsertAction(Test test, Long loggedinProviderId) {
//		// first locate the user
//		User provider = JdbcDaoStaticHelper.findUserByUserId(getNamedParameterJdbcTemplate(), loggedinProviderId);
//		// problem if the user is missing
//		if (provider == null) {
//			throw new RuntimeException ("Need to be Logged in to Insert or Update Test!");
//		}
//		// problem if the user is not a provider 
//		if (provider.getAuthorities() == null || !provider.getAuthorities().contains (UserDao.ROLE_PROVIDER)) {
//			return "Logged in user is Not a Provider!";
//		}
//		// problem if the user has no permission to UPDATE_TEST (or UPDATE_ANY_TEST)
//		if (provider.getPermissions() == null || (!provider.getPermissions().contains ("UPDATE_TEST") && !provider.getPermissions().contains("UPDATE_ANY_TEST"))) {
//			return "Logged in Provider Does Not have permissions to Create or Update Tests!";
//		}
//		// problem if the user is not associated with an organization or if the associated organization does not 
//		// have permissions to create tests for a channel
//		String sql = validateUpdateActionSQL;
//        Map<String, Object> args = new HashMap<String, Object>();
//        args.put("idSystem", test.getIdChannel());
//        args.put("idUser", loggedinProviderId);
//		int count = getNamedParameterJdbcTemplate().queryForObject(sql, args, Integer.class);
//        if (count <= 0) {
//			return "Provider or the Associated Organization do not have Access to this function.";
//        }
//        // all conditions pass - return true
//		return null;
//	}

	private String checkPermissionsForUpdateAction(Test test, Long loggedInProviderId) {
		// first locate the user
		User provider = JdbcDaoStaticHelper.findUserByUserId(getNamedParameterJdbcTemplate(), loggedInProviderId);
		// problem if the user is missing
		if (provider == null) {
			throw new RuntimeException ("Need to be Logged in to Insert or Update Test!");
		}
		// problem if the user is not a provider 
		if (provider.getAuthorities() == null || !provider.getAuthorities().contains (UserDao.ROLE_PROVIDER)) {
			return "Logged in user is Not a Provider!";
		}
		// problem if the user has no permission to UPDATE_TEST (or UPDATE_ANY_TEST)
		if (provider.getPermissions() == null || (!provider.getPermissions().contains ("UPDATE_TEST") && !provider.getPermissions().contains("UPDATE_ANY_TEST"))) {
			return "Logged in Provider Does Not have permissions to Create or Update Tests!";
		}
		// problem if the user is not associated with an organization or if the associated organization does not 
		// have permissions to create tests for a channel
		String sql = validateUpdateActionSQL;
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idSystem", test.getIdChannel());
        args.put("idUser", loggedInProviderId);
        int count = getNamedParameterJdbcTemplate().queryForObject(sql, args, Integer.class);
        if (count <= 0) {
			return "Provider or the Associated Organization do not have Access to this function on this Channel.";
        }
        
        // one final thing - (only for updates) either the logged in user is the test owner or he has UPDATE_ANY_TEST permission
        if (test.getIdProvider() != null && !test.getIdProvider().equals(0l)) {
        	if (!loggedInProviderId.equals(test.getIdProvider()) &&  !provider.getPermissions().contains("UPDATE_ANY_TEST")) {
        		return "Provider is NOT the Author of the Test and the Provider Does not have UPDATE_ANY_TEST permission.";
        	}
        }
        // all conditions pass - return true
		return null;
	}

	@Override
	public String deleteTest(Long idTest) {
		Test test = null;
		// make sure its a valid test;
		if (idTest == null) {
			// no foul.
			return ("Delete failed.  Test ID is Invalid.");
		} else {
			test = findByTestId(idTest);
			if (test == null) {
				return ("No test/assignment found in the data store with ID: '" + idTest + "'");
			} else {
				return deleteTest(test);
			}
		}
	}

	@Override
	public String deleteTest(Test test) {
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinProviderId == null) {
			return ("User not logged in.  Cannot delete test or assignment!");
		}

		User provider = JdbcDaoStaticHelper.findUserByUserId(getNamedParameterJdbcTemplate(), loggedinProviderId);
		if (!test.getIdProvider().equals(loggedinProviderId) && !provider.getPermissions().contains("UPDATE_ANY_TEST")) {
			return ("Logged in user not owner of the test or assignment and does not have the UPDATE_ANY_TEST permission!");
		} else {
			if (test.getPublished() != null && test.getPublished() == 1) {
				return ("Test: '" + test.getIdTest() + "' is Published.  Please un-publish the test before deleting.");
			} else {
				// This is a stored proc that deleted the following 3 object types:
				// 1.) Test Sections associated with the test
				// 2.) Test Segments associated with the test
				// 3.) The test itself.  
				// 4.) all testinstances and usertests have FK of cascade.  that will delete any instances
				SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("rulefree").withProcedureName("delete_test");
		        SqlParameterSource in = new MapSqlParameterSource().addValue("idTest", test.getIdTest(), Types.NUMERIC);
		        Map out = simpleJdbcCall.execute(in);
		        int deleteStatusCode = (Integer) out.get("status_code");
		        if (deleteStatusCode == 0) {
					return ("Delete Successful for Test/Assignment: '" + test.getIdTest() + "'");
		        } else {
		        	return ("Delete Failed for Test/Assignment: '" + test.getIdTest() + "'.  " + out.get("status_message"));
		        }
			}
		}
	}

	// convenience method to grab skills associated with a topic
	private List<Testsegment> findTestsegmentsForTest (Long idTest) {
		return findTestsegmentsForTest(idTest, false);
	}
	private List<Testsegment> findTestsegmentsForTest (Long idTest, boolean withSectionDetails) {
		String sql = findTestsegmentsForTestSQL;
		BeanPropertyRowMapper<Testsegment> testsegmentRowMapper = BeanPropertyRowMapper.newInstance(Testsegment.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idTest", idTest);
		List<Testsegment> testsegments = getNamedParameterJdbcTemplate()
				.query(sql, args, testsegmentRowMapper);
		if (testsegments != null && testsegments.size() > 0) {
			for (Testsegment testsegment : testsegments) {
				testsegment.setTestsections(findTestsectionsForTestsegment(testsegment.getIdTestsegment(), withSectionDetails));
				testsegment.setTestsynopsislinks(findTestsynopsislinksForTestsegment(testsegment.getIdTestsegment()));
			}
		}
		return testsegments;
	}
	
	private List<Testsection> findTestsectionsForTestsegment (Long idTestsegment, boolean withSectionDetails) {
		String sql = findTestsectionsForTestsegmentSQL;
		BeanPropertyRowMapper<Testsection> sectionRowMapper = BeanPropertyRowMapper.newInstance(Testsection.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idTestsegment", idTestsegment);
		List<Testsection> testsections = getNamedParameterJdbcTemplate().query(sql, args, sectionRowMapper);
		if (withSectionDetails) {
			for (Testsection testsection : testsections) {
				Section section = JdbcSectionDao.findSectionBySectionId(testsection.getIdSectionRef(), getNamedParameterJdbcTemplate());
				// reset the section name to testsection name
				section.setName(testsection.getName());
				// add the Question Points, Negative question Points and Unanswered Question Points from the Test Section
				section.setPointsPerQuestion(testsection.getPointsPerQuestion());
				section.setNegativePointsPerQuestion(testsection.getNegativePointsPerQuestion());
				section.setUnansweredPointsPerQuestion(testsection.getUnansweredPointsPerQuestion());
				// set the questionStartIndex to indicate what question number we should start when rendering the test
				section.setQuestionStartIndex(testsection.getQuestionStartIndex());
				// set the distributedScoring flag to indicate of we need to distribute points among correct parts of the question 
				section.setDistributedScoring(testsection.getDistributedScoring());
				
				// now set the text of the testsection if one is set during testcreation 
				// Note that this is about as lame/inefficient as anything I have done. However, since the test is cached 
				// I hope to not get caught with my pants down
				if (testsection.getInstructionsName() != null && testsection.getInstructionsName().trim().length() > 0) {
					section.setText(getTestsectionInstructionsText(testsection.getInstructionsName()));
				}
				// now set the section on the test section
				testsection.setSection(section);
			}
		}
		return testsections;
	}
	
	private List<Testsynopsislink> findTestsynopsislinksForTestsegment (Long idTestsegment) {
		String sql = findTestsynopsislinksForTestsegmentSQL;
		BeanPropertyRowMapper<Testsynopsislink> testsynopsislinkRowMapper = BeanPropertyRowMapper.newInstance(Testsynopsislink.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idTestsegment", idTestsegment);
		List<Testsynopsislink> testsynopsislinks = getNamedParameterJdbcTemplate().query(sql, args, testsynopsislinkRowMapper);
		return testsynopsislinks;
	}
	
    private String getTestsectionInstructionsText(String instructionsName) {
		String sql = "select * from testsection_instructions where instructions_name = :instructionsName";
		BeanPropertyRowMapper<TestsectionInstructions> testsectionInstructionsRowMapper = BeanPropertyRowMapper.newInstance(TestsectionInstructions.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("instructionsName", instructionsName);
		TestsectionInstructions testsectionInstructions = null;
        try {
        	testsectionInstructions = getNamedParameterJdbcTemplate().queryForObject(sql, args, testsectionInstructionsRowMapper);
        	return testsectionInstructions == null ? null : testsectionInstructions.getText();
        	// massage the published and public attributes
        } catch (IncorrectResultSizeDataAccessException e) {}
        return null;
	}

    /**
     * WARNING:  NO CACHING FOR THIS METHOD
     */
	@Override
	public Test findCompleteTestByTestIdForPrint(Long idTest) {
        Test testFull = null; 
        if (idTest != null) {
            String sql = findCompleteTestByTestIdForPrintSQL;
            BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("idTest", idTest);
    		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
            try {
            	testFull = getNamedParameterJdbcTemplate().queryForObject(sql, args, testRowMapper);
            	// massage the published and public attributes
            } catch (IncorrectResultSizeDataAccessException e) {}
            if (testFull != null) {
            	// set testsegments - the true below states that we need sections, questions and answers also in the response
            	boolean withSectionDetails = true;
            	testFull.setTestsegments(findTestsegmentsForTest(testFull.getIdTest(), withSectionDetails));
            }
        }
//        if (testFull == null) {
//        	// return "test not found"
//        	testFull = new Test();
//        	testFull.setIdTest(-1l);
//        }
       	return testFull;
	}
	
	@Override
	public Test findCompleteTestByUsertestIdForPrint(Long idUsertest) {
//        String sql = findTestByUsertestIdSQL;
        Test test = null;
        String sql = "SELECT t.* FROM usertest ut LEFT JOIN test t ON ut.id_test = t.id_test WHERE ut.id_usertest = :idUsertest";
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUsertest", idUsertest);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        try {
        	test = getNamedParameterJdbcTemplate().queryForObject(sql, args, testRowMapper);
        	// massage the published and public attributes
        } catch (IncorrectResultSizeDataAccessException e) {}

        return findCompleteTestByTestIdForPrint(test == null ? null : test.getIdTest());
        
	}
	
	// Most important function used on administering the test.  It returns a Test object 
    // with any associated response.  Not sure where this needs to be situated - TestDao 
    // or UsaertestDao.  For now, though, I choose to put it here.  
	@Override
	public Optional<TestWithResponse> findTestByUsertestIdWithResponse(Long idUsertest) {
		Long loggedinStudentId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
        // first get the usertest response, uesrtest status and uesrtest test id corresponding to the user test
        // here we use a temporary test object to do that
        TestWithResponse testWithResponse = null;
        BeanPropertyRowMapper<TestWithResponse> testWithResponseRowMapper = BeanPropertyRowMapper.newInstance(TestWithResponse.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUsertest", idUsertest);
        args.put("userid", loggedinStudentId);
        
        // queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        String sql = findTestByUsertestIdWithResponseSQL;
        try {
        	testWithResponse = getNamedParameterJdbcTemplate().queryForObject(sql, args, testWithResponseRowMapper);
        	// massage the published and public attributes
        } catch (IncorrectResultSizeDataAccessException e) {}
        
        // now locate the actual test...
        if (testWithResponse != null) {
        	Test test = locateTestInSystem(testWithResponse.getIdTest());
        	if (test == null) {
        		testWithResponse.setIdTest(-1l);
        	} else {
        		testWithResponse.setTest(test);
        	}
        }
        return Optional.ofNullable(testWithResponse);
	}
	
	// Used by the provider while grading the test.  
	@Override
	public TestWithResponse findProviderTestByUsertestIdWithResponse(Long idUsertest) {
        TestWithResponse testWithResponse = null;
        String sql = findProviderTestByUsertestIdWithResponseSQL;
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
        BeanPropertyRowMapper<TestWithResponse> testWithResponseRowMapper = BeanPropertyRowMapper.newInstance(TestWithResponse.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUsertest", idUsertest);
        args.put("providerid", loggedinProviderId);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        try {
        	testWithResponse = getNamedParameterJdbcTemplate().queryForObject(sql, args, testWithResponseRowMapper);
        	// massage the published and public attributes
        } catch (IncorrectResultSizeDataAccessException e) {}
        if (testWithResponse == null) {
        	// return "test not found"
        	testWithResponse = new TestWithResponse();
        	testWithResponse.setIdTest(-1l);
        	return testWithResponse;
        }
        // now locate the actual test...
        Test test = locateTestInSystem(testWithResponse.getIdTest());
        if (test == null) {
        	testWithResponse.setIdTest(-1l);
        	return testWithResponse;
        }
        testWithResponse.setTest(test);
        return testWithResponse;
	}

	/**
	 * This is the function that takes the onus of locating the test. Here is how it does it:
	 * 1.) If its available in EtesterCache, it will simply return it.  - This should be the case 99.9% of the time
	 * 2.) If not, returns a Reconstituted test from the serialized version (after saving a copy of it to the EtesterCache)
	 * 		- note that we do this because we would always use the serialized version - helps with test versioning
	 * 3.) If not,  creates a Complete test from scratch, puts it in EtesterCache, serializes it and saves it to database and return it. 
	 * @param idTest
	 * @return test
	 */
    private Test locateTestInSystem(Long idTest) {
        // see if it already exists in cache
    	if (EtesterCacheController.isCaching()) {
    		Test test = EtesterCacheController.getTest(idTest);
    		if (test != null) {
    			System.out.println("Found Test in Cache for Idtest: " + idTest);
    			return test;
    		} 
    		// else, see if there is a serialized version that can be reconstituted
    		// create a test from a serialized version of the test
    		Test reconstitutedTest = findTestFromSerializedTestByTestId(idTest);
    		if (reconstitutedTest != null) {
    			// Add the test to cache before sending it back
    			EtesterCacheController.putTest(reconstitutedTest);
    			// now return the reconstituted test
    			return reconstitutedTest;
    		}
            // Now create the Complete test from querying for various pieces in the database
            Test freshTest = findCompleteTestByTestId(idTest);
            if (freshTest != null) {
                // Add the test to cache before sending it back
                EtesterCacheController.putTest(freshTest);
                // Also serialize it and put into the database in serialized form
                saveSerializedTest (freshTest);
                return freshTest;
            } else {
            	return null;
            }
    	} else {
    		return findCompleteTestByTestId(idTest);
    	}
	}

	private Test findTestFromSerializedTestByTestId(Long idTest) {
        Test deserializedTest = null;
		String sql = findSerializedtestByTestId;
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTest", idTest);
		// first try to locate a SerializedTest that may already exist in the system
        BeanPropertyRowMapper<SerializedTest> serializedtestRowMapper = BeanPropertyRowMapper.newInstance(SerializedTest.class);
        SerializedTest serializedTest = null;
        try {
        	serializedTest = getNamedParameterJdbcTemplate().queryForObject(sql, args, serializedtestRowMapper);
    		System.out.println("Found in Database: " + serializedTest.getTestStringJson());
        	// massage the published and public attributes
        } catch (IncorrectResultSizeDataAccessException e) {}
        if (serializedTest == null) {
        	return null;
        } 
        // reconstitute the test and return it.
        String completeTestStringJson = serializedTest.getTestStringJson();
        ObjectMapper mapper = new ObjectMapper();
        try {
        	deserializedTest =  mapper.readValue(completeTestStringJson, Test.class);
        } catch (JsonGenerationException e) {
           e.printStackTrace();
        } catch (JsonMappingException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        }
        
        return deserializedTest;
	}

	//	@Override
//	public SerializedTest findSerializedTestByUsertestIdWithResponse(Long idUsertest) {
//        SerializedTest serializedTest = null;
//		String sql = null;
//		Long loggedinStudentId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
//        Map<String, Object> args = new HashMap<String, Object>();
//        args.put("idUsertest", idUsertest);
//        args.put("userid", loggedinStudentId);
//		// first try to locate a SerializedTest that may already exist in the system
//        sql = findSerializedtestByUsertestIdWithResponseSQL;
//		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
//
//        BeanPropertyRowMapper<SerializedTest> serializedtestRowMapper = BeanPropertyRowMapper.newInstance(SerializedTest.class);
//        try {
//        	serializedTest = getNamedParameterJdbcTemplate().queryForObject(sql, args, serializedtestRowMapper);
//        	// massage the published and public attributes
//        } catch (IncorrectResultSizeDataAccessException e) {}
//        if (serializedTest != null) {
//        	// Done.  Found it.  Return.  - Most common scenario.
//    		System.out.println("Found in Database: " + serializedTest.getTestStringJson());
//        	return serializedTest;
//        } else {
//        	sql = findTestByUsertestIdWithResponseSQL;
//        	// The serialized test may not have been created yet
//            BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
//    		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
//            Test test = null;
//            try {
//            	test = getNamedParameterJdbcTemplate().queryForObject(sql, args, testRowMapper);
//            	// massage the published and public attributes
//            } catch (IncorrectResultSizeDataAccessException e) {}
//            
//            if (test == null) {
//            	// oops Test is not found for the "user"
//            	// return "serializedTest not found"
//            	serializedTest = new SerializedTest();
//            	serializedTest.setIdTest(-1l);
//            	return serializedTest;
//            }
//            
//            Test completeTest  = findCompleteTestByTestId(test.getIdTest());
//           	if (completeTest == null) {
//            	// oops Complete Test is not found for the "user" - Should NEVER happen
//            	// return "serializedTest not found"
//            	serializedTest = new SerializedTest();
//            	serializedTest.setIdTest(-1l);
//            	return serializedTest;
//            }
//           	// found a complete test.  Serialize it....
//            String completeTestStringJson = null;
//
////            DefaultProxyStore testStore = new DefaultProxyStore();
////			ProxySerializer testSerializer = xqeeRequestFactory.getSerializer(testStore);
////			// More than one proxy could be serialized
////			String key = testSerializer.serialize(testProxy);
////			// Create the flattened representation
////			completeTestStringJson = testStore.encode();
//
//    		ObjectMapper mapper = new ObjectMapper();
//    		try {
//    			completeTestStringJson = mapper.writeValueAsString(completeTest);
//        		System.out.println("Newly Minted: " + completeTestStringJson);
//    		} catch (JsonGenerationException e) {
//    			// TODO Auto-generated catch block
//    			e.printStackTrace();
//    		} catch (JsonMappingException e) {
//    			// TODO Auto-generated catch block
//    			e.printStackTrace();
//    		} catch (IOException e) {
//    			// TODO Auto-generated catch block
//    			e.printStackTrace();
//    		}
//           	if (completeTestStringJson == null) {
//            	// oops Complete Test is not found for the "user" - Should NEVER happen
//            	// return "serializedTest not found"
//            	serializedTest = new SerializedTest();
//            	serializedTest.setIdTest(-1l);
//            	return serializedTest;
//            }
//    		// Save it to the database for future use 
//    		saveSerializedTest (completeTest.getIdTest(), completeTestStringJson);
//    		// Compose and return a Serialized test
//           	serializedTest = new SerializedTest();
//    		serializedTest.setIdTest(test.getIdTest());
//    		serializedTest.setTestStringJson(completeTestStringJson);
//    		serializedTest.setTestResponse(test.getTestResponse());
//    		serializedTest.setTestStatus(test.getTestStatus());
//    		return serializedTest;
//        }
//		
//	}

	/**
	 * 
	 * @param test
	 */
	private void saveSerializedTest (Test test) {
        String completeTestStringJson = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			completeTestStringJson = mapper.writeValueAsString(test);
			System.out.println("Newly Minted: " + completeTestStringJson);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SerializedTest serializedTest = new SerializedTest();
		serializedTest.setIdTest(test.getIdTest());
		serializedTest.setTestStringJson(completeTestStringJson);
		serializedTest.setDateSaved(new Date());
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(serializedTest);
        try {
        	getNamedParameterJdbcTemplate().update(insertSerializedtestSQL, parameterSource);
        } catch (DataAccessException dae) {
        	dae.printStackTrace();
        }
	}

//	@Override
//	public TestWithResponse findTestByUsertestIdWithResponse(Long idUsertest) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public TestWithResponse findProviderTestByUsertestIdWithResponse(
//			Long idUsertest) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public TestWithResponse findByTestWithResponseId(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

//	private void saveSerializedTest(Long idTest, String completeTestStringJson) {
//		SerializedTest serializedTest = new SerializedTest();
//		serializedTest.setIdTest(idTest);
//		serializedTest.setTestStringJson(completeTestStringJson);
//    	SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(serializedTest);
//    	getNamedParameterJdbcTemplate().update(insertSerializedtestSQL, parameterSource);
//	}
	
	
	
	
	
	// Declare a dataformat for the next function to use
	private static final DateFormat freeTrialDateFormat = new SimpleDateFormat("d MMM yyyy");
	/**
	 * This method returns mock exams that are available for users to attempt - for a given exam track.  
	 * Example examtrack values can be "EAMCETEngineering", "", "", "", "", etc..
	 * @param examtrack
	 */
	@Override
	public List<Test> findMockExamsByTrack(String examtrack) {
		// note that this functionality will be different for subscribers....
		// see if the user is logged in and is a subscriber 
		User user = JdbcDaoStaticHelper.findCurrentUser(getNamedParameterJdbcTemplate());
		
      String sql = findAllMockExamsInTrackSQL;
      BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
      Map<String, Object> args = new HashMap<String, Object>();
      args.put("examtrack", examtrack);
      List<Test> tests = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
      // compose any free messages - if the test is scheduled for a future free preview...
      Date currentDate = new Date();
      for (Test loopTest : tests) {
    	  // do logic only for tests that are not already free for all
    	  if (loopTest.getIsFree() != null && loopTest.getIsFree() == 0) {
    		  // if end date is not null and set to a value greater than today (preview period has not ended)
    		  boolean userHasSubscriptionToTestChannel = hasSubscription(user, loopTest.getIdChannel());
        	  if ((loopTest.getDateFreeEnd() != null && loopTest.getDateFreeEnd().after(currentDate)) ||
        			  (userHasSubscriptionToTestChannel && (loopTest.getSubscriptionDateFreeEnd() != null && loopTest.getSubscriptionDateFreeEnd().after(currentDate)))){
        		  if (loopTest.getDateFreeStart() == null || loopTest.getDateFreeStart().before(currentDate)) {
        			  // active free test period...
        			  // set isFree = 1 to mak the test free
        			  loopTest.setFreeMessage("Try Now!!!");
        			  if (userHasSubscriptionToTestChannel && loopTest.getSubscriptionDateFreeEnd() != null) {
        				  int diffInDays = (int)( (loopTest.getSubscriptionDateFreeEnd().getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24) );
           				  loopTest.setFreeMessageMore("Subscription ends in " + diffInDays + " days");
        			  } else {
        				  int diffInDays = (int)( (loopTest.getDateFreeEnd().getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24) );
            			  if (diffInDays < 30) {
            				  loopTest.setFreeMessageMore("Trial ends in " + diffInDays + " days");
            			  }
        			  }
        			  // set the addl More Message only if the trial ends in less than 30 days
        			  // Indicate the test as free
        			  loopTest.setIsFree(1);
        		  } else {
        			  // future free test period
        			  int diffInDays = (int)( (loopTest.getDateFreeStart().getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24) );
        			  if (diffInDays < 30) {
        				  loopTest.setFreeMessage("Trial Starts " + freeTrialDateFormat.format(loopTest.getDateFreeStart()));
        				  loopTest.setFreeMessageMore(diffInDays + " days to go...");
        			  } else {
        				  loopTest.setFreeMessage("Coming Soon...");
        				  loopTest.setFreeMessage("Free trial starts " + freeTrialDateFormat.format(loopTest.getDateFreeStart()));
        			  }
        		  }
        		  
        		  
        		  
        	  }
    	  }
      }
      return tests;
	}
	
	/**
	 * Checks is the user has subscription to a channel
	 * @param user
	 * @param idChannel
	 * @return
	 */
	private boolean hasSubscription(User user, Long idChannel) {
		if (user == null || idChannel == null) {
			return false;
		} else {
			boolean hasSubscription = false;
			if (user.getSubscriptions() != null) {
				for (Channel channel : user.getSubscriptions()) {
					if (idChannel.equals(channel.getIdSystem())) {
						hasSubscription = true;
						break;
					}
				}
			}
			return hasSubscription;
		}
	}

	private RedumptionCode getRedumptionCodeForCode(String code) {
		String sql = "SELECT * FROM redumption_code WHERE redumption_code = :code";
		BeanPropertyRowMapper<RedumptionCode> redumptionCodesRowMapper = BeanPropertyRowMapper.newInstance(RedumptionCode.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("code", code == null || code.trim().length() == 0 ? "" : code.trim());
		RedumptionCode redumptionCode = null;
		try {
			redumptionCode = getNamedParameterJdbcTemplate().queryForObject(sql, args, redumptionCodesRowMapper);
			// massage the published and public attributes
		} catch (IncorrectResultSizeDataAccessException e) {}
		if (redumptionCode != null) {
			// set any test restrictions
			String query = "SELECT id_test FROM redumption_code_test WHERE redumption_code = :code"; 
			List<Long> testRestrictions = (List<Long>) getNamedParameterJdbcTemplate().queryForList(query, args, Long.class);
			redumptionCode.setTestRestrictions(testRestrictions);
		}
		return redumptionCode;
	}

	@Override
	public String activateTest(Long idTest, String code) {
		// first get the logged in user id.
		Long loggedinStudentId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinStudentId == null || loggedinStudentId.equals(JdbcDaoStaticHelper.ANONYMOUS_USER_ID)) {
			return "Please Login before Activating an Exam";
		}

		// validate to make sure the user does not already have the test
		String sql = "SELECT count(*) FROM usertest WHERE id_test = :idTest AND id_user = :loggedinStudentId ";
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idTest", idTest);
		args.put("loggedinStudentId", loggedinStudentId);
		int existsCount = getNamedParameterJdbcTemplate().queryForObject(sql, args, Integer.class);
		
		if (existsCount > 0) {
//			return "You already have this Exam.  Please check your 'My Zone' menu";
			return "You already have this Exam.  Please check your <span style=\"font-weight:bold; color:green;\">'My Zone'</span> menu";
		}
		
		// Its acceptable to get null codes for free test activations
		if (code == null || code.trim().length() == 0) {
//			String isFreeSql = "SELECT is_free FROM test WHERE id_test = :idTest ";
//			Map<String, Object> argsFree = new HashMap<String, Object>();
//			args.put("idTest", idTest);
//			int isFree = getNamedParameterJdbcTemplate().queryForInt(isFreeSql, argsFree);
//			if (isFree == 0) {
//				return "Test is not marked free.  Please purchase the test or enter a valid Activation Code.";
//			} else {
//				
//			}
				
			MapSqlParameterSource in = new MapSqlParameterSource();
			in.addValue("v_idTest", idTest, Types.NUMERIC);
			in.addValue("v_idUser", loggedinStudentId, Types.NUMERIC);
//			getNamedParameterJdbcTemplate().update("call rulefree.activate_usertest_for_free(:v_idTest, :v_idUser, :v_redumptionCode)", 
//					in);
			SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(getJdbcTemplate()).withProcedureName("activate_usertest_for_free");
			Map<String, Object> out = simpleJdbcCall.execute(in);
			String status = (String) out.get("v_status");
			if (status != null && !status.equalsIgnoreCase("success")) {
				return status;
			} else {
				return null;
			}
		} else {

			// First order of business, upper case the code
			code = code.toUpperCase();
			
			RedumptionCode redumptionCode = getRedumptionCodeForCode(code);
			// is there a valid code that matches the entered code
			if (redumptionCode == null) {
				return "Code is Invalid.";
			}
			// has the code been redeemed already?
			if (redumptionCode.getRedeemed() == 1
					|| redumptionCode.getCurrentUses() == redumptionCode
							.getTotalUses()) {
				return "Code has been used already.";
			}
			// is the code currently active?
			if (redumptionCode.getRedeemed() == 1
					|| redumptionCode.getCurrentUses() == redumptionCode
							.getTotalUses()) {
				return "Code has been used already.";
			}
			// is the code valid for the test - is the code test restricted to
			// include the current test

			// if (redumptionCode.getRedeemed() == 1 ||
			// redumptionCode.getCurrentUses() == redumptionCode.getTotalUses()) {
			// return "Code is not Active.";
			// }
			//
			if (redumptionCode.getTestRestrictions() != null
					&& redumptionCode.getTestRestrictions().size() > 0
					&& !redumptionCode.getTestRestrictions().contains(idTest)) {
				return "Code is NOT VALID for Exam.";
			}
			// Finally assign user test, increment redumption_uses and return success 
			// call a stored proc to update question and point counts on the test - this will happen on 
			// every create or update of a test.
			MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
			mapSqlParameterSource.addValue("v_idTest", idTest, Types.NUMERIC);
			mapSqlParameterSource.addValue("v_idUser", loggedinStudentId, Types.NUMERIC);
			mapSqlParameterSource.addValue("v_redumptionCode", code, Types.VARCHAR);
			getNamedParameterJdbcTemplate().update("call rulefree.activate_usertest_with_redumption(:v_idTest, :v_idUser, :v_redumptionCode)", 
					mapSqlParameterSource);

			return null;
		}
	}
	
	
	
	/*************************************************************************************************************************
	 * Upload/Download related functions
	 *************************************************************************************************************************/
	@Override
	public List<Test> findTestsForChannelProviderAndTypeForDownload(Long idChannel, Long idProvider, String testType) {
		List<Test> testList = null;
		Map<String, Object> args = new HashMap<String, Object>();
		String sql = null;
		if (testType == null || testType.equalsIgnoreCase(TestConstants.ALL_TEST_TYPES)) {
	        sql = "SELECT * FROM test WHERE id_channel = :idChannel AND id_provider = :idProvider ";
	        args.put("idChannel", idChannel);
	        args.put("idProvider", idProvider);
		} else {
	        sql = "SELECT * FROM test WHERE id_channel = :idChannel AND id_provider = :idProvider AND test_type = :testType";
	        args.put("idChannel", idChannel);
	        args.put("idProvider", idProvider);
	        args.put("testType", testType);
		}
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        testList = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        // get the testsegments associated with all the tests
		if (testList != null && testList.size() > 0) {
			for (int i = 0; i < testList.size(); i++) {
				testList.get(i).setTestsegments(findTestsegmentsForTest(testList.get(i).getIdTest()));
			}
		}
        return testList;
	}

	@Override
	public List<Test> findTestsForChannelAndTypeForDownload(Long idChannel, String testType) {
		List<Test> testList = null;
		Map<String, Object> args = new HashMap<String, Object>();
		String sql = null;
		if (testType == null || testType.equalsIgnoreCase(TestConstants.ALL_TEST_TYPES)) {
	        sql = "SELECT * FROM test WHERE id_channel = :idChannel";
	        args.put("idChannel", idChannel);
		} else {
	        sql = "SELECT * FROM test WHERE id_channel = :idChannel AND test_type = :testType";
	        args.put("idChannel", idChannel);
	        args.put("testType", testType);
		}
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        testList = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        // get the testsegments associated with all the tests
		if (testList != null && testList.size() > 0) {
			for (int i = 0; i < testList.size(); i++) {
				testList.get(i).setTestsegments(findTestsegmentsForTest(testList.get(i).getIdTest()));
			}
		}
        return testList;
	}

	@Override
	public List<Test> findTestsForProviderAndTypeForDownload(Long idProvider, String testType) {
		List<Test> testList = null;
		Map<String, Object> args = new HashMap<String, Object>();
		String sql = null;
		if (testType == null || testType.equalsIgnoreCase(TestConstants.ALL_TEST_TYPES)) {
	        sql = "SELECT * FROM test WHERE id_provider = :idProvider";
	        args.put("idProvider", idProvider);
		} else {
	        sql = "SELECT * FROM test WHERE id_provider = :idProvider AND test_type = :testType";
	        args.put("idProvider", idProvider);
	        args.put("testType", testType);
		}
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        testList = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        // get the testsegments associated with all the tests
		if (testList != null && testList.size() > 0) {
			for (int i = 0; i < testList.size(); i++) {
				testList.get(i).setTestsegments(findTestsegmentsForTest(testList.get(i).getIdTest()));
			}
		}
        return testList;
	}

	@Override
	public List<Test> findTestsForTestIdListForDownload(List<Long> testIdList) {
		if (testIdList == null || testIdList.size() == 0) {
			return null;
		}
		StringBuffer idTestListBuffer = new StringBuffer();
		boolean first = true;
		for (Long idTest : testIdList) {
			if (first) {
				idTestListBuffer.append(idTest);
				first = false;
			} else {
				idTestListBuffer.append (",").append(idTest);
			}
		}
		List<Test> testList = null;
		Map<String, Object> args = new HashMap<String, Object>();
		String sql = null;
        sql = "SELECT * FROM test WHERE id_test in (:idTestList)";
        String testListString = idTestListBuffer.toString();
	    args.put("idTestList", testListString);
        BeanPropertyRowMapper<Test> testRowMapper = BeanPropertyRowMapper.newInstance(Test.class);
        testList = getNamedParameterJdbcTemplate().query(sql, args, testRowMapper);
        // get the testsegments associated with all the tests
		if (testList != null && testList.size() > 0) {
			for (int i = 0; i < testList.size(); i++) {
				testList.get(i).setTestsegments(findTestsegmentsForTest(testList.get(i).getIdTest()));
			}
		}
        return testList;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Upload tests functionality
	// Begin....
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public List<UpdateStatusBean> insertTestBatch(List<Test> tests, boolean reload) {
		return insertTestBatch(tests, getNamedParameterJdbcTemplate(), getJdbcTemplate(), reload);
	}

	@Override
	public List<UpdateStatusBean> insertTestBatch(List<Test> tests) {
		return insertTestBatch(tests, false);
	}

	public List<UpdateStatusBean> insertTestBatch (List<Test> tests, NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate, boolean reload) {
		List<UpdateStatusBean> returnStatuses = new ArrayList<UpdateStatusBean>();
		if (tests != null && tests.size() > 0) {
			for (Test test : tests) {
				UpdateStatusBean updateStatus = updateTestFromUpload (test, namedParameterJdbcTemplate, jdbcTemplate, reload);
				returnStatuses.add(updateStatus);
			}
		}
		return returnStatuses;
	}

	public UpdateStatusBean updateTestFromUpload (Test test, NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate, boolean reload) {	
        // if reload flag is set, delete the data prior to loading again
		// if reload flag is set, delete the data prior to loading again
		int deleteStatusCode = 0;
		if (reload) {
			// delete the section if one already exists
//			JdbcTemplate simpleJdbcTemplate = new JdbcTemplate(dataSource);
			SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withCatalogName("rulefree").withProcedureName("delete_test");
	        SqlParameterSource in = new MapSqlParameterSource().addValue("idTest", test.getIdTest(), Types.NUMERIC);
	        Map out = simpleJdbcCall.execute(in);
	        deleteStatusCode = (Integer) out.get("status_code");
		    System.out.println("statusCode: " + deleteStatusCode);
		    System.out.println("statusMessage: " + out.get("status_message"));
		}

		UpdateStatusBean status = null;
		if (deleteStatusCode == 0) {
			try {
				Test testAfterUpdate = updateTestInternal(test, null);
				status = new UpdateStatusBean(testAfterUpdate.getIdTest(), 0, "Success!");
			} catch (RuntimeException re) {
				status = new UpdateStatusBean(test.getIdTest(), -1, re.getMessage());
			}
		} else {
			try {
				// updateTestInternal will simply update metadata for tests that have usertests or testinstances
				Test testAfterUpdate = updateTestInternal(test, null);
				status = new UpdateStatusBean(testAfterUpdate.getIdTest(), 0, "Success!");
			} catch (RuntimeException re) {
				status = new UpdateStatusBean(test.getIdTest(), -1, re.getMessage());
			}
		}
        return status;
	}

	@Override
	public Integer savePrintsettings(Printsettings printsettings) {
		// create BeanPropertySqlParameterSource objects for section
		BeanPropertySqlParameterSource printsettingsParameters = new BeanPropertySqlParameterSource(printsettings);
		// add section
		getNamedParameterJdbcTemplate().update(updatePrintsettingsSQL, printsettingsParameters);
		return 0;
	}

	@Override
	public Integer deletePrintsettings(Long idTest) {
		// delete any print settings for test
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTest", idTest);
        getNamedParameterJdbcTemplate().update(deletePrintsettingsSQL, args);
		return 0;
	}

}
