package com.etester.data.domain.test;

import java.util.List;
import java.util.Optional;

import com.etester.data.domain.util.UpdateStatusBean;

public interface TestDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	// Serialized Test Stuff - used for caching
	public static final String findSerializedtestByTestId = 
    		"SELECT st.* FROM serialized_test st WHERE st.id_test = :idTest ";
	public static final String insertSerializedtestSQL = "INSERT INTO serialized_test (id_test, test_string_json, date_saved) "
			+ " VALUES (:idTest, :testStringJson, :dateSaved)";

	public static String getTestByTestIdSQL = "SELECT * FROM test WHERE id_test = :idTest"; 

	// Most fundemental SQL statements
	// 1.) findByTestId - public
	public static final String findByTestIdSQL = // "SELECT * FROM test WHERE id_test = :idTest";
			  " SELECT t.*, o.`name` AS organization_name, s.`name` AS channel_name, "
			+ "		CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name "
			+ "	FROM test t LEFT JOIN organization o ON t.id_organization = o.id_organization "
			+ "				LEFT JOIN `system` s ON t.id_channel = s.id_system "
			+ "				LEFT JOIN user u ON t.id_provider = u.id_user "
			+ "	WHERE id_test = :idTest ";
	// *2.) findTestsByTestName - public (not  used)
	public static final String findTestsByTestNameSQL = "SELECT * FROM test WHERE name = :name";
	
	// 3.) findCompleteTestByTestId - private 
	public static final String findCompleteTestByTestIdSQL = "SELECT * FROM test WHERE id_test = :idTest";
	
	// 4.) findTestsegmentsForTest - private
	public static final String findTestsegmentsForTestSQL = "SELECT * FROM testsegment WHERE id_test = :idTest ORDER BY seq";
	
	// 5.) findTestsectionsForTestsegmentSQL - private
	public static final String findTestsectionsForTestsegmentSQL = "SELECT * FROM testsection WHERE id_testsegment = :idTestsegment ORDER BY seq";
	
	// 6.) findTestsynopsislinksForTestsegmentSQL - private
	public static final String findTestsynopsislinksForTestsegmentSQL = "SELECT * FROM testsynopsislink WHERE id_testsegment = :idTestsegment ORDER BY seq";
	
	
	public static final String validateUpdateActionSQL = 
			  " SELECT COUNT(u.id_user) FROM user u "
			+ "		LEFT JOIN organization_provider op ON op.id_provider = u.id_user "
			+ "		LEFT JOIN organization_channel oc ON oc.id_organization = op.id_organization "
			+ "		LEFT JOIN system s ON s.id_system = oc.id_channel "
			+ "	WHERE s.id_system = :idSystem AND "
			+ "			u.id_user = :idUser ";
			
	// note that tests are inserted Un-Published
//	public static final String insertTestSQL = "INSERT INTO test (id_test, id_provider, id_organization, id_channel, examtrack, name, description, text, addl_info, timed, time_to_answer, published, access_level, test_type) "
//			+ " VALUES (:idTest, :idProvider, :idOrganization, :idChannel, :examtrack, :name, :description, :text, :addlInfo, :timed, :timeToAnswer, 0, :accessLevel, :testType)";

	// note that tests can update "published" flag via the web
//	public static final String updateTestSQL = "UPDATE test SET name = :name, description = :description, text = :text, addl_info = :addlInfo, " +
//			" access_level = :accessLevel, examtrack = :examtrack, " +
//			" timed = :timed " +
//			" WHERE id_test = :idTest and id_provider = :idProvider";

	// Note the logic that "New Tests" are marked published=0.  "Old tests" are not updated for the published flag.  Meaning publishing is NOT controlled from the Frontend for Tests.
	// Also note that AccessLevel cannot be controlled from the front end.  Tests are created "Private Access" and cannot be updated from the frontend. 
	public static final String upsertTestSQL = "INSERT INTO test (id_test, id_provider, id_organization, id_channel, examtrack, name, description, text, addl_info, auto_grade, auto_publish_results, report_by_subject, combine_sections, timed, time_to_answer, published, access_level, test_type) "
			+ " VALUES (:idTest, :idProvider, :idOrganization, :idChannel, :examtrack, :name, :description, :text, :addlInfo, :autoGrade, :autoPublishResults, :reportBySubject, :combineSections, :timed, :timeToAnswer, 0, 1, :testType) "
			+ " ON DUPLICATE KEY "
			+ " UPDATE name = :name, description = :description, text = :text, addl_info = :addlInfo, auto_grade = :autoGrade, auto_publish_results = :autoPublishResults, report_by_subject = :reportBySubject, combine_sections = :combineSections, examtrack = :examtrack, timed = :timed ";
	
//	// note that Assignments can be inserted Published or Un-Published
//	public static final String insertAssignmentSQL = "INSERT INTO test (id_test, id_provider, id_organization, id_channel, examtrack, name, description, text, addl_info, timed, time_to_answer, published, access_level, test_type) "
//			+ " VALUES (:idTest, :idProvider, :idOrganization, :idChannel, :examtrack, :name, :description, :text, :addlInfo, :timed, :timeToAnswer, :published, :accessLevel, :testType)";
//
//	public static final String updateAssignmentSQL = "UPDATE test SET name = :name, description = :description, text = :text, addl_info = :addlInfo, " +
//			" published = :published, access_level = :accessLevel, examtrack = :examtrack, " +
//			" timed = :timed " +
//			" WHERE id_test = :idTest and id_provider = :idProvider";

	// Note the logic that "New Assignments" are marked published=<the flag coming in>.  "Old Assignments" are in fact updated with the new setting for the published flag.
	// Also note that AccessLevel cannot be controlled from the front end.  Tests are created "Private Access" and cannot be updated from the frontend. 
	public static final String upsertAssignmentSQL = "INSERT INTO test (id_test, id_provider, id_organization, id_channel, examtrack, name, description, text, addl_info, auto_grade, auto_publish_results, report_by_subject, combine_sections, timed, time_to_answer, published, access_level, test_type) "
			+ " VALUES (:idTest, :idProvider, :idOrganization, :idChannel, :examtrack, :name, :description, :text, :addlInfo, :autoGrade, :autoPublishResults, :reportBySubject, :combineSections, :timed, :timeToAnswer, :published, 1, :testType) "
			+ " ON DUPLICATE KEY "
			+ " UPDATE name = :name, description = :description, text = :text, addl_info = :addlInfo, auto_grade = :autoGrade, auto_publish_results = :autoPublishResults, report_by_subject = :reportBySubject, combine_sections = :combineSections, published = :published, examtrack = :examtrack, timed = :timed ";
			
	public static final String findAllTestsSQL = "SELECT * FROM test "; 
//	public static final String findAllTestsSQL = 
//			  " SELECT t.*, "
//			+ " 		IF(ti.id_provider IS NULL OR ti.id_provider = 0, "
//			+ "			CONCAT(IFNULL(p.first_name, ''), IF(p.first_name IS NULL OR p.first_name = '','',' '), IFNULL(p.middle_name, ''), IF(p.middle_name IS NULL OR p.middle_name = '','',' '), IFNULL(p.last_name, '')), "
//			+ "			CONCAT(IFNULL(p2.first_name, ''), IF(p2.first_name IS NULL OR p2.first_name = '','',' '), IFNULL(p2.middle_name, ''), IF(p2.middle_name IS NULL OR p2.middle_name = '','',' '), IFNULL(p2.last_name, '')) "
//			+ "		) AS provider_name, "
//			+ "		o.`name` AS organization_name, s.`name` AS channel_name "
//			+ "	FROM test t LEFT JOIN user u ON t.id_provider = u.id_user "
//			+ " LEFT JOIN organization o ON t.id_organization = o.id_organization " 
//			+ " ORDER BY t.id_test ASC "; 

	public static final String findAllTestsInChannelSQL = 
			  " SELECT t.* FROM test t WHERE t.id_channel = :idChannel ORDER BY t.id_test ASC "; 

	public static final String findAllTestsInChannelByTypeSQL = 
			  " SELECT t.* FROM test t WHERE t.id_channel = :idChannel AND t.test_type = :testType ORDER BY t.id_test ASC "; 
	
	public static final String findAllTestsOwnedByProviderSQL = 
			  " SELECT t.*, "
			+ "		o.name AS organization_name, "
			+ "		CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name "
			+ "	FROM test t LEFT JOIN user u ON t.id_provider = u.id_user "
			+ " LEFT JOIN organization o ON t.id_organization = o.id_organization " 
			+ " WHERE t.id_provider = :idProvider "
			+ " ORDER BY t.id_test ASC "; 

	public static final String findTestsOwnedByProviderByTypeSQL = 
			  " SELECT t.*, "
			+ "		o.name AS organization_name, "
			+ "		CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name "
			+ "	FROM test t LEFT JOIN user u ON t.id_provider = u.id_user "
			+ " LEFT JOIN organization o ON t.id_organization = o.id_organization " 
			+ " WHERE t.id_provider = :idProvider "
			+ "		AND t.test_type = :testType "
			+ " ORDER BY t.id_test ASC "; 

	public static final String findAllTestsOwnedByProviderOrOrganizationSQL = 
			  " SELECT t.*, "
			  + "	o.name AS organization_name, "
			  + "	CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name "
			  + " FROM test t LEFT JOIN user u ON t.id_provider = u.id_user "
			  + "				LEFT JOIN organization o ON t.id_organization = o.id_organization "
			  + " WHERE (t.id_provider = :idProvider OR t.id_organization IN ( SELECT id_organization FROM organization_provider WHERE id_provider = :idProvider )) "
			  + " ORDER BY t.id_test ASC "; 

	public static final String findTestsOwnedByProviderOrOrganizationByTypeSQL = 
			  " SELECT t.*, "
			  + "	o.name AS organization_name, "
			  + "	CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name "
			  + " FROM test t LEFT JOIN user u ON t.id_provider = u.id_user "
			  + "				LEFT JOIN organization o ON t.id_organization = o.id_organization "
			  + " WHERE (t.id_provider = :idProvider OR t.id_organization IN ( SELECT id_organization FROM organization_provider WHERE id_provider = :idProvider )) "
			  + "		AND t.test_type = :testType "
			  + " ORDER BY t.id_test ASC "; 

	public static final String findAllTestsAvailableToAssignForProviderSQL = 
			  " SELECT t.*, "
			  + "	o.name AS organization_name, "
			  + "	CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name "
			  + " FROM test t LEFT JOIN user u ON t.id_provider = u.id_user "
			  + "				LEFT JOIN organization o ON t.id_organization = o.id_organization "
			  + "				LEFT JOIN organization_provider op ON u.id_user = op.id_provider "
			  + " WHERE t.published = 1 "
			  + "	AND (t.id_provider = :idProvider OR "
			  + "				t.access_level = :accessLevelPublic OR "
			  + "				(t.access_level = :accessLevelOrganization AND t.id_organization IN ( SELECT id_organization FROM organization_provider WHERE id_provider = :idProvider ))) "
			  + " ORDER BY t.id_test ASC "; 
	
	public static final String findTestsAvailableToAssignForProviderByTypeSQL = 
			  " SELECT t.*, "
			  + "	o.name AS organization_name, "
			  + "	CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name "
			  + " FROM test t LEFT JOIN user u ON t.id_provider = u.id_user "
			  + "				LEFT JOIN organization o ON t.id_organization = o.id_organization "
			  + "				LEFT JOIN organization_provider op ON u.id_user = op.id_provider "
			  + " WHERE t.published = 1 "
			  + "	AND (t.id_provider = :idProvider OR "
			  + "				t.access_level = :accessLevelPublic OR "
			  + "				(t.access_level = :accessLevelOrganization AND t.id_organization IN ( SELECT id_organization FROM organization_provider WHERE id_provider = :idProvider ))) "
			  + "	AND t.test_type = :testType "
			  + " ORDER BY t.id_test ASC "; 
	
    public static final String findTestByUsertestIdWithResponseSQL = 
    		"SELECT t.*, ut.test_status AS test_status, ts.response AS test_response "
    		+ "FROM test t INNER JOIN usertest ut ON t.id_test = ut.id_test "
    		+ "LEFT JOIN usertestresponse ts ON ut.id_usertest = ts.id_usertest "
    		+ "WHERE ut.id_usertest = :idUsertest AND ut.id_user = :userid";

    public static final String findProviderTestByUsertestIdWithResponseSQL = 
    		"SELECT t.*, ut.test_status AS test_status, ts.response AS test_response "
    		+ "FROM test t INNER JOIN usertest ut ON t.id_test = ut.id_test "
    		+ "LEFT JOIN usertestresponse ts ON ut.id_usertest = ts.id_usertest "
    		+ "WHERE ut.id_usertest = :idUsertest AND ut.id_provider = :providerid";

	public static final String findAllMockExamsInTrackSQL = 
			  " SELECT t.*, ta.date_free_start AS date_free_start, ta.date_free_end AS date_free_end, ta.subscription_date_free_end AS subscription_date_free_end, "
			+ "		o.name AS organization_name, "
			+ "		CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name, "
			+ " 	et.description AS examtrack_description "
			+ "	FROM test t LEFT JOIN user u ON t.id_provider = u.id_user "
			+ " LEFT JOIN testalias ta ON t.id_test = ta.id_test " 
			+ " LEFT JOIN organization o ON t.id_organization = o.id_organization "
			+ " LEFT JOIN examtrack et ON t.examtrack = et.examtrack "
			+ " WHERE t.published = 1 "
			+ " 	AND t.access_level = 3 "
			+ " 	AND t.examtrack = :examtrack "
			+ " ORDER BY o.name ASC, t.id_test ASC "; 

//	public static final String findAllMockExamsInTrackSQL = 
//			  " SELECT t.*, "
//			+ "		o.name AS organization_name, "
//			+ "		CONCAT(IFNULL(u.first_name, ''), IF(u.first_name IS NULL OR u.first_name = '','',' '), IFNULL(u.middle_name, ''), IF(u.middle_name is null OR u.middle_name = '','',' '), IFNULL(u.last_name, '')) AS provider_name, "
//			+ " 	et.description AS examtrack_description "
//			+ "	FROM test t LEFT JOIN user u ON t.id_provider = u.id_user "
//			+ " LEFT JOIN organization o ON t.id_organization = o.id_organization "
//			+ " LEFT JOIN examtrack et ON t.examtrack = et.examtrack "
//			+ " WHERE t.published = 1 "
//			+ " 	AND t.examtrack = :examtrack "
//			+ " ORDER BY o.name ASC, t.id_test ASC "; 

//    public static final String findSerializedtestByUsertestIdWithResponseSQL = 
//    		"SELECT st.*, ut.test_status AS test_status, ts.response AS test_response "
//    		+ "FROM serialized_test st INNER JOIN usertest ut ON st.id_test = ut.id_test "
//    		+ "LEFT JOIN usertestresponse ts ON ut.id_usertest = ts.id_usertest "
//    		+ "WHERE ut.id_usertest = :idUsertest AND ut.id_user = :userid";
//
    
	public List<Test> findAllTests();
    public List<Test> findAllTestsInChannel(Long idChannel);
    public List<Test> findAllTestsInChannelByType(Long idChannel, String testType);


    public Test findByTestId(Long idTest);

    public Test findCompleteTestById(Long idTest);

	public List<Test> findTestsByTestName(String name);
	
    public List<Test> findAllEditableTestsOwnedByCurrentProvider();
    public List<Test> findAllEditableTestsOwnedByProvider(Long idProvider);
    public List<Test> findEditableTestsOwnedByCurrentProviderByType(String testType);
    public List<Test> findEditableTestsOwnedByProviderByType(Long idProvider, String testType);

    public List<Test> findAllEditableTestsOwnedByCurrentProviderOrOrganization();
    public List<Test> findAllEditableTestsOwnedByProviderOrOrganization(Long idProvider);
    public List<Test> findEditableTestsOwnedByCurrentProviderOrOrganizationByType(String testType);
    public List<Test> findEditableTestsOwnedByProviderOrOrganizationByType(Long idProvider, String testType);

    public List<Test> findAllTestsAvailableToAssignForCurrentProvider();
    public List<Test> findTestsAvailableToAssignForCurrentProviderByType(String testType);
    public List<Test> findAllTestsAvailableToAssignForProvider(Long idProvider);
    public List<Test> findTestsAvailableToAssignForProviderByType(Long idProvider, String testType);

//	public List<Testsegment> findTestsegmentsForTest (Long idTest);

//    public Test insert(Test test);

    public Test updateTest(Test test);

    public Test updateTest(Test test, Long loggedInProviderId);

    public String deleteTest(Long idTest);

    public String deleteTest(Test test);

//    public List<Test> findTestsForCurrentUser();

//    public List<Test> findTestsForUsername(String username);

//    public List<Test> findTestsForCurrentUser(String testType);

//    public List<Test> findTestsForUsername(String username, String testType);

//    // Most important function used on administering the test.  It returns a Test object 
//    // with any associated response.  Not sure where this needs to be situated - TestDao 
//    // or UsaertestDao.  For now, though, I choose to put it here.  
//	public Test findTestByUsertestIdWithResponse(Long idUsertest);
//
//    // Used by the provider while grading the test.  
//	public Test findProviderTestByUsertestIdWithResponse(Long idUsertest);

    // Most important function used on administering the test.  It returns a Test object 
    // with any associated response.  Not sure where this needs to be situated - TestDao 
    // or UsaertestDao.  For now, though, I choose to put it here.  
	public Optional<TestWithResponse> findTestByUsertestIdWithResponse(Long idUsertest);

    // Used by the provider while grading the test.  
	public TestWithResponse findProviderTestByUsertestIdWithResponse(Long idUsertest);

	// need this to allow for TestWithResponse Service located needed method (need for EntityProxy)
	public TestWithResponse findByTestWithResponseId(Long id);

	/**
	 * This method returns mock exams that are available for users to attempt - for a given exam track.  
	 * examtrack values are defined in the examtrack table.  Example examtracks can be  "EAMCETEngineering", 
	 * "EAMCETMedical", "IITEngineering", "BITSATEngineering", "BankPO", etc.
	 * 
	 * @param trackname
	 * @return
	 */
	public List<Test> findMockExamsByTrack(String trackname);
	
	/**
	 * Attempts to activate a test - for a given user.
	 * @param id_test
	 * @param redumptionCode
	 * @return
	 */
	public String activateTest(Long id_test, String redumptionCode);
	
	// Test Upload/Download related functions
	/**
	 * Find all Tests for a given Channel and of the requested Type for Download.  Note that testType can be "All" for all types.
	 * @param idChannel
	 * @param idProvider
	 * @param testType
	 * @return List<Test> 
	 */
	public List<Test> findTestsForChannelProviderAndTypeForDownload (Long idChannel, Long idProvider, String testType);
	
	/**
	 * Find all Tests for a given Channel and of the requested Type for Download.  Note that testType can be "All" for all types.
	 * @param idChannel
	 * @param testType
	 * @return List<Test> 
	 */
	public List<Test> findTestsForChannelAndTypeForDownload (Long idChannel, String testType);
	
	/**
	 * Find all Tests for a given Provider and of the requested Type for Download.  Note that testType can be "All" for all types.
	 * @param idProvider
	 * @param testType
	 * @return List<Test> 
	 */
	public List<Test> findTestsForProviderAndTypeForDownload (Long idProvider, String testType);
	
    /**
     * Find all Tests for a given list of testId's for Download.  
     * @param derivedSection
     * @return List<Test> 
     */
	public List<Test> findTestsForTestIdListForDownload(List<Long> testIdList);

	/**
	 * Inserts a batch of tests as part of an Upload operation 
	 * @param tests
	 */
    public List<UpdateStatusBean> insertTestBatch(List<Test> tests);

    /**
     * Inserts a batch of tests as part of an Upload operation.  This operation will reload the test if requested
     * @param tests
     * @param reload
     * @return
     */
    public List<UpdateStatusBean> insertTestBatch(List<Test> tests, boolean reload);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Print Functionality
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Used to get a handle on the complete Test object (Testsegments, Testsections and Questions with Answers).  
	 * Used for print purposes.
	 * @param idTest
	 * @return
	 */
    public Test findCompleteTestByTestIdForPrint(Long idTest);

	/**
	 * Used to get a handle on the complete Test object (Testsegments, Testsections and Questions with Answers).  
	 * Used for print purposes.
	 * @param idUsertest
	 * @return
	 */
    public Test findCompleteTestByUsertestIdForPrint(Long idUsertest);
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Printsettings related functions
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String findCompleteTestByTestIdForPrintSQL = "SELECT t.*, ps.settings AS printsettings FROM test t LEFT JOIN printsettings ps ON ps.id_test = t.id_test WHERE t.id_test = :idTest"; 
	
	public static final String updatePrintsettingsSQL = "INSERT INTO printsettings (id_test, settings, date_saved) VALUES (:idTest, :settings, :dateSaved) "
			+ " ON DUPLICATE KEY "
			+ " UPDATE settings = :settings, date_saved = :dateSaved ";

	public static final String deletePrintsettingsSQL = "DELETE FROM printsettings WHERE id_test = :idTest";

	/**
     * Invoked by a Administrator to save any settings on the test.
     * @param printsettings
     * @return
     */
    public Integer savePrintsettings(Printsettings printsettings);
   
	/**
     * Invoked by a Administrator to delete any settings on the test.
     * @param idTest
     * @return
     */
    public Integer deletePrintsettings(Long idTest);
   

}
