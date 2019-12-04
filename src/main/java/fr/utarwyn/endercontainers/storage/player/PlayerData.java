package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.storage.StorageWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * Storage wrapper to manage data of a specific player
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public abstract class PlayerData extends StorageWrapper {

    /**
     * UUID of the player linked to this wrapper
     */
    private UUID uuid;

    /**
     * Construct a new storage wrapper for a player (even offline)
     *
     * @param uuid The uuid of the player to manage its data
     */
    PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.load();
    }

    /**
     * Returns the UUID of the player
     *
     * @return UUID of the player
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * The player linked to the storage wrapper
     *
     * @return Player linked if online otherwise null.
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    /**
     * Returns the formatted UUID without hyphen
     *
     * @return The formatted UUID
     */
    String getMinimalUUID() {
        return this.uuid.toString().replace("-", "");
    }

    @Override
    protected boolean hasParams(Object... params) {
        return params.length == 1 && this.uuid.equals(params[0]);
    }

    /**
     * Returns contents of a saved enderchest
     *
     * @param enderChest Get this enderchest's contents
     * @return The map filled with the contents of the chest
     */
    public abstract ConcurrentMap<Integer, ItemStack> getEnderchestContents(EnderChest enderChest);

    /**
     * Returns the number of rows saved for an enderchest
     *
     * @param enderChest Get this enderchest's number of rows
     * @return Number of rows stored for the enderchest
     */
    public abstract int getEnderchestRows(EnderChest enderChest);

    /**
     * Save the enderchest with its content.
     *
     * @param chest    enderchest to save
     * @param contents contents of its container
     */
    public abstract void saveEnderchest(EnderChest chest, ConcurrentMap<Integer, ItemStack> contents);

}
