package dev.voidframework.core.lifecycle;

import com.typesafe.config.Config;
import dev.voidframework.core.exception.LifeCycleException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Life cycle manager takes care of executing the various hooks defined by the
 * use of the {@link dev.voidframework.core.lifecycle.LifeCycleStart} and {@link dev.voidframework.core.lifecycle.LifeCycleStop} annotations.
 */
public final class LifeCycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifeCycleManager.class);

    private final Config configuration;
    private final List<StartHandler> startHandlerList;
    private final List<StopHandler> stopHandlerList;

    private boolean isRunning;

    /**
     * Build a new instance.
     *
     * @param configuration The current configuration
     */
    public LifeCycleManager(final Config configuration) {

        this.configuration = configuration;
        this.startHandlerList = new ArrayList<>();
        this.stopHandlerList = new ArrayList<>();
        this.isRunning = false;
    }

    /**
     * Register a "START" method.
     *
     * @param classInstance The class instance where is located the method to invoke
     * @param method        The method to invoke
     * @param priority      The priority
     */
    public void registerStart(final Object classInstance, final Method method, final int priority) {

        LOGGER.debug("Register LifeCycle 'START' {}::{} (priority={})", classInstance.getClass().getName(), method.getName(), priority);

        if (this.isRunning) {
            this.invokeMethodStart(new StartHandler(classInstance, method, priority));
        } else {
            this.startHandlerList.add(new StartHandler(classInstance, method, priority));
        }
    }

    /**
     * Register a "STOP" method.
     *
     * @param classInstance                The class instance where is located the method to invoke
     * @param method                       The method to invoke
     * @param priority                     The priority
     * @param gracefulStopTimeoutConfigKey The graceful stop timeout configuration key
     */
    public void registerStop(final Object classInstance, final Method method, final int priority, final String gracefulStopTimeoutConfigKey) {

        LOGGER.debug("Register LifeCycle 'STOP' {}::{} (priority={})", classInstance.getClass().getName(), method.getName(), priority);
        this.stopHandlerList.add(new StopHandler(classInstance, method, priority, gracefulStopTimeoutConfigKey));
    }

    /**
     * Invoke all registered "START" methods.
     */
    public void startAll() {

        if (!this.isRunning) {
            this.isRunning = true;

            this.startHandlerList
                .stream()
                .sorted(Comparator.comparingInt(StartHandler::priority))
                .forEach(this::invokeMethodStart);
        }
    }

    /**
     * Invoke all registered "STOP" methods.
     */
    public void stopAll() {

        if (this.isRunning) {
            this.stopHandlerList
                .stream()
                .sorted(Comparator.comparingInt(StopHandler::priority))
                .forEach(this::invokeMethodStop);

            this.isRunning = false;
        }
    }

    /**
     * Invokes a "START" method.
     *
     * @param startHandler The method handler
     */
    private void invokeMethodStart(final StartHandler startHandler) {

        try {
            final long start = System.currentTimeMillis();
            startHandler.method.invoke(startHandler.classInstance);
            final long end = System.currentTimeMillis();

            LOGGER.info("{}::{} executed in {}ms", startHandler.classInstance.getClass().getName(), startHandler.method.getName(), end - start);
        } catch (final Throwable t) {
            throw new LifeCycleException.InvocationFailure(startHandler.classInstance.getClass().getName(), startHandler.method.getName(), t);
        }
    }

    /**
     * Invokes a "STOP" method.
     *
     * @param stopHandler The method handler
     */
    private void invokeMethodStop(final StopHandler stopHandler) {

        try {
            final Thread thread = new Thread(() -> {
                try {
                    stopHandler.method.invoke(stopHandler.classInstance);
                } catch (final Throwable t) {
                    LOGGER.error("Can't invoke {}::{}", stopHandler.classInstance.getClass().getName(), stopHandler.method.getName(), t);
                }
            });

            long gracefulStopTimeout = 0;
            if (StringUtils.isNotBlank(stopHandler.gracefulStopTimeoutConfigKey)
                && this.configuration.hasPath(stopHandler.gracefulStopTimeoutConfigKey)) {

                gracefulStopTimeout = this.configuration.getLong(stopHandler.gracefulStopTimeoutConfigKey);
            }

            final long start = System.currentTimeMillis();
            thread.start();
            thread.join(gracefulStopTimeout);
            final long end = System.currentTimeMillis();

            LOGGER.info("{}::{} executed in {}ms", stopHandler.classInstance.getClass().getName(), stopHandler.method.getName(), end - start);
        } catch (final InterruptedException e) {
            LOGGER.info("{}::{} INTERRUPTED!", stopHandler.classInstance.getClass().getName(), stopHandler.method.getName());
        }
    }

    /**
     * "START" method handler.
     *
     * @param classInstance The class instance where is located the method to invoke
     * @param method        The method to invoke
     * @param priority      The priority
     */
    private record StartHandler(Object classInstance,
                                Method method,
                                int priority) {
    }

    /**
     * "STOP" method handler.
     *
     * @param classInstance                The class instance where is located the method to invoke
     * @param method                       The method to invoke
     * @param priority                     The priority
     * @param gracefulStopTimeoutConfigKey The graceful stop timeout configuration key
     */
    private record StopHandler(Object classInstance,
                               Method method,
                               int priority,
                               String gracefulStopTimeoutConfigKey) {
    }
}
