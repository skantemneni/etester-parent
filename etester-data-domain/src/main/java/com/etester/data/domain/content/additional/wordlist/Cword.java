package com.etester.data.domain.content.additional.wordlist;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name="cword")
public class Cword {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cword")
	private Long idCword;
	
    @Column(name = "id_system")
	private Long idSystem;

	@NotNull
    @Column(name = "id_provider")
	private Long idProvider;

	@NotNull
    @Column(name = "id_level")
	private Long idLevel;
	
    @NotNull
    @Size(min=1, max=100)
	@Column(name = "name")
	private String name;

    @Size(min=0, max=500)
	@Column(name = "pronunciation")
	private String pronunciation;

    @Size(min=0, max=100)
	@Column(name = "syllables")
	private String syllables;

	@Column(name = "published")
    private Integer published;

    @Size(min=0, max=100)
	@Column(name = "themes")
	private String themes;

    @Size(min=0, max=5)
	@Column(name = "rank")
	private String rank;

	@Column(name = "syllable_count")
    private Integer syllableCount;

    @Size(min=0, max=100)
	@Column(name = "source")
	private String source;

    @Size(min=0, max=200)
	@Column(name = "audio_file_url")
	private String audioFileUrl;

    @Size(min=0, max=200)
	@Column(name = "video_file_url")
	private String videoFileUrl;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name= "id_cword")
    private List<Cworddef> wordDefinitions;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name= "id_cword")
    private List<Cwordusage> wordUsages;

}
