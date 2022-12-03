package com.etester.data.domain.user;

import org.springframework.security.core.GrantedAuthority;

import lombok.Data;

@Data
public class GrantedAuthorityImpl implements GrantedAuthority {
	private static final long serialVersionUID = 1L;
	private String authority;
	public GrantedAuthorityImpl() {
		super();
	}
	public GrantedAuthorityImpl(String authority) {
		super();
		this.authority = authority;
	}
}
