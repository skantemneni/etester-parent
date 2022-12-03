package com.etester.data.domain.content.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.etester.data.dao.JdbcDataDaoParent;
import com.etester.data.domain.test.JdbcDaoStaticHelper;

@Repository
public class JdbcTopicDao extends JdbcDataDaoParent implements TopicDao {

	public JdbcTopicDao(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public void insert(Topic topic) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(topic);
        getNamedParameterJdbcTemplate().update(insertTopicSQL, parameterSource);
	}

	@Override
	public void update(Topic topic) {
        String sql = "UPDATE topic SET name = :name, display_name = :displayName, description = :description, published = :published WHERE id_topic = :idTopic and id_level = :idLevel and id_provider = :idProvider";
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(topic);
        getNamedParameterJdbcTemplate().update(sql, parameterSource);
	}

	@Override
	public void delete(Long idTopic) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTopic", idTopic);
        String sql = "DELETE FROM topic WHERE id_topic = :idTopic";
        getNamedParameterJdbcTemplate().update(sql, args);
		
	}

	@Override
	public Topic findByTopicId(Long idTopic) {
        String sql = "SELECT * FROM topic WHERE id_topic = :idTopic";
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTopic", idTopic);
        Topic topic = getNamedParameterJdbcTemplate().queryForObject(sql, args, topicRowMapper);
        // set skills
        topic.setSkills(findSkillsForTopic(topic.getIdTopic()));
        return topic;
	}

	@Override
	public List<Topic> findByTopicName(String name, Long idProvider) {
        String sql = "SELECT * FROM topic WHERE name = :name and id_provider in (:idProvider, :idRfProvider)";
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("name", name);
        args.put("idProvider", idProvider);
        args.put("idRfProvider", Level.RFPROVIDER);
        List<Topic> topics = getNamedParameterJdbcTemplate().query(sql, args, topicRowMapper);
        // get the skills associated with all the topics
		if (topics != null && topics.size() > 0) {
			for (int i = 0; i < topics.size(); i++) {
				topics.get(i).setSkills(findSkillsForTopic(topics.get(i).getIdTopic()));
			}
		}
        return topics;
	}

	@Override
	public void insertBatch(List<Topic> topics, boolean reload) {
		JdbcDaoStaticHelper.insertTopicBatch(topics, getNamedParameterJdbcTemplate(), reload);
	}

	@Override
	public void insertBatch(List<Topic> topics) {
		insertBatch(topics, false);
	}

	@Override
	public List<Topic> findTopicsForLevel (Long idLevel) {
        String sql = "SELECT * FROM topic WHERE id_level = :idLevel ORDER BY id_topic ASC";
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevel);
        List<Topic> topics = getNamedParameterJdbcTemplate().query(sql, args, topicRowMapper);
        // get the skills associated with all the topics
		if (topics != null && topics.size() > 0) {
			for (int i = 0; i < topics.size(); i++) {
				topics.get(i).setSkills(findSkillsForTopic(topics.get(i).getIdTopic()));
			}
		}
        return topics;
	}

	@Override
	public List<Topic> findTopicsForLevelAndProvider (Long idLevel, Long idProvider) {
        String sql = "SELECT * FROM topic WHERE id_level = :idLevel and id_provider = :idProvider";
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevel);
        args.put("idProvider", idProvider);
        List<Topic> topics = getNamedParameterJdbcTemplate().query(sql, args, topicRowMapper);
        // get the skills associated with all the topics
		if (topics != null && topics.size() > 0) {
			for (int i = 0; i < topics.size(); i++) {
				topics.get(i).setSkills(findSkillsForTopic(topics.get(i).getIdTopic()));
			}
		}
        return topics;
	}

	@Override
	public List<Topic> findUnattachedTopicsForProvider (Long idProvider) {
        String sql = "SELECT * FROM topic WHERE id_provider = :idProvider AND id_level is NULL";
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idProvider", idProvider);
        List<Topic> topics = getNamedParameterJdbcTemplate().query(sql, args, topicRowMapper);
        // get the skills associated with all the topics
		if (topics != null && topics.size() > 0) {
			for (int i = 0; i < topics.size(); i++) {
				topics.get(i).setSkills(findSkillsForTopic(topics.get(i).getIdTopic()));
			}
		}
        return topics;
	}

	@Override
	public List<Topic> findAllTopicsForLevelAndProvider (Long idLevel, Long idProvider) {
        String sql = "SELECT * FROM topic WHERE id_level = :idLevel and id_provider in (:idProvider, :idRfProvider)";
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idLevel", idLevel);
        args.put("idProvider", idProvider);
        args.put("idRfProvider", Level.RFPROVIDER);
        List<Topic> topics = getNamedParameterJdbcTemplate().query(sql, args, topicRowMapper);
        // get the skills associated with all the topics
		if (topics != null && topics.size() > 0) {
			for (int i = 0; i < topics.size(); i++) {
				topics.get(i).setSkills(findSkillsForTopic(topics.get(i).getIdTopic()));
			}
		}
        return topics;
	}

	@Override
	public List<Topic> findAllTopics() {
        String sql = "SELECT * FROM topic ORDER BY id_topic ASC";
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        List<Topic> topics = getNamedParameterJdbcTemplate().query(sql, topicRowMapper);
        // get the skills associated with all the topics
		if (topics != null && topics.size() > 0) {
			for (int i = 0; i < topics.size(); i++) {
				// blank out the skills list.  too expensive an operation
				topics.get(i).setSkills(new ArrayList<Skill>());
			}
		}
        return topics;
	}

	@Override
	public List<Topic> findAllTopicsAndSkills() {
        String sql = "SELECT * FROM topic ORDER BY id_topic ASC";
        BeanPropertyRowMapper<Topic> topicRowMapper = BeanPropertyRowMapper.newInstance(Topic.class);
        List<Topic> topics = getNamedParameterJdbcTemplate().query(sql, topicRowMapper);
        // get the skills associated with all the topics
		if (topics != null && topics.size() > 0) {
			for (int i = 0; i < topics.size(); i++) {
				topics.get(i).setSkills(findSkillsForTopic(topics.get(i).getIdTopic()));
			}
		}
        return topics;
	}

	@Override
	public int countAll() {
		// return 0
		return 0;
	}

	// convenience method to grab skills associated with a topic
	private List<Skill> findSkillsForTopic(Long idTopic) {
		String sql = "SELECT * FROM skill WHERE id_topic = :idTopic";
		BeanPropertyRowMapper<Skill> skillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idTopic", idTopic);
		List<Skill> skills = getNamedParameterJdbcTemplate()
				.query(sql, args, skillRowMapper);
		return skills;
	}
	



}
