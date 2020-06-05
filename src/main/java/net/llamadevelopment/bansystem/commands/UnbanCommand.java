package net.llamadevelopment.bansystem.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import net.llamadevelopment.bansystem.Configuration;
import net.llamadevelopment.bansystem.components.api.BanSystemAPI;
import net.llamadevelopment.bansystem.components.managers.database.Provider;

public class UnbanCommand extends Command {

    public UnbanCommand(String name) {
        super(name, "Cancel an active ban.");
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false)
        });
        setPermission("bansystem.command.unban");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        Provider api = BanSystemAPI.getProvider();
        if (sender.hasPermission(getPermission())) {
            if (args.length == 1) {
                String player = args[0];
                if (api.playerIsBanned(player)) {
                    api.unbanPlayer(player);
                    sender.sendMessage(Configuration.getAndReplace("PlayerUnbanned", player));
                } else sender.sendMessage(Configuration.getAndReplace("PlayerIsNotBanned"));
            } else sender.sendMessage(Configuration.getAndReplace("UnbanCommandUsage", getName()));
        } else sender.sendMessage(Configuration.getAndReplace("NoPermission"));
        return false;
    }
}
