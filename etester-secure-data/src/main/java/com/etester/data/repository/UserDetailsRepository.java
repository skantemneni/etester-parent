package com.etester.data.repository;

import java.util.Optional;

import com.etester.data.domain.user.User;

public interface UserDetailsRepository {

	public static String findByUsernameForLoginSQL = "SELECT * FROM user u WHERE u.username = :username ";
	public static String findRolesByUsernameForLoginSQL = "SELECT * FROM authorities a WHERE a.username = :username ";
	public static String existsByUsernameSQL = "SELECT count(*) FROM user u WHERE u.username = :username ";
	public static String existsByEmailAddressSQL = "SELECT count(*) FROM user u WHERE u.email_address = :emailAddress ";
	
	// update
	public static String updateUserMetadataSQL = "UPDATE user SET first_name = :firstName, last_name = :lastName, email_address = :emailAddress, middle_name = :middleName WHERE username = :username";

	Optional<User> findUserDetailsByUsername(String username);

//	Optional<User> findUserDetailsByEmailAddress(String emailAddress);

	Boolean existsByUsername(String username);

	Boolean existsByEmailAddress(String emailAddress);


	
//    public user findbyusername(string username);
    



}
