package com.creatorcorner.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    @JsonProperty("userId")
    UUID userId;

    @NotBlank
    @NotNull
    @JsonProperty("firstName")
    String firstName;

    @NotBlank
    @NotNull
    @JsonProperty("lastName")
    String lastName;

    @NotBlank
    @NotNull
    @Email
    @JsonProperty("email")
    String email;

    /*
        Password Requirements:
        - At least 8 characters long
        - At least 1 uppercase letter
        - At least 1 lowercase letter
        - At least 1 special character
        - No whitespace
     */
    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?_\\-&])[A-Za-z\\d@$!%*_\\-?&]{8,}$")
    @JsonProperty("password")
    String password;

    @JsonProperty("createdTsEpoch")
    LocalDateTime createdTsEpoch;

    @JsonProperty("updatedTsEpoch")
    LocalDateTime updatedTsEpoch;

    public String creationLogString() {
        return String.format("[firstName: %s, lastName: %s, email: %s]", firstName, lastName, email);
    }

    public String persistedLogString() {
        return String.format(
                "[userId: %s, firstName: %s, lastName: %s, email: %s, createdTsEpoch: %s, updatedTsEpoch: %s]",
                userId,
                firstName,
                lastName,
                email,
                createdTsEpoch,
                updatedTsEpoch
        );
    }
}
