package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.bungee.chats.StaffChat;
import me.fiftyfour.punisher.systems.Permissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class KickCommand extends Command {
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public KickCommand() {
        super("kick", "punisher.kick");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (strings.length < 1) {
                player.sendMessage(new ComponentBuilder(prefix).append("Kick a player from the server").color(ChatColor.RED).append("\nUsage: /kick <player name> [custom meesage]").color(ChatColor.WHITE).create());
                return;
            }
            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(strings[0]);
            if (target == null) {
                player.sendMessage(new ComponentBuilder(prefix).append("That is not an online player's name!").color(ChatColor.RED).create());
                return;
            }
            if (!Permissions.higher(player, target.getUniqueId().toString().replace("-", ""), target.getName())){
                player.sendMessage(new ComponentBuilder(prefix).append("You cannot punish that player!").color(ChatColor.RED).create());
                return;
            }
            StringBuilder sb = new StringBuilder();
            if (strings.length == 1) {
                target.disconnect(new TextComponent(ChatColor.RED + "You have been Kicked from the server!\nYou were kicked for the reason: Manually Kicked!\nYou may reconnect at anytime, but make sure to read the /rules!"));
                StaffChat.sendMessage(player.getName() + " Kicked: " + target.getName() + " for: Manually Kicked");
            } else {
                for (int i = 1; i < strings.length; i++)
                    sb.append(strings[i]).append(" ");
                StaffChat.sendMessage(player.getName() + " Kicked: " + target.getName() + " for: " + sb.toString());
                target.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', sb.toString())));
            }
            BungeeMain.Logs.info(target.getName() + " was kicked by: " + player.getName() + " for: " + sb.toString());
        } else {
            commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
        }
    }
}