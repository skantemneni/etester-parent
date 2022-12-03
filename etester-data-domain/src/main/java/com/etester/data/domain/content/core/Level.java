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
@Table(name="level")
public class Level {

	public static Long RFPROVIDER = new Long (0);
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_level")
	private Long idLevel;
	
	@NotNull
    @Column(name = "id_system")
	private Long idSystem;

	@NotNull
    @Column(name = "id_provider")
	private Long idProvider;

	private String gradekey;
    
	@NotNull
    @Column(name = "subject")
    @Size(min=1, max=100)
	private String subject;
    
    private String subjectDescription;
    
	@Column(name = "subject_header")
	private String subjectHeader;

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

	@Column(name = "published")
    private Integer published;

	@Column(name = "derived")
    private Integer derived;

	@Column(name = "id_level_reference")
    private Long idLevelReference;

	@NotNull
    @Column(name = "topiccount")
	private Long topicCount;

	@NotNull
    @Column(name = "skillcount")
	private Long skillCount;

	@Column(name = "display_order")
    private Integer displayOrder;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name= "id_level")
    private List<Topic> topics;

	@Transient 
    private List<AdaptiveTest> practiceSections;

    public Level() {
	}

	public Level(Long idLevel, Long idSystem, Long idProvider, String name,
			String description) {
		this.idLevel = idLevel;
		this.idSystem = idSystem;
		this.idProvider = idProvider;
		this.name = name;
		this.description = description;
	}

}
