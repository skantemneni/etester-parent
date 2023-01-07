package com.etester.security.login.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
public class Role {
  @Id	
  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private ERole authority;

  public Role() {

  }

  public Role(ERole authority) {
    this.authority = authority;
  }

  public ERole getAuthority() {
    return authority;
  }

  public void setAuthority(ERole authority) {
    this.authority = authority;
  }
}