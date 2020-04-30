package net.llamadevelopment.bansystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import net.llamadevelopment.bansystem.BanSystem;
import net.llamadevelopment.bansystem.components.managers.BanManager;
import net.llamadevelopment.bansystem.components.managers.MuteManager;
import net.llamadevelopment.bansystem.components.utils.BanUtil;
import net.llamadevelopment.bansystem.components.utils.MessageUtil;
import net.llamadevelopment.bansystem.components.utils.MuteUtil;
import net.llamadevelopment.bansystem.components.utils.MutedPlayer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class TempmuteCommand extends CommandManager {

    private BanSystem plugin;

    public TempmuteCommand(BanSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Tempmute"), "Temporarily mute a player.", "/tempmute");
        this.plugin = plugin;
        
        // command params
        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false),
                new CommandParameter("type", false, new String[] {"days", "hours"}),
                new CommandParameter("time", CommandParamType.INT, false),
                new CommandParameter("reason", CommandParamType.TEXT, false)
        });
    }

    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender.hasPermission("bansystem.command.tempmute")) {
            if (args.length >= 4) {
                String player = args[0];
                if (args[1].equalsIgnoreCase("days") || args[1].equalsIgnoreCase("hours")) {
                    String timeUnit = args[1];
                    try {
                        int time = Integer.parseInt(args[2]);
                        int seconds = 0;
                        String reason = "";
                        for (int i = 3; i < args.length; ++i) reason = reason + args[i] + " ";
                        if (timeUnit.equalsIgnoreCase("days")) seconds = time * 86400;
                        if (timeUnit.equalsIgnoreCase("hours")) seconds = time * 3600;
                        MuteManager.setMuted(player, reason, getBanID(), sender.getName(), getDate(), seconds);
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.MuteSuccess").replace("%player%", player).replace("&", "§"));
                        MuteUtil muteUtil = MuteManager.getPlayer(player);
                        long end = muteUtil.getEnd();
                        String reason1 = muteUtil.getReason();
                        String id = muteUtil.getId();
                        String banner = muteUtil.getBanner();
                        MutedPlayer mutedPlayer = new MutedPlayer(end, reason1, id, banner);
                        Player player1 = BanSystem.getInstance().getServer().getPlayer(player);
                        if (player1 != null) BanSystem.getInstance().mutedCache.put(player1, mutedPlayer);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.TimeMustNumber").replace("&", "§"));
                    }
                } else {
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.TempmuteCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Tempmute")));
                }
            } else {
                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.TempmuteCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Tempmute")));
            }
        } else {
            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.NoPermission").replace("&", "§"));
        }
        return false;
    }

    private String getBanID() {
        String string = "";
        int lastrandom = 0;
        for (int i = 0; i < 6; i++) {
            Random random = new Random();
            int rand = random.nextInt(9);
            while (rand == lastrandom) {
                rand = random.nextInt(9);
            }
            lastrandom = rand;
            string = string + rand;
        }
        return string;
    }

    private String getDate() {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
        String now1 = dateFormat.format(now);
        return now1;
    }
}
