package me.fiftyfour.punisher.bukkit.objects;

import me.fiftyfour.punisher.bukkit.BukkitMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IconMenu implements Listener {

    private List<String> viewing = new ArrayList<>();
    private String name;
    private int size;
    private onClick click;
    private ItemStack[] items;

    IconMenu(String name, int size, onClick click) {
        this.name = name;
        this.size = size * 9;
        items = new ItemStack[this.size];
        this.click = click;
        BukkitMain plugin = BukkitMain.getInstance();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    public void setSize(int size) {
        this.size = size * 9;
        items = Arrays.copyOf(items, this.size);
    }
    public void setName(String name) {
        this.name = name;
    }

    void setClick(onClick onClick) {
        this.click = onClick;
    }
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        for (Player p : this.getViewers())
            close(p);
    }

    void open(Player p) {
        p.openInventory(getInventory(p));
        viewing.add(p.getName());
    }
    private Inventory getInventory(Player p) {
        Inventory inv = Bukkit.createInventory(p, size, name);
        for (int i = 0; i < items.length; i++)
            if (items[i] != null)
                inv.setItem(i, items[i]);
        return inv;
    }

    void close(Player p) {
        if (p.getOpenInventory().getTitle().equals(name))
            p.closeInventory();
    }
    private List<Player> getViewers() {
        List<Player> viewers = new ArrayList<>();
        for (String s : viewing)
            viewers.add(Bukkit.getPlayer(s));
        return viewers;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (viewing.contains(event.getWhoClicked().getName())) {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            if (event.getClickedInventory() != null && event.getCurrentItem() != null && Arrays.equals(event.getClickedInventory().getContents(), this.items)) {
                if (click.click(p, this, event.getSlot(), event.getCurrentItem()))
                    close(p);
            }
        }
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (viewing.contains(event.getPlayer().getName()))
            viewing.remove(event.getPlayer().getName());
    }

    void addButton(int position, ItemStack item, String name, String... lore) {
        items[position] = getItem(item, name, lore);
    }
    private ItemStack getItem(ItemStack item, String name, String... lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }

    interface onClick {
        boolean click(Player clicker, IconMenu menu, int slot, ItemStack item);
    }
}