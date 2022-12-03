package com.etester.data.domain.content.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import com.etester.data.domain.test.JdbcDaoStaticHelper;

public class JdbcQuestionDao extends NamedParameterJdbcDaoSupport implements QuestionDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	@Override
	// NOT BEING USED.  DELETE to avoid confusion
	public Question findByQuestionId(Long idQuestion) {
        String sql = "SELECT * FROM question WHERE id_question = :idQuestion";
        BeanPropertyRowMapper<Question> questionRowMapper = BeanPropertyRowMapper.newInstance(Question.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idQuestion", idQuestion);
        Question question = getNamedParameterJdbcTemplate().queryForObject(sql, args, questionRowMapper);
        // Try and see if this works
        // get the answers also...
        String answerSql = SectionDao.findAnswersForQuestionSQL;
        BeanPropertyRowMapper<Answer> answerRowMapper = BeanPropertyRowMapper.newInstance(Answer.class);
        args = new HashMap<String, Object>();
        args.put("idQuestion", idQuestion);
        List<Answer> answers = getNamedParameterJdbcTemplate().query(answerSql, args, answerRowMapper);
        question.setAnswers(JdbcDaoStaticHelper.parseAnswerCompareAddlInfoOnAnswers(answers));
        return question;
	}

//	@Override
	// NOT BEING USED.  DELETE to avoid confusion
	public List<Question> findQuestionsForSection(Long idSection) {
        String sql = "SELECT * FROM question WHERE id_section = :idSection";
        BeanPropertyRowMapper<Question> questionRowMapper = BeanPropertyRowMapper.newInstance(Question.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idSection", idSection);
        List<Question> questions = getNamedParameterJdbcTemplate().query(sql, args, questionRowMapper);
        // Try and see if this works
        // get the answers also...
        String answerSql = SectionDao.findAnswersForQuestionSQL;
        BeanPropertyRowMapper<Answer> answerRowMapper = null;
        for (int i = 0; i < questions.size(); i++) {
        	answerRowMapper = BeanPropertyRowMapper.newInstance(Answer.class);
            args = new HashMap<String, Object>();
            args.put("idQuestion", questions.get(i).getIdQuestion());
            List<Answer> answers = getNamedParameterJdbcTemplate().query(answerSql, args, answerRowMapper);
            questions.get(i).setAnswers(JdbcDaoStaticHelper.parseAnswerCompareAddlInfoOnAnswers(answers));
        }
        return questions;
	}

}
