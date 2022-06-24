package dev.voidframework.core.conversion;

import dev.voidframework.core.conversion.impl.DefaultConversion;
import dev.voidframework.core.conversion.impl.DefaultConverterManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.MethodName.class)
public final class ConverterManagerTest {

    private static Conversion conversion;

    @BeforeAll
    public static void beforeAll() {
        final ConverterManager converterManager = new DefaultConverterManager();
        converterManager.registerConverter(String.class, Integer.class, new StringToIntegerConverter());
        converterManager.registerConverter(Integer.class, String.class, new IntegerToStringConverter());

        ConverterManagerTest.conversion = new DefaultConversion(converterManager);
    }

    @Test
    public void canConvertByClass() {
        Assertions.assertTrue(conversion.canConvert(String.class, Integer.class));
        Assertions.assertTrue(conversion.canConvert(Integer.class, String.class));
        Assertions.assertFalse(conversion.canConvert(Integer.class, Boolean.class));
    }

    @Test
    public void canConvertByInstance() {
        final String numberAsString = "10";
        final Integer number = 10;

        Assertions.assertTrue(conversion.canConvert(numberAsString, Integer.class));
        Assertions.assertTrue(conversion.canConvert(number, String.class));
        Assertions.assertFalse(conversion.canConvert(number, Boolean.class));
    }

    @Test
    public void convertNull() {
        Assertions.assertNull(conversion.convert((String) null, String.class, Integer.class));
    }

    @Test
    public void convertExplicit() {
        final String numberAsString = "10";

        Assertions.assertEquals(Integer.valueOf(10), conversion.convert(numberAsString, String.class, Integer.class));
    }

    @Test
    public void convertImplicit() {
        final String numberAsString = "10";

        Assertions.assertEquals(Integer.valueOf(10), conversion.convert(numberAsString, Integer.class));
    }

    @Test
    public void convertListExplicit() {
        final List<String> numberAsStringList = List.of("1", "2", "3");

        Assertions.assertEquals(List.of(1, 2, 3), conversion.convert(numberAsStringList, String.class, Integer.class));
    }

    @Test
    public void convertListImplicit() {
        final List<String> numberAsStringList = List.of("1", "2", "3");

        Assertions.assertEquals(List.of(1, 2, 3), conversion.convert(numberAsStringList, Integer.class));
    }

    @Test
    public void convertSetExplicit() {
        final Set<String> numberAsStringSet = Set.of("1", "2", "3");

        Assertions.assertEquals(Set.of(1, 2, 3), conversion.convert(numberAsStringSet, String.class, Integer.class));
    }

    @Test
    public void convertSetImplicit() {
        final Set<String> numberAsStringSet = Set.of("1", "2", "3");

        Assertions.assertEquals(Set.of(1, 2, 3), conversion.convert(numberAsStringSet, Integer.class));
    }

    public static class StringToIntegerConverter implements TypeConverter<String, Integer> {

        @Override
        public Integer convert(final String source) {
            try {
                return Integer.valueOf(source);
            } catch (final NumberFormatException ignore) {
                return null;
            }
        }
    }

    public static class IntegerToStringConverter implements TypeConverter<Integer, String> {

        @Override
        public String convert(final Integer source) {
            return source.toString();
        }
    }
}
