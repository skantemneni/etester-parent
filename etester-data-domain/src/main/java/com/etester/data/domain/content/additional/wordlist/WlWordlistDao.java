package com.etester.data.domain.content.additional.wordlist;

import java.util.List;

public interface WlWordlistDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	public static String insertWlWordlistSQL = "INSERT INTO wl_wordlist (id_wordlist, id_skill, id_provider, name, description) "
			+ " VALUES (:idWordlist, :idSkill, :idProvider, :name, :description)";

	public static String insertWlWordSQL = "INSERT INTO wl_word (id_word, id_wordlist, word, definition, pronunciation, synonym, antonym, thesaurus, sampletext) "
			+ " VALUES (:idWord, :idWordlist, :word, :definition, :pronunciation, :synonym, :antonym, :thesaurus, :sampletext)";

	public static String insertWlPassageSQL = "INSERT INTO wl_passage (id_passage, id_wordlist, text) "
			+ " VALUES (:idPassage, :idWordlist, :text)";

//	public static String insertWlQuestionSQL = "INSERT INTO wl_question (id_question, id_section, question_type, text, addl_info) "
//			+ " VALUES (:idQuestion, :idQuestion, :questionType, :text, :addlInfo)";
//
//	public static String insertWlAnswerSQL = "INSERT INTO wl_answer (id_answer, id_question, text) "
//			+ " VALUES (:idAnswer, :idQuestion, :text)";

    public WlWordlist findByWordlistId(Long idWordlist);

    public void insertBatch(List<WlWordlist> wordlists);

    // This deletes the data before reloading the same
    public void insertBatch(List<WlWordlist> wordlists, boolean reload);

    /**
     * Value added function.  This returns a level that is loaded with Topics and Skills. 
     * @param idLevel
     * @return
     */
    public WlWordlist getWordlistWithDetails (Long idWordlist);

}
