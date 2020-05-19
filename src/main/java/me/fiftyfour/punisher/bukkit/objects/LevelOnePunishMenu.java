package me.fiftyfour.punisher.bukkit.objects;

import me.fiftyfour.punisher.bukkit.commands.PunishCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LevelOnePunishMenu {

    private static IconMenu menu;

    public static void setupMenu() {
        menu = new IconMenu(null, 2, null);
        ItemStack fill = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE, 1);
        ItemMeta fillmeta = fill.getItemMeta();
        fillmeta.addEnchant(Enchantment.DURABILITY, 1, true);
        fillmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        fill.setItemMeta(fillmeta);
        ItemStack Iron_Boots = new ItemStack(Material.IRON_BOOTS, 1);
        ItemMeta ibmeta = Iron_Boots.getItemMeta();
        ibmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        Iron_Boots.setItemMeta(ibmeta);
        menu.addButton(0, fill, " ");
        menu.addButton(1, fill, " ");
        menu.addButton(2, fill, " ");
        menu.addButton(3, fill, " ");
        menu.addButton(4, fill, " ");
        menu.addButton(5, fill, " ");
        menu.addButton(6, fill, " ");
        menu.addButton(7, fill, " ");
        menu.addButton(8, fill, " ");
        menu.addButton(9, fill, " ");
        menu.addButton(10, fill, " ");
        menu.addButton(11, new ItemStack(Material.WATER_BUCKET, 1), ChatColor.RED + "Minor Chat Offence", ChatColor.WHITE + "Spam, Flood ETC");
        menu.addButton(12, new ItemStack(Material.NETHERRACK, 1), ChatColor.RED + "Major Chat Offence", ChatColor.WHITE + "Racism, Disrespect ETC");
        menu.addButton(13, Iron_Boots, ChatColor.RED + "DDoS/DoX Threats", ChatColor.WHITE + "Includes Hinting At It and", ChatColor.WHITE + "Saying They Have a Player's DoX");
        menu.addButton(14, new ItemStack(Material.WRITABLE_BOOK, 1), ChatColor.RED + "Inappropriate Link", ChatColor.WHITE + "Includes Pm's");
        menu.addButton(15, new ItemStack(Material.DRAGON_HEAD, 1), ChatColor.RED + "Scamming", ChatColor.WHITE + "When a player is unfairly taking a player's money", ChatColor.WHITE + "Through a fake scheme");
        menu.addButton(16, fill, " ");
        menu.addButton(17, fill, " ");
    }

    public void open(final Player punisher, String targetuuid, String targetName, String reputation) {
        menu.setClick((player, menu, slot, item) -> {
            if (item.getItemMeta().getLore() != null) {
                menu.close(player);
                PunishCommand.punishmentSelected(targetuuid, targetName, slot, item.getItemMeta().getLore().toString(), player);
                return true;
            }
            return false;
        });
        menu.setName(ChatColor.LIGHT_PURPLE + "Punish: " + targetName + " " + reputation);
        menu.open(punisher);
    }
}
