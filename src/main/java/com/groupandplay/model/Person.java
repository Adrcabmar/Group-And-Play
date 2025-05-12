package com.groupandplay.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class Person extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    @NotBlank 
	@Size(min = 1, max = 16)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank 
	@Size(min = 1, max = 16)
    private String lastName;

    @Column(name = "username", nullable = false, unique = true)
    @NotBlank
	@Size(min = 1, max = 16)
    private String username;

	@Column(name = "email", nullable = false, unique = true)
    @NotBlank
	@Email
    private String email;

    @Size(max = 256)
    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "password", nullable = false)
    @NotBlank
    private String password;

}
