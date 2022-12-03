package com.etester.data.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="usergroupmember")
public class Usergroupmember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usergroupmember")
    private Long idUsergroupmember;
	@NotNull
	@Column(name = "id_usergroup")
	private Long idUsergroup;
	@Column(name = "username", length = 50, nullable = false)
	private String username;

	public Usergroupmember() {
    }

	public Usergroupmember(Long idUsergroup, String username) {
		this.idUsergroup = idUsergroup;
		this.username = username;
    }

	/**
	 * @return the idUsergroupmember
	 */
	public Long getIdUsergroupmember() {
		return idUsergroupmember;
	}

	/**
	 * @param idUsergroupmember the idUsergroupmember to set
	 */
	public void setIdUsergroupmember(Long idUsergroupmember) {
		this.idUsergroupmember = idUsergroupmember;
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
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

}
