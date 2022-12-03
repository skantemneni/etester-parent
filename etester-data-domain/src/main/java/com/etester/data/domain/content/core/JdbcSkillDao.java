package com.etester.data.domain.content.core;

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
public class JdbcSkillDao extends JdbcDataDaoParent implements SkillDao {

	public JdbcSkillDao(DataSource dataSource) {
		super(dataSource);
	}

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	@Override
	public void insert(Skill skill) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(skill);
        getNamedParameterJdbcTemplate().update(insertSkillSQL, parameterSource);
	}

	@Override
	public void update(Skill skill) {
        String sql = "UPDATE skill SET name = :name, display_name = :displayName, description = :description, published = :published WHERE id_skill = :idSkill and id_topic = :idTopic and id_provider = :idProvider";
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(skill);
        getNamedParameterJdbcTemplate().update(sql, parameterSource);
	}

	@Override
	public void delete(Long idSkill) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idSkill", idSkill);
        String sql = "DELETE FROM skill WHERE id_skill = :idSkill";
        getNamedParameterJdbcTemplate().update(sql, args);
		
	}

	@Override
	public Skill findBySkillId(Long idSkill) {
        String sql = findBySkillIdSQL;
        BeanPropertyRowMapper<Skill> skillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idSkill", idSkill);
        Skill skill = getNamedParameterJdbcTemplate().queryForObject(sql, args, skillRowMapper);
        skill.setSections(findSectionsForSkill(skill.getIdSkill()));
        return skill;
	}

	@Override
	public List<Skill> findBySkillName(String name, Long idProvider) {
        String sql = "SELECT * FROM skill WHERE name = :name and id_provider in (:idProvider, :idRfProvider)";
        BeanPropertyRowMapper<Skill> skillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("name", name);
        args.put("idProvider", idProvider);
        args.put("idRfProvider", Level.RFPROVIDER);
        List<Skill> skills = getNamedParameterJdbcTemplate().query(sql, args, skillRowMapper);
        return skills;
	}

	@Override
	public void insertBatch(List<Skill> skills, boolean reload) {
		JdbcDaoStaticHelper.insertSkillBatch(skills, getNamedParameterJdbcTemplate(), reload);
	}

	@Override
	public void insertBatch(List<Skill> skills) {
		insertBatch (skills, false);
	}

	@Override
	public List<Skill> findSkillsForTopic (Long idTopic) {
        String sql = "SELECT * FROM skill WHERE id_topic = :idTopic ORDER BY id_skill ASC";
        BeanPropertyRowMapper<Skill> skillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTopic", idTopic);
        List<Skill> skills = getNamedParameterJdbcTemplate().query(sql, args, skillRowMapper);
        return skills;
	}

	@Override
	public List<Skill> findSkillsForTopicAndProvider (Long idTopic, Long idProvider) {
        String sql = "SELECT * FROM skill WHERE id_topic = :idTopic AND id_provider = :idProvider";
        BeanPropertyRowMapper<Skill> skillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTopic", idTopic);
        args.put("idProvider", idProvider);
        List<Skill> skills = getNamedParameterJdbcTemplate().query(sql, args, skillRowMapper);
        return skills;
	}

	@Override
	public List<Skill> findUnattachedSkillsForProvider (Long idProvider) {
        String sql = "SELECT * FROM skill WHERE id_provider = :idProvider AND id_topic is NULL";
        BeanPropertyRowMapper<Skill> skillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idProvider", idProvider);
        List<Skill> skills = getNamedParameterJdbcTemplate().query(sql, args, skillRowMapper);
        return skills;
	}

	@Override
	public List<Skill> findAllSkillsForTopicAndProvider (Long idTopic, Long idProvider) {
        String sql = "SELECT * FROM skill WHERE id_topic = :idTopic AND id_provider in (:idProvider, :idRfProvider)";
        BeanPropertyRowMapper<Skill> skillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTopic", idTopic);
        args.put("idProvider", idProvider);
        args.put("idRfProvider", Level.RFPROVIDER);
        List<Skill> skills = getNamedParameterJdbcTemplate().query(sql, args, skillRowMapper);
        return skills;
	}

	@Override
	public List<Skill> findAllSkills() {
        String sql = "SELECT * FROM skill ORDER BY id_skill ASC";
        BeanPropertyRowMapper<Skill> skillRowMapper = BeanPropertyRowMapper.newInstance(Skill.class);
        List<Skill> skills = getNamedParameterJdbcTemplate().query(sql, skillRowMapper);
        return skills;
	}

	@Override
	public int countAll() {
		// return 0
		return 0;
	}

	// convenience method to grab skills associated with a topic
	private List<Section> findSectionsForSkill(Long idSkill) {
		String sql = "SELECT * FROM section WHERE id_skill = :idSkill";
		BeanPropertyRowMapper<Section> sectionRowMapper = BeanPropertyRowMapper.newInstance(Section.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idSkill", idSkill);
		List<Section> sections = getNamedParameterJdbcTemplate()
				.query(sql, args, sectionRowMapper);
		return sections;
	}
	
	

}
