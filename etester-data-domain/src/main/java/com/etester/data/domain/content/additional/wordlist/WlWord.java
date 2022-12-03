package com.etester.data.domain.content.additional.wordlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name="wl_word")
public class WlWord {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_word")
	private Long idWord;
	
	@NotNull
    @Column(name = "id_wordlist")
	private Long idWordlist;
	
	@NotNull
    @Size(min=1, max=100)
	@Column(name = "word")
	private String word;

    @Size(min=0, max=1000)
	@Column(name = "definition")
	private String definition;

    @Size(min=0, max=500)
	@Column(name = "pronunciation")
	private String pronunciation;

	private String syllables;

	private String pos;

	private String audioFileUrl;

    @Size(min=0, max=400)
	@Column(name = "synonym")
	private String synonym;

    @Size(min=0, max=200)
	@Column(name = "antonym")
	private String antonym;

    @Size(min=0, max=200)
	@Column(name = "thesaurus")
	private String thesaurus;

	@Column(name = "sampletext")
	private String sampletext;

}
