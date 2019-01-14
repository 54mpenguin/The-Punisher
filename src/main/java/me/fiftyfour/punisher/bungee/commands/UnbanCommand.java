package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import me.fiftyfour.punisher.universal.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;
import java.util.concurrent.*;

public class UnbanCommand extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private String targetuuid;
    private int sqlfails = 0;
    private PunishmentManager punishMnger = PunishmentManager.getInstance();

    public UnbanCommand() {
        super("unban", "punisher.unban", "pardon");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length == 0) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("Unban a player").color(ChatColor.RED).append("\nUsage: /unban <player name>").color(ChatColor.WHITE).create());
                return;
            }
            ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[0]);
            Future<String> future = null;
            ExecutorService executorService = null;
            if (findTarget != null){
                targetuuid = findTarget.getUniqueId().toString().replace("-", "");
            }else {
                UUIDFetcher uuidFetcher = new UUIDFetcher();
                uuidFetcher.fetch(strings[0]);
                executorService = Executors.newSingleThreadExecutor();
                future = executorService.submit(uuidFetcher);
            }
            if (future != null) {
                try {
                    targetuuid = future.get(10, TimeUnit.SECONDS);
                } catch (TimeoutException te) {
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                    BungeeMain.Logs.severe("ERROR: Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!");
                    BungeeMain.Logs.severe("Error message: " + te.getMessage());
                    StringBuilder stacktrace = new StringBuilder();
                    for (StackTraceElement stackTraceElement : te.getStackTrace()){
                        stacktrace.append(stackTraceElement.toString()).append("\n");
                    }
                    BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                    executorService.shutdown();
                    return;
                } catch (Exception e) {
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Unexpected error while executing command! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                    player.sendMessage(new ComponentBuilder(plugin.prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                    BungeeMain.Logs.severe("ERROR: Unexpected error while trying executing command in class: " + this.getName() + " Unable to fetch " + strings[0] + "'s uuid");
                    BungeeMain.Logs.severe("Error message: " + e.getMessage());
                    StringBuilder stacktrace = new StringBuilder();
                    for (StackTraceElement stackTraceElement : e.getStackTrace()){
                        stacktrace.append(stackTraceElement.toString()).append("\n");
                    }
                    BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                    executorService.shutdown();
                    return;
                }
                executorService.shutdown();
            }
            if (targetuuid != null) {
                String targetname = NameFetcher.getName(targetuuid);
                if (targetname == null) {
                    targetname = strings[0];
                }
                try {
                    if (punishMnger.isBanned(targetuuid)){
                        punishMnger.revoke(punishMnger.getBan(targetuuid), player, targetname, false, true);
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("Successfully unbanned " + targetname).color(ChatColor.GREEN).create());
                    }else{
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append(targetname + " is not currently banned!").color(ChatColor.RED).create());
                    }
                }catch (SQLException e){
                    plugin.getLogger().severe(plugin.prefix + e);
                    sqlfails++;
                    if(sqlfails > 5){
                        plugin.getProxy().getPluginManager().unregisterCommand(this);
                        StringBuilder sb = new StringBuilder();
                        for (String args : strings){
                            sb.append(args).append(" ");
                        }
                        commandSender.sendMessage(new ComponentBuilder(this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!").color(ChatColor.RED).create());
                        commandSender.sendMessage(new ComponentBuilder("Disabling command to prevent further damage to database").color(ChatColor.RED).create());
                        plugin.getLogger().severe(plugin.prefix + this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!");
                        plugin.getLogger().severe(plugin.prefix + "Disabling command to prevent further damage to database!");
                        BungeeMain.Logs.severe(this.getName() + " has thrown an exception more than 5 times!");
                        BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                        return;
                    }
                    if (plugin.testConnectionManual())
                        this.execute(commandSender, strings);
                }
            } else {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("That is not a player's name!").color(ChatColor.RED).create());
            }
        } else {
            if (strings.length == 0) {
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Unban a player").color(ChatColor.RED).append("\nUsage: /unban <player name>").color(ChatColor.WHITE).create());
                return;
            }
            ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[0]);
            Future<String> future = null;
            ExecutorService executorService = null;
            if (findTarget != null){
                targetuuid = findTarget.getUniqueId().toString().replace("-", "");
            }else {
                UUIDFetcher uuidFetcher = new UUIDFetcher();
                uuidFetcher.fetch(strings[0]);
                executorService = Executors.newSingleThreadExecutor();
                future = executorService.submit(uuidFetcher);
            }
            if (future != null) {
                try {
                    targetuuid = future.get(10, TimeUnit.SECONDS);
                } catch (TimeoutException te) {
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                    BungeeMain.Logs.severe("ERROR: Connection to mojang API took too long! Unable to fetch " + strings[0] + "'s uuid!");
                    BungeeMain.Logs.severe("Error message: " + te.getMessage());
                    StringBuilder stacktrace = new StringBuilder();
                    for (StackTraceElement stackTraceElement : te.getStackTrace()){
                        stacktrace.append(stackTraceElement.toString()).append("\n");
                    }
                    BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                    executorService.shutdown();
                    return;
                } catch (Exception e) {
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("ERROR: ").color(ChatColor.DARK_RED).append("Unexpected error while executing command! Unable to fetch " + strings[0] + "'s uuid!").color(ChatColor.RED).create());
                    commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("This error will be logged! Please Inform an admin asap, this plugin will no longer function as intended! ").color(ChatColor.RED).create());
                    BungeeMain.Logs.severe("ERROR: Unexpected error while trying executing command in class: " + this.getName() + " Unable to fetch " + strings[0] + "'s uuid");
                    BungeeMain.Logs.severe("Error message: " + e.getMessage());
                    StringBuilder stacktrace = new StringBuilder();
                    for (StackTraceElement stackTraceElement : e.getStackTrace()){
                        stacktrace.append(stackTraceElement.toString()).append("\n");
                    }
                    BungeeMain.Logs.severe("Stack Trace: " + stacktrace.toString());
                    executorService.shutdown();
                    return;
                }
                executorService.shutdown();
            }
            if (targetuuid != null) {
                String targetname = NameFetcher.getName(targetuuid);
                if (targetname == null) {
                    targetname = strings[0];
                }
                try {
                    if (punishMnger.isBanned(targetuuid)){
                        punishMnger.revoke(punishMnger.getBan(targetuuid), null, targetname, false, true);
                        commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Successfully unbanned " + targetname).color(ChatColor.GREEN).create());
                    }else{
                        commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(targetname + " is not currently banned!").color(ChatColor.RED).create());
                    }
                }catch (SQLException e){
                    plugin.getLogger().severe(plugin.prefix + e);
                    sqlfails++;
                    if(sqlfails > 5){
                        plugin.getProxy().getPluginManager().unregisterCommand(this);
                        StringBuilder sb = new StringBuilder();
                        for (String args : strings){
                            sb.append(args).append(" ");
                        }
                        commandSender.sendMessage(new ComponentBuilder(this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!").color(ChatColor.RED).create());
                        commandSender.sendMessage(new ComponentBuilder("Disabling command to prevent further damage to database").color(ChatColor.RED).create());
                        plugin.getLogger().severe(plugin.prefix + this.getName() + " " + sb.toString() + " has thrown an exception more than 5 times!");
                        plugin.getLogger().severe(plugin.prefix + "Disabling command to prevent further damage to database!");
                        BungeeMain.Logs.severe(this.getName() + " has thrown an exception more than 5 times!");
                        BungeeMain.Logs.severe("Disabling command to prevent further damage to database!");
                        return;
                    }
                    if (plugin.testConnectionManual())
                        this.execute(commandSender, strings);
                }
            } else {
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("That is not a player's name!").color(ChatColor.RED).create());
            }
        }
    }
}