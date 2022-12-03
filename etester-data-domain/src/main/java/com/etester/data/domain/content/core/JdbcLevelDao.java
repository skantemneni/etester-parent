package com.etester.data.domain.content.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.etester.data.dao.JdbcDataDaoParent;
import com.etester.data.domain.content.LevelWithUserstate;
import com.etester.data.domain.content.additional.wordlist.WlWordlist;
import com.etester.data.domain.content.instance.Practiceinstance;
import com.etester.data.domain.test.AdaptiveTest;
import com.etester.data.domain.test.JdbcDaoStaticHelper;
import com.etester.data.domain.test.TestConstants;
import com.etester.data.domain.util.cache.EtesterCacheController;

@Repository
public class JdbcLevelDao extends JdbcDataDaoParent implements LevelDao {

	public JdbcLevelDao(DataSource dataSource) {
		super(dataSource);
	}

	// This so I do not have to repeat 100 database queries each time there is a call for 
	// loaded level
//	private static Map<Long, Level> LEVEL_WITH_TOPICSKILLS_CACHE = new HashMap<Long, Level>();
	
	// This so I do not have to repeat 100 database queries each time there is a call for 
	// loaded level
//	private static Map<Long, Level> LEVEL_WITH_TOPICSKILLS_AND_SECTIONS_CACHE = new HashMap<Long, Level>();
	
	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	@Override
	public void insert(Level level) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(level);
        getNamedParameterJdbcTemplate().update(insertLevelSQL, parameterSource);
	}

	@Override
	public void update(Level level) {
        String sql = "UPDATE level SET name = :name, display_name = :displayName, description = :description WHERE id_level = :idLevel and id_system = :idSystem and id_provider = :idProvider";
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(level);
        getNamedParameterJdbcTemplate().update(sql, parameterSource);
	}

	@Override
	public void delete(Long idLevel) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevel);
        String sql = "DELETE FROM level WHERE id_level = :idLevel";
        getNamedParameterJdbcTemplate().update(sql, args);
		
	}

	@Override
	public Level findByLevelId(Long idLevel) {
        String sql = "SELECT * FROM level WHERE id_level = :idLevel";
        BeanPropertyRowMapper<Level> levelRowMapper = BeanPropertyRowMapper.newInstance(Level.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevel);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Level level = null;
        try {
        	level = getNamedParameterJdbcTemplate().queryForObject(sql, args, levelRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return level;
	}

	@Override
	public List<Level> findByLevelName(String name) {
        String sql = "SELECT * FROM level WHERE name = :name";
        BeanPropertyRowMapper<Level> levelRowMapper = BeanPropertyRowMapper.newInstance(Level.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("name", name);
        List<Level> levels = getNamedParameterJdbcTemplate().query(sql, args, levelRowMapper);
        return levels;
	}

	@Override
	public List<Level> findBySubjectName(String subject) {
        String sql = "SELECT * FROM level WHERE LOWER(subject) = LOWER(:subject)";
        BeanPropertyRowMapper<Level> levelRowMapper = BeanPropertyRowMapper.newInstance(Level.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("subject", subject);
        List<Level> levels = getNamedParameterJdbcTemplate().query(sql, args, levelRowMapper);
        return levels;
	}

	@Override
	public void insertBatch(List<Level> levels, boolean reload) {
		JdbcDaoStaticHelper.insertLevelBatch(levels, getNamedParameterJdbcTemplate(), reload);
	}

	@Override
	public void insertBatch(List<Level> levels) {
		insertBatch(levels, false);
	}

	@Override
	public List<Level> findLevelsForProvider (Long idProvider) {
        String sql = "SELECT * FROM level WHERE id_provider = :idProvider";
        BeanPropertyRowMapper<Level> levelRowMapper = BeanPropertyRowMapper.newInstance(Level.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idProvider", idProvider);
        List<Level> levels = getNamedParameterJdbcTemplate().query(sql, args, levelRowMapper);
        return levels;
	}

	@Override
	public List<Level> findAllLevelsForProvider (Long idProvider) {
        String sql = "SELECT * FROM level WHERE id_provider in (:idProvider, :idRfProvider)";
        BeanPropertyRowMapper<Level> levelRowMapper = BeanPropertyRowMapper.newInstance(Level.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idProvider", idProvider);
        args.put("idRfProvider", Level.RFPROVIDER);
        List<Level> levels = getNamedParameterJdbcTemplate().query(sql, args, levelRowMapper);
        return levels;
	}

	@Override
	public List<Level> findAllLevels() {
        String sql = "SELECT * FROM level ORDER BY id_level ASC";
        BeanPropertyRowMapper<Level> levelRowMapper = BeanPropertyRowMapper.newInstance(Level.class);
        List<Level> levels = getNamedParameterJdbcTemplate().query(sql, levelRowMapper);
        return levels;
	}

	@Override
	public List<Level> findLevelsForChannel(Long idChannel) {
        BeanPropertyRowMapper<Level> levelRowMapper = BeanPropertyRowMapper.newInstance(Level.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idChannel", idChannel);
        List<Level> levels = getNamedParameterJdbcTemplate().query(findLevelsForChannelSQL, args, levelRowMapper);
        return levels;
	}

	@Override
	public List<Level> findLevelsForChannelAndSubject(Long idChannel, String subject) {
        String sql = "SELECT * FROM level WHERE id_system = :idChannel AND subject = :subject ORDER BY id_level ASC";
        BeanPropertyRowMapper<Level> levelRowMapper = BeanPropertyRowMapper.newInstance(Level.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idChannel", idChannel);
        args.put("subject", subject);
        List<Level> levels = getNamedParameterJdbcTemplate().query(sql, args, levelRowMapper);
        return levels;
	}

	@Override
	public Integer countAll() {
		// return 0
		return 0;
	}

    /**
     * This method is invoked by the User Activity "LevelDetailsBaseActivity" on receiving a "LevelPracticePlace" place message. 
     * This returns a LevelWithUserstate object that is loaded with Level, Topics and Skills. 
     * Used from the LevelDetailsBaseActivity:loadLevelDetailsForPractice
     * @param idLevel
     * @return LevelWithUserstate
     */
	@Override
	public LevelWithUserstate getLevelWithTopicsAndUserstate(Long idLevel) {
		LevelWithUserstate levelWithUserstate = new LevelWithUserstate();
		levelWithUserstate.setIdLevel(idLevel);
		levelWithUserstate.setLevel(getLevelWithTopics(idLevel));
		// set userstate
		Long idUser = JdbcDaoStaticHelper.getCurrentUserId(getNamedParameterJdbcTemplate());
		if (idUser != null && !idUser.equals(0l)) {
			levelWithUserstate.setSkillPracticeinstanceList(getSkillPracticeinstanceListForUserAndLevel(idLevel, idUser));
		}
		return levelWithUserstate;
	}

	private List<Practiceinstance> getSkillPracticeinstanceListForUserAndLevel(Long idLevel, Long idUser) {
        String sql = getSkillPracticeinstanceListForUserAndLevelSQL;
        BeanPropertyRowMapper<Practiceinstance> practiceinstanceRowMapper = BeanPropertyRowMapper.newInstance(Practiceinstance.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevel);
        args.put("idUser", idUser);
        List<Practiceinstance> practiceinstanceList = getNamedParameterJdbcTemplate().query(sql, args, practiceinstanceRowMapper);
        return practiceinstanceList;
	}

	@Override
	public Level getLevelWithTopics(Long idLevel) {
		if (EtesterCacheController.isCaching()) {
			// check to see if the information is available in the cache
	        // see if it already exists in cache
	    	Level level = EtesterCacheController.getLevel(idLevel);
			if (level != null) {
				return level;
			} else {
				boolean withSections = false;
				level = findByLevelId(idLevel);
				if (level == null) {
					return null;
				}
				level.setTopics(findTopicsForLevel (idLevel, withSections));
				EtesterCacheController.putLevel(level);
				return level;
			}
		} else {
			boolean withSections = false;
			Level level = findByLevelId(idLevel);
			if (level == null) {
				return null;
			}
			level.setTopics(findTopicsForLevel (idLevel, withSections));
			return level;
		}
	}

	// DO NOT CACHE THIS RESULTS - since its only used in compose which is a rare activity
	// TODO: Arre Idiot...why the heck are you caching then?
	@Override
	public Level getLevelWithTopicsAndSections(Long idLevel) {
		if (EtesterCacheController.isCaching()) {
			// check to see if the information is available in the cache
	    	Level level = EtesterCacheController.getLevelWithSections(idLevel);
			if (level != null) {
				return level;
			} else {
				boolean withSections = true;
				level = findByLevelId(idLevel);
				if (level == null) {
					return null;
				}
				level.setTopics(findTopicsForLevel (idLevel, withSections));
				EtesterCacheController.putLevelWithSections(level);
				return level;
			}
		} else {
			boolean withSections = true;
			Level level = findByLevelId(idLevel);
			if (level == null) {
				return null;
			}
			level.setTopics(findTopicsForLevel (idLevel, withSections));
			return level;
		}
	}

	/**
	 * This returns a level With Topics and Skills with all the synopsis lessons at each of these 
	 * objects listed as a link (string).
	 * Very specialized method.  Only used during Assignment Compose.
	 */
	@Override
	public Level getLevelWithSynopsisLinks(Long idLevel) {
		Level level = findByLevelId(idLevel);
		if (level == null) {
			return null;
		}
		level.setTopics(getTopicWithSynopsisLinks (idLevel));
		return level;
	}
	private List<Topic> getTopicWithSynopsisLinks (Long idLevel) {
        String sql = findTopicsWithSynopsisLinksSQL; 
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevel);
        List<Topic> topics = getNamedParameterJdbcTemplate().query(sql, args, topicRowMapper);
        // get the skills associated with all the topics - with synopsis links
		if (topics != null && topics.size() > 0) {
			for (int i = 0; i < topics.size(); i++) {
				topics.get(i).setSkills(getSkillsWithSynopsisLinks(topics.get(i).getIdTopic()));
			}
		}
        return topics;
	}
	private List<Skill> getSkillsWithSynopsisLinks (Long idTopic) {
        String sql = findSkillsWithSynopsisLinksSQL; 
        BeanPropertyRowMapper<Skill> skillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTopic", idTopic);
        List<Skill> skills = getNamedParameterJdbcTemplate().query(sql, args, skillRowMapper);
        return skills;
	}

	

    /**
     * This method is invoked by the Admin Activity "ComposeLevelPracticeActivity" on receiving a "ComposeLevelPracticePlace" place message. 
     */
	@Override
	public Level getLevelWithTopicsAndPracticeSections(Long idLevel) {
		// very rarely invoked function (only by Administrators).  No caching for that reason
		Level level = findByLevelId(idLevel);
		if (level == null) {
			return null;
		}
		level.setTopics(findTopicsForLevel (idLevel, false));
		// now fill in practice sections at each level (level, topics and skills)
		setPracticeSections(level);
		return level;
	}

	private void setPracticeSections(Level level) {
		if (level == null) {
			return;
		}
		// first set the practice skills on the level
		level.setPracticeSections(getPracticeSkillsForArtifact(level.getIdLevel(), level.getIdLevel(), TestConstants.CORE_TYPE_LEVEL));
		if (level.getTopics() != null) {
			for (int i = 0; i < level.getTopics().size(); i++) {
				// first set at topic
				level.getTopics().get(i).setPracticeSections(getPracticeSkillsForArtifact(level.getIdLevel(), level.getTopics().get(i).getIdTopic(), TestConstants.CORE_TYPE_TOPIC));
				// now set at skill
				if (level.getTopics().get(i).getSkills() != null) {
					for (int j = 0; j < level.getTopics().get(i).getSkills().size(); j++) {
						// set at skill
						level.getTopics().get(i).getSkills().get(j).setPracticeSections(getPracticeSkillsForArtifact(level.getIdLevel(), level.getTopics().get(i).getSkills().get(j).getIdSkill(), TestConstants.CORE_TYPE_SKILL));
					}
				}
			}
		}
	}

	private List<AdaptiveTest> getPracticeSkillsForArtifact(Long idLevel, Long idArtifact, String artifactType) {
        String sql = "SELECT ad.* FROM adaptive_test ad  WHERE id_level = :idLevel AND id_core_artifact = :idArtifact AND core_artifact_type = :artifactType "; 
        BeanPropertyRowMapper<AdaptiveTest> skillRowMapper = BeanPropertyRowMapper.newInstance(AdaptiveTest.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevel);
        args.put("idArtifact", idArtifact);
        args.put("artifactType", artifactType);
        List<AdaptiveTest> sections = getNamedParameterJdbcTemplate().query(sql, args, skillRowMapper);
		return sections;
	}

	private List<Topic> findTopicsForLevel (Long idLevel, boolean withSections) {
        String sql = findTopicsForLevelSQL; 
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevel);
        List<Topic> topics = getNamedParameterJdbcTemplate().query(sql, args, topicRowMapper);
        // get the skills associated with all the topics
		if (topics != null && topics.size() > 0) {
			for (int i = 0; i < topics.size(); i++) {
				if (topics.get(i).getIdTopicReference() == null) {
					topics.get(i).setSkills(findSkillsForTopicAndLevel(topics.get(i).getIdTopic(), idLevel, withSections));
				} else {
					topics.get(i).setSkills(findSkillsForTopicAndLevel(topics.get(i).getIdTopicReference(), idLevel, withSections));
				}
			}
		}
        return topics;
	}

	// convenience method to grab skills associated with a topic
	private List<Skill> findSkillsForTopicAndLevel(Long idTopic, Long idLevel, boolean withSections) {
		String sql = findSkillsForTopicAndLevelSQL;
		BeanPropertyRowMapper<Skill> skillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idTopic", idTopic);
		args.put("idLevel", idLevel);
		List<Skill> skills = getNamedParameterJdbcTemplate().query(sql, args, skillRowMapper);

		// At this time we get all the skills with their overlay mappings (sections).  However, here are the 2 problems
		// 1.) Since we are using levelId in our statement, only overlays in the current channel are shown
		// 2.) Overlays can happen in many levels.  
		// 		a.) Standard overlay where this skill is associated with the correct level
		// 		b.) Overlay in another channel where the Associated Topic has been referred.....(in this case we will have a new Topic ID, however, we will have the same skill id)
		// In this case we will consider the parent overlay to be the one where the skill is referred in its natural overlay (defined as natural idLevel = idSkill/100000)
		String tempSql = findSkillsForSkillAndBaseLevelSQL;
		for (Skill skill : skills) {
			if (skill.getIdPracticeSection() == null && (skill.getPracticeTestsString() == null || skill.getPracticeTestsString().trim().length() == 0)) {
		        BeanPropertyRowMapper<Skill> tempSkillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
		        Map<String, Object> tempArgs = new HashMap<String, Object>();
		        tempArgs.put("idSkill", skill.getIdSkill());
				// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
		        Skill tempSkill = null;
		        try {
		        	tempSkill = getNamedParameterJdbcTemplate().queryForObject(tempSql, tempArgs, tempSkillRowMapper);
		        } catch (IncorrectResultSizeDataAccessException e) {}
		        if (tempSkill != null && tempSkill.getIdPracticeSection() != null) {
		        	skill.setIdPracticeSection(tempSkill.getIdPracticeSection());
		        	skill.setPracticeTestsString(tempSkill.getPracticeTestsString());
		        }
			}
		}
		
        // get the sections associated with all the skills
		if (skills != null && skills.size() > 0 && withSections) {
			for (int i = 0; i < skills.size(); i++) {
				skills.get(i).setSections(findSectionsForSkill(skills.get(i).getIdSkill()));
			}
		}
		return skills;
	}
	
	private List<Section> findSectionsForSkill( Long idSkill) {
		String sql = "SELECT * FROM section WHERE id_skill = :idSkill";
		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper.newInstance(Section.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSkill", idSkill);
		List<Section> sections = getNamedParameterJdbcTemplate().query(sql, args, sectionRowMapper);
		
		// here is something I am doing that probably should be done differently
		sql = "SELECT * FROM wl_wordlist WHERE id_skill = :idSkill";
		BeanPropertyRowMapper<WlWordlist> wordlistRowMapper = BeanPropertyRowMapper.newInstance(WlWordlist.class);
		List<WlWordlist> wordlists = getNamedParameterJdbcTemplate().query(sql, args, wordlistRowMapper);
		sections.addAll(mapWordListsToSections (wordlists));
		return sections;
	}
	
	private List<Section> mapWordListsToSections (List<WlWordlist> wordlists) {
		List<Section> mappedSections = new ArrayList<Section>();
		if (wordlists != null && wordlists.size() > 0) {
			for (WlWordlist wordlist : wordlists) {
				Section section = new Section();
				section.setIdSection(wordlist.getIdWordlist());
				section.setName(wordlist.getName());
				section.setDescription(wordlist.getDescription());
				section.setIsExternal(1);
				section.setSectionType(Section.WORD_LIST_SECTION_TYPE);
				mappedSections.add(section);
			}
		}
		return mappedSections;
	}

	@Override
	public Integer insertAdaptiveTestRecords(Long idLevel, List<AdaptiveTest> adaptiveTestList) {
		// this does a complete delete and reload....
		// first do the delete
		String sqlDelete = "DELETE FROM adaptive_test WHERE id_adaptive_test > 0 AND id_level = :idLevel";
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevel);
        getNamedParameterJdbcTemplate().update(sqlDelete, args);
		// now add the records - if any
        if (adaptiveTestList != null && adaptiveTestList.size() > 0) {
            String sqlInsert = "INSERT INTO adaptive_test(id_level, id_section, name, id_core_artifact, core_artifact_type, test_artifacts_string, test_type, test_mode) "
            		+ " VALUES (:idLevel, :idSection, :name, :idCoreArtifact, :coreArtifactType, :testArtifactsString, :testType, :testMode) ";
    		List<SqlParameterSource> adaptiveTestParameters = new ArrayList<SqlParameterSource>();
    		for (AdaptiveTest adaptiveTest : adaptiveTestList) {
    			adaptiveTestParameters.add(new BeanPropertySqlParameterSource(adaptiveTest));
    		}
    		getNamedParameterJdbcTemplate().batchUpdate(sqlInsert, adaptiveTestParameters.toArray(new SqlParameterSource[0]));
        }
		return 0;
	}

}
