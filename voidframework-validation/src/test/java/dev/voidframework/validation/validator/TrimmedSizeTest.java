package dev.voidframework.validation.validator;

import dev.voidframework.validation.Validated;
import dev.voidframework.validation.ValidationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Locale;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName.class)
public final class TrimmedSizeTest {

    @Test
    public void withError() {
        final Pojo pojo = new Pojo("                                 ");
        final ValidationService validationService = new ValidationService();
        final Validated<Pojo> pojoValidated = validationService.validate(pojo, Locale.ENGLISH);

        Assertions.assertNotNull(pojoValidated);
        Assertions.assertTrue(pojoValidated.hasError());
        Assertions.assertFalse(pojoValidated.isValid());

        Assertions.assertEquals(pojo, pojoValidated.getInstance());
    }

    @Test
    public void withoutError() {
        final Pojo pojo = new Pojo("           abc@local             ");
        final ValidationService validationService = new ValidationService();
        final Validated<Pojo> pojoValidated = validationService.validate(pojo, Locale.ENGLISH);

        Assertions.assertNotNull(pojoValidated);
        Assertions.assertFalse(pojoValidated.hasError());
        Assertions.assertTrue(pojoValidated.isValid());

        Assertions.assertEquals(pojo, pojoValidated.getInstance());
    }

    /**
     * Pojo.
     *
     * @param firstName The first name
     */
    private record Pojo(@TrimmedLength(min = 1, max = 10) String firstName) {
    }
}