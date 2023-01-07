package com.etester.security.login.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.etester.security.login.models.Role;
import com.etester.security.login.models.LoginUser;

@Repository
public class JdbcUserRepository extends NamedParameterJdbcDaoSupport implements UserRepository {

	PasswordEncoder passwordEncoder;

	public JdbcUserRepository(DataSource dataSource, PasswordEncoder passwordEncoder) {
		super();
		setDataSource(dataSource);
//		this.dataSource = dataSource;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Optional<LoginUser> findByUsername(String username) {
		String sqlUser = findByUsernameForLoginSQL;
        BeanPropertyRowMapper<LoginUser> userRowMapper = BeanPropertyRowMapper.newInstance(LoginUser.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("username", username);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        LoginUser user = null;
        try {
        	user = getNamedParameterJdbcTemplate().queryForObject(sqlUser, args, userRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        if (user != null) {
    		String sqlRoles = findRolesByUsernameForLoginSQL;
            BeanPropertyRowMapper<Role> roleRowMapper = BeanPropertyRowMapper.newInstance(Role.class);
    		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
            List<Role> rolesList = getNamedParameterJdbcTemplate().query(sqlRoles, args, roleRowMapper);
            if (rolesList != null && rolesList.size() > 0) {
            	user.setRoles(Set.copyOf(rolesList));
            }
        }
        return Optional.ofNullable(user);
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

	@Override
	public Optional<LoginUser> save(LoginUser user) {
		// Add the user
		SqlParameterSource userParameterSource = new BeanPropertySqlParameterSource(user);
        try {
        	getNamedParameterJdbcTemplate().update(insertNewUserSQL, userParameterSource);
        } catch (Exception e) {
        	throw e;
//        	return SaveResponse.responseValue(SaveResponse.SAVE_FAIL_UNKNOWN_USER);
        }
        // Add roles
        if (user.getRoles() != null && user.getRoles().size() > 0) {
        	user.getRoles().forEach(roleName -> {
        		Map<String, String> authorityAsMap = Map.of("username",user.getUsername(), "authority", roleName.getAuthority().toString());
            	getNamedParameterJdbcTemplate().update(insertAuthoritiesSql, authorityAsMap);
        	});
        }
        return findByUsername(user.getUsername());
	}
	
}
