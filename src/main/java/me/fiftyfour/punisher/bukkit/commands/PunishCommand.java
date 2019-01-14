package me.fiftyfour.punisher.bukkit.commands;


import me.fiftyfour.punisher.bukkit.BukkitMain;
import me.fiftyfour.punisher.bukkit.objects.IconMenu;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import me.fiftyfour.punisher.universal.fetchers.UUIDFetcher;
import me.fiftyfour.punisher.universal.fetchers.UserFetcher;
import me.fiftyfour.punisher.universal.systems.Permissions;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.PermissionData;
import me.lucko.luckperms.api.context.ContextManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.*;


public class PunishCommand implements PluginMessageListener, CommandExecutor {
    private String itemName;
    private Player clicker;
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;
    private IconMenu menu;
    private StringBuilder reputation;
    private BukkitMain plugin = BukkitMain.getInstance();

    private static void sendPluginMessage(@NotNull Player player, String channel, @NotNull String... messages) {
        try {
            ByteArrayOutputStream outbytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(outbytes);
            for (String msg : messages) {
                out.writeUTF(msg);
            }
            player.sendPluginMessage(BukkitMain.getPlugin(BukkitMain.class), channel, outbytes.toByteArray());
            out.close();
            outbytes.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("punish") || label.equalsIgnoreCase("p") || label.equalsIgnoreCase("pun")) {
            clicker = null;
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to use this command!");
                return false;
            }
            Player p = (Player) sender;
            if (p.hasPermission("punisher.punish.level.0")) {
                if (args.length <= 0) {
                    p.sendMessage(ChatColor.RED + "Please provide a player's name!");
                    return false;
                }
                String targetuuid = null;
                Player findTarget = Bukkit.getPlayer(args[0]);
                Future<String> future = null;
                ExecutorService executorService = null;
                if (findTarget != null) {
                    targetuuid = findTarget.getUniqueId().toString().replace("-", "");
                    UUID formatedUuid = UUIDFetcher.formatUUID(targetuuid);
                    if (formatedUuid.equals(p.getUniqueId())) {
                        p.sendMessage(prefix + ChatColor.RED + "You may not punish yourself!");
                        return false;
                    }
                } else {
                    UUIDFetcher uuidFetcher = new UUIDFetcher();
                    uuidFetcher.fetch(args[0]);
                    executorService = Executors.newSingleThreadExecutor();
                    future = executorService.submit(uuidFetcher);
                }
                if (future != null) {
                    try {
                        targetuuid = future.get(10, TimeUnit.SECONDS);
                    } catch (TimeoutException te) {
                        p.sendMessage(prefix + ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "Connection to mojang API took too long! Unable to fetch " + args[0] + "'s uuid!");
                        p.sendMessage(prefix + ChatColor.RED + "This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ");
                        sendPluginMessage(p, "BungeeCord", "LOG", "SEVERE", "ERROR: Connection to mojang API took too long! Unable to fetch " + args[0] + "'s uuid!");
                        sendPluginMessage(p, "BungeeCord", "LOG", "SEVERE", "Error message: " + te.getMessage());
                        StringBuilder stacktrace = new StringBuilder();
                        for (StackTraceElement stackTraceElement : te.getStackTrace()) {
                            stacktrace.append(stackTraceElement.toString()).append("\n");
                        }
                        sendPluginMessage(p, "BungeeCord", "LOG", "SEVERE", "Stack Trace: " + stacktrace.toString());
                        executorService.shutdown();
                        return false;
                    } catch (Exception e) {
                        p.sendMessage(prefix + ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "Unexpected Error while setting up GUI! Unable to fetch " + args[0] + "'s uuid!");
                        p.sendMessage(prefix + ChatColor.RED + "This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ");
                        sendPluginMessage(p, "BungeeCord", "LOG", "SEVERE", "ERROR: Unexpected error while setting up GUI! Unable to fetch " + args[0] + "'s uuid!");
                        sendPluginMessage(p, "BungeeCord", "LOG", "SEVERE", "Error message: " + e.getMessage());
                        StringBuilder stacktrace = new StringBuilder();
                        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                            stacktrace.append(stackTraceElement.toString()).append("\n");
                        }
                        sendPluginMessage(p, "BungeeCord", "LOG", "SEVERE", "Stack Trace: " + stacktrace.toString());
                        executorService.shutdown();
                        return false;
                    }
                    executorService.shutdown();
                }
                if (targetuuid == null) {
                    p.sendMessage(ChatColor.RED + "That is not a player's name!");
                    return false;
                }
                String targetName = NameFetcher.getName(targetuuid);
                if (targetName == null) {
                    targetName = args[0];
                }
                if (!BukkitMain.repCache.containsKey(targetuuid))
                    sendPluginMessage(p, "BungeeCord", "getrep", targetuuid, targetName);
                else{
                    double rep = BukkitMain.repCache.get(targetuuid);
                    String repString = new DecimalFormat("##.##").format(rep);
                    reputation = new StringBuilder();
                    if (rep == 5) {
                        reputation.append(ChatColor.WHITE).append("(").append(repString).append("/10").append(")");
                    } else if (rep > 5) {
                        reputation.append(ChatColor.GREEN).append("(").append(repString).append("/10").append(")");
                    } else if (rep < 5 && rep > -1) {
                        reputation.append(ChatColor.YELLOW).append("(").append(repString).append("/10").append(")");
                    } else if (rep < -1 && rep > -8) {
                        reputation.append(ChatColor.GOLD).append("(").append(repString).append("/10").append(")");
                    } else if (rep < -8) {
                        reputation.append(ChatColor.RED).append("(").append(repString).append("/10").append(")");
                    }
                    openGUI(p, targetuuid, targetName);
                }

            } else {
                p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
        }
        return false;
    }

    private void openGUI(Player p, String targetuuid, String targetName) {
        ItemStack shimmer = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
        ItemMeta smeta = shimmer.getItemMeta();
        smeta.addEnchant(Enchantment.DURABILITY, 1, true);
        smeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        shimmer.setItemMeta(smeta);
        ItemStack Wood_Sword = new ItemStack(Material.WOOD_SWORD, 1);
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
        ItemStack Iron_Boots = new ItemStack(Material.IRON_BOOTS, 1);
        ItemMeta ibmeta = Iron_Boots.getItemMeta();
        ibmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        Iron_Boots.setItemMeta(ibmeta);
        menu = new IconMenu(ChatColor.LIGHT_PURPLE + "Punish: " + targetName + " " + reputation.toString(), 2, (player, menu, row, slot, item) -> {
            itemName = item.getItemMeta().getDisplayName();
            clicker = player;
            if (item.getItemMeta().getLore() != null) {
                punishmentSelected(targetuuid, targetName, row.getRow(), slot, item.getItemMeta().getLore().toString());
                return true;
            }
            return false;
        });
        menu.addButton(0, shimmer, " ");
        menu.addButton(1, shimmer, " ");
        menu.addButton(2, shimmer, " ");
        menu.addButton(3, shimmer, " ");
        menu.addButton(4, shimmer, " ");
        menu.addButton(5, shimmer, " ");
        menu.addButton(6, shimmer, " ");
        menu.addButton(7, shimmer, " ");
        menu.addButton(8, shimmer, " ");
        menu.addButton(9, shimmer, " ");
        menu.addButton(10, shimmer, " ");
        menu.addButton(11, shimmer, " ");
        menu.addButton(12, new ItemStack(Material.WATER_BUCKET, 1), ChatColor.RED + "Minor Chat Offence", ChatColor.WHITE + "Spam, Flood ETC");
        menu.addButton(13, new ItemStack(Material.NETHERRACK, 1), ChatColor.RED + "Major Chat Offence", ChatColor.WHITE + "Racism, Disrespect ETC");
        menu.addButton(14, new ItemStack(Material.SKULL_ITEM, 1, (short) 2), ChatColor.RED + "Other Offence", ChatColor.WHITE + "For Other Not Listed Offences", ChatColor.WHITE + "(Will ban for 30 minutes regardless of offence)");
        menu.addButton(15, shimmer, " ");
        menu.addButton(16, shimmer, " ");
        menu.addButton(17, shimmer, " ");
        if (p.hasPermission("punisher.punish.level.1")) {
            menu.addButton(11, new ItemStack(Material.WATER_BUCKET, 1), ChatColor.RED + "Minor Chat Offence", ChatColor.WHITE + "Spam, Flood ETC");
            menu.addButton(12, new ItemStack(Material.NETHERRACK, 1), ChatColor.RED + "Major Chat Offence", ChatColor.WHITE + "Racism, Disrespect ETC");
            menu.addButton(13, Iron_Boots, ChatColor.RED + "DDoS/DoX Threats", ChatColor.WHITE + "Includes Hinting At It and", ChatColor.WHITE + "Saying They Have a Player's DoX");
            menu.addButton(14, new ItemStack(Material.BOOK_AND_QUILL, 1), ChatColor.RED + "Inappropriate Link", ChatColor.WHITE + "Includes Pm's");
            if (plugin.getServer().getVersion().contains("1.8"))
                menu.addButton(15, new ItemStack(Material.SKULL_ITEM, 1, (short) 1), ChatColor.RED + "Scamming", ChatColor.WHITE + "When a player is unfairly taking a player's money", ChatColor.WHITE + "Through a fake scheme");
            else
                menu.addButton(15, new ItemStack(Material.SKULL_ITEM, 1, (short) 5), ChatColor.RED + "Scamming", ChatColor.WHITE + "When a player is unfairly taking a player's money", ChatColor.WHITE + "Through a fake scheme");
            if (p.hasPermission("punisher.punish.level.2")) {
                menu.setSize(4);
                menu.addButton(18, new ItemStack(Material.GLASS, 1), ChatColor.RED + "X-Raying", ChatColor.WHITE + "Mining Straight to Ores/Bases/Chests", ChatColor.WHITE + "Includes Chest Esp and Player Esp");
                menu.addButton(19, Wood_Sword, ChatColor.RED + "AutoClicker (Non PvP)", ChatColor.WHITE + "Using AutoClicker to Farm Mobs ETC");
                menu.addButton(20, new ItemStack(Material.FEATHER, 1), ChatColor.RED + "Fly/Speed Hacking", ChatColor.WHITE + "Includes Hacks Such as Jesus and Spider");
                menu.addButton(21, Diamond_Sword, ChatColor.RED + "Malicous PvP Hacks", ChatColor.WHITE + "Includes Hacks Such as Kill Aura and Reach");
                menu.addButton(22, Iron_Sword, ChatColor.RED + "Disallowed Mods", ChatColor.WHITE + "Includes Hacks Such as Derp and Headless");
                menu.addButton(23, new ItemStack(Material.TNT, 1), ChatColor.RED + "Greifing", ChatColor.WHITE + "Excludes Cobble Monstering and Bypassing land claims", ChatColor.WHITE + "Includes Things Such as 'lava curtaining' and TnT Cannoning");
                menu.addButton(24, new ItemStack(Material.SIGN, 1), ChatColor.RED + "Server Advertisment", ChatColor.RED + "Warning: Must Also Clear Chat After you have proof!!");
                if (plugin.getServer().getVersion().contains("1.10") || plugin.getServer().getVersion().contains("1.9"))
                    menu.addButton(25, new ItemStack(Material.FURNACE, 1), ChatColor.RED + "Exploiting", ChatColor.WHITE + "Includes Bypassing Land Claims and Cobble Monstering");
                else
                    menu.addButton(25, new ItemStack(Material.PURPLE_SHULKER_BOX, 1), ChatColor.RED + "Exploiting", ChatColor.WHITE + "Includes Bypassing Land Claims and Cobble Monstering");
                menu.addButton(26, new ItemStack(Material.IRON_TRAPDOOR, 1), ChatColor.RED + "TPA-Trapping", ChatColor.WHITE + "Sending a TPA Request to Someone", ChatColor.WHITE + "and Then Killing Them Once They Tp");
                menu.addButton(27, shimmer, " ");
                menu.addButton(28, shimmer, " ");
                menu.addButton(29, shimmer, " ");
                menu.addButton(30, new ItemStack(Material.SKULL_ITEM, 1, (short) 2), ChatColor.RED + "Other Minor Offence", ChatColor.WHITE + "For Other Minor Offences", ChatColor.WHITE + "(Will ban for 7 days regardless of offence)");
                menu.addButton(31, new ItemStack(Material.SKULL_ITEM, 1, (short) 3), ChatColor.RED + "Impersonation", ChatColor.WHITE + "Any type of Impersonation");
                menu.addButton(32, new ItemStack(Material.SKULL_ITEM, 1, (short) 4), ChatColor.RED + "Other Major Offence", ChatColor.WHITE + "Includes Inappropriate IGN's and Other Major Offences", ChatColor.WHITE + "(Will ban for 30 days regardless of offence)");
                menu.addButton(33, shimmer, " ");
                menu.addButton(34, shimmer, " ");
                menu.addButton(35, shimmer, " ");
                if (p.hasPermission("punisher.punish.level.3")) {
                    menu.setSize(6);
                    menu.addButton(36, new ItemStack(Material.COBBLESTONE, 1), ChatColor.RED + "Warn", ChatColor.WHITE + "Manually Warn the Player");
                    menu.addButton(37, new ItemStack(Material.COAL, 1), ChatColor.RED + "1 Hour Mute", ChatColor.WHITE + "Manually Mute the Player For 1 Hour");
                    menu.addButton(38, new ItemStack(Material.IRON_INGOT, 1), ChatColor.RED + "1 Day Mute", ChatColor.WHITE + "Manually Mute the Player For 1 Day");
                    menu.addButton(39, new ItemStack(Material.GOLD_INGOT, 1), ChatColor.RED + "3 Day Mute", ChatColor.WHITE + "Manually Mute the Player For 3 Days");
                    menu.addButton(40, new ItemStack(Material.INK_SACK, 1, (short) 4), ChatColor.RED + "1 Week Mute", ChatColor.WHITE + "Manually Mute the Player For 1 Week");
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
            }
            menu.open(p);
        }
    }

    private void punishmentSelected(String toPunishuuid, String targetName, int row, int slot, String item) {
        menu.close(clicker);
        User user = LuckPerms.getApi().getUser(targetName);
        if (user == null) {
            UserFetcher userFetcher = new UserFetcher();
            userFetcher.setUuid(UUIDFetcher.formatUUID(toPunishuuid));
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<User> userFuture = executorService.submit(userFetcher);
            try {
                user = userFuture.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                clicker.sendMessage(prefix + ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "Luckperms was unable to fetch permission data on: " + targetName);
                clicker.sendMessage(prefix + ChatColor.RED + "This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ");
                sendPluginMessage(clicker, "BungeeCord", "LOG", "SEVERE", "ERROR: Luckperms was unable to fetch permission data on: " + targetName);
                sendPluginMessage(clicker, "BungeeCord", "LOG", "SEVERE", "Error message: " + e.getMessage());
                StringBuilder stacktrace = new StringBuilder();
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    stacktrace.append(stackTraceElement.toString()).append("\n");
                }
                sendPluginMessage(clicker, "BungeeCord", "LOG", "SEVERE", "Stack Trace: " + stacktrace.toString());
                executorService.shutdown();
                return;
            }
            executorService.shutdown();
            if (user == null) {
                throw new IllegalStateException();
            }
        }
        ContextManager cm = LuckPerms.getApi().getContextManager();
        Contexts contexts = cm.lookupApplicableContexts(user).orElse(cm.getStaticContexts());
        PermissionData permissionData = user.getCachedData().getPermissionData(contexts);
        try {
            if (permissionData.getPermissionValue("punisher.bypass").asBoolean() || Permissions.higher(clicker, toPunishuuid, targetName)) {
                clicker.sendMessage(prefix + ChatColor.GREEN + "Punishing " + targetName + " for: " + itemName + ChatColor.GREEN);
                if (row == 1 && (slot == 2 || slot == 3) && item.contains("Spam, Flood ETC")) 
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Minor_Chat_Offence", "Minor Chat Offence");
                else if (row == 1 && (slot == 3 || slot == 4) && item.contains("Racism, Disrespect ETC"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Major_Chat_Offence", "Major Chat Offence");
                else if (row == 1 && slot == 5 && item.contains("For Other Not Listed Offences"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Other_Offence", "Other Offence");
                else if (row == 1 && slot == 4 && item.contains("Includes Hinting At It and"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "DDoS_DoX_Threats", "DDoS/DoX Threats");
                else if (row == 1 && slot == 5 && item.contains("Includes Pm's"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Inappropriate_Link", "Inappropriate Link");
                else if (row == 1 && slot == 6 && item.contains("When a player is unfairly taking a player's"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Scamming", "Scamming");
                else if (row == 2 && slot == 0 && item.contains("Mining Straight to Ores/Bases/Chests"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "X_Raying", "X-Raying");
                else if (row == 2 && slot == 1 && item.contains("Using AutoClicker to Farm Mobs ETC"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "AutoClicker", "AutoClicker(Non PvP)");
                else if (row == 2 && slot == 2 && item.contains("Includes Hacks Such as Jesus and Spider"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Fly_Speed_Hacking", "Fly/Speed Hacking");
                else if (row == 2 && slot == 3 && item.contains("Includes Hacks Such as Kill Aura and Reach"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Malicious_PvP_Hacks", "Malicious PvP Hacks");
                else if (row == 2 && slot == 4 && item.contains("Includes Hacks Such as Derp and Headless"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Disallowed_Mods", "Disallowed Mods");
                else if (row == 2 && slot == 5 && item.contains("Excludes Cobble Monstering and Bypassing land claims"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Greifing", "Greifing");
                else if (row == 2 && slot == 6 && item.contains("Warning: Must Also Clear Chat After you have proof!"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Server_Advertisment", "Server Advertisment");
                else if (row == 2 && slot == 7 && item.contains("Includes Bypassing Land Claims and Cobble Monstering"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Exploiting", "Exploiting");
                else if (row == 2 && slot == 8 && item.contains("Sending a TPA Request to Someone"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Tpa_Trapping", "TPA-Trapping");
                else if (row == 3 && slot == 5 && item.contains("Includes Inappropriate IGN's and Other Major Offences"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Other_Major_Offence", "Other Major Offence");
                else if (row == 3 && slot == 3 && item.contains("For Other Minor Offences"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Other_Minor_Offence", "Other Minor Offence");
                else if (row == 3 && slot == 4 && item.contains("Any type of Impersonation"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Impersonation", "Player Impersonation");
                else if (row == 4 && slot == 0 && item.contains("Manually Warn the Player"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual", "WARN", "Manually Warned");
                else if (row == 4 && slot == 1 && item.contains("Manually Mute the Player For 1 Hour"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_Hour", "MUTE", "Manually Muted for 1 Hour");
                else if (row == 4 && slot == 2 && item.contains("Manually Mute the Player For 1 Day"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_1_Day", "MUTE", "Manually Muted for 1 Day");
                else if (row == 4 && slot == 3 && item.contains("Manually Mute the Player For 3 Days"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_3_Day", "MUTE", "Manually Muted for 3 Days");
                else if (row == 4 && slot == 4 && item.contains("Manually Mute the Player For 1 Week"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_1_Week", "MUTE", "Manually Muted for 1 Week");
                else if (row == 4 && slot == 5 && item.contains("Manually Mute the Player For 2 Weeks"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_2_Week", "MUTE", "Manually Muted for 2 Weeks");
                else if (row == 4 && slot == 6 && item.contains("Manually Mute the Player For 3 Weeks"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_3_Week", "MUTE", "Manually Muted for 3 Weeks");
                else if (row == 4 && slot == 7 && item.contains("Manually Mute the Player For 1 Month"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_1_Month", "MUTE", "Manually Muted for 1 Month");
                else if (row == 4 && slot == 8 && item.contains("Manually Mute the Player Permanently"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_Permanently", "MUTE", "Manually Muted Permanently");
                else if (row == 5 && slot == 0 && item.contains("Manually Kick the Player"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual", "KICK", "Manually Kicked");
                else if (row == 5 && slot == 1 && item.contains("Manually Ban The Player For 1 Hour"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_Hour", "BAN", "Manually Banned for 1 Hour");
                else if (row == 5 && slot == 2 && item.contains("Manually Ban the Player For 1 Day"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_1_Day", "BAN", "Manually Banned for 1 Day");
                else if (row == 5 && slot == 3 && item.contains("Manually Ban the Player For 3 Days"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_3_Day", "BAN", "Manually Banned for 3 Days");
                else if (row == 5 && slot == 4 && item.contains("Manually Ban the Player For 1 Week"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_1_Week", "BAN", "Manually Banned for 1 Week");
                else if (row == 5 && slot == 5 && item.contains("Manually Ban the Player For 2 Weeks"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_2_Week", "BAN", "Manually Banned for 2 Weeks");
                else if (row == 5 && slot == 6 && item.contains("Manually Ban the Player For 3 Weeks"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_3_Week", "BAN", "Manually Banned for 3 Weeks");
                else if (row == 5 && slot == 7 && item.contains("Manually Ban the Player For 1 Month"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_1_Month", "BAN", "Manually Banned For 1 Month");
                else if (row == 5 && slot == 8 && item.contains("Manually Ban the Player Permanently"))
                    sendPluginMessage(clicker, "BungeeCord", "punish", targetName, toPunishuuid, "Manual_Permanently", "BAN", "Manually Banned Permanently");
            } else {
                clicker.sendMessage(prefix + ChatColor.RED + "You cannot Punish that player!");
            }
        } catch (Exception e) {
            clicker.sendMessage(prefix + ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "Luckperms was unable to fetch permission data on: " + targetName);
            clicker.sendMessage(prefix + ChatColor.RED + "This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ");
            sendPluginMessage(clicker, "BungeeCord", "LOG", "SEVERE", "ERROR: Luckperms was unable to fetch permission data on: " + targetName);
            sendPluginMessage(clicker, "BungeeCord", "LOG", "SEVERE", "Error message: " + e.getMessage());
            StringBuilder stacktrace = new StringBuilder();
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                stacktrace.append(stackTraceElement.toString()).append("\n");
            }
            sendPluginMessage(clicker, "BungeeCord", "LOG", "SEVERE", "Stack Trace: " + stacktrace.toString());
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals("BungeeCord")) {
            try {
                ByteArrayInputStream inBytes = new ByteArrayInputStream(message);
                DataInputStream in = new DataInputStream(inBytes);
                String subchannel = in.readUTF();
                if (subchannel.equals("rep")) {
                    String repstring = in.readUTF();
                    double rep;
                    String targetuuid = in.readUTF();
                    String targetname = in.readUTF();
                    reputation = new StringBuilder();
                    try {
                        rep = Double.parseDouble(repstring);
                    } catch (NumberFormatException e) {
                        reputation.append(ChatColor.WHITE).append("(").append("-").append("/10").append(")");
                        openGUI(player, targetuuid, targetname);
                        return;
                    }
                    String repString = new DecimalFormat("##.##").format(rep);
                    if (rep == 5) {
                        reputation.append(ChatColor.WHITE).append("(").append(repString).append("/10").append(")");
                    } else if (rep > 5) {
                        reputation.append(ChatColor.GREEN).append("(").append(repString).append("/10").append(")");
                    } else if (rep < 5 && rep > -1) {
                        reputation.append(ChatColor.YELLOW).append("(").append(repString).append("/10").append(")");
                    } else if (rep < -1 && rep > -8) {
                        reputation.append(ChatColor.GOLD).append("(").append(repString).append("/10").append(")");
                    } else if (rep < -8) {
                        reputation.append(ChatColor.RED).append("(").append(repString).append("/10").append(")");
                    }
                    openGUI(player, targetuuid, targetname);
                }
                in.close();
                inBytes.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}