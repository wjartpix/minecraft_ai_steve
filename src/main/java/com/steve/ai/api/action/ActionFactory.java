package com.steve.ai.api.action;

import com.steve.ai.action.Task;
import com.steve.ai.action.actions.BaseAction;
import com.steve.ai.di.ServiceContainer;
import com.steve.ai.entity.SteveEntity;
import com.steve.ai.execution.ActionContext;

/**
 * Factory interface for creating action instances.
 *
 * <p>This interface is part of the API layer and defines the contract for
 * action creation in a decoupled manner. Implementations are registered
 * via the ActionRegistry and used to instantiate actions dynamically.</p>
 *
 * <p><b>Design Pattern:</b> Factory Pattern</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * // Register a factory
 * registry.register("mine", (steve, task, ctx) -> new MineBlockAction(steve, task));
 *
 * // Create an action
 * BaseAction action = factory.create(steve, task, context);
 * </pre>
 *
 * @since 2.0.0
 * @see BaseAction
 * @see ActionRegistry
 */
@FunctionalInterface
public interface ActionFactory {

    /**
     * Creates a new action instance.
     *
     * @param steve   The Steve entity that will execute the action
     * @param task    The task containing action parameters
     * @param context The action context with dependencies and metadata
     * @return A new action instance ready to be started
     * @throws IllegalArgumentException if parameters are invalid
     */
    BaseAction create(SteveEntity steve, Task task, ActionContext context);
}
