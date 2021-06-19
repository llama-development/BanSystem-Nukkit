package net.llamadevelopment.bansystem.components.provider;

import cn.nukkit.Player;
import cn.nukkit.Server;
import net.llamadevelopment.bansystem.BanSystem;
import net.llamadevelopment.bansystem.components.data.Ban;
import net.llamadevelopment.bansystem.components.data.Mute;
import net.llamadevelopment.bansystem.components.data.Warn;
import net.llamadevelopment.bansystem.components.event.*;
import net.llamadevelopment.bansystem.components.language.Language;
import net.llamadevelopment.bansystem.components.simplesqlclient.MySqlClient;
import net.llamadevelopment.bansystem.components.simplesqlclient.objects.SqlColumn;
import net.llamadevelopment.bansystem.components.simplesqlclient.objects.SqlDocument;
import net.llamadevelopment.bansystem.components.simplesqlclient.objects.SqlDocumentSet;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MysqlProvider extends Provider {

    private MySqlClient client;

    @Override
    public void connect(BanSystem server) {
        CompletableFuture.runAsync(() -> {
            try {
                this.client = new MySqlClient(
                        server.getConfig().getString("MySql.Host"),
                        server.getConfig().getString("MySql.Port"),
                        server.getConfig().getString("MySql.User"),
                        server.getConfig().getString("MySql.Password"),
                        server.getConfig().getString("MySql.Database")
                );

                this.client.createTable("bans", "id",
                        new SqlColumn("player", SqlColumn.Type.VARCHAR, 64)
                                .append("reason", SqlColumn.Type.VARCHAR, 128)
                                .append("id", SqlColumn.Type.VARCHAR, 64)
                                .append("banner", SqlColumn.Type.VARCHAR, 64)
                                .append("date", SqlColumn.Type.VARCHAR, 64)
                                .append("time", SqlColumn.Type.BIGINT, 128));

                this.client.createTable("mutes", "id",
                        new SqlColumn("player", SqlColumn.Type.VARCHAR, 64)
                                .append("reason", SqlColumn.Type.VARCHAR, 128)
                                .append("id", SqlColumn.Type.VARCHAR, 64)
                                .append("banner", SqlColumn.Type.VARCHAR, 64)
                                .append("date", SqlColumn.Type.VARCHAR, 64)
                                .append("time", SqlColumn.Type.BIGINT, 128));

                this.client.createTable("warns", "id",
                        new SqlColumn("player", SqlColumn.Type.VARCHAR, 64)
                                .append("reason", SqlColumn.Type.VARCHAR, 128)
                                .append("id", SqlColumn.Type.VARCHAR, 64)
                                .append("creator", SqlColumn.Type.VARCHAR, 64)
                                .append("date", SqlColumn.Type.VARCHAR, 64));

                this.client.createTable("banlogs", "id",
                        new SqlColumn("player", SqlColumn.Type.VARCHAR, 64)
                                .append("reason", SqlColumn.Type.VARCHAR, 128)
                                .append("id", SqlColumn.Type.VARCHAR, 64)
                                .append("banner", SqlColumn.Type.VARCHAR, 64)
                                .append("date", SqlColumn.Type.VARCHAR, 64));

                this.client.createTable("mutelogs", "id",
                        new SqlColumn("player", SqlColumn.Type.VARCHAR, 64)
                                .append("reason", SqlColumn.Type.VARCHAR, 128)
                                .append("id", SqlColumn.Type.VARCHAR, 64)
                                .append("banner", SqlColumn.Type.VARCHAR, 64)
                                .append("date", SqlColumn.Type.VARCHAR, 64));

                this.client.createTable("wave", "id",
                        new SqlColumn("player", SqlColumn.Type.VARCHAR, 64)
                                .append("reason", SqlColumn.Type.VARCHAR, 128)
                                .append("id", SqlColumn.Type.VARCHAR, 64)
                                .append("banner", SqlColumn.Type.VARCHAR, 64));

                server.getLogger().info("[MySqlClient] Connection opened.");
            } catch (Exception e) {
                e.printStackTrace();
                server.getLogger().info("[MySqlClient] Failed to connect to database.");
            }
        });
    }

    @Override
    public void disconnect(BanSystem server) {
        server.getLogger().info("[MySqlClient] Connection closed.");
    }

    @Override
    public void playerIsBanned(String player, Consumer<Boolean> isBanned) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document = this.client.find("bans", "player", player).first();
            isBanned.accept(document != null);
        });
    }

    @Override
    public void playerIsMuted(String player, Consumer<Boolean> isMuted) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document = this.client.find("mutes", "player", player).first();
            isMuted.accept(document != null);
        });
    }

    @Override
    public void banIdExists(String id, boolean history, Consumer<Boolean> exists) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document;
            if (history) {
                document = this.client.find("banlogs", "id", id).first();
            } else {
                document = this.client.find("bans", "id", id).first();
            }
            exists.accept(document != null);
        });
    }

    @Override
    public void muteIdExists(String id, boolean history, Consumer<Boolean> exists) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document;
            if (history) {
                document = this.client.find("mutelogs", "id", id).first();
            } else {
                document = this.client.find("mutes", "id", id).first();
            }
            exists.accept(document != null);
        });
    }

    @Override
    public void warnIdExists(String id, Consumer<Boolean> exists) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document = this.client.find("warns", "id", id).first();
            exists.accept(document != null);
        });
    }

    @Override
    public void banPlayer(String player, String reason, String banner, int seconds) {
        CompletableFuture.runAsync(() -> {
            long current = System.currentTimeMillis();
            long end = current + seconds * 1000L;
            if (seconds == -1) end = -1L;
            String id = this.getRandomIDCode();
            String date = this.getDate();
            this.client.insert("bans", new SqlDocument("player", player)
                    .append("reason", reason)
                    .append("id", id)
                    .append("banner", banner)
                    .append("date", date)
                    .append("time", end));
            Ban ban = new Ban(player, reason, id, banner, date, end);
            this.createBanlog(ban);
            Player onlinePlayer = Server.getInstance().getPlayer(player);
            if (onlinePlayer != null) {
                onlinePlayer.kick(Language.getNP("BanScreen", ban.getReason(), ban.getBanID(), this.getRemainingTime(ban.getTime())), false);
            }
            Server.getInstance().getPluginManager().callEvent(new PlayerBanEvent(ban));
        });
    }

    @Override
    public void mutePlayer(String player, String reason, String banner, int seconds) {
        CompletableFuture.runAsync(() -> {
            long current = System.currentTimeMillis();
            long end = current + seconds * 1000L;
            if (seconds == -1) end = -1L;
            String id = this.getRandomIDCode();
            String date = this.getDate();
            this.client.insert("mutes", new SqlDocument("player", player)
                    .append("reason", reason)
                    .append("id", id)
                    .append("banner", banner)
                    .append("date", date)
                    .append("time", end));
            Mute mute = new Mute(player, reason, id, banner, date, end);
            this.createMutelog(mute);
            Server.getInstance().getPluginManager().callEvent(new PlayerMuteEvent(mute));
        });
    }

    @Override
    public void warnPlayer(String player, String reason, String creator) {
        CompletableFuture.runAsync(() -> {
            String id = this.getRandomIDCode();
            String date = this.getDate();
            this.client.insert("warns", new SqlDocument("player", player)
                    .append("reason", reason)
                    .append("id", id)
                    .append("creator", creator)
                    .append("date", date));
            Server.getInstance().getPluginManager().callEvent(new PlayerWarnEvent(new Warn(player, reason, id, creator, date)));
            Player onlinePlayer = Server.getInstance().getPlayer(player);
            if (onlinePlayer != null) onlinePlayer.kick(Language.getNP("WarnScreen", reason, creator), false);
        });
    }

    @Override
    public void unbanPlayer(String player) {
        CompletableFuture.runAsync(() -> {
            this.client.delete("bans", "player", player);
            Server.getInstance().getPluginManager().callEvent(new PlayerUnbanEvent(player, "null"));
        });
    }

    @Override
    public void unmutePlayer(String player) {
        CompletableFuture.runAsync(() -> {
            this.client.delete("mutes", "player", player);
            Server.getInstance().getPluginManager().callEvent(new PlayerUnmuteEvent(player, "null"));
        });
    }

    @Override
    public void unbanPlayer(String player, String executor) {
        CompletableFuture.runAsync(() -> {
            this.client.delete("bans", "player", player);
            Server.getInstance().getPluginManager().callEvent(new PlayerUnbanEvent(player, executor));
        });
    }

    @Override
    public void unmutePlayer(String player, String executor) {
        CompletableFuture.runAsync(() -> {
            this.client.delete("mutes", "player", player);
            Server.getInstance().getPluginManager().callEvent(new PlayerUnmuteEvent(player, executor));
        });
    }

    @Override
    public void getBan(String player, Consumer<Ban> ban) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document = this.client.find("bans", "player", player).first();
            ban.accept(new Ban(player, document.getString("reason"), document.getString("id"), document.getString("banner"), document.getString("date"), document.getLong("time")));
        });
    }

    @Override
    public void getMute(String player, Consumer<Mute> mute) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document = this.client.find("mutes", "player", player).first();
            mute.accept(new Mute(player, document.getString("reason"), document.getString("id"), document.getString("banner"), document.getString("date"), document.getLong("time")));
        });
    }

    @Override
    public void getBanById(String id, boolean history, Consumer<Ban> ban) {
        CompletableFuture.runAsync(() -> {
            if (history) {
                SqlDocument document = this.client.find("banlogs", "id", id).first();
                ban.accept(new Ban(document.getString("player"), document.getString("reason"), document.getString("id"), document.getString("banner"), document.getString("date"), 0));
            } else {
                SqlDocument document = this.client.find("bans", "id", id).first();
                ban.accept(new Ban(document.getString("player"), document.getString("reason"), document.getString("id"), document.getString("banner"), document.getString("date"), document.getLong("time")));
            }
        });
    }

    @Override
    public void getMuteById(String id, boolean history, Consumer<Mute> mute) {
        CompletableFuture.runAsync(() -> {
            if (history) {
                SqlDocument document = this.client.find("mutelogs", "id", id).first();
                mute.accept(new Mute(document.getString("player"), document.getString("reason"), document.getString("id"), document.getString("banner"), document.getString("date"), 0));
            } else {
                SqlDocument document = this.client.find("mutes", "id", id).first();
                mute.accept(new Mute(document.getString("player"), document.getString("reason"), document.getString("id"), document.getString("banner"), document.getString("date"), document.getLong("time")));
            }
        });
    }

    @Override
    public void createBanlog(Ban ban) {
        CompletableFuture.runAsync(() -> this.client.insert("banlogs", new SqlDocument("player", ban.getPlayer())
                .append("reason", ban.getReason())
                .append("id", ban.getBanID())
                .append("banner", ban.getBanner())
                .append("date", ban.getDate())));
    }

    @Override
    public void createMutelog(Mute mute) {
        CompletableFuture.runAsync(() -> this.client.insert("mutelogs", new SqlDocument("player", mute.getPlayer())
                .append("reason", mute.getReason())
                .append("id", mute.getMuteID())
                .append("banner", mute.getMuter())
                .append("date", mute.getDate())));
    }

    @Override
    public void getBanLog(String player, Consumer<Set<Ban>> banlog) {
        CompletableFuture.runAsync(() -> {
            Set<Ban> list = new HashSet<>();
            SqlDocumentSet documentSet = this.client.find("banlogs", "player", player);
            documentSet.getAll().forEach(document -> list.add(new Ban(player, document.getString("reason"), document.getString("id"), document.getString("banner"), document.getString("date"), 0)));
            banlog.accept(list);
        });
    }

    @Override
    public void getMuteLog(String player, Consumer<Set<Mute>> mutelog) {
        CompletableFuture.runAsync(() -> {
            Set<Mute> list = new HashSet<>();
            SqlDocumentSet documentSet = this.client.find("mutelogs", "player", player);
            documentSet.getAll().forEach(document -> list.add(new Mute(player, document.getString("reason"), document.getString("id"), document.getString("banner"), document.getString("date"), 0)));
            mutelog.accept(list);
        });
    }

    @Override
    public void getWarnLog(String player, Consumer<Set<Warn>> warnlog) {
        CompletableFuture.runAsync(() -> {
            Set<Warn> list = new HashSet<>();
            SqlDocumentSet documentSet = this.client.find("warns", "player", player);
            documentSet.getAll().forEach(document -> list.add(new Warn(player, document.getString("reason"), document.getString("id"), document.getString("creator"), document.getString("date"))));
            warnlog.accept(list);
        });
    }

    @Override
    public void clearBanlog(String player) {
        CompletableFuture.runAsync(() -> {
            SqlDocumentSet documentSet = this.client.find("banlogs", "player", player);
            documentSet.getAll().forEach(document -> this.client.delete("banlogs", "id", document.getString("id")));
            Server.getInstance().getPluginManager().callEvent(new ClearBanlogEvent(player, "null"));
        });
    }

    @Override
    public void clearMutelog(String player) {
        CompletableFuture.runAsync(() -> {
            SqlDocumentSet documentSet = this.client.find("mutelogs", "player", player);
            documentSet.getAll().forEach(document -> this.client.delete("mutelogs", "id", document.getString("id")));
            Server.getInstance().getPluginManager().callEvent(new ClearMutelogEvent(player, "null"));
        });
    }

    @Override
    public void clearWarns(String player) {
        CompletableFuture.runAsync(() -> {
            SqlDocumentSet documentSet = this.client.find("warns", "player", player);
            documentSet.getAll().forEach(document -> this.client.delete("warns", "id", document.getString("id")));
            Server.getInstance().getPluginManager().callEvent(new ClearWarnlogEvent(player, "null"));
        });
    }

    @Override
    public void clearBanlog(String player, String executor) {
        CompletableFuture.runAsync(() -> {
            SqlDocumentSet documentSet = this.client.find("banlogs", "player", player);
            documentSet.getAll().forEach(document -> this.client.delete("banlogs", "id", document.getString("id")));
            Server.getInstance().getPluginManager().callEvent(new ClearBanlogEvent(player, executor));
        });
    }

    @Override
    public void clearMutelog(String player, String executor) {
        CompletableFuture.runAsync(() -> {
            SqlDocumentSet documentSet = this.client.find("mutelogs", "player", player);
            documentSet.getAll().forEach(document -> this.client.delete("mutelogs", "id", document.getString("id")));
            Server.getInstance().getPluginManager().callEvent(new ClearMutelogEvent(player, executor));
        });
    }

    @Override
    public void clearWarns(String player, String executor) {
        CompletableFuture.runAsync(() -> {
            SqlDocumentSet documentSet = this.client.find("warns", "player", player);
            documentSet.getAll().forEach(document -> this.client.delete("warns", "id", document.getString("id")));
            Server.getInstance().getPluginManager().callEvent(new ClearWarnlogEvent(player, executor));
        });
    }

    @Override
    public void setBanReason(String player, String reason) {
        CompletableFuture.runAsync(() -> {
            Server.getInstance().getPluginManager().callEvent(new EditBanReasonEvent(player, reason, "null"));
            this.client.update("bans", "player", player, new SqlDocument("reason", reason));
        });
    }

    @Override
    public void setMuteReason(String player, String reason) {
        CompletableFuture.runAsync(() -> {
            this.client.update("mutes", "player", player, new SqlDocument("reason", reason));
            Server.getInstance().getPluginManager().callEvent(new EditMuteReasonEvent(player, reason, "null"));
        });
    }

    @Override
    public void setBanTime(String player, long time) {
        CompletableFuture.runAsync(() -> {
            this.client.update("bans", "player", player, new SqlDocument("time", time));
            Server.getInstance().getPluginManager().callEvent(new EditBanTimeEvent(player, time, "null"));
        });
    }

    @Override
    public void setMuteTime(String player, long time) {
        CompletableFuture.runAsync(() -> {
            this.client.update("mutes", "player", player, new SqlDocument("time", time));
            Server.getInstance().getPluginManager().callEvent(new EditMuteTimeEvent(player, time, "null"));
        });
    }

    @Override
    public void setBanReason(String player, String reason, String executor) {
        CompletableFuture.runAsync(() -> {
            this.client.update("bans", "player", player, new SqlDocument("reason", reason));
            Server.getInstance().getPluginManager().callEvent(new EditBanReasonEvent(player, reason, executor));
        });
    }

    @Override
    public void setMuteReason(String player, String reason, String executor) {
        CompletableFuture.runAsync(() -> {
            this.client.update("mutes", "player", player, new SqlDocument("reason", reason));
            Server.getInstance().getPluginManager().callEvent(new EditMuteReasonEvent(player, reason, executor));
        });
    }

    @Override
    public void setBanTime(String player, long time, String executor) {
        CompletableFuture.runAsync(() -> {
            this.client.update("bans", "player", player, new SqlDocument("time", time));
            Server.getInstance().getPluginManager().callEvent(new EditBanTimeEvent(player, time, executor));
        });
    }

    @Override
    public void setMuteTime(String player, long time, String executor) {
        CompletableFuture.runAsync(() -> {
            this.client.update("mutes", "player", player, new SqlDocument("time", time));
            Server.getInstance().getPluginManager().callEvent(new EditMuteTimeEvent(player, time, executor));
        });
    }

    @Override
    public void deleteBan(String id) {
        CompletableFuture.runAsync(() -> {
            this.client.delete("banlogs", new SqlDocument("id", id));
            Server.getInstance().getPluginManager().callEvent(new DeleteBanEvent(id, "null"));
        });
    }

    @Override
    public void deleteMute(String id) {
        CompletableFuture.runAsync(() -> {
            this.client.delete("mutelogs", new SqlDocument("id", id));
            Server.getInstance().getPluginManager().callEvent(new DeleteMuteEvent(id, "null"));
        });
    }

    @Override
    public void deleteWarn(String id) {
        CompletableFuture.runAsync(() -> {
            this.client.delete("warns", new SqlDocument("id", id));
            Server.getInstance().getPluginManager().callEvent(new DeleteWarnEvent(id, "null"));
        });
    }

    @Override
    public void deleteBan(String id, String executor) {
        CompletableFuture.runAsync(() -> {
            this.client.delete("banlogs", new SqlDocument("id", id));
            Server.getInstance().getPluginManager().callEvent(new DeleteBanEvent(id, executor));
        });
    }

    @Override
    public void deleteMute(String id, String executor) {
        CompletableFuture.runAsync(() -> {
            this.client.delete("mutelogs", new SqlDocument("id", id));
            Server.getInstance().getPluginManager().callEvent(new DeleteMuteEvent(id, executor));
        });
    }

    @Override
    public void deleteWarn(String id, String executor) {
        CompletableFuture.runAsync(() -> {
            this.client.delete("warns", new SqlDocument("id", id));
            Server.getInstance().getPluginManager().callEvent(new DeleteWarnEvent(id, executor));
        });
    }

    @Override
    public String getProvider() {
        return "MySql";
    }

}
