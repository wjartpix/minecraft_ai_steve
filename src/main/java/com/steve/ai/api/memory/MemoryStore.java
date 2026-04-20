package com.steve.ai.api.memory;

import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Interface for persistent memory storage.
 *
 * <p>Provides an abstraction over different storage backends (NBT, database, etc.)
 * for agent memory persistence.</p>
 *
 * <p><b>Design Pattern:</b> Repository Pattern for memory storage</p>
 *
 * @since 2.0.0
 * @param <T> The type of memory entries
 */
public interface MemoryStore<T extends MemoryStore.MemoryEntry> {

    /**
     * Stores a memory entry.
     *
     * @param entry The entry to store
     */
    void store(T entry);

    /**
     * Retrieves a memory entry by ID.
     *
     * @param id The entry ID
     * @return Optional containing the entry if found
     */
    Optional<T> retrieve(String id);

    /**
     * Retrieves all entries matching a predicate.
     *
     * @param predicate Filter condition
     * @return List of matching entries
     */
    List<T> retrieveAll(Predicate<T> predicate);

    /**
     * Retrieves the most recent N entries.
     *
     * @param count Number of entries to retrieve
     * @return List of recent entries
     */
    List<T> retrieveRecent(int count);

    /**
     * Deletes an entry by ID.
     *
     * @param id The entry ID
     * @return true if deleted
     */
    boolean delete(String id);

    /**
     * Clears all entries.
     */
    void clear();

    /**
     * Returns the total number of entries.
     *
     * @return Entry count
     */
    int size();

    /**
     * Serializes the memory store to NBT.
     *
     * @return NBT compound tag
     */
    CompoundTag serializeNBT();

    /**
     * Deserializes the memory store from NBT.
     *
     * @param tag NBT compound tag
     */
    void deserializeNBT(CompoundTag tag);

    /**
     * Base interface for memory entries.
     */
    interface MemoryEntry {
        /**
         * Returns the unique ID of this entry.
         *
         * @return Entry ID
         */
        String getId();

        /**
         * Returns the timestamp when this entry was created.
         *
         * @return Timestamp in milliseconds
         */
        long getTimestamp();

        /**
         * Returns the type of this entry.
         *
         * @return Entry type
         */
        String getType();

        /**
         * Serializes this entry to NBT.
         *
         * @return NBT compound tag
         */
        CompoundTag toNBT();

        /**
         * Deserializes this entry from NBT.
         *
         * @param tag NBT compound tag
         * @return Deserialized entry
         */
        static MemoryEntry fromNBT(CompoundTag tag) {
            throw new UnsupportedOperationException("Must be implemented by concrete class");
        }
    }
}
