package dev.voidframework.validation.validator.impl;

import dev.voidframework.validation.validator.TrimmedLength;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * Implementation of the annotation {@link TrimmedLength}.
 */
public class TrimmedLengthImpl implements ConstraintValidator<TrimmedLength, String> {

    private TrimmedLength constraintAnnotation;

    @Override
    public void initialize(final TrimmedLength constraintAnnotation) {

        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }

        final int length = StringUtils.trimToEmpty(value).length();
        return length >= constraintAnnotation.min() && length <= constraintAnnotation.max();
    }
}
