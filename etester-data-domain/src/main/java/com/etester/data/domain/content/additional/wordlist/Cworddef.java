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
@Table(name="cworddef")
public class Cworddef {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cworddef")
	private Long idCworddef;
	
	@NotNull
    @Column(name = "id_cword")
	private Long idCword;
	
    @Size(min=0, max=5)
	@Column(name = "pos")
	private String pos;

    @Size(min=0, max=1000)
	@Column(name = "definition")
	private String definition;

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
