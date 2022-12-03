package com.etester.data.domain.test;

import java.util.List;

public interface TestsegmentDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	public static String insertTestsegmentSQL = "INSERT INTO testsegment (id_testsegment, id_test, name, description, text, addl_info, seq, published, sectionwrapper) "
			+ " VALUES (:idTestsegment, :idTest, :name, :description, :text, :addlInfo, :seq, :published, :sectionwrapper)";

	public static String updateTestsegmentSQL = "UPDATE testsegment SET name = :name, description = :description, text = :text, addl_info = :addlInfo, seq = :seq, published = :published, sectionwrapper = :sectionwrapper " +
			" WHERE id_testsegment = :idTestsegment and id_test = :idTest";

	public static String insertTestsectionSQL = "INSERT INTO testsection (id_testsection, id_testsegment, id_section_ref, name, description, instructions_name, report_subject, time_to_answer, question_start_index, distributed_scoring, points_per_question, negative_points_per_question, unanswered_points_per_question, seq) "
			+ " VALUES (:idTestsection, :idTestsegment, :idSectionRef, :name, :description, :instructionsName, :reportSubject, :timeToAnswer, :questionStartIndex, :distributedScoring, :pointsPerQuestion, :negativePointsPerQuestion, :unansweredPointsPerQuestion, :seq)";

	public static String insertTestsynopsislinkSQL = "INSERT INTO testsynopsislink (id_testsynopsislink, id_testsegment, id_synopsis_link_ref, name, description, link, link_type, seq) "
			+ " VALUES (:idTestsynopsislink, :idTestsegment, :idSynopsisLinkRef, :name, :description, :link, :linkType, :seq)";

    public Testsegment findByTestsegmentId(Long idTestsegment);

	public List<Testsegment> findTestsegmentsForTest(Long idTest);
	
	public List<Testsegment> findUnattachedTestsegmentsForProvider (Long idProvider);

    public void insert(Testsegment testsegment);

    public void update(Testsegment testsegment);

    public void delete(Long idTestsegment);

    public void insertBatch(List<Testsegment> testsegments);

}
