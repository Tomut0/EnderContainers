package fr.utarwyn.endercontainers.enderchest.context;

import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.enderchest.VanillaEnderChest;
import fr.utarwyn.endercontainers.inventory.menu.EnderChestListMenu;
import fr.utarwyn.endercontainers.storage.StorageManager;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import fr.utarwyn.endercontainers.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A context in which all enderchests of a player are loaded.
 *
 * @author Utarwyn
 * @since 2.2.0
 */
public class PlayerContext {

    /**
     * Owner of this memory context
     */
    private final UUID owner;

    /**
     * Storage object which manages data of the player
     */
    private final PlayerData data;

    /**
     * List of all chests loaded in the context
     */
    private Set<EnderChest> chests;

    /**
     * Construct a new player context.
     *
     * @param owner owner of the context
     */
    PlayerContext(UUID owner) {
        this.owner = owner;
        this.chests = Collections.synchronizedSet(new HashSet<>());
        this.data = Managers.get(StorageManager.class).createPlayerDataStorage(this.owner);
    }

    /**
     * Get the owner of the context.
     *
     * @return owner uuid
     */
    public UUID getOwner() {
        return this.owner;
    }

    /**
     * Get the owner as a Player object.
     * The player must be online to get the object.
     *
     * @return player object of the owner if it is connected, null otherwise
     */
    public Player getOwnerAsObject() {
        Player player = Bukkit.getPlayer(this.getOwner());
        return player != null && player.isOnline() ? player : null;
    }

    /**
     * Get the storage object which manages this context.
     *
     * @return this storage object
     */
    public PlayerData getData() {
        return this.data;
    }

    /**
     * Searching in the context for a chest which has a specific number.
     *
     * @param num The number of the chest
     * @return chest found
     */
    public Optional<EnderChest> getChest(int num) {
        return this.chests.stream()
                .filter(ch -> ch.getNum() == num)
                .findFirst();
    }

    /**
     * Count the number of accessible enderchests of the owner.
     *
     * @return The number of accessible enderchests loaded in the context
     */
    public int getAccessibleChestCount() {
        return (int) this.chests.stream()
                .filter(EnderChest::isAccessible)
                .count();
    }

    /**
     * Check if there is no player using containers of this context.
     *
     * @return true if chests of this context are unused
     */
    public boolean isChestsUnused() {
        return this.chests.stream().noneMatch(EnderChest::isContainerUsed);
    }

    /**
     * Loads a certain amount of enderchests in this player context.
     *
     * @param count amount of chests to load
     */
    public void loadEnderchests(int count) {
        this.chests = IntStream.rangeClosed(0, count - 1)
                .mapToObj(this::createEnderchest)
                .collect(Collectors.toSet());
    }

    /**
     * Loads offline player profile if needed for its vanilla enderchest.
     */
    public void loadOfflinePlayerProfile() {
        if (Files.getConfiguration().isUseVanillaEnderchest() && this.getOwnerAsObject() == null) {
            this.getChest(0)
                    .map(VanillaEnderChest.class::cast)
                    .ifPresent(VanillaEnderChest::loadOfflinePlayer);
        }
    }

    /**
     * Permits to open the inventory with all enderchests
     * of a specific player to another human.
     *
     * @param viewer player who want to open the inventory
     * @param block  block which has triggered the opening, can be null
     */
    public void openListInventory(Player viewer, Block block) {
        if (this.getAccessibleChestCount() == 1 && Files.getConfiguration().isOnlyShowAccessibleEnderchests()) {
            this.openEnderchestInventory(viewer, 0);
        } else {
            new EnderChestListMenu(this).open(viewer);
        }

        if (block != null && Files.getConfiguration().isGlobalSound()) {
            MiscUtil.playSound(block.getLocation(), "CHEST_OPEN", "BLOCK_CHEST_OPEN");
        } else {
            MiscUtil.playSound(viewer, "CHEST_OPEN", "BLOCK_CHEST_OPEN");
        }
    }

    /**
     * Permits to open the inventory with all enderchests
     * of a specific player to another human.
     *
     * @param viewer player who want to open the inventory
     */
    public void openListInventory(Player viewer) {
        this.openListInventory(viewer, null);
    }

    /**
     * Permits to open an enderchest inventory to a viewer.
     *
     * @param viewer viewer who want to open the enderchest inventory
     * @param num    number of the enderchest to open
     * @return true if the enderchest has been opened
     */
    public boolean openEnderchestInventory(Player viewer, int num) {
        Optional<EnderChest> chest = this.getChest(num);
        boolean accessible = false;

        if (chest.isPresent()) {
            accessible = chest.get().isAccessible();

            if (accessible) {
                chest.get().openContainerFor(viewer);
            }
        }

        return accessible;
    }

    /**
     * Save all datas stored in the context.
     */
    public void save() {
        this.chests.forEach(EnderChest::updateContainer);
        this.data.saveContext(this.chests);
    }

    /**
     * Create an object to manage an enderchest.
     *
     * @param number number of the chest
     * @return created enderchest instance
     */
    private EnderChest createEnderchest(int number) {
        if (number == 0 && Files.getConfiguration().isUseVanillaEnderchest()) {
            return new VanillaEnderChest(this);
        } else {
            return new EnderChest(this, number);
        }
    }

}
