package net.llamadevelopment.bansystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import net.llamadevelopment.bansystem.BanSystem;
import net.llamadevelopment.bansystem.components.managers.MuteManager;
import net.llamadevelopment.bansystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.bansystem.components.managers.database.MySqlProvider;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UnmuteCommand extends CommandManager {

    private BanSystem plugin;

    public UnmuteCommand(BanSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Unmute"), "Unmute a player.", "/unmute");
        this.plugin = plugin;
        
        // command params
        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender.hasPermission("bansystem.command.unmute")) {
            if (args.length == 1) {
                String player = args[0];
                if (plugin.isMongodb()) {
                    Document document = new Document("name", player);
                    Document found = (Document) MongoDBProvider.getMuteCollection().find(document).first();
                    if (found == null) {
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.PlayerNotMuted").replace("&", "§"));
                        return true;
                    }
                } else if (plugin.isMysql()) {
                    try {
                        PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM mutes WHERE NAME = ?");
                        preparedStatement.setString(1, player);
                        ResultSet rs = preparedStatement.executeQuery();
                        if (rs.next()) {
                            if (rs.getString("NAME") == null) {
                                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.PlayerNotMuted").replace("&", "§"));
                                return true;
                            }
                        } else {
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.PlayerNotMuted").replace("&", "§"));
                            return true;
                        }
                        rs.close();
                        preparedStatement.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (plugin.isYaml()) {
                    Config bans = new Config(BanSystem.getInstance().getDataFolder() + "/mutes.yml", Config.YAML);
                    if (!bans.exists("Player." + player)) {
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.PlayerNotMuted").replace("&", "§"));
                        return true;
                    }
                }
                MuteManager.unMute(player);
                Player online = BanSystem.getInstance().getServer().getPlayer(player);
                if (online != null) {
                    BanSystem.getInstance().mutedCache.remove(online);
                }
                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.UnmuteSuccess").replace("&", "§").replace("%player%", player));
            } else {
                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.UnmuteCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Unmute")));
            }
        } else {
            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.NoPermission").replace("&", "§"));
        }
        return false;
    }
}
