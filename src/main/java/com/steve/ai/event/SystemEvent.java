package com.steve.ai.event;

/**
 * Marker interface for system events.
 *
 * <p>System events represent technical occurrences within the application,
 * such as configuration reloads, health check failures, or performance metrics.</p>
 *
 * <p><b>Example Events:</b></p>
 * <ul>
 *   <li>ConfigurationReloadedEvent</li>
 *   <li>HealthCheckFailedEvent</li>
 *   <li>PerformanceMetricEvent</li>
 * </ul>
 *
 * @since 2.0.0
 * @see EventBus
 */
public interface SystemEvent {
    // Marker interface
}
