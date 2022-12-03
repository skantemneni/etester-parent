package com.etester.security.login.repository;

import java.util.Optional;

import com.etester.security.login.models.LoginUser;

public interface UserRepository {

	public static String findByUsernameForLoginSQL = "SELECT * FROM user u WHERE u.username = :username ";
	public static String findRolesByUsernameForLoginSQL = "SELECT * FROM authorities a WHERE a.username = :username ";
	public static String existsByUsernameSQL = "SELECT count(*) FROM user u WHERE u.username = :username ";
	public static String existsByEmailAddressSQL = "SELECT count(*) FROM user u WHERE u.email_address = :emailAddress ";
	
	// USER SQL
	// new
	public static String insertNewUserSQL = "INSERT INTO user (username, password, enabled, email_address, first_name, last_name, middle_name) "
			+ " VALUES (:username, :password, :enabled, :emailAddress, :firstName, :lastName, :middleName)";
	
	public static String insertAuthoritiesSql = "INSERT INTO authorities (username, authority) VALUES (:username, :authority)";

	// update
	public static String updateUserMetadataSQL = "UPDATE user SET first_name = :firstName, last_name = :lastName, email_address = :emailAddress, middle_name = :middleName WHERE username = :username";

	Optional<LoginUser> findByUsername(String username);

	Boolean existsByUsername(String username);

	Boolean existsByEmailAddress(String emailAddress);

	public Optional<LoginUser> save(LoginUser user);
}
