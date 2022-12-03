package com.etester.data.domain.profile;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import com.etester.data.domain.test.JdbcDaoStaticHelper;
import com.etester.data.domain.test.TestConstants;
import com.etester.data.domain.user.User;
import com.etester.data.domain.user.UserDao;

public class JdbcProfileDao extends NamedParameterJdbcDaoSupport implements ProfileDao {

	@Override
	public Profile findByProfileId(Long idProfile) {
        BeanPropertyRowMapper<Profile> profileRowMapper = BeanPropertyRowMapper.newInstance(Profile.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idProfile", idProfile);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Profile profile = null;
        try {
        	profile = getNamedParameterJdbcTemplate().queryForObject(findByProfileIdForEditSQL, args, profileRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        // set testsegments
        if (profile != null) {
        	profile.setProfilesegments(findProfilesegmentsForProfile(profile.getIdProfile()));
        }
        return profile;
	}
	
	private List<Profilesegment> findProfilesegmentsForProfile(Long idProfile) {
		String sql = findProfilesegmentsForProfileSQL;
		BeanPropertyRowMapper<Profilesegment> proeilesegmentRowMapper = BeanPropertyRowMapper.newInstance(Profilesegment.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idProfile", idProfile);
		List<Profilesegment> profilesegments = getNamedParameterJdbcTemplate().query(sql, args, proeilesegmentRowMapper);
		if (profilesegments != null && profilesegments.size() > 0) {
			for (Profilesegment profilesegment : profilesegments) {
				profilesegment.setProfiletests(findProfiletestsForProfilesegment(profilesegment.getIdProfilesegment()));
			}
		}
		return profilesegments;
	}

	private List<Profiletest> findProfiletestsForProfilesegment(Long idProfilesegment) {
		String sql = findProfiletestsForProfilesegmentSQL;
		BeanPropertyRowMapper<Profiletest> profiletestRowMapper = BeanPropertyRowMapper.newInstance(Profiletest.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idProfilesegment", idProfilesegment);
		List<Profiletest> profiletests = getNamedParameterJdbcTemplate().query(sql, args, profiletestRowMapper);
		return profiletests;
	}

	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update ProfileSegment functionality...allows for updates of names and descriptions of Profilesegments 
	// on published Profiles
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String updateProfilesegment(Profilesegment profilesegment) {
		return this.updateProfilesegment(profilesegment, JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate()));
	}
	private String updateProfilesegment(Profilesegment profilesegment, Long loggedInProviderId) {
		// Null checks
		if (profilesegment == null || profilesegment.getIdProfilesegment() == null || profilesegment.getIdProfilesegment() == 0l) {
			return "ERROR: Profilesegment is NULL";
		}
		// Validate the user has permissions to update the Profile which contains this Profilesegment
		String permissionsFailure = checkPermissionsForUpdateProfilesegmentAction(profilesegment, loggedInProviderId);
		if (permissionsFailure != null) {
			return permissionsFailure;
		}
		// update Profilesegment metadata
		try {
			SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(profilesegment);
			getNamedParameterJdbcTemplate().update(updateProfilesegmentSQL, parameterSource);
		} catch (DataAccessException dae) {
			return "Error: " + dae.getMessage();
		}
		return null;
	}

	private String checkPermissionsForUpdateProfilesegmentAction(Profilesegment profilesegment, Long loggedInProviderId) {
		if (profilesegment == null || profilesegment.getIdProfile() == null || profilesegment.getIdProfile() == 0l) {
			return "ERROR: ProfileId Associated with the Profilesegment is NULL";
		}
		// get the profile associated with the profile segment...
		Profile profile = findByProfileId(profilesegment.getIdProfile());
		if (profile == null) {
			return "ERROR: Profile with ID '" + profilesegment.getIdProfile() + "' Associated with the Profilesegment is NULL";
		}
		return checkPermissionsForUpdateProfileAction(profile, loggedInProviderId);
	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Profiletest functionality...allows for updates of names, descriptions, Provision and Removal dates 
	// and days on published Profiles
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String updateProfiletest(Profiletest profiletest) {
		return this.updateProfiletest(profiletest, JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate()));
	}

	private String updateProfiletest(Profiletest profiletest, Long loggedInProviderId) {
		// Null checks
		if (profiletest == null || profiletest.getIdProfiletest() == null || profiletest.getIdProfiletest() == 0l) {
			return "ERROR: Profiletest is NULL";
		}
		// Validate the user has permissions to update the Profile which contains this Profilesegment
		if (profiletest.getIdProfile() == null || profiletest.getIdProfile() == 0l) {
			return "ERROR: ProfileId Associated with the Profiletest is NULL";
		}
		// get the profile associated with the profile segment...
		Profile profile = findByProfileId(profiletest.getIdProfile());
		if (profile == null) {
			return "ERROR: Profile with ID '" + profiletest.getIdProfile() + "' Associated with the Profiletest is NULL";
		}
		String permissionsFailure = checkPermissionsForUpdateProfileAction(profile, loggedInProviderId);
		if (permissionsFailure != null) {
			return permissionsFailure;
		}
		// update Profiletest metadata as follows...
		// 1.) Update the Profiletest table with names, descriptions and dates/days and times
		// 2.) And the trickiest thing.......Update all Student Profile Tests (userprofiletest table) with updates to reflect this change...
		try {
			SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(profiletest);
			getNamedParameterJdbcTemplate().update(updateProfiletestSQL, parameterSource);
			getNamedParameterJdbcTemplate().update(updateProfiletest_userprofiletestSQL, parameterSource);
		} catch (DataAccessException dae) {
			return "Error: " + dae.getMessage();
		}
		return null;
	}
//	private String checkPermissionsForUpdateProfiletestAction(Profiletest profiletest, Long loggedInProviderId) {
//		if (profiletest == null || profiletest.getIdProfile() == null || profiletest.getIdProfile() == 0l) {
//			return "ERROR: ProfileId Associated with the Profiletest is NULL";
//		}
//		// get the profile associated with the profile segment...
//		Profile profile = findByProfileId(profiletest.getIdProfile());
//		if (profile == null) {
//			return "ERROR: Profile with ID '" + profiletest.getIdProfile() + "' Associated with the Profiletest is NULL";
//		}
//		return checkPermissionsForUpdateProfileAction(profile, loggedInProviderId);
//	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Profile functionality...allows for updates of names, descriptions Only on Published profiles.
	// For unpublished profiles, everything on the profile can be updated in one shot
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Profile updateProfile(Profile profile) {
		return this.updateProfile(profile, JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate()));
	}

	@Override
	public Profile updateProfile(Profile profile, Long loggedInProviderId) {
		// make sure the user is logged in.  
		if (loggedInProviderId == null) {
			throw new RuntimeException ("Need to be Logged in to Create or Update Profile!");
		}
		String permissionsFailure = checkPermissionsForUpdateProfileAction(profile, loggedInProviderId);
		if (permissionsFailure != null) {
			throw new RuntimeException (permissionsFailure);
		}
		return updateProfileInternal(profile, loggedInProviderId);
	}


	private Profile updateProfileInternal(Profile profile, Long loggedInProviderId) {
		// now get the current state of the test....if it exists & set some flags indicating newTest or published test
//		boolean isProfile = profile.getTestType() != null && test.getTestType().equalsIgnoreCase(TestConstants.TEST_TYPE_TEST);
		boolean isPublished = false;
		Profile databaseProfile = null;
		if (profile.getIdProfile() != null && profile.getIdProfile().longValue() != 0l) {
			databaseProfile = findByProfileId(profile.getIdProfile());
		}
		if (databaseProfile == null) {
			isPublished = false;
		} else {
			isPublished = databaseProfile.getPublished() != null && databaseProfile.getPublished() == 1;
		}

		Long idProfile = null;
		if (profile.getIdProfile() == null || profile.getIdProfile().longValue() == 0l) {
			// id_test is not a AUTO INCREMENT...call a function to get a new test id
			String sql = "SELECT get_new_test_id()";
			idProfile = getNamedParameterJdbcTemplate().queryForObject(sql, new HashMap<String, Object>(), Long.class);
			// set the id on the test object and send it on its merry way
			profile.setIdProfile(idProfile);
			// set the provider id on the test to the currently logged in provider
			if (profile.getIdProvider() == null || profile.getIdProvider() == 0l) {
				profile.setIdProvider(loggedInProviderId);
			}
		} else {
			idProfile = profile.getIdProfile();
		}

		// update test metadata
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(profile);
        getNamedParameterJdbcTemplate().update(upsertProfileSQL, parameterSource);
        
        // now insert/overwrite the profilesegments
        // delete any existing profilesegments
		// Only unpublished tests can be updated with new Profilesegments
        String cannotUpdateProfilesegmentsReason = null;
        // Profiles have to be marked unpublished to be updated.  
		if (isPublished) {
			cannotUpdateProfilesegmentsReason = "The Profile is marked Published.  Only test Meta Data was updated.  Please unpublish the Profile for a FULL Update.";
		}
		// see if profile has been assigned somewhere
		if (cannotUpdateProfilesegmentsReason == null) {
			// see if the test has been assigned (has some usertests)
			boolean hasUsers = databaseProfile == null ? false : profileHasUsers(idProfile);
			if (hasUsers) {
				cannotUpdateProfilesegmentsReason = "Only Meta Data of the Profile was updated.  The Profile has been assigned to users.  Please delete the Assignments before updating the full Profile";
			}
		}
		if (cannotUpdateProfilesegmentsReason == null) {
	        if (profile.getProfilesegments() != null && profile.getProfilesegments().size() > 0) {
	        	JdbcDaoStaticHelper.insertProfilesegmentsForProfile(profile.getProfilesegments(), idProfile, getNamedParameterJdbcTemplate(), getJdbcTemplate());
	        }
		} else {
			throw new RuntimeException (cannotUpdateProfilesegmentsReason);
		}
		
        // return the database row post update
//        return findByProfileId(idProfile);
		// returning the database row post update will cause the following Problem...documented at the following places
        // "check to see you are returning the exact same instance that was passed in, just remembered I did actually run into the same problem when you return a different instance that has the same identity"
		// 1.) "https://groups.google.com/forum/?fromgroups#!topic/google-web-toolkit/h2u9u-LjmA4"
		// 2.) "https://code.google.com/archive/p/google-web-toolkit/issues/7341"
		// 3.) "http://stackoverflow.com/questions/12660219/autobean-has-frozen-exception-on-gwt-app-with-objectify"

		// return profile with the new profile segments from the database added in...
    	profile.setProfilesegments(findProfilesegmentsForProfile(profile.getIdProfile()));
		
    	return profile;
	}

	private boolean profileHasUsers(Long idProfile) {
		// TODO Auto-generated method stub
		return false;
	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// checkPermissionsForUpdateProfileAction Checks user permissions On the Profile for the action we seek.
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private String checkPermissionsForUpdateProfileAction(Profile profile, Long loggedInProviderId) {
		// first locate the user
		User provider = JdbcDaoStaticHelper.findUserByUserId(getNamedParameterJdbcTemplate(), loggedInProviderId);
		// problem if the user is missing
		if (provider == null) {
			throw new RuntimeException ("Need to be Logged in to Create or Update Profile!");
		}
		// problem if the user is not a provider 
		if (provider.getAuthorities() == null || !provider.getAuthorities().contains (UserDao.ROLE_PROVIDER)) {
			return "Logged in user is Not a Provider!";
		}
		// problem if the user has no permission to UPDATE_TEST (or UPDATE_ANY_TEST)
		if (provider.getPermissions() == null || (!provider.getPermissions().contains ("UPDATE_TEST") && !provider.getPermissions().contains("UPDATE_ANY_TEST"))) {
			return "Logged in Provider Does Not have permissions to Create or Update Profiles!";
		}
        
        // one final thing - (only for updates) either the logged in user is the profile owner or he has UPDATE_ANY_TEST permission
        if (profile.getIdProvider() != null && !profile.getIdProvider().equals(0l)) {
        	if (!loggedInProviderId.equals(profile.getIdProvider()) &&  !provider.getPermissions().contains("UPDATE_ANY_TEST")) {
        		return "Provider is NOT the Author of the Profile and the Provider Does not have UPDATE_ANY_TEST permission.";
        	}
        }
        // all conditions pass - return true
		return null;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Profile functionality...
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String deleteProfile(Long idProfile) {
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinProviderId == null) {
			return ("User not logged in.  Cannot delete Profile!");
		}
		Profile profile = null;
		// make sure its a valid test;
		if (idProfile == null) {
			// no foul.
			return ("Delete failed.  Profile ID is Invalid.");
		} else {
			profile = findByProfileId(idProfile);
			if (profile == null) {
				return ("No Profile found in the data store with ID: '" + idProfile + "'");
			} else {
				return deleteProfileInternal(profile, loggedinProviderId);
			}
		}
	}

	@Override
	public String deleteProfile(Profile profile) {
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinProviderId == null) {
			return ("User not logged in.  Cannot delete Profile!");
		}
		return deleteProfileInternal(profile, loggedinProviderId);
	}

	private String deleteProfileInternal(Profile profile, Long loggedinProviderId) {
		String permissionsFailure = checkPermissionsForUpdateProfileAction(profile, loggedinProviderId);
		if (permissionsFailure != null) {
			throw new RuntimeException (permissionsFailure);
		}
		if (profile.getPublished() != null && profile.getPublished() == 1) {
			return ("Profile: '" + profile.getIdProfile() + "' is Published.  Please un-publish the Profile before deleting.");
		} else {
			// This is a stored proc that deleted the following 3 object types:
			// 1.) Profiletest's associated with the profile
			// 2.) Profilesegments associated with the profile
			// 3.) The Profile itself.  
			// 4.) all userprofile's and userprofiletest's have FK of cascade.  that will delete any instances
			SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("rulefree").withProcedureName("delete_profile");
	        SqlParameterSource in = new MapSqlParameterSource().addValue("idProfile", profile.getIdProfile(), Types.NUMERIC);
	        Map out = simpleJdbcCall.execute(in);
	        int deleteStatusCode = (Integer) out.get("status_code");
	        if (deleteStatusCode == 0) {
				return ("Delete Successful for Profile: '" + profile.getIdProfile() + "'");
	        } else {
	        	return ("Delete Failed for Profile: '" + profile.getIdProfile() + "'.  " + out.get("status_message"));
	        }
		}
	}

	@Override
	public List<Profile> findProfilesForCurrentProvider() {
		List<Profile> profiles = null;
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinProviderId != null) {
			profiles = findProfilesForProvider(loggedinProviderId);
		}
		return profiles;
	}

	@Override
	public List<Profile> findProfilesForProvider(Long idProvider) {
		List<Profile> profiles = null;
		if (idProvider != null) {
		    String sql = findProfilesForProviderSQL;
		    BeanPropertyRowMapper<Profile> profileRowMapper = BeanPropertyRowMapper.newInstance(Profile.class);
		    Map<String, Object> args = new HashMap<String, Object>();
		    args.put("idProvider", idProvider);
		    profiles = getNamedParameterJdbcTemplate().query(sql, args, profileRowMapper);
		}
		return profiles;
	}

	@Override
	public List<Profile> findAllProfilesForAdministrator() {
		User loggedInUser = JdbcDaoStaticHelper.findCurrentUser(getNamedParameterJdbcTemplate());
		// problem if the user is not a provider 
		if ((loggedInUser.getAuthorities() != null && loggedInUser.getAuthorities().contains (UserDao.ROLE_ADMIN)) || 
				(loggedInUser.getPermissions() != null && loggedInUser.getPermissions().contains("UPDATE_ANY_TEST"))) {
			return findAllProfiles();
		} else {
			throw new RuntimeException ("Logged in User is not an Administrator And Does not have EDIT_ANY_TEST Permission.");
		}
	}
	private List<Profile> findAllProfiles() {
	    BeanPropertyRowMapper<Profile> profileRowMapper = BeanPropertyRowMapper.newInstance(Profile.class);
	    return getNamedParameterJdbcTemplate().query(findAllProfilesSQL, profileRowMapper);
	}

	@Override
	public List<Profile> findAllProfilesAvailableToAssignForCurrentProvider() {
		List<Profile> profiles = null;
		Long loggedinProviderId = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (loggedinProviderId != null) {
			profiles = findAllProfilesAvailableToAssignForProvider(loggedinProviderId);
		}
		return profiles;
	}

	@Override
	public List<Profile> findAllProfilesAvailableToAssignForProvider(Long idProvider) {
		List<Profile> profiles = null;
		if (idProvider != null) {
		    String sql = findAllProfilesAvailableToAssignForProviderSQL;
		    BeanPropertyRowMapper<Profile> profileRowMapper = BeanPropertyRowMapper.newInstance(Profile.class);
		    Map<String, Object> args = new HashMap<String, Object>();
	        args.put("idProvider", idProvider);
	        args.put("accessLevelPublic", TestConstants.AccessLevelVisibility.PUBLIC.visibility());
	        args.put("accessLevelOrganization", TestConstants.AccessLevelVisibility.ORGANIZATION.visibility());
		    profiles = getNamedParameterJdbcTemplate().query(sql, args, profileRowMapper);
		}
		return profiles;
	}

	/**
	 * This is a method that gets invoked when a profile is assigned to a user...
	 */
	@Override
	public String insertUserprofileBatch(List<Userprofile> userprofiles) {
		JdbcDaoStaticHelper.insertUserprofileBatchForCurrentUser(userprofiles, getNamedParameterJdbcTemplate(), getJdbcTemplate(), true);
		return "Update Successful";
	}

}
