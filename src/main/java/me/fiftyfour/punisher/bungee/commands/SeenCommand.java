package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.PunisherPlugin;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.bungee.listeners.ServerConnect;
import me.fiftyfour.punisher.universal.exceptions.DataFecthException;
import me.fiftyfour.punisher.universal.util.NameFetcher;
import me.fiftyfour.punisher.universal.util.UUIDFetcher;
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

    private PunisherPlugin plugin = PunisherPlugin.getInstance();


    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length != 1) {
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("View the last seen information about a player.").color(ChatColor.RED).create());
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /seen <player>").color(ChatColor.WHITE).create());
            return;
        }
        if (strings[0].contains("#")) {
            int joinid;
            try {
                joinid = Integer.parseInt(strings[0].replace("#", ""));
            } catch (NumberFormatException nfe) {
                commandSender.sendMessage(new ComponentBuilder(strings[0] + " is not a valid join id, use #<joinid> to get seen information on the person with that id.").color(ChatColor.RED).create());
                return;
            }
            if (joinid > ServerConnect.lastJoinId) {
                commandSender.sendMessage(new ComponentBuilder(strings[0] + " is higher than the last used join id. Highest last used join id: " + ServerConnect.lastJoinId).color(ChatColor.RED).create());
                return;
            }
            String targetuuid = PunisherPlugin.playerInfoConfig.getString(String.valueOf(joinid));
            String targetname = NameFetcher.getName(targetuuid);
            if (targetname == null) {
                targetname = strings[0];
            }

            long lastlogin = (System.currentTimeMillis() - PunisherPlugin.playerInfoConfig.getLong(targetuuid + ".lastlogin"));
            long lastlogout = (System.currentTimeMillis() - PunisherPlugin.playerInfoConfig.getLong(targetuuid + ".lastlogout"));
            String lastloginString, lastlogoutString;
            int daysago = (int) (lastlogin / (1000 * 60 * 60 * 24));
            int hoursago = (int) (lastlogin / (1000 * 60 * 60) % 24);
            int minutesago = (int) (lastlogin / (1000 * 60) % 60);
            int secondsago = (int) (lastlogin / 1000 % 60);
            if (secondsago <= 0) secondsago = 1;
            if (daysago >= 1)
                lastloginString = daysago + "d " + hoursago + "h " + minutesago + "m " + secondsago + "s " + " ago";
            else if (hoursago >= 1) lastloginString = hoursago + "h " + minutesago + "m " + secondsago + "s " + " ago";
            else if (minutesago >= 1) lastloginString = minutesago + "m " + secondsago + "s " + " ago";
            else lastloginString = secondsago + "s " + " ago";
            daysago = (int) (lastlogout / (1000 * 60 * 60 * 24));
            hoursago = (int) (lastlogout / (1000 * 60 * 60) % 24);
            minutesago = (int) (lastlogout / (1000 * 60) % 60);
            secondsago = (int) (lastlogout / 1000 % 60);
            if (secondsago <= 0) secondsago = 1;
            if (daysago >= 1)
                lastlogoutString = daysago + "d " + hoursago + "h " + minutesago + "m " + secondsago + "s " + " ago";
            else if (hoursago >= 1) lastlogoutString = hoursago + "h " + minutesago + "m " + secondsago + "s " + " ago";
            else if (minutesago >= 1) lastlogoutString = minutesago + "m " + secondsago + "s " + " ago";
            else lastlogoutString = secondsago + "s " + " ago";
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Last seen Information for " + targetname + "(#" + PunisherPlugin.playerInfoConfig.getInt(targetuuid + ".joinid") + ")").color(ChatColor.RED).create());
            commandSender.sendMessage(new ComponentBuilder("First Joined Date: ").color(ChatColor.RED).append(PunisherPlugin.playerInfoConfig.getString(targetuuid + ".firstjoin")).color(ChatColor.GREEN).create());
            commandSender.sendMessage(new ComponentBuilder("Last Login: ").color(ChatColor.RED).append(lastloginString).color(ChatColor.GREEN).create());
            commandSender.sendMessage(new ComponentBuilder("Last Server Played: ").color(ChatColor.RED).append(PunisherPlugin.playerInfoConfig.getString(targetuuid + ".lastserver")).color(ChatColor.GREEN).create());
            if (PunisherPlugin.playerInfoConfig.contains(targetuuid + ".lastlogout"))
                commandSender.sendMessage(new ComponentBuilder("Last Logout: ").color(ChatColor.RED).append(lastlogoutString).color(ChatColor.GREEN).create());
            return;
        }
        ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[0]);
        Future<String> future;
        ExecutorService executorService;
        String targetuuid;
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
                targetuuid = future.get(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                try {
                    throw new DataFecthException("UUID Required for next step", strings[0], "UUID", this.getName(), e);
                }catch (DataFecthException dfe){
                    ErrorHandler errorHandler = ErrorHandler.getINSTANCE();
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
        if (!PunisherPlugin.playerInfoConfig.contains(targetuuid)) {
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(targetname + " has not joined the server yet!").color(ChatColor.RED).create());
            return;
        }
        long lastlogin = (System.currentTimeMillis() - PunisherPlugin.playerInfoConfig.getLong(targetuuid + ".lastlogin"));
        long lastlogout = (System.currentTimeMillis() - PunisherPlugin.playerInfoConfig.getLong(targetuuid + ".lastlogout"));
        String lastloginString, lastlogoutString;
        int daysago = (int) (lastlogin / (1000 * 60 * 60 * 24));
        int hoursago = (int) (lastlogin / (1000 * 60 * 60) % 24);
        int minutesago = (int) (lastlogin / (1000 * 60) % 60);
        int secondsago = (int) (lastlogin / 1000 % 60);
        if (secondsago <= 0) secondsago = 1;
        if (daysago >= 1)
            lastloginString = daysago + "d " + hoursago + "h " + minutesago + "m " + secondsago + "s " + " ago";
        else if (hoursago >= 1) lastloginString = hoursago + "h " + minutesago + "m " + secondsago + "s " + " ago";
        else if (minutesago >= 1) lastloginString = minutesago + "m " + secondsago + "s " + " ago";
        else lastloginString = secondsago + "s " + " ago";
        daysago = (int) (lastlogout / (1000 * 60 * 60 * 24));
        hoursago = (int) (lastlogout / (1000 * 60 * 60) % 24);
        minutesago = (int) (lastlogout / (1000 * 60) % 60);
        secondsago = (int) (lastlogout / 1000 % 60);
        if (secondsago <= 0) secondsago = 1;
        if (daysago >= 1)
            lastlogoutString = daysago + "d " + hoursago + "h " + minutesago + "m " + secondsago + "s " + " ago";
        else if (hoursago >= 1) lastlogoutString = hoursago + "h " + minutesago + "m " + secondsago + "s " + " ago";
        else if (minutesago >= 1) lastlogoutString = minutesago + "m " + secondsago + "s " + " ago";
        else lastlogoutString = secondsago + "s " + " ago";
        commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Last seen Information for " + targetname + "(#" + PunisherPlugin.playerInfoConfig.getInt(targetuuid + ".joinid") + ")").color(ChatColor.RED).create());
        commandSender.sendMessage(new ComponentBuilder("First Joined Date: ").color(ChatColor.RED).append(PunisherPlugin.playerInfoConfig.getString(targetuuid + ".firstjoin")).color(ChatColor.GREEN).create());
        commandSender.sendMessage(new ComponentBuilder("Last Login: ").color(ChatColor.RED).append(lastloginString).color(ChatColor.GREEN).create());
        commandSender.sendMessage(new ComponentBuilder("Last Server Played: ").color(ChatColor.RED).append(PunisherPlugin.playerInfoConfig.getString(targetuuid + ".lastserver")).color(ChatColor.GREEN).create());
        if (PunisherPlugin.playerInfoConfig.contains(targetuuid + ".lastlogout"))
            commandSender.sendMessage(new ComponentBuilder("Last Logout: ").color(ChatColor.RED).append(lastlogoutString).color(ChatColor.GREEN).create());
    }
}
