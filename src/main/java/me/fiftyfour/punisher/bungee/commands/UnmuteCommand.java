package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.PunisherPlugin;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.universal.exceptions.DataFecthException;
import me.fiftyfour.punisher.universal.exceptions.PunishmentsDatabaseException;
import me.fiftyfour.punisher.universal.util.NameFetcher;
import me.fiftyfour.punisher.universal.util.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class UnmuteCommand extends Command {
    private final PunisherPlugin plugin = PunisherPlugin.getInstance();
    private String targetuuid;
    private final PunishmentManager punishMnger = PunishmentManager.getINSTANCE();

    public UnmuteCommand() {
        super("unmute", "punisher.unmute");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length == 0) {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("Unmute a player").color(ChatColor.RED).append("\nUsage: /unmute <player name>").color(ChatColor.WHITE).create());
                return;
            }
            ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[0]);
            Future<String> future = null;
            ExecutorService executorService = null;
            if (findTarget != null) {
                targetuuid = findTarget.getUniqueId().toString().replace("-", "");
            } else {
                UUIDFetcher uuidFetcher = new UUIDFetcher();
                uuidFetcher.fetch(strings[0]);
                executorService = Executors.newSingleThreadExecutor();
                future = executorService.submit(uuidFetcher);
            }
            if (future != null) {
                try {
                    targetuuid = future.get(1, TimeUnit.SECONDS);
                } catch (Exception e) {
                    try {
                        throw new DataFecthException("UUID Required for next step", strings[0], "UUID", this.getName(), e);
                    } catch (DataFecthException dfe) {
                        ErrorHandler errorHandler = ErrorHandler.getINSTANCE();
                        errorHandler.log(dfe);
                        errorHandler.alert(dfe, commandSender);
                    }
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
                    if (punishMnger.isMuted(targetuuid)) {
                        punishMnger.remove(punishMnger.getMute(targetuuid), player, true, false, true);
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append("Successfully unmuted " + targetname).color(ChatColor.GREEN).create());
                    } else {
                        player.sendMessage(new ComponentBuilder(plugin.prefix).append(targetname + " is not currently muted!").color(ChatColor.RED).create());
                    }
                } catch (SQLException e) {
                    try {
                        throw new PunishmentsDatabaseException("Unmuting a player", targetname, this.getName(), e, "/unmute", strings);
                    } catch (PunishmentsDatabaseException pde) {
                        ErrorHandler errorHandler = ErrorHandler.getINSTANCE();
                        errorHandler.log(pde);
                        errorHandler.alert(pde, commandSender);
                    }
                }
            } else {
                player.sendMessage(new ComponentBuilder(plugin.prefix).append("That is not a player's name!").color(ChatColor.RED).create());
            }
        } else {
            if (strings.length == 0) {
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Unmute a player").color(ChatColor.RED).append("\nUsage: /unmute <player name>").color(ChatColor.WHITE).create());
                return;
            }
            ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[0]);
            Future<String> future = null;
            ExecutorService executorService = null;
            if (findTarget != null) {
                targetuuid = findTarget.getUniqueId().toString().replace("-", "");
            } else {
                UUIDFetcher uuidFetcher = new UUIDFetcher();
                uuidFetcher.fetch(strings[0]);
                executorService = Executors.newSingleThreadExecutor();
                future = executorService.submit(uuidFetcher);
            }
            if (future != null) {
                try {
                    targetuuid = future.get(1, TimeUnit.SECONDS);
                } catch (Exception e) {
                    try {
                        throw new DataFecthException("UUID Required for next step", strings[0], "UUID", this.getName(), e);
                    } catch (DataFecthException dfe) {
                        ErrorHandler errorHandler = ErrorHandler.getINSTANCE();
                        errorHandler.log(dfe);
                        errorHandler.alert(dfe, commandSender);
                    }
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
                    if (punishMnger.isMuted(targetuuid)) {
                        punishMnger.remove(punishMnger.getMute(targetuuid), null, true, false, true);
                        commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Successfully unmuted " + targetname).color(ChatColor.GREEN).create());
                    } else {
                        commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(targetname + " is not currently muted!").color(ChatColor.RED).create());
                    }
                } catch (SQLException e) {
                    try {
                        throw new PunishmentsDatabaseException("Unmuting a player", targetname, this.getName(), e, "/unmute", strings);
                    } catch (PunishmentsDatabaseException pde) {
                        ErrorHandler errorHandler = ErrorHandler.getINSTANCE();
                        errorHandler.log(pde);
                        errorHandler.alert(pde, commandSender);

                    }
                }
            } else {
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("That is not a player's name!").color(ChatColor.RED).create());
            }
        }
    }
}