package com.etester.data.domain.user;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="usergroup")
public class Usergroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usergroup")
    private Long idUsergroup;
	@Column(name = "provider_username", length = 50, nullable = false)
	private String providerUsername;
	@Size(min = 0, max = 100)
	private String name;
	@Size(min = 0, max = 200)
	private String description;

	@Transient
	private List<User> users;

	public Usergroup() {
    }

	public Usergroup(String providerUsername, String name, List<User> users) {
		this.providerUsername = providerUsername;
		this.name = name;
		this.users = users;
    }

	/**
	 * @return the idUsergroup
	 */
	public Long getIdUsergroup() {
		return idUsergroup;
	}

	/**
	 * @param idUsergroup the idUsergroup to set
	 */
	public void setIdUsergroup(Long idUsergroup) {
		this.idUsergroup = idUsergroup;
	}

	/**
	 * @return the providerUsername
	 */
	public String getProviderUsername() {
		return providerUsername;
	}

	/**
	 * @param providerUsername the providerUsername to set
	 */
	public void setProviderUsername(String providerUsername) {
		this.providerUsername = providerUsername;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the users
	 */
	public List<User> getUsers() {
		return users;
	}

	/**
	 * @param users the users to set
	 */
	public void setUsers(List<User> users) {
		this.users = users;
	}

}
