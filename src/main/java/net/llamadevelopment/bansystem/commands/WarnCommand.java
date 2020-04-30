package net.llamadevelopment.bansystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import net.llamadevelopment.bansystem.BanSystem;
import net.llamadevelopment.bansystem.components.managers.WarnManager;
import net.llamadevelopment.bansystem.components.utils.MessageUtil;

import java.util.Random;

public class WarnCommand extends CommandManager {

    private BanSystem plugin;

    public WarnCommand(BanSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Warn"), "Warn a player.", "/warn");
        this.plugin = plugin;
        
        // command params
        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false),
                new CommandParameter("reason", CommandParamType.TEXT, false)
        });
    }

    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender.hasPermission("bansystem.command.warn")) {
            if (args.length >= 2) {
                Player player = plugin.getServer().getPlayer(args[0]);
                String reason = "";
                for (int i = 1; i < args.length; ++i) reason = reason + args[i] + " ";
                String kickMessage = MessageUtil.warnScreen(reason, getID(), sender.getName());
                WarnManager.createWarning(args[0], reason, sender.getName());
                WarnManager.updatePlayer(args[0]);
                if (player != null) {
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.WarnSuccess").replace("%player%", args[0]).replace("&", "§"));
                    player.kick(kickMessage, false);
                } else {
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.PlayerNotOnline").replace("&", "§"));
                }
            } else {
                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.WarnCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Warn")));
            }
        } else {
            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.NoPermission").replace("&", "§"));
        }
        return false;
    }

    private String getID() {
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
}
