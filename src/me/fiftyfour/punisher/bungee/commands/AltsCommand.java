package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.fetchers.NameFetcher;
import me.fiftyfour.punisher.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AltsCommand extends Command {
    private BungeeMain plugin = BungeeMain.getInstance();
    private String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Punisher" + ChatColor.GRAY + "] " + ChatColor.RESET;

    public AltsCommand() {
        super("alts", "punisher.alts", "alt", "altsearch");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        try {
            if (commandSender instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) commandSender;
                if (strings.length < 2) {
                    player.sendMessage(new ComponentBuilder(prefix).append("Check a players alt or reset their stored ip").color(ChatColor.RED).append("\nUsage: /alts <reset|get> <player>").color(ChatColor.WHITE).create());
                    return;
                }
                String targetuuid = UUIDFetcher.getUUID(strings[1]);
                String targetname = NameFetcher.getName(targetuuid);
                if (targetname.equals("null")) {
                    targetname = strings[1];
                }
                if (strings[0].equalsIgnoreCase("reset") && player.hasPermission("punisher.alts.reset")) {
                    String sql = "SELECT * FROM `iplist` WHERE UUID='" + targetuuid + "'";
                    PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                    ResultSet results = stmt.executeQuery();
                    if (results.next()) {
                        String sql1 = "DELETE FROM `iplist` WHERE `UUID`='" + targetuuid + "' ;";
                        PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                        stmt1.executeUpdate();
                        player.sendMessage(new ComponentBuilder(prefix).append(targetname + "'s stored ip address has been reset!").color(ChatColor.RED).create());
                        BungeeMain.Logs.info(player.getName() + " reset " + targetname + "'s stored ip address");
                        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetuuid);
                        if (target != null) {
                            String sql2 = "INSERT INTO `iplist` (`UUID`, `ip`) VALUES ('" + targetuuid + "', '" + target.getAddress().getHostString() + "');";
                            PreparedStatement stmt2 = plugin.connection.prepareStatement(sql2);
                            stmt2.executeUpdate();
                        }
                    } else {
                        player.sendMessage(new ComponentBuilder(prefix).append("That player has no stored ip address!").color(ChatColor.RED).create());
                    }
                } else if (strings[0].equals("get")) {
                    if (strings[1].contains(".") && player.hasPermission("punisher.alts.ip")){
                        String ip = strings[1];
                        StringBuilder altslist = new StringBuilder();
                        String sql = "SELECT * FROM `iplist` WHERE ip='" + ip + "'";
                        PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                        ResultSet results = stmt.executeQuery();
                        while(results.next()) {
                            String concacc = NameFetcher.getName(results.getString("uuid"));
                            altslist.append(ChatColor.RED).append(concacc).append(" ");
                        }
                        if (altslist.toString().isEmpty()){
                            player.sendMessage(new ComponentBuilder(prefix).append("That ip is not stored in our database!").color(ChatColor.RED).create());
                            return;
                        }
                        player.sendMessage(new ComponentBuilder(prefix).append("Accounts connected to the ip: " + ip + ": ").color(ChatColor.RED).create());
                        player.sendMessage(new ComponentBuilder(prefix).append(altslist.toString()).color(ChatColor.RED).create());
                        BungeeMain.Logs.info(player.getName() + " Looked at accounts connected to ip: " + ip + " Accounts connected at time of logging: " + altslist.toString());
                    }else {
                        if (targetuuid.equals("null")){
                            player.sendMessage(new ComponentBuilder(prefix).append("That is not a player's name!").color(ChatColor.RED).create());
                            return;
                        }
                        StringBuilder altslist = new StringBuilder();
                        String sql = "SELECT * FROM `iplist` WHERE UUID='" + targetuuid + "'";
                        PreparedStatement stmt = plugin.connection.prepareStatement(sql);
                        ResultSet results = stmt.executeQuery();
                        if (results.next()) {
                            String ip = results.getString("ip");
                            String sql1 = "SELECT * FROM `iplist` WHERE ip='" + ip + "'";
                            PreparedStatement stmt1 = plugin.connection.prepareStatement(sql1);
                            ResultSet results1 = stmt1.executeQuery();
                            while (results1.next()) {
                                String concacc = NameFetcher.getName(results1.getString("uuid"));
                                if (!concacc.equals(targetname)) {
                                    altslist.append(ChatColor.RED).append(concacc).append(" ");
                                }
                            }
                            if (altslist.toString().isEmpty()) {
                                player.sendMessage(new ComponentBuilder(prefix).append("That player has no connected accounts!").color(ChatColor.RED).create());
//                                return;
                            }
                            if (player.hasPermission("punisher.alts.ip")) {
                                player.sendMessage(new ComponentBuilder(prefix).append("Accounts connected to " + targetname + " on the ip " + ip + ": ").color(ChatColor.RED).create());
                            } else {
                                player.sendMessage(new ComponentBuilder(prefix).append("Accounts connected to " + targetname + ": ").color(ChatColor.RED).create());
                            }
                            player.sendMessage(new ComponentBuilder(prefix).append(altslist.toString()).color(ChatColor.RED).create());
                            TextComponent status;
                            Long mutetime;
                            Long bantime;
                            String sql3 = "SELECT * FROM `mutes` WHERE UUID='" + targetuuid + "'";
                            PreparedStatement stmt3 = plugin.connection.prepareStatement(sql3);
                            ResultSet results3 = stmt3.executeQuery();
                            String sql4 = "SELECT * FROM `bans` WHERE UUID='" + targetuuid + "'";
                            PreparedStatement stmt4 = plugin.connection.prepareStatement(sql4);
                            ResultSet results4 = stmt4.executeQuery();
                            if (results4.next()) {
                                bantime = results4.getLong("Length");
                                Long banleftmillis = bantime - System.currentTimeMillis();
                                long daysleft = banleftmillis / (1000 * 60 * 60 * 24);
                                long hoursleft = (long) Math.floor(banleftmillis / (1000 * 60 * 60) % 24);
                                long minutesleft = (long) Math.floor(banleftmillis / (1000 * 60) % 60);
                                long secondsleft = (long) Math.floor(banleftmillis / 1000 % 60);
                                String reason = results4.getString("Reason");
                                String punisher = results4.getString("Punisher");
                                status = new TextComponent("banned for " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s. Reason: " + reason + " by: " + punisher);
                                status.setColor(ChatColor.RED);
                            } else if (results3.next()) {
                                mutetime = results3.getLong("Length");
                                Long muteleftmillis = mutetime - System.currentTimeMillis();
                                long daysleft = muteleftmillis / (1000 * 60 * 60 * 24);
                                long hoursleft = (long) Math.floor(muteleftmillis / (1000 * 60 * 60) % 24);
                                long minutesleft = (long) Math.floor(muteleftmillis / (1000 * 60) % 60);
                                long secondsleft = (long) Math.floor(muteleftmillis / 1000 % 60);
                                String reason = results3.getString("Reason");
                                String punisher = results3.getString("Punisher");
                                status = new TextComponent("Muted for " + daysleft + "d " + hoursleft + "h " + minutesleft + "m " + secondsleft + "s. Reason: " + reason + " by: " + punisher);
                                status.setColor(ChatColor.RED);
                            } else {
                                status = new TextComponent("No currently active punishments!");
                                status.setColor(ChatColor.GREEN);
                            }
                            player.sendMessage(new ComponentBuilder(prefix).append("Current Status: ").color(ChatColor.RED).append(status).create());
                            BungeeMain.Logs.info(player.getName() + " looked at " + targetname + "'s connected accounts, at the time of logging they were: " + altslist.toString());
                        } else {
                            player.sendMessage(new ComponentBuilder(prefix).append("That player has no connected accounts!").color(ChatColor.RED).create());
                        }
                    }
                }else{
                    player.sendMessage(new ComponentBuilder(prefix).append("Check a players alt or reset their stored ip").color(ChatColor.RED).append("\nUsage: /alts <reset|get> <player>").color(ChatColor.WHITE).create());
                }
            } else {
                commandSender.sendMessage(new TextComponent("You must be a player to use this command!"));
            }
        }catch (SQLException e){
            plugin.mysqlfail(e);
            if (plugin.testConnectionManual())
                this.execute(commandSender, strings);
        }
    }
}
