package com.steve.ai.test;

import com.steve.ai.di.ServiceContainer;
import com.steve.ai.di.SimpleServiceContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Test-specific service container with mock support.
 *
 * <p>Extends SimpleServiceContainer with additional methods for
 * registering mock services in tests.</p>
 *
 * @since 2.0.0
 * @see SimpleServiceContainer
 */
public class TestServiceContainer implements ServiceContainer {

    private final SimpleServiceContainer delegate;
    private final Map<Class<?>, Object> mocks;

    public TestServiceContainer() {
        this.delegate = new SimpleServiceContainer();
        this.mocks = new HashMap<>();
    }

    /**
     * Registers a mock service.
     *
     * @param <T>   The service type
     * @param type  The service class
     * @param mock  The mock implementation
     */
    public <T> void registerMock(Class<T> type, T mock) {
        mocks.put(type, mock);
        delegate.register(type, mock);
    }

    /**
     * Registers a named mock service.
     *
     * @param <T>   The service type
     * @param name  The service name
     * @param mock  The mock implementation
     */
    public <T> void registerMock(String name, T mock) {
        delegate.register(name, mock);
    }

    /**
     * Checks if a service is a mock.
     *
     * @param type The service type
     * @return true if the service is a registered mock
     */
    public boolean isMock(Class<?> type) {
        return mocks.containsKey(type);
    }

    @Override
    public <T> void register(Class<T> type, T instance) {
        delegate.register(type, instance);
    }

    @Override
    public <T> void register(String name, T instance) {
        delegate.register(name, instance);
    }

    @Override
    public <T> T getService(Class<T> type) {
        return delegate.getService(type);
    }

    @Override
    public <T> T getService(String name, Class<T> type) {
        return delegate.getService(name, type);
    }

    @Override
    public <T> java.util.Optional<T> findService(Class<T> type) {
        return delegate.findService(type);
    }

    @Override
    public <T> java.util.Optional<T> findService(String name, Class<T> type) {
        return delegate.findService(name, type);
    }

    @Override
    public boolean hasService(Class<?> type) {
        return delegate.hasService(type);
    }

    @Override
    public boolean hasService(String name) {
        return delegate.hasService(name);
    }

    @Override
    public boolean unregister(Class<?> type) {
        mocks.remove(type);
        return delegate.unregister(type);
    }

    @Override
    public boolean unregister(String name) {
        return delegate.unregister(name);
    }

    @Override
    public void clear() {
        mocks.clear();
        delegate.clear();
    }

    @Override
    public int getServiceCount() {
        return delegate.getServiceCount();
    }

    /**
     * Returns the underlying container for advanced operations.
     *
     * @return The delegate container
     */
    public SimpleServiceContainer getDelegate() {
        return delegate;
    }
}
