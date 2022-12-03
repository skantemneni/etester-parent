package com.etester.data.domain.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import com.etester.data.domain.profile.Profile;
import com.etester.data.domain.profile.Userprofile;
import com.etester.data.domain.test.JdbcDaoStaticHelper;
import com.etester.data.domain.test.instance.Usertest;

public class JdbcUsergroupDao extends NamedParameterJdbcDaoSupport implements UsergroupDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	
	private static final String UNGROUPED_USERS_GROUP_NAME = "UNGROUPED_USERS";
	private static final String ALL_USERS_GROUP_NAME = "ALL_USERS";

	@Override
	public void insert(Usergroup usergroup) {
		List<Usergroup> usergroups = new ArrayList<Usergroup>();
		usergroups.add(usergroup);
		JdbcDaoStaticHelper.insertUsergroupBatchForCurrentUser(usergroups, getNamedParameterJdbcTemplate(), true);
	}

	@Override
	public void insertBatch(List<Usergroup> usergroups) {
		List<User> allUsersList = null;
		if (usergroups != null) {
			int loc = -1;
			for (int i = 0; i < usergroups.size(); i++) {
				if (usergroups.get(i).getName().equals(ALL_USERS_GROUP_NAME)) {
					loc = i;
					break;
				}
			}
			if (loc >= 0) {
				allUsersList = usergroups.get(loc).getUsers();
				usergroups.remove(loc);
			}
		}
		if (allUsersList != null && allUsersList.size() > 0) {
			JdbcDaoStaticHelper.insertUsersForCurrentProvider(allUsersList, getNamedParameterJdbcTemplate(), true);
		}
		if (usergroups != null && usergroups.size() > 0) {
			JdbcDaoStaticHelper.insertUsergroupBatchForCurrentUser(usergroups, getNamedParameterJdbcTemplate(), true);
		}
	}

	@Override
	public Usergroup findByUsergroupId(Long idUsergroup) {
        BeanPropertyRowMapper<Usergroup> usergroupRowMapper = BeanPropertyRowMapper.newInstance(Usergroup.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idUsergroup", idUsergroup);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Usergroup usergroup = null;
        try {
        	usergroup = getNamedParameterJdbcTemplate().queryForObject(findByUsergroupIdSQL, args, usergroupRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        // set testsegments
        usergroup.setUsers(findUsersInUsergroup(usergroup.getIdUsergroup()));
        return usergroup;
	}
	

	private List<Usergroup> findGroupsForCurrentProvider() {
		List<Usergroup> resultList = null;
		String loggedinProviderUsername = JdbcDaoStaticHelper.getCurrentUserName();
		if (loggedinProviderUsername != null && loggedinProviderUsername.trim().length() > 0) {
			resultList = findGroupsForProvider(loggedinProviderUsername, false, false);
		}
		return resultList;
	}
	
	private List<Usergroup> findGroupsForProvider(String providerUsername, boolean withUsertests, boolean withUserprofiles) {
        String sql = findGroupsByUserNameSQL;
        BeanPropertyRowMapper<Usergroup> usergroupRowMapper = BeanPropertyRowMapper.newInstance(Usergroup.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("providerUsername", providerUsername);
        List<Usergroup> usergroups = getNamedParameterJdbcTemplate().query(sql, args, usergroupRowMapper);
        // get the testsegments associated with all the tests
		if (usergroups != null && usergroups.size() > 0) {
			for (int i = 0; i < usergroups.size(); i++) {
				usergroups.get(i).setUsers(findUsersInUsergroup(usergroups.get(i).getIdUsergroup(), providerUsername, withUsertests, withUserprofiles));
			}
		}
        return usergroups;
	}

//	@Override
//	public List<User> findUngroupedUsersForCurrentProvider() {
//		List<User> resultList = null;
//		String loggedinProviderUsername = JdbcDaoStaticHelper.getCurrentUserName();
//		if (loggedinProviderUsername != null && loggedinProviderUsername.trim().length() > 0) {
//			resultList = findUngroupedUsersForProvider(loggedinProviderUsername);
//		}
//		return resultList;
//	}

	
	private List<User> findUngroupedUsersForProvider(String providerUsername, boolean withUsertests, boolean withUserprofiles) {
        String sql = findUngroupedUsersForProvider;
        BeanPropertyRowMapper<User> userRowMapper = BeanPropertyRowMapper.newInstance(User.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("providerUsername", providerUsername);
        List<User> ungroupedUsers = getNamedParameterJdbcTemplate().query(sql, args, userRowMapper);
		if (ungroupedUsers != null && ungroupedUsers.size() > 0 && providerUsername != null) {
			for (User user : ungroupedUsers) {
				if (withUsertests) {
					user.setTests(findUsertestsForProviderUser (user.getIdUser(), providerUsername));
				}
				if (withUserprofiles) {
					user.setProfiles(findUserprofilesForProviderUser (user.getIdUser(), providerUsername));
				}
			}
		}
        return ungroupedUsers;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	@Override
	public List<Usergroup> findGroupsAndUsersWithUsertestsForCurrentProvider() {
		List<Usergroup> resultList = null;
		String loggedinProviderUsername = JdbcDaoStaticHelper.getCurrentUserName();
		if (loggedinProviderUsername != null && loggedinProviderUsername.trim().length() > 0) {
			resultList = findGroupsAndUsersWithUsertestsForProvider(loggedinProviderUsername);
		}
		return resultList;
	}

	@Override
	public List<Usergroup> findGroupsAndUsersWithUsertestsForProvider(String providerUsername) {
		return findGroupsAndUsersForProvider(providerUsername, true, false);
	}
	
	@Override
	public List<Usergroup> findGroupsAndUsersWithUserprofilesForCurrentProvider() {
		List<Usergroup> resultList = null;
		String loggedinProviderUsername = JdbcDaoStaticHelper.getCurrentUserName();
		if (loggedinProviderUsername != null && loggedinProviderUsername.trim().length() > 0) {
			resultList = findGroupsAndUsersWithUserprofilesForProvider(loggedinProviderUsername);
		}
		return resultList;
	}

	@Override
	public List<Usergroup> findGroupsAndUsersWithUserprofilesForProvider(String providerUsername) {
		return findGroupsAndUsersForProvider(providerUsername, false, true);
	}
	
	public List<Usergroup> findGroupsAndUsersForProvider(String providerUsername, boolean withUsertests, boolean withUserprofiles) {
		List<Usergroup> resultList = null;
		if (providerUsername != null && providerUsername.trim().length() > 0) {
			resultList = findGroupsForProvider(providerUsername, withUsertests, withUserprofiles);
			List<User> ungroupedUsers = findUngroupedUsersForProvider(providerUsername, withUsertests, withUserprofiles); 
			if (ungroupedUsers != null && ungroupedUsers.size() > 0) {
				if (resultList == null) {
					resultList = new ArrayList<Usergroup>();
				}
				resultList.add(new Usergroup(providerUsername, UNGROUPED_USERS_GROUP_NAME, ungroupedUsers));
			}
		}
		return resultList;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public List<User> findAllStudentsInCurrentProviderOrganization() {
		List<User> users = null;
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(this.getNamedParameterJdbcTemplate());
		if (loggedinProviderId != null && loggedinProviderId != 0l) {
			users = findAllStudentsInProviderOrganization(loggedinProviderId);
		}
		return users;
	}

	private List<User> findAllStudentsInProviderOrganization(Long idProvider) {
		List<User> users = null;
		if (idProvider != null && idProvider != 0l) {
		    String sql = findAllStudentsInProviderOrganizationSQL;
		    BeanPropertyRowMapper<User> userRowMapper = BeanPropertyRowMapper.newInstance(User.class);
		    Map<String, Object> args = new HashMap<String, Object>();
		    args.put("idProvider", idProvider);
		    users = getNamedParameterJdbcTemplate().query(sql, args, userRowMapper);
		}
		return users;
	}

	@Override
	public List<Profile> findProfilesForCurrentProvider() {
		List<Profile> profiles = null;
		String loggedinProviderUsername = JdbcDaoStaticHelper.getCurrentUserName();
		if (loggedinProviderUsername != null && loggedinProviderUsername.trim().length() > 0) {
			profiles = findProfilesForProvider(loggedinProviderUsername);
		}
		return profiles;
	}

	@Override
	public List<Profile> findProfilesForProvider(String providerUsername) {
		List<Profile> profiles = null;
		if (providerUsername != null && providerUsername.trim().length() > 0) {
		    String sql = findProfilesByUserNameSQL;
		    BeanPropertyRowMapper<Profile> profileRowMapper = BeanPropertyRowMapper.newInstance(Profile.class);
		    Map<String, Object> args = new HashMap<String, Object>();
		    args.put("providerUsername", providerUsername);
		    profiles = getNamedParameterJdbcTemplate().query(sql, args, profileRowMapper);
		}
		return profiles;
	}

	/***********************************************************************
	 * Private functions
	 ***********************************************************************/
	private List<User> findUsersInUsergroup (Long idUsergroup) {
		return findUsersInUsergroup(idUsergroup, null);
	}

	private List<User> findUsersInUsergroup (Long idUsergroup, String providerUsername) {
		return findUsersInUsergroup(idUsergroup, providerUsername, false, false);
	}

	private List<User> findUsersInUsergroup (Long idUsergroup, boolean withUsertests, boolean withUserprofiles) {
		return findUsersInUsergroup (idUsergroup, null, withUsertests, withUserprofiles);
	}

	private List<User> findUsersInUsergroup (Long idUsergroup, String providerUsername, boolean withUsertests, boolean withUserprofiles) {
		String sql = findGroupMembersByUsergroupIdSQL;
		BeanPropertyRowMapper<User> userRowMapper = BeanPropertyRowMapper.newInstance(User.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idUsergroup", idUsergroup);
		List<User> users = getNamedParameterJdbcTemplate().query(sql, args, userRowMapper);
		if (users != null && users.size() > 0 && providerUsername != null) {
			for (User user : users) {
				if (withUsertests) {
					user.setTests(findUsertestsForProviderUser (user.getIdUser(), providerUsername));
				}
				if (withUserprofiles) {
					user.setProfiles(findUserprofilesForProviderUser (user.getIdUser(), providerUsername));
				}
				
			}
		}
		return users;
	}

	// Only return NON-PROFILED usertests
	private List<Usertest> findUsertestsForProviderUser(Long idUser, String providerUsername) {
		String sql = "SELECT ut.* "
				+ " FROM usertest ut INNER JOIN user u ON u.id_user = ut.id_user "
				+ "					 INNER JOIN user u2 ON u2.id_user = ut.id_provider "
				+ " WHERE u.id_user = :idUser AND "
				+ "			u2.username = :providerUsername AND "
				+ "			ut.id_profile = 0 ";
		BeanPropertyRowMapper<Usertest> usertestRowMapper = BeanPropertyRowMapper.newInstance(Usertest.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idUser", idUser);
		args.put("providerUsername", providerUsername);
		List<Usertest> usertests = getNamedParameterJdbcTemplate().query(sql, args, usertestRowMapper);
		return usertests;
	}

	private List<Userprofile> findUserprofilesForProviderUser(Long idUser, String providerUsername) {
		String sql = "SELECT up.*, p.name AS name, p.description AS description "
				+ " FROM userprofile up INNER JOIN profile p ON up.id_profile = p.id_profile "
				+ " 	INNER JOIN user u ON u.id_user = up.id_student "
				+ "		INNER JOIN user u2 ON u2.id_user = up.id_provider "
				+ " WHERE u.id_user = :idUser AND u2.username = :providerUsername ";
		BeanPropertyRowMapper<Userprofile> userprofileRowMapper = BeanPropertyRowMapper.newInstance(Userprofile.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idUser", idUser);
		args.put("providerUsername", providerUsername);
		List<Userprofile> userprofiles = getNamedParameterJdbcTemplate().query(sql, args, userprofileRowMapper);
		return userprofiles;
	}

}
