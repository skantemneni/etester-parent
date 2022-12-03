package com.etester.data.domain.test.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import com.etester.data.dao.JdbcDataDaoParent;
import com.etester.data.domain.test.JdbcDaoStaticHelper;
import com.etester.data.domain.test.TestConstants;
import com.etester.data.domain.test.TestResponse;

@Repository
public class JdbcUsertestDao extends JdbcDataDaoParent implements UsertestDao {

	public JdbcUsertestDao(DataSource dataSource) {
		super(dataSource);
	}

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.

	@Override
	public void insert(Usertest usertest) {
		List<Usertest> usertests = new ArrayList<Usertest>();
		usertests.add(usertest);
		JdbcDaoStaticHelper.insertUsertestBatchForCurrentUser(usertests, getNamedParameterJdbcTemplate(), getJdbcTemplate(), true);
	}

	@Override
	public String insertBatch(List<Usertest> usertests) {
		JdbcDaoStaticHelper.insertUsertestBatchForCurrentUser(usertests, getNamedParameterJdbcTemplate(), getJdbcTemplate(), true);
		return "Update Successful";
	}

	@Override
	public Usertest findByUsertestId(Long idUsertest) {
        BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUsertest", idUsertest);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Usertest usertest = null;
        try {
        	usertest = getNamedParameterJdbcTemplate().queryForObject(findByUsertestIdSQL, args, usertestRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return usertest;
	}

	@Override
	public TestResponse findUsertestresponseForTest(Long idTest) {
		// Test response if wholly dependent on the logged in user.  If a logged in 
		// user is not detected, the response will be null 
		String username = JdbcDaoStaticHelper.getCurrentUserName();
		if (username == null || username.trim().length() == 0) {
			return null;
		}

		String sql = findTestByUsertestIdWithResponseSQL;
		BeanPropertyRowMapper<TestResponse> testResponseRowMapper = BeanPropertyRowMapper.newInstance(TestResponse.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("username", username);
		args.put("idTest", idTest);
		// queryForObject throws an exception when the section is missing.  this should be ignored/swallowed
		TestResponse testResponse = null;
		try {
			testResponse = getNamedParameterJdbcTemplate()
					.queryForObject(sql, args, testResponseRowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {}
		return testResponse;
	}


	
//	@Override
//	public List<Usertest> findUsertestsForCurrentProvider() {
//		return findUsertestsForProvider(JdbcDaoStaticHelper.getCurrentUserName());
//	}
//
//	@Override
//	public List<Usertest> findUsertestsForProvider(String providername) {
//		List<Usertest> resultList = null;
//        String sql = findAllUsertestsByProviderUsernameSQL;
//		if (providername != null && providername.trim().length() > 0) {
//	        BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
//	        Map<String, Object> args = new HashMap<String, Object>();
//	        args.put("providerUsername", providername);
//	        resultList = getNamedParameterJdbcTemplate().query(sql, args, usertestRowMapper);
//		}
//        return resultList;
//	}
//
	@Override
	public List<Usertest> findAllUsertestsForUsername(String username) {
		return JdbcDaoStaticHelper.findAllUsertestsForUsername(getNamedParameterJdbcTemplate(), username);
	}

	@Override
	public List<Usertest> findAllAssignedUsertestsForCurrentUser() {
		List<Usertest> resultList = null;
		String loggedinUsername = JdbcDaoStaticHelper.getCurrentUserName();
		if (loggedinUsername != null && loggedinUsername.trim().length() > 0) {
			resultList = findAllAssignedUsertestsForUsername(loggedinUsername);
		}
		return resultList;
	}

	@Override
	public List<Usertest> findAllAssignedUsertestsForUsername(String username) {
        String sql = findAllAssignedUsertestsForUserNameSQL;
        BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("username", username);
        List<Usertest> tests = getNamedParameterJdbcTemplate().query(sql, args, usertestRowMapper);
        return tests;
	}

	@Override
	public List<Usertest> findAssignedUsertestsForCurrentUserByType(String testType) {
		List<Usertest> resultList = null;
		String loggedinUsername = JdbcDaoStaticHelper.getCurrentUserName();
		if (loggedinUsername != null && loggedinUsername.trim().length() > 0) {
			resultList = findAssignedUsertestsForUsernameByType(loggedinUsername, testType);
		}
		return resultList;
	}

	@Override
	public List<Usertest> findAssignedUsertestsForUsernameByType(String username, String testType) {
        String sql = findAssignedUsertestTypesForUserNameSQL;
        BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("username", username);
        args.put("testType", testType);
        List<Usertest> tests = getNamedParameterJdbcTemplate().query(sql, args, usertestRowMapper);
        return tests;
	}

	@Override
	public List<Usertest> findCorrectionsUsertestsForCurrentUserByType(String testType) {
		List<Usertest> resultList = null;
		String loggedinUsername = JdbcDaoStaticHelper.getCurrentUserName();
		if (loggedinUsername != null && loggedinUsername.trim().length() > 0) {
			resultList = findCorrectionsUsertestsForUsernameByType(loggedinUsername, testType);
		}
		return resultList;
	}

	@Override
	public List<Usertest> findCorrectionsUsertestsForUsernameByType(String username, String testType) {
        String sql = null;
        BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
        Map<String, Object> args = new HashMap<String, Object>();
		if (testType != null && testType.trim().equals(TestConstants.TEST_TYPE_ALL)) {
			sql = findAllCorrectionsUsertestsForUserNameSQL;
	        args.put("username", username);
		} else {
			sql = findCorrectionsUsertestTypesForUserNameSQL;
	        args.put("username", username);
	        args.put("testType", testType);
		}
        List<Usertest> tests = getNamedParameterJdbcTemplate().query(sql, args, usertestRowMapper);
        return tests;
	}

//	@Override
//	public List<Usertest> findAssignmentUsertestsForCurrentUser() {
//		return findAssignedUsertestsForCurrentUserByType (TestConstants.TEST_TYPE_ASSIGNMENT);
//	}
//
//	@Override
//	public List<Usertest> findAssignmentUsertestsForUsername(String username) {
//		return findAssignedUsertestsForUsernameByType (username, TestConstants.TEST_TYPE_ASSIGNMENT);
//	}
//
//	@Override
//	public List<Usertest> findTestUsertestsForCurrentUser() {
//		return findAssignedUsertestsForCurrentUserByType (TestConstants.TEST_TYPE_TEST);
//	}
//
//	@Override
//	public List<Usertest> findTestUsertestsForUsername(String username) {
//		return findAssignedUsertestsForUsernameByType (username, TestConstants.TEST_TYPE_TEST);
//	}
//
//	@Override
//	public List<Usertest> findQuizUsertestsForCurrentUser() {
//		return findAssignedUsertestsForCurrentUserByType (TestConstants.TEST_TYPE_QUIZ);
//	}
//
//	@Override
//	public List<Usertest> findQuizUsertestsForUsername(String username) {
//		return findAssignedUsertestsForUsernameByType (username, TestConstants.TEST_TYPE_QUIZ);
//	}
//
//	@Override
//	public List<Usertest> findChallengeUsertestsForCurrentUser() {
//		return findAssignedUsertestsForCurrentUserByType (TestConstants.TEST_TYPE_CHALLENGE);
//	}
//
//	@Override
//	public List<Usertest> findChallengeUsertestsForUsername(String username) {
//		return findAssignedUsertestsForUsernameByType (username, TestConstants.TEST_TYPE_CHALLENGE);
//	}
//
//	@Override
//	public List<Usertest> findSubmittedAssignmentUsertestsForCurrentProvider() {
//		return findSubmittedAssignmentUsertestsForProvidername(JdbcDaoStaticHelper.getCurrentUserName());
//	}
//
//	@Override
//	public List<Usertest> findSubmittedAssignmentUsertestsForProvidername(String providername) {
//		return findSubmittedUsertestsForProvidernameByType(providername, TestConstants.TEST_TYPE_ASSIGNMENT);
//	}
//
//	@Override
//	public List<Usertest> findSubmittedTestUsertestsForCurrentProvider() {
//		return findSubmittedTestUsertestsForProvidername(JdbcDaoStaticHelper.getCurrentUserName());
//	}
//
//	@Override
//	public List<Usertest> findSubmittedTestUsertestsForProvidername(String providername) {
//		return findSubmittedUsertestsForProvidernameByType(providername, TestConstants.TEST_TYPE_TEST);
//	}
//	
	@Override
	public List<Usertest> findSubmittedUsertestsForCurrentProviderByType(String testType) {
		return findSubmittedUsertestsForProvidernameByType(JdbcDaoStaticHelper.getCurrentUserName(), testType);
	}

	@Override
	public List<Usertest> findSubmittedUsertestsForProvidernameByType(String providername, String testType) {
		List<Usertest> resultList = null;
		if (providername != null && providername.trim().length() > 0) {
	        String sql = null;
	        BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
	        Map<String, Object> args = new HashMap<String, Object>();
			if (testType != null && testType.trim().equals(TestConstants.TEST_TYPE_ALL)) {
				sql = findAllSubmittedUsertestsByProviderUsernameSQL;
		        args.put("providername", providername);
			} else {
				sql = findSubmittedUsertestTypesByProviderUsernameSQL;
		        args.put("providername", providername);
		        args.put("testType", testType);
			}
	        resultList = getNamedParameterJdbcTemplate().query(sql, args, usertestRowMapper);
		}
		return resultList;
	}

	@Override
	public List<Usertest> findAllAssignedUsertestsForTestId(Long idTest) {
        String sql = findAllAssignedUsertestsForTestIdSQL;
        BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTest", idTest);
        List<Usertest> tests = getNamedParameterJdbcTemplate().query(sql, args, usertestRowMapper);
        return tests;
	}
	@Override
	public List<Usertest> findAllAssignedUsertestsForUserId(Long idUser) {
        String sql = findAllAssignedUsertestsForUserIdSQL;
        BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUser", idUser);
        List<Usertest> tests = getNamedParameterJdbcTemplate().query(sql, args, usertestRowMapper);
        return tests;
	}
}
