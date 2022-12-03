package com.etester.data.domain.test;

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
@Table(name="adaptive_test")
public class AdaptiveTest {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adaptive_test")
	private Long idAdaptiveTest;
	
	@NotNull
    @Column(name = "id_level")
	private Long idLevel;

    @Column(name = "id_section")
	private Long idSection;

	@Size(min = 0, max = 100)
	private String name;

	@NotNull
    @Column(name = "id_core_artifact")
	private Long idCoreArtifact;

	@NotNull
    @Column(name = "core_artifact_type")
    @Size(min=1, max=30)
	private String coreArtifactType;

    @Column(name = "test_type")
	private String testType;

    @Column(name = "test_mode")
	private String testMode;

    @Column(name = "test_artifacts_string")
	private String testArtifactsString;

    
    public AdaptiveTest() {
	}

    public AdaptiveTest(Long idAdaptiveTest, Long idLevel, Long idSection, Long idCoreArtifact, String coreArtifactType) {
		this.idAdaptiveTest = idAdaptiveTest;
		this.idLevel = idLevel;
		this.idSection = idSection;
		this.idCoreArtifact = idCoreArtifact;
		this.coreArtifactType = coreArtifactType;
	}

}