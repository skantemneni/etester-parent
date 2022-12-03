package com.etester.data.domain.test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

public class JdbcTestsegmentDao extends NamedParameterJdbcDaoSupport implements TestsegmentDao {

	@Override
	public Testsegment findByTestsegmentId(Long idTestsegment) {
        String sql = "SELECT * FROM testsegment WHERE id_testsegment = :idTestsegment";
        BeanPropertyRowMapper<Testsegment> testsegmentRowMapper = BeanPropertyRowMapper.newInstance(Testsegment.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTestsegment", idTestsegment);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        Testsegment testsegment = null;
        try {
        	testsegment = getNamedParameterJdbcTemplate().queryForObject(sql, args, testsegmentRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        // set testsegments
        testsegment.setTestsections(findTestsectionsForTestsegment(testsegment.getIdTestsegment()));
        return testsegment;
	}

	@Override
	public List<Testsegment> findTestsegmentsForTest(Long idTest) {
        String sql = "SELECT * FROM testsegment WHERE id_test = :idTest";
        BeanPropertyRowMapper<Testsegment> testsegmentRowMapper = BeanPropertyRowMapper.newInstance(Testsegment.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTest", idTest);
        List<Testsegment> testsegments = getNamedParameterJdbcTemplate().query(sql, args, testsegmentRowMapper);
        // get the testsegments associated with all the tests
		if (testsegments != null && testsegments.size() > 0) {
			for (int i = 0; i < testsegments.size(); i++) {
				testsegments.get(i).setTestsections(findTestsectionsForTestsegment(testsegments.get(i).getIdTestsegment()));
			}
		}
        return testsegments;
	}

	@Override
	public List<Testsegment> findUnattachedTestsegmentsForProvider (Long idProvider) {
        String sql = "SELECT * FROM testsegment WHERE id_provider = :idProvider AND id_test is NULL";
        BeanPropertyRowMapper<Testsegment> testsegmentRowMapper = BeanPropertyRowMapper.newInstance(Testsegment.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idProvider", idProvider);
        List<Testsegment> testsegments = getNamedParameterJdbcTemplate().query(sql, testsegmentRowMapper);
        // get the sections associated with all the tests
		if (testsegments != null && testsegments.size() > 0) {
			for (int i = 0; i < testsegments.size(); i++) {
				testsegments.get(i).setTestsections(findTestsectionsForTestsegment(testsegments.get(i).getIdTestsegment()));
			}
		}
        return testsegments;
	}

	@Override
	public void insert(Testsegment testsegment) {
		List<Testsegment> testsegments = new ArrayList<Testsegment>();
		testsegments.add(testsegment);
		JdbcDaoStaticHelper.insertTestsegmentBatch(testsegments, getNamedParameterJdbcTemplate(), true);
	}

	@Override
	public void insertBatch(List<Testsegment> testsegments) {
		JdbcDaoStaticHelper.insertTestsegmentBatch(testsegments, getNamedParameterJdbcTemplate(), true);
	}

	// Work on this method when necessary
	@Override
	public void update(Testsegment testsegment) {
//		List<Testsegment> testsegments = new ArrayList<Testsegment>();
//		testsegments.add(testsegment);
//        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(testsegment);
//        getNamedParameterJdbcTemplate().update(updateTestsegmentSQL, parameterSource);
	}

	@Override
	public void delete(Long idTestsegment) {
		getNamedParameterJdbcTemplate().update("call rulefree.delete_testsegment(:idTestsegment)", new MapSqlParameterSource().
				addValue("idTestsegment", idTestsegment, Types.NUMERIC));
	}

	public List<Testsection> findTestsectionsForTestsegment (Long idTestsegment) {
		String sql = "SELECT * FROM testsection WHERE id_testsegment = :idTestsegment";
		BeanPropertyRowMapper<Testsection> sectionRowMapper = BeanPropertyRowMapper.newInstance(Testsection.class);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("idTestsegment", idTestsegment);
		List<Testsection> testsections = getNamedParameterJdbcTemplate().query(sql, args, sectionRowMapper);
		return testsections;
	}
	
}
