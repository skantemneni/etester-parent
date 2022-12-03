package com.etester.data.domain.content.additional.wordlist;

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

@Repository
public class JdbcCwordDao extends JdbcDataDaoParent implements CwordDao {

	public JdbcCwordDao(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public List<Cword> getWordsByClistId(Long levelId, Long clistId) {
		Long minCwordId = clistId * 100;
		Long maxCwordId = minCwordId + 100;
        String cword_sql = "SELECT * FROM cword WHERE id_cword > :minCwordId and id_cword <= :maxCwordId and id_level = :levelId";
        BeanPropertyRowMapper<Cword> cwordRowMapper = BeanPropertyRowMapper.newInstance(Cword.class);
        Map<String, Object> cword_args = new HashMap<String, Object>();
        cword_args.put("minCwordId", minCwordId);
        cword_args.put("maxCwordId", maxCwordId);
        cword_args.put("levelId", levelId);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        List<Cword> cwords = getNamedParameterJdbcTemplate().query(cword_sql, cword_args, cwordRowMapper);
        if (cwords != null && cwords.size() > 0) {
        	for (Cword cword : cwords) {
        		// set definitions
        		String cworddef_sql = "SELECT * FROM cworddef WHERE id_cword = :idCword";
                BeanPropertyRowMapper<Cworddef> cworddefRowMapper = BeanPropertyRowMapper.newInstance(Cworddef.class);
                Map<String, Object> cworddef_args = new HashMap<String, Object>();
                cworddef_args.put("idCword", cword.getIdCword());
                List<Cworddef> cworddefs = getNamedParameterJdbcTemplate().query(cworddef_sql, cworddef_args, cworddefRowMapper);
                cword.setWordDefinitions(cworddefs);
        		// set usages
        		String cwordusage_sql = "SELECT * FROM cwordusage WHERE id_cword = :idCword";
                BeanPropertyRowMapper<Cwordusage> cwordusageRowMapper = BeanPropertyRowMapper.newInstance(Cwordusage.class);
                Map<String, Object> cwordusage_args = new HashMap<String, Object>();
                cwordusage_args.put("idCword", cword.getIdCword());
                List<Cwordusage> cwordusages = getNamedParameterJdbcTemplate().query(cwordusage_sql, cwordusage_args, cwordusageRowMapper);
                cword.setWordUsages(cwordusages);
        	}
        }
        return cwords;
	}

	@Override
	public void updateBatch(List<Cword> cwords) {
		// check to make sure we have some Levels to work with
		if (cwords == null || cwords.size() == 0) {
			// nothing to do here.  simply return
			return;
		}
		for (Cword cword : cwords) {
	        // update the main word
			SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(cword);
	        getNamedParameterJdbcTemplate().update(CwordDao.updateCwordSQL, parameterSource);
			// update definitions and usages (delete and reload)
	        // first delete
	        Map<String, Object> args = new HashMap<String, Object>();
	        args.put("idCword", cword.getIdCword());
	        String cworddef_delete_sql = "DELETE FROM cworddef WHERE id_cword = :idCword";
	        getNamedParameterJdbcTemplate().update(cworddef_delete_sql, args);
	        String cwordusage_delete_sql = "DELETE FROM cwordusage WHERE id_cword = :idCword";
	        getNamedParameterJdbcTemplate().update(cwordusage_delete_sql, args);
	        // now reload
	        if (cword.getWordDefinitions() != null && cword.getWordDefinitions().size() > 0) {
	            List<SqlParameterSource> cworddefParameters = new ArrayList<SqlParameterSource>();
	        	for (Cworddef cworddef : cword.getWordDefinitions()) {
	        		cworddefParameters.add(new BeanPropertySqlParameterSource(cworddef));
	        	}
	        	getNamedParameterJdbcTemplate().batchUpdate(CwordDao.insertCworddefSQL, cworddefParameters.toArray(new SqlParameterSource[0]));
	        }
	        if (cword.getWordUsages() != null && cword.getWordUsages().size() > 0) {
	            List<SqlParameterSource> cwordusageParameters = new ArrayList<SqlParameterSource>();
	        	for (Cwordusage cwordusage : cword.getWordUsages()) {
	        		cwordusageParameters.add(new BeanPropertySqlParameterSource(cwordusage));
	        	}
	        	getNamedParameterJdbcTemplate().batchUpdate(CwordDao.insertCwordusageSQL, cwordusageParameters.toArray(new SqlParameterSource[0]));
	        }
		}
	}



}
