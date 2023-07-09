package me.dhvakr.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

/**
 * Using Record bean to build the form and Uses (JSR-303) annotations for automatic validation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SignUpFormModel {

    @Email
    String email;

    @NotNull @Length(min = 4, max = 10)
    String forgotPasswordKey;

    @NotNull @Length(min = 4, max = 18)
    String password;
}
