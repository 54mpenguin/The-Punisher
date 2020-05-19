package me.fiftyfour.punisher.bukkit.objects;

import me.fiftyfour.punisher.bukkit.commands.PunishCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LevelThreePunishMenu {

    private static IconMenu menu;

    public static void setupMenu() {
        menu = new IconMenu(null, 6, null);
        ItemStack fill = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE, 1);
        ItemMeta fillmeta = fill.getItemMeta();
        fillmeta.addEnchant(Enchantment.DURABILITY, 1, true);
        fillmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        fill.setItemMeta(fillmeta);
        ItemStack Iron_Boots = new ItemStack(Material.IRON_BOOTS, 1);
        ItemMeta ibmeta = Iron_Boots.getItemMeta();
        ibmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        Iron_Boots.setItemMeta(ibmeta);
        ItemStack Wood_Sword = new ItemStack(Material.WOODEN_SWORD, 1);
        ItemMeta wsmeta = Wood_Sword.getItemMeta();
        wsmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        Wood_Sword.setItemMeta(wsmeta);
        ItemStack Iron_Sword = new ItemStack(Material.IRON_SWORD, 1);
        ItemMeta ismeta = Iron_Sword.getItemMeta();
        ismeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        Iron_Sword.setItemMeta(ismeta);
        ItemStack Diamond_Sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        ItemMeta dsmeta = Diamond_Sword.getItemMeta();
        dsmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        Diamond_Sword.setItemMeta(dsmeta);
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
        menu.addButton(18, new ItemStack(Material.GLASS, 1), ChatColor.RED + "X-Raying", ChatColor.WHITE + "Mining Straight to Ores/Bases/Chests", ChatColor.WHITE + "Includes Chest Esp and Player Esp");
        menu.addButton(19, Wood_Sword, ChatColor.RED + "AutoClicker (Non PvP)", ChatColor.WHITE + "Using AutoClicker to Farm Mobs ETC");
        menu.addButton(20, new ItemStack(Material.FEATHER, 1), ChatColor.RED + "Fly/Speed Hacking", ChatColor.WHITE + "Includes Hacks Such as Jesus and Spider");
        menu.addButton(21, Diamond_Sword, ChatColor.RED + "Malicous PvP Hacks", ChatColor.WHITE + "Includes Hacks Such as Kill Aura and Reach");
        menu.addButton(22, Iron_Sword, ChatColor.RED + "Disallowed Mods", ChatColor.WHITE + "Includes Hacks Such as Derp and Headless");
        menu.addButton(23, new ItemStack(Material.TNT, 1), ChatColor.RED + "Greifing", ChatColor.WHITE + "Excludes Cobble Monstering and Bypassing land claims", ChatColor.WHITE + "Includes Things Such as 'lava curtaining' and TnT Cannoning");
        menu.addButton(24, new ItemStack(Material.SIGN, 1), ChatColor.RED + "Server Advertisment", ChatColor.RED + "Warning: Must Also Clear Chat After you have proof!!");
        menu.addButton(25, new ItemStack(Material.PURPLE_SHULKER_BOX, 1), ChatColor.RED + "Exploiting", ChatColor.WHITE + "Includes Bypassing Land Claims and Cobble Monstering");
        menu.addButton(26, new ItemStack(Material.IRON_TRAPDOOR, 1), ChatColor.RED + "TPA-Trapping", ChatColor.WHITE + "Sending a TPA Request to Someone", ChatColor.WHITE + "and Then Killing Them Once They Tp");
        menu.addButton(27, fill, " ");
        menu.addButton(28, fill, " ");
        menu.addButton(29, fill, " ");
        menu.addButton(30, new ItemStack(Material.ZOMBIE_HEAD, 1), ChatColor.RED + "Other Minor Offence", ChatColor.WHITE + "For Other Minor Offences", ChatColor.WHITE + "(Will ban for 7 days regardless of offence)");
        menu.addButton(31, new ItemStack(Material.PLAYER_HEAD, 1), ChatColor.RED + "Impersonation", ChatColor.WHITE + "Any type of Impersonation");
        menu.addButton(32, new ItemStack(Material.CREEPER_HEAD, 1), ChatColor.RED + "Other Major Offence", ChatColor.WHITE + "Includes Inappropriate IGN's and Other Major Offences", ChatColor.WHITE + "(Will ban for 30 days regardless of offence)");
        menu.addButton(33, fill, " ");
        menu.addButton(34, fill, " ");
        menu.addButton(35, fill, " ");
        menu.addButton(36, new ItemStack(Material.COBBLESTONE, 1), ChatColor.RED + "Warn", ChatColor.WHITE + "Manually Warn the Player");
        menu.addButton(37, new ItemStack(Material.COAL, 1), ChatColor.RED + "1 Hour Mute", ChatColor.WHITE + "Manually Mute the Player For 1 Hour");
        menu.addButton(38, new ItemStack(Material.IRON_INGOT, 1), ChatColor.RED + "1 Day Mute", ChatColor.WHITE + "Manually Mute the Player For 1 Day");
        menu.addButton(39, new ItemStack(Material.GOLD_INGOT, 1), ChatColor.RED + "3 Day Mute", ChatColor.WHITE + "Manually Mute the Player For 3 Days");
        menu.addButton(40, new ItemStack(Material.LAPIS_LAZULI, 1), ChatColor.RED + "1 Week Mute", ChatColor.WHITE + "Manually Mute the Player For 1 Week");
        menu.addButton(41, new ItemStack(Material.REDSTONE, 1), ChatColor.RED + "2 Week Mute", ChatColor.WHITE + "Manually Mute the Player For 2 Weeks");
        menu.addButton(42, new ItemStack(Material.DIAMOND, 1), ChatColor.RED + "3 Week Mute", ChatColor.WHITE + "Manually Mute the Player For 3 Weeks");
        menu.addButton(43, new ItemStack(Material.EMERALD, 1), ChatColor.RED + "1 Month Mute", ChatColor.WHITE + "Manually Mute the Player For 1 Month");
        menu.addButton(44, new ItemStack(Material.BARRIER, 1), ChatColor.RED + "Perm Mute", ChatColor.WHITE + "Manually Mute the Player Permanently");
        menu.addButton(45, new ItemStack(Material.STONE, 1), ChatColor.RED + "Kick", ChatColor.WHITE + "Manually Kick the Player");
        menu.addButton(46, new ItemStack(Material.COAL_ORE, 1), ChatColor.RED + "1 Hour Ban", ChatColor.WHITE + "Manually Ban The Player For 1 Hour");
        menu.addButton(47, new ItemStack(Material.IRON_ORE, 1), ChatColor.RED + "1 Day Ban", ChatColor.WHITE + "Manually Ban the Player For 1 Day");
        menu.addButton(48, new ItemStack(Material.GOLD_ORE, 1), ChatColor.RED + "3 Day Ban", ChatColor.WHITE + "Manually Ban the Player For 3 Days");
        menu.addButton(49, new ItemStack(Material.LAPIS_ORE, 1), ChatColor.RED + "1 Week Ban", ChatColor.WHITE + "Manually Ban the Player For 1 Week");
        menu.addButton(50, new ItemStack(Material.REDSTONE_ORE, 1), ChatColor.RED + "2 Week Ban", ChatColor.WHITE + "Manually Ban the Player For 2 Weeks");
        menu.addButton(51, new ItemStack(Material.DIAMOND_ORE, 1), ChatColor.RED + "3 Week Ban", ChatColor.WHITE + "Manually Ban the Player For 3 Weeks");
        menu.addButton(52, new ItemStack(Material.EMERALD_ORE, 1), ChatColor.RED + "1 Month Ban", ChatColor.WHITE + "Manually Ban the Player For 1 Month");
        menu.addButton(53, new ItemStack(Material.BEDROCK, 1), ChatColor.RED + "Perm Ban", ChatColor.WHITE + "Manually Ban the Player Permanently");
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
