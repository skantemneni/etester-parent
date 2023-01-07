package com.etester.security.login.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etester.security.login.models.ERole;
import com.etester.security.login.models.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
  Optional<Role> findByAuthority(ERole authority);
}