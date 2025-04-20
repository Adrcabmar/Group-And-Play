package com.groupandplay.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {

    private String actualPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String newPassword;
}
