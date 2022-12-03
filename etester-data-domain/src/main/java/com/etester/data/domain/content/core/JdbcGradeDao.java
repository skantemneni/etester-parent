package com.etester.data.domain.content.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import com.etester.data.dao.JdbcDataDaoParent;

@Repository
public class JdbcGradeDao extends JdbcDataDaoParent implements GradeDao {

	public JdbcGradeDao(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public Grade findByGradeName(String gradeName) {
        String sql = "SELECT * FROM grade WHERE grade_name = :gradeName";
        BeanPropertyRowMapper<Grade> gradeRowMapper = BeanPropertyRowMapper.newInstance(Grade.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("gradeName", gradeName);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Grade grade = null;
        try {
        	grade = getNamedParameterJdbcTemplate().queryForObject(sql, args, gradeRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return grade;
	}

	@Override
	public List<Grade> findAllGrades() {
        String sql = "SELECT * FROM grade";
        BeanPropertyRowMapper<Grade> gradeRowMapper = BeanPropertyRowMapper.newInstance(Grade.class);
        List<Grade> grades = getNamedParameterJdbcTemplate().query(sql, gradeRowMapper);
        return grades;
	}
}
