package net.llamadevelopment.bansystem.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import net.llamadevelopment.bansystem.Configuration;
import net.llamadevelopment.bansystem.components.api.BanSystemAPI;
import net.llamadevelopment.bansystem.components.data.Ban;
import net.llamadevelopment.bansystem.components.managers.database.Provider;

import java.util.concurrent.CompletableFuture;

public class CheckbanCommand extends Command {

    public CheckbanCommand(String name) {
        super(name, "Check if a player is banned.");
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false)
        });
        setPermission("bansystem.command.checkban");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        Provider api = BanSystemAPI.getProvider();
        if (sender.hasPermission(getPermission())) {
            if (args.length == 1) {
                String player = args[0];
                CompletableFuture.runAsync(() -> {
                    if (api.playerIsBanned(player)) {
                        Ban ban = api.getBan(player);
                        sender.sendMessage(Configuration.getAndReplace("CheckbanInfo", player));
                        sender.sendMessage(Configuration.getAndReplace("CheckbanReason", ban.getReason()));
                        sender.sendMessage(Configuration.getAndReplace("CheckbanID", ban.getBanID()));
                        sender.sendMessage(Configuration.getAndReplace("CheckbanBanner", ban.getBanner()));
                        sender.sendMessage(Configuration.getAndReplace("CheckbanDate", ban.getDate()));
                        sender.sendMessage(Configuration.getAndReplace("CheckbanTime", api.getRemainingTime(ban.getTime())));
                    } else sender.sendMessage(Configuration.getAndReplace("PlayerNotBanned"));
                });
            } else sender.sendMessage(Configuration.getAndReplace("CheckbanCommandUsage", getName()));
        } else sender.sendMessage(Configuration.getAndReplace("NoPermission"));
        return false;
    }
}
