package com.etester.data.domain.content.additional.wordlist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import com.etester.data.dao.JdbcDataDaoParent;
import com.etester.data.domain.content.core.Answer;
import com.etester.data.domain.content.core.Question;
import com.etester.data.domain.content.core.SectionDao;
import com.etester.data.domain.test.JdbcDaoStaticHelper;

@Repository
public class JdbcWlWordlistDao extends JdbcDataDaoParent implements WlWordlistDao {

	public JdbcWlWordlistDao(DataSource dataSource) {
		super(dataSource);
	}

	// This so I do not have to repeat 100 database queries each time there is a call for 
	// loaded level
	private static Map<Long, WlWordlist> WORDLIST_WITH_DETAILS_CACHE = new HashMap<Long, WlWordlist>();
	
	@Override
	public WlWordlist findByWordlistId(Long idWordlist) {
        String sql = "SELECT * FROM wl_wordlist WHERE id_wordlist = :idWordlist";
        BeanPropertyRowMapper<WlWordlist> wordlistRowMapper = BeanPropertyRowMapper.newInstance(WlWordlist.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idWordlist", idWordlist);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        WlWordlist wlWordlist = null;
        try {
        	wlWordlist = getNamedParameterJdbcTemplate().queryForObject(sql, args, wordlistRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return wlWordlist;
	}

	@Override
	public void insertBatch(List<WlWordlist> wordlists, boolean reload) {
		JdbcDaoStaticHelper.insertWordlistBatch(wordlists, getNamedParameterJdbcTemplate(), reload);
	}

	@Override
	public void insertBatch(List<WlWordlist> wordlists) {
		insertBatch(wordlists, false);
	}


	@Override
	public WlWordlist getWordlistWithDetails(Long idWordlist) {
		// check to see if the information is available in the cache
		if (WORDLIST_WITH_DETAILS_CACHE.containsKey(idWordlist)) {
			return WORDLIST_WITH_DETAILS_CACHE.get(idWordlist);
		} else {
			WlWordlist wordlist = findByWordlistId(idWordlist);
			if (wordlist == null) {
				return null;
			}
			wordlist.setWords(findWordsForWordlist (idWordlist));
			wordlist.setPassages(findPassagesForWordlist (idWordlist));
			wordlist.setQuestions(findQuestionsForWordlist (idWordlist));
			
			WORDLIST_WITH_DETAILS_CACHE.put(idWordlist, wordlist);
			return wordlist;
		}
	}
	
	private List<WlWord> findWordsForWordlist (Long idWordlist) {
        String sql = "SELECT * FROM wl_word WHERE id_wordlist = :idWordlist ORDER BY id_word";
        BeanPropertyRowMapper<WlWord> wordRowMapper = BeanPropertyRowMapper.newInstance(WlWord.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idWordlist", idWordlist);
        List<WlWord> words = getNamedParameterJdbcTemplate().query(sql, args, wordRowMapper);
        return words;
	}

	private List<WlPassage> findPassagesForWordlist (Long idWordlist) {
        String sql = "SELECT * FROM wl_passage WHERE id_wordlist = :idWordlist ORDER BY id_passage";
        BeanPropertyRowMapper<WlPassage> passageRowMapper = BeanPropertyRowMapper.newInstance(WlPassage.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idWordlist", idWordlist);
        List<WlPassage> passages = getNamedParameterJdbcTemplate().query(sql, args, passageRowMapper);
        return passages;
	}

	private List<Question> findQuestionsForWordlist (Long idWordlist) {
        String sql = "SELECT * FROM question WHERE id_section = :idWordlist ORDER BY id_question";
        BeanPropertyRowMapper<Question> questionRowMapper = BeanPropertyRowMapper.newInstance(Question.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idWordlist", idWordlist);
        List<Question> questions = getNamedParameterJdbcTemplate().query(sql, args, questionRowMapper);
        if (questions != null && questions.size() > 0) {
        	for (Question question : questions) {
        		question.setAnswers(findAnswersForWordlistQuestion (question.getIdQuestion()));
        	}
        }
        return questions;
	}

	private List<Answer> findAnswersForWordlistQuestion (Long idQuestion) {
//        String sql = "SELECT * FROM answer WHERE id_question = :idQuestion ORDER BY id_answer";
        BeanPropertyRowMapper<Answer> answerRowMapper = BeanPropertyRowMapper.newInstance(Answer.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idQuestion", idQuestion);
        List<Answer> answers = getNamedParameterJdbcTemplate().query(SectionDao.findAnswersForQuestionSQL, args, answerRowMapper);
        return JdbcDaoStaticHelper.parseAnswerCompareAddlInfoOnAnswers(answers);
	}



}
