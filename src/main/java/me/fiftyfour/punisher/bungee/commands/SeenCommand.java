package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.exceptions.DataFecthException;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import me.fiftyfour.punisher.universal.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SeenCommand extends Command {
    public SeenCommand() {
        super("seen", "punisher.seen", "lastseen");
    }
    
    private BungeeMain plugin = BungeeMain.getInstance();
    private String targetuuid;


    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length != 1) {
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("View the last seen information about a player.").color(ChatColor.RED).create());
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /seen <player>").color(ChatColor.WHITE).create());
            return;
        }
//        if (strings[0].contains("#")){
//            //get the joinid of the player that has this id, this is in the next version of the plugin
//        }
        ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[0]);
        Future<String> future;
        ExecutorService executorService;
        String targetname = null;
        if (findTarget != null) {
            targetuuid = findTarget.getUniqueId().toString().replace("-", "");
            targetname = findTarget.getName();
        } else {
            UUIDFetcher uuidFetcher = new UUIDFetcher();
            uuidFetcher.fetch(strings[0]);
            executorService = Executors.newSingleThreadExecutor();
            future = executorService.submit(uuidFetcher);
            try {
                targetuuid = future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                try {
                    throw new DataFecthException("UUID Required for next step", strings[0], "UUID", this.getName(), e);
                }catch (DataFecthException dfe){
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.log(dfe);
                    errorHandler.alert(dfe, commandSender);
                }
                executorService.shutdown();
                return;
            }
            executorService.shutdown();
        }
        if (targetuuid == null) {
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(strings[0] + " is not a player's name!").color(ChatColor.RED).create());
            return;
        }
        if (targetname == null) {
            targetname = NameFetcher.getName(targetuuid);
            if (targetname == null) {
                targetname = strings[0];
            }
        }
        if (!BungeeMain.InfoConfig.contains(targetuuid)){
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(targetname + " has not joined the server yet!").color(ChatColor.RED).create());
            return;
        }
        long lastlogin = (System.currentTimeMillis() - BungeeMain.InfoConfig.getLong(targetuuid + ".lastlogin"));
        long lastlogout = (System.currentTimeMillis() - BungeeMain.InfoConfig.getLong(targetuuid + ".lastlogout"));
        String lastloginString, lastlogoutString;
        int daysago = (int) (lastlogin / (1000 * 60 * 60 * 24));
        int hoursago = (int) (lastlogin / (1000 * 60 * 60) % 24);
        int minutesago = (int) (lastlogin / (1000 * 60) % 60);
        int secondsago = (int) (lastlogin / 1000 % 60);
        if (secondsago <= 0) secondsago = 1;
        if (daysago >= 1) lastloginString = daysago + "d " + hoursago + "h " + minutesago + "m " + secondsago + "s " + " ago";
        else if (hoursago >= 1) lastloginString = hoursago + "h " + minutesago + "m " + secondsago + "s " + " ago";
        else if (minutesago >= 1) lastloginString = minutesago + "m " + secondsago + "s " + " ago";
        else lastloginString = secondsago + "s " + " ago";
        daysago = (int) (lastlogout / (1000 * 60 * 60 * 24));
        hoursago = (int) (lastlogout / (1000 * 60 * 60) % 24);
        minutesago = (int) (lastlogout / (1000 * 60) % 60);
        secondsago = (int) (lastlogout / 1000 % 60);
        if (secondsago <= 0) secondsago = 1;
        if (daysago >= 1) lastlogoutString = daysago + "d " + hoursago + "h " + minutesago + "m " + secondsago + "s " + " ago";
        else if (hoursago >= 1) lastlogoutString = hoursago + "h " + minutesago + "m " + secondsago + "s " + " ago";
        else if (minutesago >= 1) lastlogoutString = minutesago + "m " + secondsago + "s " + " ago";
        else lastlogoutString = secondsago + "s " + " ago";
        commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Last seen Information for " + targetname + "(#" + BungeeMain.InfoConfig.getInt(targetuuid + ".joinid") + ")").color(ChatColor.RED).create());
        commandSender.sendMessage(new ComponentBuilder("First Joined Date: ").color(ChatColor.RED).append(BungeeMain.InfoConfig.getString(targetuuid + ".firstjoin")).color(ChatColor.GREEN).create());
        commandSender.sendMessage(new ComponentBuilder("Last Login: ").color(ChatColor.RED).append(lastloginString).color(ChatColor.GREEN).create());
        commandSender.sendMessage(new ComponentBuilder("Last Logout: ").color(ChatColor.RED).append(lastlogoutString).color(ChatColor.GREEN).create());
        commandSender.sendMessage(new ComponentBuilder("Last Server Played: ").color(ChatColor.RED).append(BungeeMain.InfoConfig.getString(targetuuid + ".lastserver")).color(ChatColor.GREEN).create());
    }
}
