package me.fiftyfour.punisher.bungee.discordbot;

import me.fiftyfour.punisher.bungee.PunisherPlugin;
import me.fiftyfour.punisher.bungee.discordbot.commands.DiscordCommand;
import me.fiftyfour.punisher.bungee.discordbot.listeners.PlayerDisconnect;
import me.fiftyfour.punisher.bungee.discordbot.listeners.ServerConnect;
import me.fiftyfour.punisher.bungee.discordbot.listeners.ServerConnected;
import me.fiftyfour.punisher.bungee.discordbot.listeners.discord.BotReady;
import me.fiftyfour.punisher.bungee.discordbot.listeners.discord.PrivateMessageReceived;
import me.fiftyfour.punisher.bungee.managers.PunishmentManager;
import me.fiftyfour.punisher.bungee.managers.WorkerManager;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.yaml.snakeyaml.Yaml;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DiscordMain {

    private static final PunisherPlugin plugin = PunisherPlugin.getInstance();
    public static JDA jda;
    public static Map<UUID, String> userCodes = new HashMap<>();
    public static Map<UUID, String> verifiedUsers = new HashMap<>();
    private static final Yaml YAML_LOADER = new Yaml();
    public static List<ScheduledTask> updateTasks = new ArrayList<>();
    private static boolean firstenable = true;
    private static final PunishmentManager punishMngr = PunishmentManager.getINSTANCE();
    public static Guild guild;

    public static void startBot() {
        plugin.getLogger().info(plugin.prefix + ChatColor.GREEN + "Starting Discord bot...");
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(PunisherPlugin.config.getString("DiscordIntegration.BotToken")).build();
            jda.addEventListener(new BotReady());
            jda.addEventListener(new PrivateMessageReceived());
            if (firstenable) {
                ProxyServer.getInstance().getPluginManager().registerCommand(plugin, new DiscordCommand());
                firstenable = false;
            }
            guild = jda.getGuildById(PunisherPlugin.config.getString("DiscordIntegration.GuildId"));
            if (PunisherPlugin.config.getBoolean("DiscordIntegration.EnableJoinLogging")) {
                ProxyServer.getInstance().getPluginManager().registerListener(plugin, new ServerConnected());
                ProxyServer.getInstance().getPluginManager().registerListener(plugin, new PlayerDisconnect());
                updateTasks.add(ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                    TextChannel loggingChannel = DiscordMain.jda.getTextChannelById(PunisherPlugin.config.getString("DiscordIntegration.JoinLoggingChannelId"));
                    if (loggingChannel == null)
                        throw new NullPointerException("Could not find logging channel!");
                    loggingChannel.getManager().setTopic(ProxyServer.getInstance().getPlayers().size() + " players online | "
                            + me.fiftyfour.punisher.bungee.listeners.ServerConnect.lastJoinId + " unique players ever joined").queue();
                }, 10, 5, TimeUnit.SECONDS));
            }
            if (PunisherPlugin.config.getBoolean("DiscordIntegration.EnableRoleSync")) {
                updateTasks.add(ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                            WorkerManager.getINSTANCE().runWorker(new WorkerManager.Worker(() -> {
                                new HashMap<>(verifiedUsers).forEach((uuid, id) -> {//clean up old users that have deleted their accounts or have left the guild
                                    User user = jda.getUserById(id);
                                    if (user == null)
                                        verifiedUsers.remove(uuid);
                                    else if (!guild.isMember(user))
                                        verifiedUsers.remove(uuid);
                                });
                                verifiedUsers.forEach((uuid, id) -> {//sync linked user roles over to the discord
                                    for (String roleids : PunisherPlugin.config.getStringList("DiscordIntegration.RolesIdsToAddToLinkedUser")) {
                                        guild.addRoleToMember(guild.getMember(jda.getUserById(id)), guild.getRoleById(roleids)).queue();
                                    }
                                });
                                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {//sync user's synced roles over to the discord
                                    if (verifiedUsers.containsKey(player.getUniqueId())) {
                                        User user = jda.getUserById(verifiedUsers.get(player.getUniqueId()));
                                        for (String roleids : PunisherPlugin.config.getStringList("DiscordIntegration.RolesToSync")) {
                                            guild.getMember(user).getRoles().forEach((role -> {
                                                if (roleids.equals(role.getId()) && !player.hasPermission("punisher.discord.role." + roleids))
                                                    guild.removeRoleFromMember(guild.getMember(user), role).queue();
                                            }));
                                            if (player.hasPermission("punisher.discord.role." + roleids) && guild.getRoleById(roleids) != null)
                                                guild.addRoleToMember(guild.getMember(user), guild.getRoleById(roleids)).queue();
                                        }
                                    }
                                }
                            }));
                        }
                        , 10, 30, TimeUnit.SECONDS));
            }
            if (PunisherPlugin.config.getBoolean("DiscordIntegration.EnableRoleSync") || PunisherPlugin.config.getBoolean("DiscordIntegration.EnableJoinLogging")) {
                ProxyServer.getInstance().getPluginManager().registerListener(plugin, new ServerConnect());
            }
            try {
                Object obj = YAML_LOADER.load(new FileInputStream(PunisherPlugin.discordIntegrationFile));
                if (obj instanceof HashMap)
                    verifiedUsers = (HashMap<UUID, String>) obj;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            plugin.getLogger().info(plugin.prefix + ChatColor.GREEN + "Discord bot Started!");
        } catch (LoginException e) {
            plugin.getLogger().severe(plugin.prefix + ChatColor.RED + "Could not start Discord bot!");
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        if (jda != null) {
            plugin.getLogger().info(plugin.prefix + ChatColor.GREEN + "Shutting down Discord bot...");
            try {
                YAML_LOADER.dump(verifiedUsers, new FileWriter(PunisherPlugin.discordIntegrationFile));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            jda.shutdownNow();
            jda = null;
            ProxyServer.getInstance().getPluginManager().unregisterListener(new ServerConnected());
            ProxyServer.getInstance().getPluginManager().unregisterListener(new PlayerDisconnect());
            ProxyServer.getInstance().getPluginManager().unregisterListener(new ServerConnect());
            for (ScheduledTask task : updateTasks)
                task.cancel();
            plugin.getLogger().info(plugin.prefix + ChatColor.GREEN + "Discord bot Shut down!");
        }
    }
}
