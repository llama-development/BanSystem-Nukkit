package net.llamadevelopment.bansystem.components.utils;

import cn.nukkit.command.CommandSender;
import net.llamadevelopment.bansystem.BanSystem;

public class MessageUtil {

    public static String banScreen(String reason, String id, String time, String banner) {
        String str = BanSystem.getInstance().getConfig().getString("Messages.BanScreen");
        str = str.replace("%reason%", reason);
        str = str.replace("%id%", id);
        str = str.replace("%time%", time);
        str = str.replace("%banner%", banner);
        return str.replace("&", "§");
    }

    public static String warnScreen(String reason, String id, String creator) {
        String str = BanSystem.getInstance().getConfig().getString("Messages.WarnScreen");
        str = str.replace("%reason%", reason);
        str = str.replace("%id%", id);
        str = str.replace("%creator%", creator);
        return str.replace("&", "§");
    }

    public static String muteScreen(String reason, String id, String time, String banner) {
        String str = BanSystem.getInstance().getConfig().getString("Messages.MuteScreen");
        str = str.replace("%reason%", reason);
        str = str.replace("%id%", id);
        str = str.replace("%time%", time);
        str = str.replace("%banner%", banner);
        return str.replace("&", "§");
    }

    public static void sendBanHelp(CommandSender sender, BanSystem plugin) {
        int i = plugin.getConfig().getInt("BanReasons.Count");
        for (int p = 1; p <= i; p++) {
            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + convertBanHelp(String.valueOf(p), plugin.getConfig().getString("BanReasons." + p + ".Reason")));
        }
    }

    public static void sendMuteHelp(CommandSender sender, BanSystem plugin) {
        int i = plugin.getConfig().getInt("MuteReasons.Count");
        for (int p = 1; p <= i; p++) {
            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + convertMuteHelp(String.valueOf(p), plugin.getConfig().getString("MuteReasons." + p + ".Reason")));
        }
    }

    private static String convertBanHelp(String id, String reason) {
        String str = BanSystem.getInstance().getConfig().getString("Messages.BanReasonFormat");
        str = str.replace("%reason%", reason);
        str = str.replace("%id%", id);
        return str.replace("&", "§");
    }

    private static String convertMuteHelp(String id, String reason) {
        String str = BanSystem.getInstance().getConfig().getString("Messages.MuteReasonFormat");
        str = str.replace("%reason%", reason);
        str = str.replace("%id%", id);
        return str.replace("&", "§");
    }
}
