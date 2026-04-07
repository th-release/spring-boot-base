package com.threlease.base.common.validation;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.threlease.base.common.annotation.ValidPhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private String defaultRegion;

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.defaultRegion = constraintAnnotation.region();
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(phoneNumber, defaultRegion);
            return phoneUtil.isValidNumber(parsedNumber);
        } catch (com.google.i18n.phonenumbers.NumberParseException e) {
            return false;
        }
    }
}
