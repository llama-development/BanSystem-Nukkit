package net.llamadevelopment.bansystem.commands;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import net.llamadevelopment.bansystem.BanSystem;
import net.llamadevelopment.bansystem.components.managers.BanManager;
import net.llamadevelopment.bansystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.bansystem.components.managers.database.MySqlProvider;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UnbanCommand extends CommandManager {

    private BanSystem plugin;

    public UnbanCommand(BanSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Unban"), "Unban a player.", "/unban");
        this.plugin = plugin;
        
        // command params
        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender.hasPermission("bansystem.command.unban")) {
            if (args.length == 1) {
                String player = args[0];
                if (plugin.isMongodb()) {
                    Document document = new Document("name", player);
                    Document found = (Document) MongoDBProvider.getBanCollection().find(document).first();
                    if (found == null) {
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.PlayerNotBanned").replace("&", "§"));
                        return true;
                    }
                } else if (plugin.isMysql()) {
                    try {
                        PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM bans WHERE NAME = ?");
                        preparedStatement.setString(1, player);
                        ResultSet rs = preparedStatement.executeQuery();
                        if (rs.next()) {
                            if (rs.getString("NAME") == null) {
                                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.PlayerNotBanned").replace("&", "§"));
                                return true;
                            }
                        } else {
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.PlayerNotBanned").replace("&", "§"));
                            return true;
                        }
                        rs.close();
                        preparedStatement.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (plugin.isYaml()) {
                    Config bans = new Config(BanSystem.getInstance().getDataFolder() + "/bans.yml", Config.YAML);
                    if (!bans.exists("Player." + player)) {
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.PlayerNotBanned").replace("&", "§"));
                        return true;
                    }
                }
                BanManager.unBan(player);
                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.UnbanSuccess").replace("&", "§").replace("%player%", player));
            } else {
                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.UnbanCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Unban")));
            }
        } else {
            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.NoPermission").replace("&", "§"));
        }
        return false;
    }
}
