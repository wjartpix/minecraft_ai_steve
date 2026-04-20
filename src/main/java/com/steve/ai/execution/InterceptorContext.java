package com.steve.ai.execution;

import java.util.HashMap;
import java.util.Map;

/**
 * Context passed to interceptors during action execution.
 *
 * <p>Allows interceptors to share data and control execution flow.</p>
 *
 * @since 2.0.0
 * @see ActionInterceptor
 */
public class InterceptorContext {

    private final Map<String, Object> attributes;
    private boolean cancelled;
    private String cancellationReason;

    public InterceptorContext() {
        this.attributes = new HashMap<>();
        this.cancelled = false;
    }

    /**
     * Sets an attribute.
     *
     * @param key   The attribute key
     * @param value The attribute value
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Gets an attribute.
     *
     * @param key The attribute key
     * @return The attribute value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * Gets an attribute with a default value.
     *
     * @param key          The attribute key
     * @param defaultValue The default value
     * @return The attribute value, or default if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }

    /**
     * Checks if an attribute exists.
     *
     * @param key The attribute key
     * @return true if the attribute exists
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * Removes an attribute.
     *
     * @param key The attribute key
     * @return The removed value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T removeAttribute(String key) {
        return (T) attributes.remove(key);
    }

    /**
     * Cancels the action execution.
     *
     * @param reason The cancellation reason
     */
    public void cancel(String reason) {
        this.cancelled = true;
        this.cancellationReason = reason;
    }

    /**
     * Checks if the action has been cancelled.
     *
     * @return true if cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Gets the cancellation reason.
     *
     * @return The cancellation reason, or null if not cancelled
     */
    public String getCancellationReason() {
        return cancellationReason;
    }

    /**
     * Returns all attribute keys.
     *
     * @return Set of attribute keys
     */
    public java.util.Set<String> getAttributeKeys() {
        return new java.util.HashSet<>(attributes.keySet());
    }

    /**
     * Clears all attributes.
     */
    public void clearAttributes() {
        attributes.clear();
    }
}
