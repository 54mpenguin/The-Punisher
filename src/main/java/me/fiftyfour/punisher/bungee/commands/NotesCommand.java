package me.fiftyfour.punisher.bungee.commands;

import me.fiftyfour.punisher.bungee.BungeeMain;
import me.fiftyfour.punisher.universal.fetchers.NameFetcher;
import me.fiftyfour.punisher.universal.fetchers.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class NotesCommand extends Command {
    public NotesCommand() {
        super("notes", "punisher.notes", "playernotes", "playernote", "note");
    }

    private String targetuuid;
    private BungeeMain plugin = BungeeMain.getInstance();

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length < 2){
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Add, remove or list a player's notes").color(ChatColor.RED).create());
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /notes <add|remove|list> <player>").color(ChatColor.WHITE).create());
            return;
        }
        ProxiedPlayer findTarget = ProxyServer.getInstance().getPlayer(strings[1]);
        Future<String> future = null;
        ExecutorService executorService = null;
        if (findTarget != null) {
            targetuuid = findTarget.getUniqueId().toString().replace("-", "");
        } else {
            UUIDFetcher uuidFetcher = new UUIDFetcher();
            uuidFetcher.fetch(strings[1]);
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
        if (targetuuid == null) {
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(strings[0] + " is not a player's name!").color(ChatColor.RED).create());
            return;
        }
        String targetname = NameFetcher.getName(targetuuid);
        if (targetname == null) {
            targetname = strings[1];
        }
        if (strings[0].equalsIgnoreCase("add")){
            if (strings.length < 3){
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Add a note to a player").color(ChatColor.RED).create());
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /notes add <player> <note to add>").color(ChatColor.WHITE).create());
                return;
            }
            StringBuilder notes = new StringBuilder();
            notes.append("\"");
            for (int i = 2; i < strings.length; i ++){
                notes.append(strings[i]).append(" ");
            }
            if (commandSender instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) commandSender;
                notes.append("\" - ").append(player.getName());
            }else {
                notes.append("\" - CONSOLE");
            }
            if (BungeeMain.InfoConfig.contains(targetuuid + ".notes")){
                List<String> previousNotes = BungeeMain.InfoConfig.getStringList(targetuuid + ".notes");
                previousNotes.add(notes.toString());
                BungeeMain.InfoConfig.set(targetuuid + ".notes", previousNotes);
            }else {
                List<String> notesList = new ArrayList<>();
                notesList.add(notes.toString());
                BungeeMain.InfoConfig.set(targetuuid + ".notes", notesList);
            }
            BungeeMain.saveInfo();
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Note Added to " + targetname).color(ChatColor.GREEN).create());
        }else if (strings[0].equalsIgnoreCase("remove")){
            if (strings.length < 3){
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Remove a player's note by id").color(ChatColor.RED).create());
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Usage: /notes remove <player> <id>").color(ChatColor.WHITE).create());
                return;
            }
            int id = 1;
            try {
                id = Integer.parseInt(strings[2]);
            }catch (NumberFormatException nfe){
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(strings[2] + " is not a valid number").color(ChatColor.RED).create());
                return;
            }
            if (!BungeeMain.InfoConfig.contains(targetuuid + ".notes")){
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(targetname + " Does not have any notes!").color(ChatColor.RED).create());
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Add one by doing: /notes add <player> <note>").color(ChatColor.WHITE).create());
                return;
            }
            List<String> previousNotes = BungeeMain.InfoConfig.getStringList(targetuuid + ".notes");
            previousNotes.remove(id - 1);
            BungeeMain.InfoConfig.set(targetuuid + ".notes", previousNotes);
            BungeeMain.saveInfo();
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Note with id " + id + " removed from " + targetname).color(ChatColor.GREEN).create());
        }else if (strings[0].equalsIgnoreCase("list")){
            if (!BungeeMain.InfoConfig.contains(targetuuid + ".notes")){
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append(targetname + " Does not have any notes!").color(ChatColor.RED).create());
                commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Add one by doing: /notes add <player> <note>").color(ChatColor.WHITE).create());
                return;
            }
            List<String> previousNotes = BungeeMain.InfoConfig.getStringList(targetuuid + ".notes");
            commandSender.sendMessage(new ComponentBuilder(plugin.prefix).append("Notes for " + targetname + ":").color(ChatColor.GREEN).create());
            for(String note : previousNotes){
                commandSender.sendMessage(new ComponentBuilder(previousNotes.indexOf(note) + ". ").color(ChatColor.GREEN).append(note).color(ChatColor.GREEN).create());
            }
        }
    }
}
