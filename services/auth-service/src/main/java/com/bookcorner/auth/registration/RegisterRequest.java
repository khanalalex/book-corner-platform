package com.bookcorner.auth.registration;

import com.bookcorner.auth.web.validation.MaxUtf8Bytes;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        // Length matters more than "must contain a symbol". The 72-byte ceiling is a bcrypt limit.
        @NotBlank
        @Size(min = 10, message = "must be at least 10 characters")
        @MaxUtf8Bytes(72)
        String password,

        @NotBlank
        @Size(max = 120)
        String fullName,

        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "must be 7 to 15 digits, optionally starting with +")
        String phone) {

    /** Never let the password reach a log line by accident. */
    @Override
    public String toString() {
        return "RegisterRequest[email=" + email + ", password=****, fullName=" + fullName + ", phone=" + phone + "]";
    }
}
