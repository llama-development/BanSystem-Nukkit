package net.llamadevelopment.bansystem.commands;

import cn.nukkit.command.CommandSender;
import net.llamadevelopment.bansystem.BanSystem;
import net.llamadevelopment.bansystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.bansystem.components.managers.database.MySqlProvider;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BanlogCommand extends CommandManager {

    private BanSystem plugin;

    public BanlogCommand(BanSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Banlog"), "See the history of a player.", "/banlog");
        this.plugin = plugin;
        
        // command parameters
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false)
        });
    }


    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender.hasPermission("bansystem.command.banlog")) {
            if (args.length == 1) {
                int a = 1;
                if (plugin.isMongodb()) {
                    for (Document doc : MongoDBProvider.getBanlogCollection().find(new Document("player", args[0]))) {
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Banlog.Info").replace("&", "§").replace("%count%", String.valueOf(a)));
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Banlog.Player").replace("&", "§").replace("%player%", doc.getString("player")));
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Banlog.Reason").replace("&", "§").replace("%reason%", doc.getString("reason")));
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Banlog.ID").replace("&", "§").replace("%id%", doc.getString("id")));
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Banlog.Banner").replace("&", "§").replace("%banner%", doc.getString("banner")));
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Banlog.Date").replace("&", "§").replace("%date%", doc.getString("date")));
                        a++;
                    }
                } else if (plugin.isMysql()) {
                    try {
                        PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM banlogs WHERE PLAYER = ?");
                        preparedStatement.setString(1, args[0]);
                        ResultSet rs = preparedStatement.executeQuery();
                        while (rs.next()) {
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Banlog.Info").replace("&", "§").replace("%count%", String.valueOf(a)));
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Banlog.Player").replace("&", "§").replace("%player%", rs.getString("PLAYER")));
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Banlog.Reason").replace("&", "§").replace("%reason%", rs.getString("REASON")));
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Banlog.ID").replace("&", "§").replace("%id%", rs.getString("ID")));
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Banlog.Banner").replace("&", "§").replace("%banner%", rs.getString("BANNER")));
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Banlog.Date").replace("&", "§").replace("%date%", rs.getString("DATE")));
                            a++;
                        }
                        rs.close();
                        preparedStatement.close();
                    } catch (Exception ignored) {
                    }
                }
                if (a == 1) sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.NoDataFound").replace("&", "§"));
            } else {
                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.BanlogCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Banlog")));
            }
        } else {
            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.NoPermission").replace("&", "§"));
        }
        return false;
    }
}


