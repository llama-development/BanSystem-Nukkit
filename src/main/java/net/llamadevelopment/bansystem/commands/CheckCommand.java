package net.llamadevelopment.bansystem.commands;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import net.llamadevelopment.bansystem.BanSystem;
import net.llamadevelopment.bansystem.components.managers.BanManager;
import net.llamadevelopment.bansystem.components.managers.MuteManager;
import net.llamadevelopment.bansystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.bansystem.components.managers.database.MySqlProvider;
import net.llamadevelopment.bansystem.components.utils.BanUtil;
import net.llamadevelopment.bansystem.components.utils.MuteUtil;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckCommand extends CommandManager {

    private BanSystem plugin;

    public CheckCommand(BanSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Check"), "Check if a player is banned or muted.", "/check");
        this.plugin = plugin;
        
        // command parameters
        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false),
                new CommandParameter("type", false, new String[] {"ban", "mute"})
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender.hasPermission("bansystem.command.check")) {
            if (args.length == 2) {
                String player = args[0];
                if (args[1].equalsIgnoreCase("ban")) {
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
                    BanUtil banUtil = BanManager.getPlayer(player);
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Ban.Info").replace("&", "§"));
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Ban.Player").replace("&", "§").replace("%player%", banUtil.getPlayer()));
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Ban.Reason").replace("&", "§").replace("%reason%", banUtil.getReason()));
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Ban.ID").replace("&", "§").replace("%id%", banUtil.getId()));
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Ban.Banner").replace("&", "§").replace("%banner%", banUtil.getBanner()));
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Ban.Date").replace("&", "§").replace("%date%", banUtil.getDate()));
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Ban.RemainingTime").replace("&", "§").replace("%time%", BanManager.getRemainingTime(banUtil.getEnd())));
                } else if (args[1].equalsIgnoreCase("mute")) {
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
                            }
                            rs.close();
                            preparedStatement.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (plugin.isYaml()){
                        Config bans = new Config(BanSystem.getInstance().getDataFolder() + "/mutes.yml", Config.YAML);
                        if (!bans.exists("Player." + player)) {
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.PlayerNotMuted").replace("&", "§"));
                            return true;
                        }
                    }
                    MuteUtil muteUtil = MuteManager.getPlayer(player);
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Mute.Info").replace("&", "§"));
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Mute.Player").replace("&", "§").replace("%player%", muteUtil.getPlayer()));
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Mute.Reason").replace("&", "§").replace("%reason%", muteUtil.getReason()));
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Mute.ID").replace("&", "§").replace("%id%", muteUtil.getId()));
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Mute.Muter").replace("&", "§").replace("%muter%", muteUtil.getBanner()));
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Mute.Date").replace("&", "§").replace("%date%", muteUtil.getDate()));
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Check.Mute.RemainingTime").replace("&", "§").replace("%time%", MuteManager.getRemainingTime(muteUtil.getEnd())));
                } else {
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.CheckCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Check")));
                }
            } else {
                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.CheckCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Check")));
            }
        } else {
            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.NoPermission").replace("&", "§"));
        }
        return false;
    }
}
