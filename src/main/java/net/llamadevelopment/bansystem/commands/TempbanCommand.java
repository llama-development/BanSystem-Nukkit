package net.llamadevelopment.bansystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import net.llamadevelopment.bansystem.BanSystem;
import net.llamadevelopment.bansystem.components.managers.BanManager;
import net.llamadevelopment.bansystem.components.utils.BanUtil;
import net.llamadevelopment.bansystem.components.utils.MessageUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class TempbanCommand extends CommandManager {

    private BanSystem plugin;

    public TempbanCommand(BanSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Tempban"), "Temporarily ban a player.", "/tempban");
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
        if (sender.hasPermission("bansystem.command.tempban")) {
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
                        BanManager.setBanned(player, reason, getBanID(), sender.getName(), getDate(), seconds);
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.BanSuccess").replace("%player%", player).replace("&", "§"));
                        Player target = BanSystem.getInstance().getServer().getPlayer(player);
                        BanUtil banUtil = BanManager.getPlayer(player);
                        if (target != null) {
                            target.kick(MessageUtil.banScreen(banUtil.getReason(), banUtil.getId(), BanManager.getRemainingTime(banUtil.getEnd()), banUtil.getBanner()), false);
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.TimeMustNumber").replace("&", "§"));
                    }
                } else {
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.TempbanCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Tempban")));
                }
            } else {
                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.TempbanCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Tempban")));
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
