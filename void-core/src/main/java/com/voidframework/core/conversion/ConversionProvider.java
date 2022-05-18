package com.voidframework.core.conversion;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.voidframework.core.conversion.impl.DefaultConversion;
import com.voidframework.core.exception.ConversionException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodInfoList;
import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeArgument;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Provider for the interface {@link Conversion} implementation.
 */
@Singleton
public class ConversionProvider implements Provider<Conversion> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conversion.class);

    private final Config configuration;
    private final ConverterManager converterManager;
    private final Injector injector;

    @Inject
    public ConversionProvider(final Config configuration,
                              final ConverterManager converterManager,
                              final Injector injector) {
        this.configuration = configuration;
        this.converterManager = converterManager;
        this.injector = injector;
    }

    @Override
    public Conversion get() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final String[] converterPackage = configuration.getStringList("voidframework.core.converterScanPackages")
            .stream().filter(StringUtils::isNotEmpty)
            .toArray(String[]::new);

        if (converterPackage.length == 0) {
            return new DefaultConversion(converterManager);
        }

        LOGGER.info("Searching converters...");
        int discoveredConverterCount = 0;
        try (final ScanResult scanResult = new ClassGraph()
            .acceptPackages(converterPackage)
            .addClassLoader(classLoader)
            .enableAllInfo()
            .scan()) {

            for (final ClassInfo classInfo : scanResult.getAllClasses()) {
                if (classInfo.implementsInterface(TypeConverter.class)) {
                    // Determine source class and target class
                    final List<TypeArgument> typeArgumentList = classInfo.getTypeSignature().getSuperinterfaceSignatures()
                        .get(0)
                        .getTypeArguments();
                    if (typeArgumentList.size() != 2) {
                        // Technically not possible, but you might as well be 100% sure
                        throw new ConversionException.InvalidConverter(classInfo.getName(), "Bad number of type parameter");
                    }

                    final String sourceClassName = typeArgumentList.get(0).getTypeSignature().toString();
                    final Class<?> sourceClassType = revolveClassFromString(sourceClassName, classLoader)
                        .orElseThrow(() -> new ConversionException.InvalidConverter(
                            classInfo.getName(), "Can't retrieve Class<?> from '" + sourceClassName + "'"));

                    final String targetClassName = typeArgumentList.get(1).getTypeSignature().toString();
                    final Class<?> targetClassType = revolveClassFromString(targetClassName, classLoader)
                        .orElseThrow(() -> new ConversionException.InvalidConverter(
                            classInfo.getName(), "Can't retrieve Class<?> from '" + targetClassName + "'"));

                    // Retrieves constructor
                    final MethodInfoList constructorInfoList = classInfo.getConstructorInfo();
                    if (constructorInfoList.isEmpty()) {
                        throw new ConversionException.InvalidConverter(classInfo.getName(), "No constructor found");
                    }

                    final MethodInfo constructorInfo = constructorInfoList.get(0);

                    // Try to instantiate converter
                    final TypeConverter<?, ?> converter = (TypeConverter<?, ?>) injector.getInstance(
                        constructorInfo.loadClassAndGetConstructor().getDeclaringClass());

                    // Adds it to the Map of instantiated converters
                    LOGGER.debug("Register new Converter<source={}, target={}>", sourceClassName, targetClassName);
                    converterManager.registerConverter(sourceClassType, targetClassType, converter);
                }
            }
        }

        LOGGER.info("{} converter(s) has been discovered", discoveredConverterCount);
        return new DefaultConversion(converterManager);
    }

    /**
     * Resolve {@code Class<?>} from a class name.
     *
     * @param className   Name of the class
     * @param classLoader Loader to use
     * @return Resolved {@code Class<?>}
     */
    private Optional<Class<?>> revolveClassFromString(final String className, final ClassLoader classLoader) {
        if (StringUtils.isBlank(className)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Class.forName(className, false, classLoader));
        } catch (final ClassNotFoundException ignore) {
            return Optional.empty();
        }
    }
}