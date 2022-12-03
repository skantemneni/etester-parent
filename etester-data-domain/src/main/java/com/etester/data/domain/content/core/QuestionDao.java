package com.etester.data.domain.content.core;


public interface QuestionDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	public static String insertQuestionSQL = "INSERT INTO question (id_question, id_section, name, description, text, addl_info, text_precontext, text_postcontext, banner, heading, instructions, question_type, multiple_answers, all_answers, id_questionset, reference_skills, id_reference_topic, id_reference_level) "
			+ " VALUES (:idQuestion, :idSection, :name, :description, :text, :addlInfo, :textPrecontext, :textPostcontext, :banner, :heading, :instructions, :questionType, :multipleAnswers, :allAnswers, :idQuestionset, :referenceSkills, :idReferenceTopic, :idReferenceLevel)";

	public static final String upsertQuestionSQL = "INSERT INTO question (id_question, id_section, name, description, text, addl_info, text_precontext, text_postcontext, banner, heading, instructions, question_type, multiple_answers, all_answers, id_questionset, reference_skills, id_reference_topic, id_reference_level) "
			+ " VALUES (:idQuestion, :idSection, :name, :description, :text, :addlInfo, :textPrecontext, :textPostcontext, :banner, :heading, :instructions, :questionType, :multipleAnswers, :allAnswers, :idQuestionset, :referenceSkills, :idReferenceTopic, :idReferenceLevel) "
			+ " ON DUPLICATE KEY UPDATE name = :name, description = :description, text = :text, addl_info = :addlInfo, text_precontext = :textPrecontext, text_postcontext = :textPostcontext, banner = :banner, heading = :heading, instructions = :instructions, question_type = :questionType, "
			+ " multiple_answers = :multipleAnswers, all_answers = :allAnswers, id_questionset = :idQuestionset, reference_skills = :referenceSkills, id_reference_topic = :idReferenceTopic, id_reference_level = :idReferenceLevel ";

	public static String insertAnswerSQL = "INSERT INTO answer (id_answer, id_question, seq, correct, text, addl_info, answer_compare_type, answer_compare_addl_info) "
			+ " VALUES (:idAnswer, :idQuestion, :seq, :correct, :text, :addlInfo, :answerCompareType, :answerCompareAddlInfo)";

//	public static String upsertAnswerSQL = "INSERT INTO answer (id_answer, id_question, seq, correct, text, addl_info, answer_compare_type, answer_compare_addl_info) "
//			+ " VALUES (:idAnswer, :idQuestion, :seq, :correct, :text, :addlInfo, :answerCompareType, :answerCompareAddlInfo) "
//			+ " ON DUPLICATE KEY UPDATE seq = :seq, correct = :correct, text = :text, addl_info = :addlInfo, answer_compare_type = :answerCompareType, answer_compare_addl_info = :answerCompareAddlInfo ";

	public Question findByQuestionId(Long idQuestion);
//
//	public List<Question> findQuestionsForSection (Long idSection);

}
