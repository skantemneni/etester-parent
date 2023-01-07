package com.etester.security.login.models;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class LoginUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_user")
	private Long idUser;

	@NotBlank
	@Size(max = 50)
	private String username;

    @NotBlank
	@Size(max = 200)
	private String password;

    private boolean enabled;

	@NotBlank
	@Size(max = 45)
	@Column(name = "first_name")
	private String firstName;

	@NotBlank
	@Size(max = 45)
	@Column(name = "last_name")
	private String lastName;

	@Size(max = 45)
	@Column(name = "middle_name")
	private String middleName;

	@NotBlank
	@Size(max = 50)
	@Email
	@Column(name = "email_address")
	private String emailAddress;

    private String fullName;

    @ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "authorities", joinColumns = @JoinColumn(name = "username"), inverseJoinColumns = @JoinColumn(name = "authority"))
	private Set<Role> roles = new HashSet<>();

	public LoginUser() {
	}

	public LoginUser(String username, String firstName, String lastName, String middleName, String emailAddress, String password) {
		this(username, firstName, lastName, middleName, emailAddress, password, new HashSet<Role>());
	}

	public LoginUser(String username, String firstName, String lastName, String middleName, String emailAddress, String password, Set<Role> authorities) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleName = middleName;
		this.emailAddress = emailAddress;
		this.password = password;
		this.roles = authorities;
	}

//
//    @Transient private List<String> permissions;
//
//    @Transient private List<Usertest> tests;
//	
//	@Transient private List<Userprofile> profiles;
//	
//	// private webuser (containing extended user attributes - used in programmatically loading users)
//	@Transient private Webuser webuser;
//	// private channelSubscriptions list of Subscription channels and durations - used in programmatically loading users
//	@Transient private List<ChannelSubscription> channelSubscriptions;
//	// private studentOrganizations corresponding to the organizations the student is associated with - used in programmatically loading users
//	@Transient private List<Long> idOrganizationsList;
//
//	// organizationName corresponding to id_organization
//	private String organizationName;
//    
//	private Long idOrganization;
//	
//	@Transient private List<Channel> channels;
//
//	@Transient private List<Channel> subscriptions;
//
//	
//	
//	/*
//	 * public Long getIdUser() { return idUser; }
//	 * 
//	 * public void setIdUser(Long idUser) { this.idUser = idUser; }
//	 * 
//	 * public String getUsername() { return username; }
//	 * 
//	 * public void setUsername(String username) { this.username = username; }
//	 * 
//	 * public String getEmailAddress() { return emailAddress; }
//	 * 
//	 * public void setEmailAddress(String emailAddress) { this.emailAddress =
//	 * emailAddress; }
//	 * 
//	 * public String getPassword() { return password; }
//	 * 
//	 * public void setPassword(String password) { this.password = password; }
//	 * 
//	 * public Set<Role> getRoles() { return roles; }
//	 * 
//	 * public void setRoles(Set<Role> roles) { this.roles = roles; }
//	 */
}
