package com.etester.data.domain.content.core;

import java.util.List;

import com.etester.data.domain.content.LevelWithUserstate;
import com.etester.data.domain.test.AdaptiveTest;

public interface LevelDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	public static String insertLevelSQL = "INSERT INTO level (id_level, id_system, id_provider, subject, subject_header, name, display_name, description, text, addl_info, published, derived, id_level_reference, topiccount, skillcount) "
			+ " VALUES (:idLevel, :idSystem, :idProvider, :subject, :subjectHeader, :name, :displayName, :description, :text, :addlInfo, :published, :derived, :idLevelReference, :topicCount, :skillCount)";

	public static final String findByLevelIdSQL = "SELECT * FROM level WHERE id_level = :idLevel";

	public static String findLevelsForChannelSQL = "SELECT * FROM level WHERE id_system = :idChannel ORDER BY id_level ASC";
	
	public static final String findTopicsWithSynopsisLinksSQL = 
			"SELECT t.id_topic, t.name, t.subject, t.id_level, t.id_provider, sl.synopsis_link AS synopsis_link, sl.synopsis_video_link AS synopsis_video_link "
	    		+ "	FROM topic t LEFT JOIN synopsis_link sl ON sl.id_core_artifact = t.id_topic AND sl.core_artifact_type = 'topic' "
	    		+ " WHERE t.id_level = :idLevel "
	    		+ "	GROUP BY t.id_topic "
	    		+ "	ORDER BY id_topic ASC "; 

	public static final String findSkillsWithSynopsisLinksSQL = 
			"SELECT s.id_skill, s.name, s.subject, s.id_topic, s.id_provider, sl.synopsis_link AS synopsis_link, sl.synopsis_video_link AS synopsis_video_link "
	    		+ "	FROM skill s LEFT JOIN synopsis_link sl ON sl.id_core_artifact = s.id_skill AND sl.core_artifact_type = 'skill' "
	    		+ " WHERE s.id_topic = :idTopic "
	    		+ "	GROUP BY s.id_skill "
	    		+ "	ORDER BY id_skill ASC "; 

	
	//    public static final String findTopicsForLevelSQL = "SELECT * FROM topic WHERE id_level = :idLevel ORDER BY id_topic ASC";
//    public static final String findTopicsForLevelSQL = "SELECT t.*, sl.synopsis_link AS synopsis_link, sl.synopsis_video_link AS synopsis_video_link, MAX(a.id_section) AS id_practice_section, MAX(a.test_artifacts_string) AS practice_tests_string  "
//    		+ "	FROM topic t LEFT JOIN adaptive_test a ON a.id_core_artifact = t.id_topic "
//			+ " 			LEFT JOIN synopsis_link sl ON sl.id_core_artifact = t.id_topic "
//    		+ " WHERE t.id_level = :idLevel "
//    		+ "	GROUP BY t.id_topic "
//    		+ "	ORDER BY id_topic ASC "; 

    public static final String findTopicsForLevelSQL = 
    		  " SELECT t.*, sl.synopsis_link AS synopsis_link, sl.synopsis_video_link AS synopsis_video_link, "
    		+ "		IFNULL(MAX(a.id_section), MAX(ap.id_section)) AS id_practice_section, "
    		+ " 	IFNULL(MAX(a.test_artifacts_string), MAX(ap.test_artifacts_string)) AS practice_tests_string "
    		+ " FROM topic t LEFT JOIN level l ON t.id_level IN (l.id_level, l.id_level_reference) "
    		+ "			LEFT JOIN adaptive_test a ON a.id_core_artifact = t.id_topic AND a.id_level = l.id_level "
    		+ "			LEFT JOIN adaptive_test ap ON ap.id_core_artifact = t.id_topic AND ap.id_level = t.id_level " 
    		+ "			LEFT JOIN synopsis_link sl ON sl.id_core_artifact = t.id_topic "
    		+ " WHERE l.id_level = :idLevel "
    		+ " GROUP BY t.id_topic ";    
    

//    public static final String findSkillsForTopicSQL = "SELECT s.*, MAX(a.id_section) AS id_practice_section "
//    		+ "	FROM skill s LEFT JOIN adaptive_test a ON a.id_core_artifact = s.id_skill "
//    		+ "	WHERE id_topic = :idTopic "
//    		+ " GROUP BY s.id_skill "
//    		+ " ORDER BY s.id_skill ASC ";
 
    // Note the tricky version of the above commented function.  This pulls the "Referenced-Skills'" practice section 
    // if one is absent on the "OverLayed" Channel's skill.
	// At this time we get all the skills with their overlay mappings (sections).  However, here are the 2 problems
	// 1.) Since we are using levelId in our statement, only overlays in the current channel are shown
	// 2.) Overlays can happen in many levels.  
	// 		a.) Standard overlay where this skill is associated with the correct level
	// 		b.) Overlay in another channel where the Associated Topic has been referred.....(in this case we will have a new Topic ID, however, we will have the same skill id)
	// In this case we will consider the parent overlay to be the one where the skill is referred in its natural overlay (defined as natural idLevel = idSkill/100000)

    // not sure I will get the overlay channels 'test_artifacts_string' or its referred skill's test_artifacts_string.
//    public static final String findSkillsForTopicAndLevelSQL = 
//    		" SELECT s.*, "
//    		+ " IFNULL(MAX(a.id_section), MAX(a2.id_section)) AS id_practice_section, "
//    	    + " IFNULL(MAX(a.test_artifacts_string), MAX(a2.test_artifacts_string)) AS practice_tests_string "
//    		+ "FROM skill s LEFT JOIN adaptive_test a ON a.id_core_artifact = s.id_skill AND a.id_level in (:idLevel,  s.id_skill DIV 100000) "
//    		+ "		LEFT JOIN adaptive_test a2 ON a2.id_core_artifact = s.id_skill_reference AND a.id_level in (:idLevel,  s.id_skill DIV 100000) "
//    		+ " WHERE id_topic = :idTopic "
//    		+ " GROUP BY s.id_skill "
//    		+ "ORDER BY s.id_skill ASC ";

    public static final String findSkillsForTopicAndLevelSQL = 
    		" SELECT s.*, "
    		+ "   MAX(a.id_section) AS id_practice_section, "
    	    + "   MAX(a.test_artifacts_string) AS practice_tests_string "
    		+ " FROM skill s LEFT JOIN adaptive_test a ON a.id_core_artifact = s.id_skill AND a.id_level = :idLevel "
    		+ " WHERE s.id_topic = :idTopic "
    		+ " GROUP BY s.id_skill "
    		+ "ORDER BY s.id_skill ASC ";

    public static final String findSkillsForSkillAndBaseLevelSQL = 
    		" SELECT s.*, "
    		+ "   MAX(a.id_section) AS id_practice_section, "
    	    + "   MAX(a.test_artifacts_string) AS practice_tests_string "
    		+ " FROM skill s LEFT JOIN adaptive_test a ON a.id_core_artifact = s.id_skill AND a.id_level = (s.id_skill DIV 100000) "
    		+ " WHERE s.id_skill = :idSkill ";

    
    
    
    
    
    // TODO: SESI
    // FOR NOW, I do not have a TopicID on practiceinstance table and hence this weird looking UNION syntax 
    public static final String getSkillPracticeinstanceListForUserAndLevelSQL = 
    		" SELECT pi.* "
    		+ " FROM practiceinstance pi LEFT JOIN skill s ON s.id_skill = pi.id_artifact "
    		+ " 					 LEFT JOIN topic t ON s.id_topic = t.id_topic "
    		+ "						 LEFT JOIN `level` l ON t.id_level = l.id_level "
    		+ " WHERE pi.id_user = :idUser AND "
    		+ " 	pi.artifact_type = 'Skill' AND "
    		+ "		l.id_level = :idLevel "
    + " UNION "     		
    		+ " SELECT pi.* "
    		+ " FROM practiceinstance pi LEFT JOIN topic t ON t.id_topic = pi.id_artifact "
    		+ " 					 LEFT JOIN `level` l ON t.id_level = l.id_level "
    		+ " WHERE pi.id_user = :idUser AND "
    		+ "		pi.artifact_type = 'Topic' AND "
    		+ "		l.id_level = :idLevel ";
    
    
    
//    public static final String getSkillPracticeinstanceListForUserAndLevelSQL = "SELECT pi.* "
//    		+ " FROM practiceinstance pi LEFT JOIN skill s ON s.id_skill = pi.id_artifact "
//    		+ " 					 LEFT JOIN topic t ON s.id_topic = t.id_topic "
//    		+ "						 LEFT JOIN `level` l ON t.id_level = l.id_level "
//    		+ " WHERE pi.id_user = :idUser AND "
//    		+ " 	pi.artifact_type = 'Skill' AND "
//    		+ "		l.id_level = :idLevel ";
//    		
//    public static final String getTopicPracticeinstanceListForUserAndLevelSQL = "SELECT pi.* "
//    		+ " FROM practiceinstance pi LEFT JOIN topic t ON t.id_topic = pi.id_artifact "
//    		+ " 					 LEFT JOIN `level` l ON t.id_level = l.id_level "
//    		+ " WHERE pi.id_user = :idUser AND "
//    		+ "		pi.artifact_type = 'Topic' AND "
//    		+ "		l.id_level = :idLevel ";
    		
	public void insert(Level level);

    public void update(Level level);

    public void delete(Long idLevel);

    public Level findByLevelId(Long idLevel);

	public List<Level> findLevelsForProvider (Long idProvider);

	public List<Level> findAllLevelsForProvider (Long idProvider);

	public List<Level> findByLevelName(String name);

	public List<Level> findBySubjectName(String subject);

	/**
	 * Return all the levels in the system
	 * @return
	 */
	public List<Level> findAllLevels();
	public List<Level> findLevelsForChannel(Long idChannel);
	public List<Level> findLevelsForChannelAndSubject(Long idChannel, String subject);

    public void insertBatch(List<Level> levels);

    // This deletes the data before reloading the same
    public void insertBatch(List<Level> levels, boolean reload);

    public Integer countAll();

    /**
     * Value added function.  This returns a level that is loaded with Topics and Skills. 
     * @param idLevel
     * @return
     */
    public Level getLevelWithTopics (Long idLevel);

    /**
     * This returns a LevelWithUserstate object that is loaded with Level, Topics and Skills. 
     * Used from the LevelDetailsBaseActivity:loadLevelDetailsForPractice
     * @param idLevel
     * @return LevelWithUserstate
     */
    public LevelWithUserstate getLevelWithTopicsAndUserstate (Long idLevel);

    /**
     * Value added function.  This returns a level that is loaded with Topics and Skills and Sections.
     *  
     * @param idLevel
     * @return
     */
    public Level getLevelWithTopicsAndSections (Long idLevel);
    
    /**
     * Value added function.  This returns a level that is loaded with all available synopsis links.
     *  
     * @param idLevel
     * @return
     */
    public Level getLevelWithSynopsisLinks (Long idLevel);
    
    /**
     * Insert/Replace Adaptive test records
     * @param idLevel
     * @param adaptiveTestList
     * @return
     */
    public Integer insertAdaptiveTestRecords(Long idLevel, List<AdaptiveTest> adaptiveTestList);

    /**
     * Value added function.  This returns a level that is loaded with Topics and Skills and Practice Sections (at all levels).
     * @param idLevel
     * @return
     */
    public Level getLevelWithTopicsAndPracticeSections(Long idLevel);
    
}
