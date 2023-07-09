package me.dhvakr.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgotPasswordModel {

    @Email
    String validateEmail;

    @NotNull @Length(min = 4, max = 10)
    String forgotPasswordKeyValidation;

    @NotNull @Length(min = 4, max = 18)
    String newPassword;
}
