package com.etester.data.domain.user;

import java.util.List;

import com.etester.data.domain.profile.Profile;

public interface UsergroupDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.

	// id is auto generated
	public static String insertUsersForProviderSQL = "INSERT INTO providerstudent (provider_username, student_username) VALUES (:providerUsername, :studentUsername)";

	// id is auto generated
	public static String deleteUsersForProviderSQL = "DELETE FROM providerstudent WHERE provider_username = :providerName ";

	// id is auto generated
	public static String insertUsergroupSQL = "INSERT INTO usergroup (provider_username, name, description) "
			+ " VALUES (:providerUsername, :name, :description)";

	// id is auto generated
	public static String insertUsergroupmemberSQL = "INSERT INTO usergroupmember (id_usergroup, username) "
			+ " VALUES (:idUsergroup, :username)";

	public static String findByUsergroupIdSQL = "SELECT * FROM usergroup WHERE id_usergroup = :idUsergroup"; 
	
	public static String findGroupsByUserNameSQL = "SELECT * FROM usergroup WHERE provider_username = :providerUsername"; 
	
	public static String findProfilesByUserNameSQL = "SELECT * FROM profile WHERE provider_username = :providerUsername"; 
	
	public static String findAllStudentsInProviderOrganizationSQL = 
			" SELECT u.id_user, u.username, u.first_name, u.last_name, u.middle_name "
			+ " FROM user u LEFT JOIN organization_student os ON u.id_user = os.id_student "
			+ "		LEFT JOIN organization o ON o.id_organization = os.id_organization "
			+ "		LEFT JOIN organization_provider op on op.id_organization = o.id_organization  "
			+ "		LEFT JOIN user p ON p.id_user = op.id_provider "
			+ " WHERE p.id_user = :idProvider ";
	
	public static String findGroupMembersByUsergroupIdSQL = 
			"SELECT u.* " +
			"FROM user u, usergroupmember ugm " +
			"WHERE u.username = ugm.username and " +
			"      ugm.id_usergroup = :idUsergroup"; 
	
	public static String findFilledGroupsByUserIdSQL = 
			"SELECT ug.id_usergroup groupid, ug.name groupname, u.first_name, u.last_name, u.middle_name, ugm.username username " +
			"FROM user u, usergroup ug, usergroupmember ugm " +
			"WHERE ug.id_usergroup = ugm.id_usergroup and " +
			"      u.username = ugm.username and " +
			"      ug.provider_username = :username "; 
	
	public static String findUngroupedUsersForProvider = 
			"SELECT * FROM user  " +
			"WHERE username IN  " +
			"( " +
			"	SELECT student_username FROM providerstudent " + 
			"	WHERE provider_username = :providerUsername AND  " +
			"		student_username NOT IN  " +
			"		(  " +
			"			SELECT username FROM usergroup ug, usergroupmember ugm " +
			"			WHERE ug.id_usergroup = ugm.id_usergroup AND " +
			"				ug.provider_username = :providerUsername " +
			"		) " +
			")";
	
	public void insert(Usergroup usergroup);

    public void insertBatch(List<Usergroup> usergroups);

    public Usergroup findByUsergroupId(Long idUsergroup);

//    public List<Usergroup> findGroupsForCurrentProvider();
//    public List<Usergroup> findGroupsForProvider(String providerUsername);

//    public List<User> findUngroupedUsersForCurrentProvider();
//    public List<User> findUngroupedUsersForProvider(String providerUsername);

    public List<Usergroup> findGroupsAndUsersWithUsertestsForCurrentProvider();
    public List<Usergroup> findGroupsAndUsersWithUsertestsForProvider(String providerUsername);

    public List<Usergroup> findGroupsAndUsersWithUserprofilesForCurrentProvider();
    public List<Usergroup> findGroupsAndUsersWithUserprofilesForProvider(String providerUsername);

    public List<Profile> findProfilesForCurrentProvider();

    public List<Profile> findProfilesForProvider(String providerUsername);
    
    public List<User> findAllStudentsInCurrentProviderOrganization();

}

