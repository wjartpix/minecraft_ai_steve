package com.steve.ai.test;

import com.steve.ai.action.Task;
import com.steve.ai.api.perception.WorldPerception;
import com.steve.ai.perception.SimulatedWorldPerception;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for action tests.
 *
 * <p>Provides common test infrastructure including:
 * <ul>
 *   <li>Test service container with mock support</li>
 *   <li>Simulated world perception</li>
 *   <li>Helper methods for creating test data</li>
 * </ul>
 * </p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>
 * public class BuildStructureActionTest extends ActionTestBase {
 *     &#64;Test
 *     void testBuildHouse() {
 *         // Given
 *         setBlockAt(pos(0, 0, 0), Blocks.GRASS_BLOCK);
 *         Task task = createTask("build", "structure", "house");
 *
 *         // When
 *         BuildStructureAction action = new BuildStructureAction(mockSteve, task);
 *         action.start();
 *
 *         // Then
 *         assertTrue(action.isComplete());
 *     }
 * }
 * </pre>
 *
 * @since 2.0.0
 */
public abstract class ActionTestBase {

    protected TestServiceContainer container;
    protected SimulatedWorldPerception worldPerception;

    @BeforeEach
    void setUp() {
        container = new TestServiceContainer();
        worldPerception = new SimulatedWorldPerception();
        container.registerMock(WorldPerception.class, worldPerception);
    }

    /**
     * Sets a block at the specified position in the simulated world.
     *
     * @param pos   The position
     * @param block The block to set
     */
    protected void setBlockAt(BlockPos pos, Block block) {
        worldPerception.setBlock(pos, block);
    }

    /**
     * Creates a BlockPos from coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return BlockPos instance
     */
    protected BlockPos pos(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    /**
     * Creates a Task with the specified action and parameters.
     *
     * @param action     The action type
     * @param paramKey   First parameter key
     * @param paramValue First parameter value
     * @return Task instance
     */
    protected Task createTask(String action, String paramKey, Object paramValue) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put(paramKey, paramValue);
        return new Task(action, params);
    }

    /**
     * Creates a Task with multiple parameters.
     *
     * @param action The action type
     * @param params The parameters map
     * @return Task instance
     */
    protected Task createTask(String action, java.util.Map<String, Object> params) {
        return new Task(action, params);
    }

    /**
     * Creates a simple ground platform for testing.
     *
     * @param centerX Center X coordinate
     * @param centerZ Center Z coordinate
     * @param radius  Radius of the platform
     */
    protected void createGroundPlatform(int centerX, int centerZ, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                setBlockAt(pos(centerX + x, 63, centerZ + z), Blocks.GRASS_BLOCK);
                // Add dirt below
                setBlockAt(pos(centerX + x, 62, centerZ + z), Blocks.DIRT);
                setBlockAt(pos(centerX + x, 61, centerZ + z), Blocks.DIRT);
            }
        }
    }

    /**
     * Waits for a condition to become true.
     *
     * @param condition The condition to check
     * @param timeoutMs Maximum wait time in milliseconds
     * @return true if condition became true within timeout
     */
    protected boolean waitFor(java.util.function.BooleanSupplier condition, long timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (condition.getAsBoolean()) {
                return true;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
}
