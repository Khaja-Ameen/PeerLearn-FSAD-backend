package com.peer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordPolicyValidator implements ConstraintValidator<PasswordPolicy, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }

        boolean hasMinLength = value.length() >= 8;
        boolean hasUppercase = value.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = value.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = value.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = value.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:'\",.<>/?`~".indexOf(ch) >= 0);

        return hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }
}
