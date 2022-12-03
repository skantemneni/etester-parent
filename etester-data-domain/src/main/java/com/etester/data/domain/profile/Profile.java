package com.etester.data.domain.profile;

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
@Table(name="profile")
public class Profile {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profile")
    private Long idProfile;
	
	@NotNull
    @Column(name = "id_provider")
	private Long idProvider;

	// providerName corresponding to id_provider
	private String providerName;

	@NotNull
    @Column(name = "id_organization")
	private Long idOrganization;

	// organizationName corresponding to id_organization
	private String organizationName;

	@NotNull
    @Size(min=1, max=100)
	private String name;
    
    @Size(min=0, max=200)
	private String description;

	@Column(name = "access_level")
    private Integer accessLevel;

	@Column(name = "published")
    private Integer published;
	
	@NotNull
	@Column(name = "profile_type")
    private Integer profileType;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_profilesegment")
	private List<Profilesegment> profilesegments;

}
