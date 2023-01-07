package com.etester.security.login.payload.request;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
 
@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;
 
    @NotBlank
    @Size(max = 45)
    private String firstName;

    @NotBlank
    @Size(max = 45)
    private String lastName;

    @Size(max = 45)
    private String middleName;

    @NotBlank
    @Size(max = 50)
    @Email
    private String emailAddress;
    
    private Set<String> role;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
  
}
