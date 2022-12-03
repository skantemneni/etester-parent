package com.etester.data.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Data;


@Data
//@Entity
@Table(name="permissions")
public class Permission {

    @Column(name = "username", length = 50, nullable = false)
    private String username;
    @Column(name = "privilege", length = 50, nullable = false)
    private String privilege;

}
