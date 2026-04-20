package com.steve.ai.event;

/**
 * Marker interface for integration events.
 *
 * <p>Integration events represent communication with external systems,
 * such as LLM API calls, database operations, or third-party service interactions.</p>
 *
 * <p><b>Example Events:</b></p>
 * <ul>
 *   <li>LLMRequestEvent</li>
 *   <li>LLMResponseEvent</li>
 *   <li>ExternalServiceCalledEvent</li>
 * </ul>
 *
 * @since 2.0.0
 * @see EventBus
 */
public interface IntegrationEvent {
    // Marker interface
}
