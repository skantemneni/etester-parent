package com.etester.data.domain.test.instance;

import java.util.List;

import com.etester.data.domain.test.TestResponse;

public interface UsertestDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.

	// id is auto generated
	public static String insertUsertestSQL = "INSERT INTO usertest (id_provider, id_test, id_user, auto_grade, auto_publish_results, test_type, user_type, test_status, name, description, test_assignment_date, is_report_available_to_view_by_student) "
			+ " VALUES (:idProvider, :idTest, :idUser, :autoGrade, :autoPublishResults, :testType, :userType, :testStatus, :name, :description, :testAssignmentDate, :isReportAvailableToViewByStudent)";

	// id is auto generated
	public static String updateUsertestSQL = "UPDATE usertest SET name = :name, description = :description, auto_grade = :autoGrade, auto_publish_results = :autoPublishResults, test_assignment_date = :testAssignmentDate, is_report_available_to_view_by_student = :isReportAvailableToViewByStudent " +
			" WHERE id_usertest = :idUsertest AND id_provider = :idProvider AND id_profile = 0";

	public static String findByUsertestIdSQL = "SELECT * FROM usertest WHERE id_usertest = :idUsertest"; 
	
	// Note the Order-By clause.  Should order the results by Provider Name and then usertest id.  
	// note that I am using the p.username in the Order-By clause instead of ut.id_provider because I want the results in Alphabetical Order
	
	public static String findAllUsertestsForUserNameSQL = "SELECT ut.*, p.username AS associatedUserName, p.first_name AS associatedFirstName, p.last_name AS associatedLastName, pr.name AS profile_name "
			+ "FROM usertest ut INNER JOIN test t ON ut.id_test = t.id_test "
			+ "					INNER JOIN user u ON u.id_user = ut.id_user "
			+ "					INNER JOIN user p  ON p.id_user = ut.id_provider "
			+ "					LEFT JOIN profile pr  ON ut.id_profile = pr.id_profile "
			+ "WHERE u.username = :username "
			+ "ORDER BY pr.name, p.username, ut.id_usertest";
		
	// Note the Order-By clause.  Should order the results by Provider Name and then usertest id.  
	// note that I am using the p.username in the Order-By clause instead of ut.id_provider because I want the results in Alphabetical Order
	public static String findAllAssignedUsertestsForUserNameSQL = "SELECT ut.*, p.username AS associatedUserName, p.first_name AS associatedFirstName, p.last_name AS associatedLastName, pr.name AS profile_name "
			+ "FROM usertest ut INNER JOIN test t ON ut.id_test = t.id_test "
			+ "					INNER JOIN user u ON u.id_user = ut.id_user "
			+ "					INNER JOIN user p  ON p.id_user = ut.id_provider "
			+ "					LEFT JOIN profile pr  ON ut.id_profile = pr.id_profile "
			+ "WHERE ut.test_status IN ('assigned', 'started') AND u.username = :username "
			+ "ORDER BY pr.name, p.username, ut.id_usertest";
		
	// This is used to Retrieve "Student Tests" of a certain "Type" for "TestList" view purpose
	// Note the Order-By clause.  Should order the results by Provider Name and then test_assignment_date.  
	// note that I am using the p.username in the Order-By clause instead of ut.id_provider because I want the results in Alphabetical Order
	public static String findAssignedUsertestTypesForUserNameSQL = "SELECT ut.*, p.username AS associatedUserName, p.first_name AS associatedFirstName, p.last_name AS associatedLastName, "
			+ " pr.name AS profile_name, t.question_count AS question_count, t.time_to_answer AS time_in_minutes, o.name AS organization_name, et.description AS examtrack_description "
			+ " FROM usertest ut INNER JOIN test t ON ut.id_test = t.id_test "
			+ "					INNER JOIN user u ON u.id_user = ut.id_user "
			+ "					INNER JOIN user p  ON p.id_user = ut.id_provider "
			+ "					LEFT JOIN profile pr  ON ut.id_profile = pr.id_profile "
			+ " 				INNER JOIN organization o ON t.id_organization = o.id_organization "
			+ "					INNER JOIN examtrack et ON t.examtrack = et.examtrack "
			+ " WHERE ut.test_status IN ('assigned', 'started') AND u.username = :username AND t.test_type = :testType "
			+ " ORDER BY pr.name, p.username, ut.test_assignment_date";
	
	// This is used to Retrieve "Student Corrections" of a certain "Type" for "CorrectionsList" view purpose
	// Note the Order-By clause.  Should order the results by Provider Name and then test_completion_date.
	// note that I am using the p.username in the Order-By clause instead of ut.id_provider because I want the results in Alphabetical Order
	public static String findCorrectionsUsertestTypesForUserNameSQL = "SELECT ut.*, p.username AS associatedUserName, p.first_name AS associatedFirstName, p.last_name AS associatedLastName, "
			+ " pr.name AS profile_name, t.question_count AS question_count, t.time_to_answer AS time_in_minutes, o.name AS organization_name, et.description AS examtrack_description "
			+ " FROM usertest ut INNER JOIN test t ON ut.id_test = t.id_test "
			+ "					INNER JOIN user u ON u.id_user = ut.id_user "
			+ "					INNER JOIN user p  ON p.id_user = ut.id_provider "
			+ "					LEFT JOIN profile pr  ON ut.id_profile = pr.id_profile "
			+ " INNER JOIN organization o ON t.id_organization = o.id_organization INNER JOIN examtrack et ON t.examtrack = et.examtrack "
			+ " WHERE ut.test_status IN ('corrections') AND u.username = :username AND t.test_type = :testType "
			+ "			AND ut.is_report_available_to_view_by_student = 1"
			+ " ORDER BY pr.name, p.username, ut.test_completion_date";
	
	// This is used to Retrieve "Student Corrections" of "All Types" for "CorrectionsList" view purpose
	// Note the Order-By clause.  Should order the results by Provider Name and then test_completion_date.
	// note that I am using the p.username in the Order-By clause instead of ut.id_provider because I want the results in Alphabetical Order
	public static String findAllCorrectionsUsertestsForUserNameSQL = "SELECT ut.*, p.username AS associatedUserName, p.first_name AS associatedFirstName, p.last_name AS associatedLastName, "
			+ " pr.name AS profile_name, t.question_count AS question_count, t.time_to_answer AS time_in_minutes, o.name AS organization_name, et.description AS examtrack_description "
			+ " FROM usertest ut INNER JOIN test t ON ut.id_test = t.id_test "
			+ "					INNER JOIN user u ON u.id_user = ut.id_user "
			+ "					INNER JOIN user p  ON p.id_user = ut.id_provider "
			+ "					LEFT JOIN profile pr  ON ut.id_profile = pr.id_profile "
			+ " 				INNER JOIN organization o ON t.id_organization = o.id_organization "
			+ "					INNER JOIN examtrack et ON t.examtrack = et.examtrack "
			+ " WHERE ut.test_status IN ('corrections') AND u.username = :username "
			+ "			AND ut.is_report_available_to_view_by_student = 1"
			+ " ORDER BY pr.name, p.username, ut.test_completion_date";
	
	// This is used to Retrieve "Provider Grading" of a certain "Type" for "GradeHomework" view purpose
	// Note the Order-By clause.  Should order the results by Student Name and then test_completion_date.
	// note that I am using the u.username in the Order-By clause instead of ut.id_user because I want the results in Alphabetical Order
	public static String findAllSubmittedUsertestsByProviderUsernameSQL = "SELECT ut.*, u.username AS associatedUserName, u.first_name AS associatedFirstName, u.last_name AS associatedLastName, "
			+ " pr.name AS profile_name, t.question_count AS question_count, t.time_to_answer AS time_in_minutes, o.name AS organization_name, et.description AS examtrack_description "
			+ " FROM usertest ut INNER JOIN test t ON ut.id_test = t.id_test "
			+ "					INNER JOIN user u ON u.id_user = ut.id_user "
			+ "					INNER JOIN user p ON p.id_user = ut.id_provider "
			+ "					LEFT JOIN profile pr  ON ut.id_profile = pr.id_profile "
			+ " 				INNER JOIN organization o ON t.id_organization = o.id_organization "
			+ "					INNER JOIN examtrack et ON t.examtrack = et.examtrack "
			+ "WHERE (ut.test_status = 'submitted' OR (ut.test_status = 'corrections' AND ut.is_report_available_to_view_by_student = 0)) AND p.username = :providername "
			+ "ORDER BY pr.name, u.username, ut.test_completion_date"; 
	
	// This is used to Retrieve "Provider Grading" of "All Types" for "GradeHomework" view purpose
	// Note the Order-By clause.  Should order the results by Student Name and then test_completion_date.  
	// note that I am using the u.username in the Order-By clause instead of ut.id_user because I want the results in Alphabetical Order
	public static String findSubmittedUsertestTypesByProviderUsernameSQL = "SELECT ut.*, u.username AS associatedUserName, u.first_name AS associatedFirstName, u.last_name AS associatedLastName, "
			+ " pr.name AS profile_name, t.question_count AS question_count, t.time_to_answer AS time_in_minutes, o.name AS organization_name, et.description AS examtrack_description "
			+ " FROM usertest ut INNER JOIN test t ON ut.id_test = t.id_test "
			+ "					INNER JOIN user u ON u.id_user = ut.id_user "
			+ "					INNER JOIN user p ON p.id_user = ut.id_provider "
			+ "					LEFT JOIN profile pr  ON ut.id_profile = pr.id_profile "
			+ " 				INNER JOIN organization o ON t.id_organization = o.id_organization "
			+ "					INNER JOIN examtrack et ON t.examtrack = et.examtrack "
			+ "WHERE (ut.test_status = 'submitted' OR (ut.test_status = 'corrections' AND ut.is_report_available_to_view_by_student = 0)) AND p.username = :providername AND t.test_type = :testType "
			+ "ORDER BY pr.name, u.username, ut.test_completion_date"; 
	
	public static String findAllAssignedUsertestsForTestIdSQL = "SELECT ut.*, u.username AS associatedUserName, u.first_name AS associatedFirstName, u.last_name AS associatedLastName "
			+ "FROM usertest ut INNER JOIN user u ON u.id_user = ut.id_user "
			+ "WHERE ut.id_test = :idTest AND ut.test_status IN ('assigned', 'started') AND ut.id_profile = 0 "
			+ "ORDER BY u.id_user";
		
	public static String findAllAssignedUsertestsForUserIdSQL = "SELECT ut.*, u.username AS associatedUserName, u.first_name AS associatedFirstName, u.last_name AS associatedLastName "
			+ "FROM usertest ut INNER JOIN user u ON u.id_user = ut.id_user "
			+ "WHERE ut.id_user = :idUser AND ut.test_status IN ('assigned', 'started') AND ut.id_profile = 0 "
			+ "ORDER BY u.id_user";
		
	

	public static final String findTestByUsertestIdWithResponseSQL = 
			"SELECT  ut.id_test as id_test, "  
			+ "ut.id_usertest as id_usertest, " 
		    + "utr.id_usertestresponse as id_usertestresponse, "
		    + "ut.test_status AS test_status, "
		    + "utr.response AS test_response, "
		    + "utr.date_saved AS date_saved "
		    + "FROM usertestresponse utr INNER JOIN usertest ut ON utr.id_usertest = ut.id_usertest	"
		    + "INNER JOIN user u ON ut.id_user = u.id_user	"
		    + "WHERE u.username = :username AND ut.id_test = :idTest";
	

    // general requests
	public void insert(Usertest usertest);

    public String insertBatch(List<Usertest> usertests);

    public Usertest findByUsertestId(Long idUsertest);

    public TestResponse findUsertestresponseForTest(Long idTest);
 
    // user usertest requests
    
    public List<Usertest> findAllUsertestsForUsername(String username);
    public List<Usertest> findAllAssignedUsertestsForCurrentUser();
    public List<Usertest> findAllAssignedUsertestsForUsername(String username);
    public List<Usertest> findAssignedUsertestsForCurrentUserByType(String testType);
    public List<Usertest> findAssignedUsertestsForUsernameByType(String username, String testType);
    public List<Usertest> findCorrectionsUsertestsForCurrentUserByType(String testType);
    public List<Usertest> findCorrectionsUsertestsForUsernameByType(String username, String testType);
//    public List<Usertest> findAssignmentUsertestsForCurrentUser();
//    public List<Usertest> findAssignmentUsertestsForUsername(String username);
//    public List<Usertest> findTestUsertestsForCurrentUser();
//    public List<Usertest> findTestUsertestsForUsername(String username);
//    public List<Usertest> findQuizUsertestsForCurrentUser();
//    public List<Usertest> findQuizUsertestsForUsername(String username);
//    public List<Usertest> findChallengeUsertestsForCurrentUser();
//    public List<Usertest> findChallengeUsertestsForUsername(String username);
    // provider usertest requests
//    public List<Usertest> findUsertestsForCurrentProvider();
//    public List<Usertest> findUsertestsForProvider(String providername);
    public List<Usertest> findSubmittedUsertestsForCurrentProviderByType(String testType);
    public List<Usertest> findSubmittedUsertestsForProvidernameByType(String providername, String testType);
    
//    public List<Usertest> findSubmittedAssignmentUsertestsForCurrentProvider();
//    public List<Usertest> findSubmittedAssignmentUsertestsForProvidername(String providername);
//    public List<Usertest> findSubmittedTestUsertestsForCurrentProvider();
//    public List<Usertest> findSubmittedTestUsertestsForProvidername(String providername);
    
    public List<Usertest> findAllAssignedUsertestsForTestId(Long idTest);
    public List<Usertest> findAllAssignedUsertestsForUserId(Long idUser);
    
}

