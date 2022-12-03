package com.etester.data.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.etester.data.domain.test.JdbcDaoStaticHelper;
import com.etester.data.domain.user.User;

@Repository
public class JdbcUserDetailsRepository extends NamedParameterJdbcDaoSupport implements UserDetailsRepository {

	PasswordEncoder passwordEncoder;

	public JdbcUserDetailsRepository(DataSource dataSource, PasswordEncoder passwordEncoder) {
		super();
		setDataSource(dataSource);
//		this.dataSource = dataSource;
		this.passwordEncoder = passwordEncoder;
	}
	
	
	@Override
	public Optional<User> findUserDetailsByUsername(String username) {
		// call the JdbcDaoStaticHelper.findUserByUsername with a Details=true flag
		User userDetails = JdbcDaoStaticHelper.findUserByUsername(getNamedParameterJdbcTemplate(), username, true);
		return Optional.ofNullable(userDetails);
	}

	@Override
	public Boolean existsByUsername(String username) {
		String sql = existsByUsernameSQL;
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("username", username);
		int existsCount = getNamedParameterJdbcTemplate().queryForObject(sql, args, Integer.class);
		if (existsCount > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Boolean existsByEmailAddress(String emailAddress) {
		String sql = existsByEmailAddressSQL;
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("emailAddress", emailAddress);
		int existsCount = getNamedParameterJdbcTemplate().queryForObject(sql, args, Integer.class);
		if (existsCount > 0) {
			return true;
		} else {
			return false;
		}
	}
}
