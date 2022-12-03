package com.etester.data.domain.user;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="webuser")
public class Webuser {
	@Id
    @Column(name = "username", length = 50, nullable = false)
    private String username;
    private String password;
    private Boolean enabled;
    @Column(name = "first_name", length = 30, nullable = false)
    private String firstName;
    @Column(name = "last_name", length = 30, nullable = false)
    private String lastName;
    @Column(name = "middle_name", length = 30, nullable = false)
    private String middleName;
	private String emailAddress;
    @Column(name = "gender", length = 1)
	private String gender;
    @Column(name = "from_channel", length = 50, nullable = false)
	private String fromChannel;
    @Column(name = "profession", length = 50, nullable = false)
	private String profession;
    @Column(name = "institution", length = 100)
	private String institution;
    @Column(name = "branch_year", length = 40)
	private String branchYear;
    @Column(name = "address_line1", length = 100)
	private String addressLine1;
    @Column(name = "address_line2", length = 100)
	private String addressLine2;
    @Column(name = "country", length = 40)
	private String country;
    @Column(name = "phone_number", length = 10)
	private String phoneNumber;
    @Column(name = "dob")
	private Date dob;
    
    // This to mitigate against issues raising from my "User" class extending from "org.springframework.security.core.userdetails.User"
    // and how I am piggy backing on this class to query for a Database "User" row in JdbcStaticDaoHelper 
	// organizationName corresponding to id_organization
    private Long idUser;
	private String organizationName;
	private Long idOrganization;
}
