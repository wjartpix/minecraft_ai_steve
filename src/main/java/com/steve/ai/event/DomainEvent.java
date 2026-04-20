package com.steve.ai.event;

/**
 * Marker interface for domain events.
 *
 * <p>Domain events represent significant occurrences within the business domain,
 * such as actions being started or completed, state transitions, etc.</p>
 *
 * <p><b>Example Events:</b></p>
 * <ul>
 *   <li>{@link ActionStartedEvent}</li>
 *   <li>{@link ActionCompletedEvent}</li>
 *   <li>{@link StateTransitionEvent}</li>
 * </ul>
 *
 * @since 2.0.0
 * @see EventBus
 */
public interface DomainEvent {
    // Marker interface
}
