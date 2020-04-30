package net.llamadevelopment.bansystem.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import net.llamadevelopment.bansystem.BanSystem;
import net.llamadevelopment.bansystem.components.managers.MuteManager;
import net.llamadevelopment.bansystem.components.managers.database.MongoDBProvider;
import net.llamadevelopment.bansystem.components.managers.database.MySqlProvider;
import net.llamadevelopment.bansystem.components.utils.MessageUtil;
import net.llamadevelopment.bansystem.components.utils.MuteUtil;
import net.llamadevelopment.bansystem.components.utils.MutedPlayer;
import org.bson.Document;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MuteCommand extends CommandManager {

    private BanSystem plugin;

    public MuteCommand(BanSystem plugin) {
        super(plugin, plugin.getConfig().getString("Commands.Mute"), "Mute a player.", "/mute");
        this.plugin = plugin;
        
        // command params
        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false),
                new CommandParameter("ID", CommandParamType.INT, false)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender.hasPermission("bansystem.command.mute")) {
            if (args.length == 2) {
                String player = args[0];
                String number = args[1];
                try {
                    int n = Integer.parseInt(args[1]);
                    int seconds = plugin.getConfig().getInt("MuteReasons." + number + ".Seconds");
                    String reason = plugin.getConfig().getString("MuteReasons." + number + ".Reason");
                    int max = plugin.getConfig().getInt("MuteReasons.Count");
                    if (plugin.isMongodb()) {
                        Document document = new Document("name", player);
                        Document found = (Document) MongoDBProvider.getMuteCollection().find(document).first();
                        if (found != null) {
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.AlreadyMuted").replace("&", "§"));
                            return true;
                        }
                    } else if (plugin.isMysql()) {
                        try {
                            PreparedStatement preparedStatement = MySqlProvider.getConnection().prepareStatement("SELECT * FROM mutes WHERE NAME = ?");
                            preparedStatement.setString(1, player);
                            ResultSet rs = preparedStatement.executeQuery();
                            if (rs.next()) {
                                if (rs.getString("NAME") != null) {
                                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.AlreadyMuted").replace("&", "§"));
                                    return true;
                                }
                            }
                            rs.close();
                            preparedStatement.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (plugin.isYaml()) {
                        Config bans = new Config(BanSystem.getInstance().getDataFolder() + "/mutes.yml", Config.YAML);
                        if (bans.exists("Player." + player)) {
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.AlreadyMuted").replace("&", "§"));
                            return true;
                        }
                    }
                    if (!(n > max)) {
                        if (n >= 1) {
                            MuteManager.setMuted(player, reason, getBanID(), sender.getName(), getDate(), seconds);
                            MuteUtil muteUtil = MuteManager.getPlayer(player);
                            long end = muteUtil.getEnd();
                            String reason1 = muteUtil.getReason();
                            String id = muteUtil.getId();
                            String banner = muteUtil.getBanner();
                            MutedPlayer mutedPlayer = new MutedPlayer(end, reason1, id, banner);
                            Player player1 = BanSystem.getInstance().getServer().getPlayer(player);
                            if (player1 != null) {
                                BanSystem.getInstance().mutedCache.put(player1, mutedPlayer);
                            }
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.MuteSuccess").replace("%player%", player).replace("&", "§"));
                        } else {
                            sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.Reasonlimit").replace("&", "§"));
                        }
                    } else {
                        sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.Reasonlimit").replace("&", "§"));
                    }
                } catch (NumberFormatException var1) {
                    sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Messages.MustNumber").replace("&", "§").replace("%max%", plugin.getConfig().getString("MuteReasons.Count")));
                    var1.getMessage();
                }
            } else {
                MessageUtil.sendMuteHelp(sender, BanSystem.getInstance());
                sender.sendMessage(plugin.getConfig().getString("Messages.Prefix").replace("&", "§") + plugin.getConfig().getString("Usage.MuteCommand").replace("&", "§").replace("%command%", "/" + plugin.getConfig().getString("Commands.Mute")));
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
