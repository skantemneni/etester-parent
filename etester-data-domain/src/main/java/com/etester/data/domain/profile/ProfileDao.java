package com.etester.data.domain.profile;

import java.util.List;

public interface ProfileDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.

//	public static String findByProfileIdSQL = "SELECT * FROM profile WHERE id_profile = :idProfile"; 
	public static String findByProfileIdForEditSQL = 
			" SELECT p.*, "
			+ "		o.name AS organization_name, "
			+ "		CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name "
			+ " FROM profile p LEFT JOIN user u ON p.id_provider = u.id_user "
			+ "			  LEFT JOIN organization o ON p.id_organization = o.id_organization "
			+ "			  LEFT JOIN organization_provider op ON u.id_user = op.id_provider "
			+ " WHERE p.id_profile = :idProfile ";

	// findProfilesegmentsForProfile - private
	public static final String findProfilesegmentsForProfileSQL = "SELECT * FROM profilesegment WHERE id_profile = :idProfile ORDER BY seq";
	// findTestsectionsForTestsegmentSQL - private
	public static final String findProfiletestsForProfilesegmentSQL = "SELECT pt.*, t.test_type FROM profiletest pt LEFT JOIN test t ON pt.id_test_ref = t.id_test WHERE id_profilesegment = :idProfilesegment ORDER BY seq" ;
	
	public static String insertProfileSQL = "INSERT INTO profile (id_profile, id_provider, id_organization, name, description, access_level, published, profile_type) "
			+ " VALUES (:idProfile, :idProvider, :idOrganization, :name, :description, :accessLevel, :published, :profileType)";

	public static final String upsertProfileSQL = "INSERT INTO profile (id_profile, id_provider, id_organization, name, description, access_level, published, profile_type) "
			+ " VALUES (:idProfile, :idProvider, :idOrganization, :name, :description, 1, :published, :profileType) "
			+ " ON DUPLICATE KEY "
			+ " UPDATE name = :name, description = :description, published = :published ";


	public static String insertProfilesegmentSQL = "INSERT INTO profilesegment (id_profilesegment, id_profile, name, description, seq, profiletest_wrapper) "
			+ " VALUES (:idProfilesegment, :idProfile, :name, :description, :seq, :profiletestWrapper)";

	public static final String updateProfilesegmentSQL = "UPDATE profilesegment SET name = :name, description = :description WHERE id_profilesegment = :idProfilesegment ";

	public static String insertProfiletestSQL = "INSERT INTO profiletest (id_profiletest, id_profile, id_profilesegment, id_test_ref, name, description, seq, test_provision_day, test_removal_day, test_provision_date, test_removal_date, initial_active) "
			+ " VALUES (:idProfiletest, :idProfile, :idProfilesegment, :idTestRef, :name, :description, :seq, :testProvisionDay, :testRemovalDay, :testProvisionDate, :testRemovalDate, :initialActive )";

	public static final String updateProfiletestSQL = "UPDATE profiletest SET name = :name, description = :description, "
			+ " 	test_provision_day = :testProvisionDay, test_removal_day = :testRemovalDay, test_provision_date = :testProvisionDate, test_removal_date = :testRemovalDate, initial_active = :initialActive "	
			+ " WHERE id_profiletest = :idProfiletest ";
	
	// One bigass statement....
	public static final String updateProfiletest_userprofiletestSQL = 
		    " UPDATE userprofiletest upt LEFT JOIN profiletest pt ON upt.id_profiletest = pt.id_profiletest " 
            + "              LEFT JOIN profilesegment ps ON pt.id_profilesegment = ps.id_profilesegment "
            + "              LEFT JOIN profile p ON ps.id_profile = p.id_profile "
            + "              LEFT JOIN userprofile up ON upt.id_userprofile =  up.id_userprofile "
		    + " SET     upt.test_provision_date = "
		    + " 	(CASE "
		    + "			WHEN p.profile_type = 1 THEN pt.test_provision_date "
		    + "         WHEN p.profile_type = 2 THEN ADDDATE(up.profile_start_date, INTERVAL pt.test_provision_day DAY) "
		    + "         ELSE pt.test_provision_date "
		    + "      END), "
		    + "      	upt.test_removal_date = "
		    + "     (CASE "
		    + "         WHEN p.profile_type = 1 THEN pt.test_removal_date "
		    + "         WHEN p.profile_type = 2 THEN ADDDATE(up.profile_start_date, INTERVAL pt.test_removal_day DAY) "
		    + "         ELSE pt.test_removal_date "
		    + "      END), "
		    + "      	upt.active = "
		    + "     (CASE "
		    + "         WHEN p.profile_type = 1 THEN 1 "
		    + "         WHEN p.profile_type = 2 THEN 1 "
		    + "         ELSE pt.initial_active "
		    + "      END) "
		    + " WHERE pt.id_profiletest = :idProfiletest ";
	

//	public static String findProfilesForProviderSQL = "SELECT * FROM profile WHERE id_provider = :idProvider"; 
	public static String findProfilesForProviderSQL = 
			" SELECT p.*, "
			+ "		o.name AS organization_name, "
			+ "		CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name "
			+ " FROM profile p LEFT JOIN user u ON p.id_provider = u.id_user "
			+ "			  LEFT JOIN organization o ON p.id_organization = o.id_organization "
			+ "			  LEFT JOIN organization_provider op ON u.id_user = op.id_provider "
			+ " WHERE p.id_provider = :idProvider "
			+ " ORDER BY p.id_profile "; 

	public static String findAllProfilesSQL = 
			" SELECT p.*, "
			+ "		o.name AS organization_name, "
			+ "		CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name "
			+ " FROM profile p LEFT JOIN user u ON p.id_provider = u.id_user "
			+ "			  LEFT JOIN organization o ON p.id_organization = o.id_organization "
			+ "			  LEFT JOIN organization_provider op ON u.id_user = op.id_provider " 
			+ " ORDER BY p.id_profile "; 

	
	public static final String findAllProfilesAvailableToAssignForProviderSQL = 
			  " SELECT p.*, "
			  + "	o.name AS organization_name, "
			  + "	CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name "
			  + " FROM profile p LEFT JOIN user u ON p.id_provider = u.id_user "
			  + "				LEFT JOIN organization o ON p.id_organization = o.id_organization "
			  + "				LEFT JOIN organization_provider op ON u.id_user = op.id_provider "
			  + " WHERE p.published = 1 "
			  + "	AND (p.id_provider = :idProvider OR "
			  + "				p.access_level = :accessLevelPublic OR "
			  + "				(p.access_level = :accessLevelOrganization AND p.id_organization IN ( SELECT id_organization FROM organization_provider WHERE id_provider = :idProvider ))) "
			  + " ORDER BY p.id_profile ASC "; 
	
	// Userprofile Related SQL Statements
	public static String insertUserprofileSQL = "INSERT INTO userprofile (id_provider, id_profile, id_student, profile_assignment_date, profile_start_date) "
			+ " VALUES (:idProvider, :idProfile, :idStudent, :profileAssignmentDate, :profileStartDate)";
	public static String updateUserprofileSQL = "UPDATE userprofile SET profile_start_date = :profileStartDate " +
			" WHERE id_userprofile = :idUserprofile and id_provider = :idProvider";

	public Profile findByProfileId(Long idProfile);

    public Profile updateProfile(Profile profile);

    public Profile updateProfile(Profile profile, Long loggedInProviderId);

    public String deleteProfile(Long idProfile);

    public String deleteProfile(Profile profile);

    public List<Profile> findAllProfilesForAdministrator();

    public List<Profile> findProfilesForCurrentProvider();

    public List<Profile> findProfilesForProvider(Long idProvider);

    public List<Profile> findAllProfilesAvailableToAssignForCurrentProvider();

    public List<Profile> findAllProfilesAvailableToAssignForProvider(Long idProvider);

    // Userprofile functionality....no where else to go since I do not have a UserprofileDao
    public String insertUserprofileBatch(List<Userprofile> userprofiles);

    public String updateProfilesegment(Profilesegment profilesegment);

    public String updateProfiletest(Profiletest profiletest);
    
}

