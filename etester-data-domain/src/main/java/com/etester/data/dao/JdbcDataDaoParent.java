package com.etester.data.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import jakarta.annotation.PostConstruct;

public abstract class JdbcDataDaoParent extends NamedParameterJdbcDaoSupport {

	protected DataSource dataSource;

	@PostConstruct
	protected void initialize() {
		setDataSource(dataSource);
	}

	public JdbcDataDaoParent(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

}
