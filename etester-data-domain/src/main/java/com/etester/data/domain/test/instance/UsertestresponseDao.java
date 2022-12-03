package com.etester.data.domain.test.instance;

import java.util.List;


public interface UsertestresponseDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	public static String insertUsertestresponseSQL = "INSERT INTO usertestresponse (id_usertest, response, date_saved) "
			+ " VALUES (:idUsertest, :response, :dateSaved)";

	public static String updateUsertestresponseByUsertestIdSQL = "UPDATE usertestresponse SET response = :response, date_saved = :dateSaved " +
			" WHERE id_usertest = :idUsertest";

	public static final String testinstanceSQL = "SELECT t.id_test AS id_test, u.id_user AS id_user, ut.id_provider AS id_provider, ut.id_usertest AS id_usertest, "
			+ "		IFNULL(ut.name, t.name) AS name, IFNULL(ut.description, t.description) AS description, "
			+ "		t.test_type AS test_type, t.report_by_subject AS report_by_subject, t.question_count AS question_count, t.point_count AS point_count, t.time_to_answer AS time_to_answer, "
			+ " 	ut.is_report_available_to_view_by_student AS isReportAvailableToViewByStudent, "
			+ "		ut.test_completion_date AS test_completion_date "
			+ " FROM usertest ut INNER JOIN test t ON ut.id_test = t.id_test  "
			+ "		 INNER JOIN user u on ut.id_user = u.id_user "
			+ " WHERE ut.id_usertest = :idUsertest " ; 
	
	// TODO: Sesi. Note that we cannot get this information from the database testsegment and testsection (just in case they are no longer there).
	// We render a test from the cache (potentially a serialized test that no longer exists in the database).  So we need to grab report related "Testsections and Testsegments" from the same 
	// location (potentially a serialized test).
	public static final String testsectioninstanceSQL = "SELECT ts.id_section_ref AS id_section, ts.name AS testsection_name, ts.description AS testsection_description, ts.report_subject as report_subject, "
			+ "		ts.question_count AS question_count, ts.point_count AS point_count, ts.time_to_answer AS time_to_answer, ts.points_per_question AS points_per_question, ts.negative_points_per_question AS negative_points_per_question, "
			+ " 	ts.unanswered_points_per_question AS unanswered_points_per_question, ts.seq AS seq, ts.distributed_scoring AS distributed_scoring, ts.question_start_index AS question_start_index, ts.id_testsection AS id_testsection "
			+ "FROM testsection ts INNER JOIN testsegment tg ON ts.id_testsegment = tg.id_testsegment  "
			+ "						INNER JOIN test t ON tg.id_test = t.id_test  "
			+ "						INNER JOIN usertest ut ON ut.id_test = t.id_test  "
			+ "WHERE ts.id_section_ref = :idSection "
			+ "		AND ut.id_usertest = :idUsertest" ;
	


	public static final String createTestinstanceSQL = "INSERT INTO testinstance (id_test, id_user, id_provider, id_usertest, name, description, test_type, report_by_subject, "
			+ "question_count, point_count, time_to_answer, correct_count, wrong_count, unanswered_count, user_points, time_in_seconds, archived, test_completion_date, is_report_available_to_view_by_student) "
			+ " VALUES (:idTest, :idUser, :idProvider, :idUsertest, :name, :description, :testType, :reportBySubject, "
			+ ":questionCount, :pointCount, :timeToAnswer, :correctCount, :wrongCount, :unansweredCount, :userPoints, :timeInSeconds, :archived, :testCompletionDate, :isReportAvailableToViewByStudent)";

	public static final String createTestinstanceSectionSQL = "INSERT INTO testinstance_section (id_testinstance, id_section, testsection_name, testsection_description, report_subject, question_count, point_count, time_to_answer, "
			+ " points_per_question, negative_points_per_question, unanswered_points_per_question, seq, distributed_scoring, question_start_index, id_testsection, "
			+ " correct_count, wrong_count, unanswered_count, user_points, time_in_seconds) "
			+ " VALUES (:idTestinstance, :idSection, :testsectionName, :testsectionDescription, :reportSubject, :questionCount, :pointCount, :timeToAnswer, "
			+ " :pointsPerQuestion, :negativePointsPerQuestion, :unansweredPointsPerQuestion, :seq, :distributedScoring, :questionStartIndex, :idTestsection, "
			+ " :correctCount, :wrongCount, :unansweredCount, :userPoints, :timeInSeconds )";

	public static final String createTestinstanceDetailSQL = "INSERT INTO testinstance_detail (id_testinstance, id_testinstance_section, id_section, id_question, question_status, answer_text, user_points, time_in_seconds, attempt_quality) "
			+ " VALUES (:idTestinstance, :idTestinstanceSection, :idSection, :idQuestion, :questionStatus, :answerText, :userPoints, :timeInSeconds, :attemptQuality)";

	public static final String questionIdListForSection = "select id_question from question where id_section = :idSection order by id_question";
	public static final String questionIdListForDerivedSection = "select id_question from derived_section_question where id_section = :idSection order by question_order";
	
	// One magical SQL that gives us what we are looking for (TotalTestTime - in minute) for TimedTests and -1 for Un-Timed Tests 
	public static final String usertestTimeToAnswerSQL = "SELECT IF(t.timed IS NULL OR t.timed = 0, -1, t.time_to_answer) FROM usertest ut INNER JOIN test t ON ut.id_test = t.id_test WHERE ut.id_usertest = :idUsertest ";

	public Usertestresponse findByUsertestresponseId(Long idTestresponse);

    public Usertestresponse findUsertestresponseForUsertest(Long idUsertest);

    /**
     * Invoked by a User/Student to save and submit test responses.
     * Note that saveTestResponse does a lot of stuff.  See the implementation comments for more detail
     * @param usertestresponse
     * @return
     */
    public Integer saveTestResponse(Usertestresponse usertestresponse);

    /**
     * Invoked by a Provider to approve test responses
     * @param usertestresponse
     * @return
     */
    public Integer approveTestResponse(Usertestresponse usertestresponse);

    /**
     * Invoked by a Provider to approve test response - with a response ID
     * Note that we retrieve the actual response from the previously saved usertestresponse object (table) and approve the same.
     * @param idUsertestresponse
     * @return
     */
    public Integer approveTestResponse(Long idUsertest);

    /**
     * Invoked by a Provider to reject test responses
     * @param usertestresponse
     * @return
     */
    public Integer rejectTestResponse(Usertestresponse usertestresponse);

    /**
     * Invoked by a Student to archive test responses
     * @param usertestresponse
     * @return
     */
    public Integer archiveTestResponse(Usertestresponse usertestresponse);

    /**
     * Invoked by a Student to archive test responses
     * @param usertestresponse
     * @return
     */
    public Integer archiveTestResponse(Long idUsertest);

}
