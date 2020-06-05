package net.llamadevelopment.bansystem.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import net.llamadevelopment.bansystem.Configuration;
import net.llamadevelopment.bansystem.components.api.BanSystemAPI;
import net.llamadevelopment.bansystem.components.managers.database.Provider;

import java.util.concurrent.CompletableFuture;

public class EditbanCommand extends Command {

    public EditbanCommand(String name) {
        super(name, "Edit the ban of a player.");
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false),
                new CommandParameter("editType", false, new String[] {"reason", "time"})
        });
        setPermission("bansystem.command.editban");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        Provider api = BanSystemAPI.getProvider();
        if (sender.hasPermission(getPermission())) {
            if (args.length >= 3) {
                String player = args[0];
                if (args[1].equalsIgnoreCase("reason")) {
                    CompletableFuture.runAsync(() -> {
                        if (api.playerIsBanned(player)) {
                            String reason = "";
                            for (int i = 2; i < args.length; ++i) reason = reason + args[i] + " ";
                            api.setBanReason(player, reason);
                            sender.sendMessage(Configuration.getAndReplace("ReasonSet"));
                        } else sender.sendMessage(Configuration.getAndReplace("PlayerIsNotBanned"));
                    });
                } else if (args.length == 4 && args[1].equalsIgnoreCase("time")) {
                    CompletableFuture.runAsync(() -> {
                        if (api.playerIsBanned(player)) {
                            try {
                                String type = args[2];
                                int time = Integer.parseInt(args[3]);
                                int seconds = 0;
                                if (type.equalsIgnoreCase("days")) seconds = time * 86400;
                                else if (type.equalsIgnoreCase("hours")) seconds = time * 3600;
                                else {
                                    sender.sendMessage(Configuration.getAndReplace("EditbanCommandUsage1", getName()));
                                    sender.sendMessage(Configuration.getAndReplace("EditbanCommandUsage2", getName()));
                                    return;
                                }
                                long end = System.currentTimeMillis() + seconds * 1000L;
                                api.setBanTime(player, end);
                                sender.sendMessage(Configuration.getAndReplace("TimeSet"));
                            } catch (NumberFormatException exception) {
                                sender.sendMessage(Configuration.getAndReplace("InvalidNumber"));
                            }
                        } else sender.sendMessage(Configuration.getAndReplace("PlayerIsNotBanned"));
                    });
                } else {
                    sender.sendMessage(Configuration.getAndReplace("EditbanCommandUsage1", getName()));
                    sender.sendMessage(Configuration.getAndReplace("EditbanCommandUsage2", getName()));
                }
            } else {
                sender.sendMessage(Configuration.getAndReplace("EditbanCommandUsage1", getName()));
                sender.sendMessage(Configuration.getAndReplace("EditbanCommandUsage2", getName()));
            }
        } else sender.sendMessage(Configuration.getAndReplace("NoPermission"));
        return false;
    }
}
