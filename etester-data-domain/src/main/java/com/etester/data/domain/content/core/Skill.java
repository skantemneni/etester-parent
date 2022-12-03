package com.etester.data.domain.content.core;

import java.util.List;

import com.etester.data.domain.test.AdaptiveTest;
import com.etester.data.domain.user.Gradeskill;

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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name="skill")
public class Skill {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_skill")
	private Long idSkill;
	
    @Column(name = "id_topic")
	private Long idTopic;
	
	@NotNull
    @Column(name = "id_provider")
	private Long idProvider;

    @Column(name = "subject")
	private String subject;
    
    @NotNull
    @Size(min=1, max=200)
	private String name;

	@Column(name = "display_name")
	private String displayName;
    
    @Size(min=0, max=500)
	private String description;

	@Column(name = "text")
	private String text;

	@Column(name = "addl_info")
	private String addlInfo;

	private String synopsisLink;

	private String synopsisVideoLink;

	@Column(name = "published")
    private Integer published;

	@Column(name = "derived_skill")
    private Integer derivedSkill;

	@Column(name = "id_skill_reference")
    private Long idSkillReference;

	private Long idPracticeSection;

	private String practiceTestsString;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name= "id_section")
    private List<Section> sections;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name= "id_skill")
    private List<Gradeskill> gradeskills;

    @Transient 
    private List<AdaptiveTest> practiceSections;

	public Skill() {
	}

	public Skill(Long idSkill, Long idTopic, Long idProvider, String name,
			String description, Integer published) {
		this.idSkill = idSkill;
		this.idTopic = idTopic;
		this.idProvider = idProvider;
		this.name = name;
		this.description = description;
		this.published = published;
	}

}
