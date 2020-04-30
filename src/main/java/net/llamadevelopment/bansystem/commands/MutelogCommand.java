package net.llamadevelopment.bansystem.commands;

import cn.nukkit.command.CommandSender;
import net.llamadevelopment.bansystem.BanSystem;
import net.llamadevelopment.bansystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.bansystem.components.managers.database.MySqlProvider;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MutelogCommand extends CommandManager {

    private BanSystem plugin;

    public MutelogCommand(BanSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Mutelog"), "See the history of a player.", "/mutelog");
        this.plugin = plugin;
        
        // command params
        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false)
        });
    }


    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender.hasPermission("bansystem.command.mutelog")) {
            if (args.length == 1) {
                int a = 1;
                if (plugin.isMongodb()) {
                    for (Document doc : MongoDBProvider.getMutelogCollection().find(new Document("player", args[0]))) {
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Mutelog.Info").replace("&", "§").replace("%count%", String.valueOf(a)));
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Mutelog.Player").replace("&", "§").replace("%player%", doc.getString("player")));
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Mutelog.Reason").replace("&", "§").replace("%reason%", doc.getString("reason")));
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Mutelog.ID").replace("&", "§").replace("%id%", doc.getString("id")));
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Mutelog.Muter").replace("&", "§").replace("%muter%", doc.getString("banner")));
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Mutelog.Date").replace("&", "§").replace("%date%", doc.getString("date")));
                        a++;
                    }
                } else if (plugin.isMysql()) {
                    try {
                        PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM mutelogs WHERE PLAYER = ?");
                        preparedStatement.setString(1, args[0]);
                        ResultSet rs = preparedStatement.executeQuery();
                        while (rs.next()) {
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Mutelog.Info").replace("&", "§").replace("%count%", String.valueOf(a)));
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Mutelog.Player").replace("&", "§").replace("%player%", rs.getString("PLAYER")));
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Mutelog.Reason").replace("&", "§").replace("%reason%", rs.getString("REASON")));
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Mutelog.ID").replace("&", "§").replace("%id%", rs.getString("ID")));
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Mutelog.Muter").replace("&", "§").replace("%muter%", rs.getString("BANNER")));
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Mutelog.Date").replace("&", "§").replace("%date%", rs.getString("DATE")));
                            a++;
                        }
                        rs.close();
                        preparedStatement.close();
                    } catch (Exception ignored) {
                    }
                }
                if (a == 1) sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.NoDataFound").replace("&", "§"));
            } else {
                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.MutelogCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Mutelog")));
            }
        } else {
            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.NoPermission").replace("&", "§"));
        }
        return false;
    }
}
