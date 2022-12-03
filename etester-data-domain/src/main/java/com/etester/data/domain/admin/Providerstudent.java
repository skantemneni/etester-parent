package com.etester.data.domain.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="providerstudent")
public class Providerstudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_providerstudent")
    private Long idProviderstudent;
    @Column(name = "provider_username", length = 50, nullable = false)
    private String providerUsername;
    @Column(name = "student_username", length = 50, nullable = false)
    private String studentUsername;

	public Providerstudent() {
    }

    public Providerstudent(String providerUsername, String studentUsername) {
    	this(null, providerUsername, studentUsername);
    }

    public Providerstudent(Long idProviderstudent, String providerUsername, String studentUsername) {
        this.idProviderstudent = idProviderstudent;
        this.providerUsername = providerUsername;
        this.studentUsername = studentUsername;
    }

}
