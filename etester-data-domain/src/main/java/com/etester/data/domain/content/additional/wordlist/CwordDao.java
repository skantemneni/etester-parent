package com.etester.data.domain.content.additional.wordlist;

import java.util.List;

public interface CwordDao {

	// Beware, tables names are case sensitive in MySQL on Linux.  Set all to lower case
	// Although not necessary, I am also doing the same with all column names.
	public static String updateCwordSQL = "UPDATE cword SET pronunciation = :pronunciation, syllables = :syllables, audio_file_url = :audioFileUrl, video_file_url = :videoFileUrl WHERE id_cword = :idCword and name = :name";

	public static String insertCworddefSQL = "INSERT INTO cworddef (id_cworddef, id_cword, pos, definition, synonym, antonym, thesaurus, sampletext) "
			+ " VALUES (:idCworddef, :idCword, :pos, :definition, :synonym, :antonym, :thesaurus, :sampletext)";

	public static String insertCwordusageSQL = "INSERT INTO cwordusage (id_cwordusage, id_cword, text, source) "
			+ " VALUES (:idCwordusage, :idCword, :text, :source)";


    public List<Cword> getWordsByClistId(Long levelId, Long clistId);

    public void updateBatch(List<Cword> cwords);

}
