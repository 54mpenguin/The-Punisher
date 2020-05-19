package me.fiftyfour.punisher.bungee.chats;

import me.fiftyfour.punisher.universal.exceptions.DataFecthException;
import me.fiftyfour.punisher.bungee.handlers.ErrorHandler;
import me.fiftyfour.punisher.universal.fetchers.UserFetcher;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.MetaData;
import me.lucko.luckperms.api.context.ContextManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class StaffChat extends Command {

    public StaffChat() {
        super("sc", "punisher.staffchat", "staffchat");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length == 0) {
                player.sendMessage(new ComponentBuilder("Send a message to all staff members").color(ChatColor.RED).append("\nUsage: /staffchat <message>").color(ChatColor.WHITE).create());
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (String arg : strings){
                sb.append(arg).append(" ");
            }
            UUID uuid = player.getUniqueId();
            User user = LuckPerms.getApi().getUser(player.getName());
            if (user == null) {
                UserFetcher userFetcher = new UserFetcher();
                userFetcher.setUuid(uuid);
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Future<User> userFuture = executorService.submit(userFetcher);
                try {
                    user = userFuture.get(5, TimeUnit.SECONDS);
                }catch (Exception e){
                    try {
                        throw new DataFecthException("User prefix required for chat message to avoid issues the prefix was set to \"\"", player.getName(), "User Instance", StaffChat.class.getName(), e);
                    }catch (DataFecthException dfe){
                        ErrorHandler errorHandler = ErrorHandler.getInstance();
                        errorHandler.log(dfe);
                        errorHandler.alert(dfe, commandSender);
                    }
                    user = null;
                }
                executorService.shutdown();
            }
            String prefix;
            if (user != null) {
                ContextManager cm = LuckPerms.getApi().getContextManager();
                Contexts contexts = cm.lookupApplicableContexts(user).orElse(cm.getStaticContexts());
                MetaData metaData = user.getCachedData().getMetaData(contexts);
                prefix = metaData.getPrefix();
                if (prefix == null) {
                    prefix = "";
                }
            }else
                prefix = "";
            BaseComponent[] messagetosend = new ComponentBuilder("[").color(ChatColor.DARK_GRAY).append("SC").color(ChatColor.RED).bold(true).append("]").color(ChatColor.DARK_GRAY).bold(false)
                    .append(" ").color(ChatColor.RESET).append(ChatColor.translateAlternateColorCodes('&', prefix + " ")).bold(false).append(player.getName() + ": " + sb)
                    .color(ChatColor.RED).bold(false).create();
            for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
                if (all.hasPermission("punisher.staffchat")) {
                    all.sendMessage(messagetosend);
                }
            }
        } else {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
        }
    }
    public static void sendMessage(String message){
        BaseComponent[] messagetosend = new ComponentBuilder("[").color(ChatColor.DARK_GRAY).append("SC").color(ChatColor.RED).bold(true).append("]").color(ChatColor.DARK_GRAY).bold(false)
                .append(" ").color(ChatColor.RESET).bold(false).append(message).color(ChatColor.RED).bold(false).create();
        for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
            if (all.hasPermission("punisher.staffchat")) {
                all.sendMessage(messagetosend);
            }
        }
    }
}