package com.credx.dispatchhub.dto.request;

import com.credx.dispatchhub.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        // NOTE: intended to accept E.164-ish numbers (e.g. +14155552671), but this
        // pattern requires the leading '+' and rejects locally-formatted numbers
        // that testers commonly type in, e.g. "4155552671" or "(415) 555-2671".
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+[1-9]\\d{9,14}$", message = "Phone number must be a valid international number")
        String phoneNumber,

        @NotNull(message = "Role is required")
        UserRole role
) {
}
