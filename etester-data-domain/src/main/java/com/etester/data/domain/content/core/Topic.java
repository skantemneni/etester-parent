package com.etester.data.domain.content.core;

import java.util.List;

import com.etester.data.domain.test.AdaptiveTest;

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
@Table(name="topic")
public class Topic {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_topic")
	private Long idTopic;
	
	@NotNull
    @Column(name = "id_level")
	private Long idLevel;

	@NotNull
    @Column(name = "id_provider")
	private Long idProvider;

    @Column(name = "subject")
	private String subject;
    
    @NotNull
    @Size(min=1, max=100)
	private String name;

	@Column(name = "display_name")
	private String displayName;
    
    @Size(min=0, max=200)
	private String description;

	@Column(name = "text")
	private String text;

	@Column(name = "addl_info")
	private String addlInfo;

	private String synopsisLink;

	private String synopsisVideoLink;

	@Column(name = "published")
    private Integer published;

	@Column(name = "derived")
    private Integer derived;

	@Column(name = "id_topic_reference")
    private Long idTopicReference;

	@Column(name = "display_order")
    private Integer displayOrder;

	private Long idPracticeSection;

	private String practiceTestsString;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name= "id_skill")
    private List<Skill> skills;

	@Transient 
    private List<AdaptiveTest> practiceSections;

	public Topic() {
	}

	public Topic(Long idTopic, Long idLevel, Long idProvider, String name,
			String description, Integer published) {
		this.idTopic = idTopic;
		this.idLevel = idLevel;
		this.idProvider = idProvider;
		this.name = name;
		this.description = description;
		this.published = published;
	}

}
