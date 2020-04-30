package net.llamadevelopment.bansystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import net.llamadevelopment.bansystem.BanSystem;
import net.llamadevelopment.bansystem.components.managers.BanManager;
import net.llamadevelopment.bansystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.bansystem.components.managers.database.MySqlProvider;
import net.llamadevelopment.bansystem.components.utils.BanUtil;
import net.llamadevelopment.bansystem.components.utils.MessageUtil;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class BanCommand extends CommandManager {

    private BanSystem plugin;

    public BanCommand(BanSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Ban"), "Ban a player.", "/ban");
        this.plugin = plugin;
        
        
        // command parameters
        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false),
                new CommandParameter("ID", CommandParamType.INT, false)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender.hasPermission("bansystem.command.ban")) {
            if (args.length == 2) {
                String player = args[0];
                String number = args[1];
                try {
                    int n = Integer.parseInt(args[1]);
                    int seconds = plugin.getConfig().getInt("BanReasons." + number + ".Seconds");
                    String reason = plugin.getConfig().getString("BanReasons." + number + ".Reason");
                    int max = plugin.getConfig().getInt("BanReasons.Count");
                    if (plugin.isMongodb()) {
                        Document document = new Document("name", player);
                        Document found = (Document) MongoDBProvider.getBanCollection().find(document).first();
                        if (found != null) {
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.AlreadyBanned").replace("&", "§"));
                            return true;
                        }
                    } else if (plugin.isMysql()) {
                        try {
                            PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM bans WHERE NAME = ?");
                            preparedStatement.setString(1, player);
                            ResultSet rs = preparedStatement.executeQuery();
                            if (rs.next()) {
                                if (rs.getString("NAME") != null) {
                                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.AlreadyBanned").replace("&", "§"));
                                    return true;
                                }
                            }
                            rs.close();
                            preparedStatement.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (plugin.isYaml()) {
                        Config bans = new Config(BanSystem.getInstance().getDataFolder() + "/bans.yml", Config.YAML);
                        if (bans.exists("Player." + player)) {
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.AlreadyBanned").replace("&", "§"));
                            return true;
                        }
                    }
                    if (!(n > max)) {
                        if (n >= 1) {
                            BanManager.setBanned(player, reason, getBanID(), sender.getName(), getDate(), seconds);
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.BanSuccess").replace("%player%", player).replace("&", "§"));
                            Player target = BanSystem.getInstance().getServer().getPlayer(player);
                            BanUtil banUtil = BanManager.getPlayer(player);
                            if (target != null) {
                                target.kick(MessageUtil.banScreen(banUtil.getReason(), banUtil.getId(), BanManager.getRemainingTime(banUtil.getEnd()), banUtil.getBanner()), false);
                            }
                        } else {
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.Reasonlimit").replace("&", "§"));
                        }
                    } else {
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.Reasonlimit").replace("&", "§"));
                    }
                } catch (NumberFormatException var1) {
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.MustNumber").replace("&", "§").replace("%max%", plugin.getConfig().getString("BanReasons.Count")));
                    var1.getMessage();
                }
            } else {
                MessageUtil.sendBanHelp(sender, BanSystem.getInstance());
                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.BanCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Ban")));
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
