package me.fiftyfour.punisher.universal.util;

import me.fiftyfour.punisher.bukkit.PunisherBukkit;
import me.fiftyfour.punisher.bungee.PunisherPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class UpdateChecker {

    private static String versionString = callURL("https://api.54mpenguin.com/the-punisher/version/");

    public static String getCurrentVersion(){
        StringBuilder result = new StringBuilder();
        readData(versionString, result, "current-version-number");
        if (result.toString().equals("null")){
            return null;
        }else
        return result.toString();
    }

    public static boolean check() throws Exception{
        if (getCurrentVersion() == null)
            return false;
        try {
            Class.forName("net.md_5.bungee.BungeeCord");
            PunisherPlugin plugin = PunisherPlugin.getInstance();
            if (!getCurrentVersion().equals(plugin.getDescription().getVersion())) {
                String[] thisParts = plugin.getDescription().getVersion().split("\\.");
                String[] thatParts = getCurrentVersion().split("\\.");
                int length = Math.max(thisParts.length, thatParts.length);
                for (int i = 0; i < length; i++) {
                    int thisPart = i < thisParts.length ?
                            Integer.parseInt(thisParts[i]) : 0;
                    int thatPart = i < thatParts.length ?
                            Integer.parseInt(thatParts[i]) : 0;
                    if (thisPart < thatPart)
                        return true;
                    if (thisPart > thatPart) {
                        throw new Exception("Your version number is newer than the version on the website, either you have a dev version or the website api has not been updated!");
                    }
                }
                return false;
            }else return false;
        }catch (ClassNotFoundException CNFE){
            PunisherBukkit plugin = PunisherBukkit.getInstance();
            if (!getCurrentVersion().equals(plugin.getDescription().getVersion())) {
                String[] thisParts = plugin.getDescription().getVersion().split("\\.");
                String[] thatParts = getCurrentVersion().split("\\.");
                int length = Math.max(thisParts.length, thatParts.length);
                for (int i = 0; i < length; i++) {
                    int thisPart = i < thisParts.length ?
                            Integer.parseInt(thisParts[i]) : 0;
                    int thatPart = i < thatParts.length ?
                            Integer.parseInt(thatParts[i]) : 0;
                    if (thisPart < thatPart)
                        return true;
                    if (thisPart > thatPart) {
                        throw new Exception("Your version number is newer than the version on the website, either you have a dev version or the website api has not been updated!");
                    }
                }
                return false;
            }else return false;
        }
    }

    public static String getRealeaseDate(){
        StringBuilder result = new StringBuilder();
        readData(versionString, result, "last-update");
        if (result.toString().equals("null")){
            return null;
        }else
        return result.toString();
    }

    private static void readData(String toRead, StringBuilder result, String lookingFor) {
        String lookFor = lookingFor + ":\"";
        if (toRead == null) {
            result.append("null");
            return;
        }
        int i = toRead.indexOf(lookFor) + lookFor.length();
        while (i < 200) {
            if (toRead.length() == 0) {
                result.append("null");
                break;
            }
            if (!String.valueOf(toRead.charAt(i)).equalsIgnoreCase("\"")) {
                result.append(String.valueOf(toRead.charAt(i)));
            } else {
                break;
            }
            i++;
        }
    }

    private static String callURL(String URL) {
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn;
        InputStreamReader in = null;
        try {
            urlConn = new URL(URL).openConnection();
            urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            urlConn.connect();
            urlConn.setReadTimeout(60 * 1000);
            if (urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                if (bufferedReader != null) {
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    bufferedReader.close();
                }
            }
            if (in != null)
                in.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
