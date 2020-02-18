package fr.utarwyn.endercontainers.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a menu generated by the plugin.
 * It has many improvements instead of a simple {@link org.bukkit.inventory.InventoryHolder}.
 * Events onClick and onClose are supported.
 *
 * @author Utarwyn
 * @since 2.0.0
 */
public abstract class AbstractMenu implements InventoryHolder {

    /**
     * The generated inventory
     */
    protected Inventory inventory;

    /**
     * Flag which defines if item moves in the menu are restricted
     */
    protected boolean itemMovingRestricted = true;

    /**
     * True if the menu has been initialized, false otherwise
     */
    private boolean initialized;

    /**
     * Returns the number of filled slots in the container.
     *
     * @return Number of fileld slots
     */
    public int getFilledSlotsNb() {
        return (int) Arrays.stream(this.inventory.getContents()).filter(Objects::nonNull).count();
    }

    /**
     * Returns the inventory with items of this menu.
     *
     * @return inventory with all items of this menu
     */
    @Nonnull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Get contents of this menu in a concurrent map.
     *
     * @return generated map with contents
     */
    public ConcurrentMap<Integer, ItemStack> getMapContents() {
        ItemStack[] contents = this.inventory.getContents();
        ConcurrentMap<Integer, ItemStack> contentsMap = new ConcurrentHashMap<>();

        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                contentsMap.put(i, contents[i]);
            }
        }

        return contentsMap;
    }

    /**
     * Check if item moves inside this menu are restricted.
     *
     * @return value of the moveRestricted flag
     */
    public boolean isItemMovingRestricted() {
        return this.itemMovingRestricted;
    }

    /**
     * Get the loading state of the menu.
     *
     * @return true if the menu has been initialized
     */
    public boolean isInitialized() {
        return this.initialized;
    }

    /**
     * Check if somebody is viewing the menu.
     *
     * @return true if there is at least one player viewing the menu
     */
    public boolean isUsed() {
        return this.inventory != null && !this.inventory.getViewers().isEmpty();
    }

    /**
     * Called when a player click on an item in the menu
     *
     * @param player The player who interacts with the menu
     * @param slot   The slot where the player has clicked
     */
    public void onClick(Player player, int slot) {
        // Not implemented
    }

    /**
     * Open the container to a specific player.
     *
     * @param player Player that will receive the container
     */
    public void open(Player player) {
        player.openInventory(this.inventory);
    }

    /**
     * Reload the menu by replacing the current inventory.
     */
    protected void reloadInventory() {
        String title = this.getTitle();
        int size = this.getRows() * 9;
        ItemStack[] itemStacks = new ItemStack[size];

        if (title.length() > 32) {
            title = title.substring(0, 32);
        }

        if (this.inventory != null) {
            itemStacks = this.inventory.getContents();
        }

        this.inventory = Bukkit.createInventory(this, size, title);
        this.inventory.setContents(itemStacks);

        this.prepare();

        this.initialized = true;
    }

    /**
     * Called when a player closes the menu
     *
     * @param player The player who closes the menu
     */
    public abstract void onClose(Player player);

    /**
     * Return the number of rows needed for this container.
     *
     * @return the number of rows of this inventory
     */
    protected abstract int getRows();

    /**
     * Prepare this menu by adding needed items directly inside the current inventory.
     */
    protected abstract void prepare();

    /**
     * Return the title displayed at the top of this container.
     *
     * @return the displayed title
     */
    protected abstract String getTitle();

}
