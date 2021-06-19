package net.llamadevelopment.bansystem.components.provider;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import net.llamadevelopment.bansystem.BanSystem;
import net.llamadevelopment.bansystem.components.data.Ban;
import net.llamadevelopment.bansystem.components.data.Mute;
import net.llamadevelopment.bansystem.components.data.Warn;
import net.llamadevelopment.bansystem.components.event.*;
import net.llamadevelopment.bansystem.components.language.Language;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class YamlProvider extends Provider {

    private Config bans, banlog, mutes, mutelog, warns, extended;

    @Override
    public void connect(BanSystem server) {
        CompletableFuture.runAsync(() -> {
            server.saveResource("/data/bans.yml");
            server.saveResource("/data/banlog.yml");
            server.saveResource("/data/mutes.yml");
            server.saveResource("/data/mutelog.yml");
            server.saveResource("/data/warns.yml");
            server.saveResource("/data/extended.yml");
            this.bans = new Config(server.getDataFolder() + "/data/bans.yml", Config.YAML);
            this.banlog = new Config(server.getDataFolder() + "/data/banlog.yml", Config.YAML);
            this.mutes = new Config(server.getDataFolder() + "/data/mutes.yml", Config.YAML);
            this.mutelog = new Config(server.getDataFolder() + "/data/mutelog.yml", Config.YAML);
            this.warns = new Config(server.getDataFolder() + "/data/warns.yml", Config.YAML);
            this.extended = new Config(server.getDataFolder() + "/data/extended.yml", Config.YAML);
        });
    }

    @Override
    public void playerIsBanned(String player, Consumer<Boolean> isBanned) {
        isBanned.accept(this.bans.exists("Ban." + player));
    }

    @Override
    public void playerIsMuted(String player, Consumer<Boolean> isMuted) {
        isMuted.accept(this.mutes.exists("Mute." + player));
    }

    @Override
    public void banIdExists(String id, boolean history, Consumer<Boolean> exists) {
        boolean b = false;
        if (history) {
            for (String s : this.banlog.getSection("Banlog").getAll().getKeys(false)) {
                boolean idSet = this.banlog.exists("Banlog." + s + "." + id);
                if (idSet) b = true;
            }
        } else {
            for (String s : this.bans.getSection("Ban").getAll().getKeys(false)) {
                String idSet = this.bans.getString("Ban." + s + ".ID");
                if (id.equals(idSet)) b = true;
            }
        }
        exists.accept(b);
    }

    @Override
    public void muteIdExists(String id, boolean history, Consumer<Boolean> exists) {
        boolean b = false;
        if (history) {
            for (String s : this.mutelog.getSection("Mutelog").getAll().getKeys(false)) {
                boolean idSet = this.mutelog.exists("Mutelog." + s + "." + id);
                if (idSet) b = true;
            }
        } else {
            for (String s : this.mutes.getSection("Mute").getAll().getKeys(false)) {
                String idSet = this.mutes.getString("Mute." + s + ".ID");
                if (id.equals(idSet)) b = true;
            }
        }
        exists.accept(b);
    }

    @Override
    public void warnIdExists(String id, Consumer<Boolean> exists) {
        boolean b = false;
        for (String s : this.warns.getSection("Warn").getAll().getKeys(false)) {
            boolean idSet = this.warns.exists("Warn." + s + "." + id);
            if (idSet) b = true;
        }
        exists.accept(b);
    }

    @Override
    public void banPlayer(String player, String reason, String banner, int seconds) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        if (seconds == -1) end = -1L;
        String id = this.getRandomIDCode();
        String date = this.getDate();
        this.bans.set("Ban." + player + ".Reason", reason);
        this.bans.set("Ban." + player + ".ID", id);
        this.bans.set("Ban." + player + ".Banner", banner);
        this.bans.set("Ban." + player + ".Date", date);
        this.bans.set("Ban." + player + ".Time", end);
        this.bans.save();
        this.bans.reload();
        Ban ban = new Ban(player, reason, id, banner, date, end);
        this.createBanlog(ban);
        Player onlinePlayer = Server.getInstance().getPlayer(player);
        if (onlinePlayer != null) {
            onlinePlayer.kick(Language.getNP("BanScreen", ban.getReason(), ban.getBanID(), this.getRemainingTime(ban.getTime())), false);
        }
        Server.getInstance().getPluginManager().callEvent(new PlayerBanEvent(ban));
    }

    @Override
    public void mutePlayer(String player, String reason, String banner, int seconds) {
        long end = System.currentTimeMillis() + seconds * 1000L;
        if (seconds == -1) end = -1L;
        String id = this.getRandomIDCode();
        String date = this.getDate();
        this.mutes.set("Mute." + player + ".Reason", reason);
        this.mutes.set("Mute." + player + ".ID", id);
        this.mutes.set("Mute." + player + ".Banner", banner);
        this.mutes.set("Mute." + player + ".Date", date);
        this.mutes.set("Mute." + player + ".Time", end);
        this.mutes.save();
        this.mutes.reload();
        Mute mute = new Mute(player, reason, id, banner, date, end);
        this.createMutelog(mute);
        Server.getInstance().getPluginManager().callEvent(new PlayerMuteEvent(mute));
    }

    @Override
    public void warnPlayer(String player, String reason, String creator) {
        String id = this.getRandomIDCode();
        String date = this.getDate();
        this.warns.set("Warn." + player + "." + id + ".Reason", reason);
        this.warns.set("Warn." + player + "." + id + ".Creator", creator);
        this.warns.set("Warn." + player + "." + id + ".Date", date);
        this.warns.save();
        this.warns.reload();
        Server.getInstance().getPluginManager().callEvent(new PlayerWarnEvent(new Warn(player, reason, id, creator, date)));
        Player onlinePlayer = Server.getInstance().getPlayer(player);
        if (onlinePlayer != null) onlinePlayer.kick(Language.getNP("WarnScreen", reason, creator), false);
    }

    @Override
    public void unbanPlayer(String player) {
        Map<String, Object> map = this.bans.getSection("Ban").getAllMap();
        map.remove(player);
        this.bans.set("Ban", map);
        this.bans.save();
        this.bans.reload();
        Server.getInstance().getPluginManager().callEvent(new PlayerUnbanEvent(player, "null"));
    }

    @Override
    public void unmutePlayer(String player) {
        Map<String, Object> map = this.mutes.getSection("Mute").getAllMap();
        map.remove(player);
        this.mutes.set("Mute", map);
        this.mutes.save();
        this.mutes.reload();
        Server.getInstance().getPluginManager().callEvent(new PlayerUnmuteEvent(player, "null"));
    }

    @Override
    public void unbanPlayer(String player, String executor) {
        Map<String, Object> map = this.bans.getSection("Ban").getAllMap();
        map.remove(player);
        this.bans.set("Ban", map);
        this.bans.save();
        this.bans.reload();
        Server.getInstance().getPluginManager().callEvent(new PlayerUnbanEvent(player, executor));
    }

    @Override
    public void unmutePlayer(String player, String executor) {
        Map<String, Object> map = this.mutes.getSection("Mute").getAllMap();
        map.remove(player);
        this.mutes.set("Mute", map);
        this.mutes.save();
        this.mutes.reload();
        Server.getInstance().getPluginManager().callEvent(new PlayerUnmuteEvent(player, executor));
    }

    @Override
    public void getBan(String player, Consumer<Ban> ban) {
        String reason = this.bans.getString("Ban." + player + ".Reason");
        String banID = this.bans.getString("Ban." + player + ".ID");
        String banner = this.bans.getString("Ban." + player + ".Banner");
        String date = this.bans.getString("Ban." + player + ".Date");
        long time = this.bans.getLong("Ban." + player + ".Time");
        ban.accept(new Ban(player, reason, banID, banner, date, time));
    }

    @Override
    public void getMute(String player, Consumer<Mute> mute) {
        String reason = this.mutes.getString("Mute." + player + ".Reason");
        String banID = this.mutes.getString("Mute." + player + ".ID");
        String banner = this.mutes.getString("Mute." + player + ".Banner");
        String date = this.mutes.getString("Mute." + player + ".Date");
        long time = this.mutes.getLong("Mute." + player + ".Time");
        mute.accept(new Mute(player, reason, banID, banner, date, time));
    }

    @Override
    public void getBanById(String id, boolean history, Consumer<Ban> ban) {
        if (history) {
            for (String s : this.banlog.getSection("Banlog").getAll().getKeys(false)) {
                if (this.banlog.exists("Banlog." + s + "." + id)) {
                    String reason = this.banlog.getString("Banlog." + s + "." + id + ".Reason");
                    String banner = this.banlog.getString("Banlog." + s + "." + id + ".Banner");
                    String date = this.banlog.getString("Banlog." + s + "." + id + ".Date");
                    ban.accept(new Ban(s, reason, id, banner, date, 0));
                }
            }
        } else {
            for (String s : this.bans.getSection("Ban").getAll().getKeys(false)) {
                String idSet = this.bans.getString("Ban." + s + ".ID");
                if (id.equals(idSet)) {
                    String reason = this.bans.getString("Ban." + s + ".Reason");
                    String banner = this.bans.getString("Ban." + s + ".Banner");
                    String date = this.bans.getString("Ban." + s + ".Date");
                    long time = this.bans.getLong("Ban." + s + ".Time");
                    ban.accept(new Ban(s, reason, id, banner, date, time));
                }
            }
        }
    }

    @Override
    public void getMuteById(String id, boolean history, Consumer<Mute> mute) {
        if (history) {
            for (String s : this.mutelog.getSection("Mutelog").getAll().getKeys(false)) {
                if (this.mutelog.exists("Mutelog." + s + "." + id)) {
                    String reason = this.mutelog.getString("Mutelog." + s + "." + id + ".Reason");
                    String banner = this.mutelog.getString("Mutelog." + s + "." + id + ".Muter");
                    String date = this.mutelog.getString("Mutelog." + s + "." + id + ".Date");
                    mute.accept(new Mute(s, reason, id, banner, date, 0));
                }
            }
        } else {
            for (String s : this.mutes.getSection("Mute").getAll().getKeys(false)) {
                String idSet = this.mutes.getString("Mute." + s + ".ID");
                if (id.equals(idSet)) {
                    String reason = this.mutes.getString("Mute." + s + ".Reason");
                    String banner = this.mutes.getString("Mute." + s + ".Banner");
                    String date = this.mutes.getString("Mute." + s + ".Date");
                    long time = this.mutes.getLong("Mute." + s + ".Time");
                    mute.accept(new Mute(s, reason, id, banner, date, time));
                }
            }
        }
    }

    @Override
    public void createBanlog(Ban ban) {
        this.banlog.set("Banlog." + ban.getPlayer() + "." + ban.getBanID() + ".Reason", ban.getReason());
        this.banlog.set("Banlog." + ban.getPlayer() + "." + ban.getBanID() + ".Banner", ban.getBanner());
        this.banlog.set("Banlog." + ban.getPlayer() + "." + ban.getBanID() + ".Date", ban.getDate());
        this.banlog.save();
        this.banlog.reload();
    }

    @Override
    public void createMutelog(Mute mute) {
        this.mutelog.set("Mutelog." + mute.getPlayer() + "." + mute.getMuteID() + ".Reason", mute.getReason());
        this.mutelog.set("Mutelog." + mute.getPlayer() + "." + mute.getMuteID() + ".Muter", mute.getMuter());
        this.mutelog.set("Mutelog." + mute.getPlayer() + "." + mute.getMuteID() + ".Date", mute.getDate());
        this.mutelog.save();
        this.mutelog.reload();
    }

    @Override
    public void getBanLog(String player, Consumer<Set<Ban>> banlog) {
        Set<Ban> list = new HashSet<>();
        for (String s : this.banlog.getSection("Banlog." + player).getAll().getKeys(false)) {
            String reason = this.banlog.getString("Banlog." + player + "." + s + ".Reason");
            String banner = this.banlog.getString("Banlog." + player + "." + s + ".Banner");
            String date = this.banlog.getString("Banlog." + player + "." + s + ".Date");
            list.add(new Ban(player, reason, s, banner, date, 0));
        }
        banlog.accept(list);
    }

    @Override
    public void getMuteLog(String player, Consumer<Set<Mute>> mutelog) {
        Set<Mute> list = new HashSet<>();
        for (String s : this.mutelog.getSection("Mutelog." + player).getAll().getKeys(false)) {
            String reason = this.mutelog.getString("Mutelog." + player + "." + s + ".Reason");
            String banner = this.mutelog.getString("Mutelog." + player + "." + s + ".Muter");
            String date = this.mutelog.getString("Mutelog." + player + "." + s + ".Date");
            list.add(new Mute(player, reason, s, banner, date, 0));
        }
        mutelog.accept(list);
    }

    @Override
    public void getWarnLog(String player, Consumer<Set<Warn>> warnlog) {
        Set<Warn> list = new HashSet<>();
        for (String s : this.warns.getSection("Warn." + player).getAll().getKeys(false)) {
            String reason = this.warns.getString("Warn." + player + "." + s + ".Reason");
            String creator = this.warns.getString("Warn." + player + "." + s + ".Creator");
            String date = this.warns.getString("Warn." + player + "." + s + ".Date");
            list.add(new Warn(player, reason, s, creator, date));
        }
        warnlog.accept(list);
    }

    @Override
    public void clearBanlog(String player) {
        CompletableFuture.runAsync(() -> {
            Map<String, Object> map = this.banlog.getSection("Banlog").getAllMap();
            map.remove(player);
            this.banlog.set("Banlog", map);
            this.banlog.save();
            this.banlog.reload();
            Server.getInstance().getPluginManager().callEvent(new ClearBanlogEvent(player, "null"));
        });
    }

    @Override
    public void clearMutelog(String player) {
        CompletableFuture.runAsync(() -> {
            Map<String, Object> map = this.mutelog.getSection("Mutelog").getAllMap();
            map.remove(player);
            this.mutelog.set("Mutelog", map);
            this.mutelog.save();
            this.mutelog.reload();
            Server.getInstance().getPluginManager().callEvent(new ClearMutelogEvent(player, "null"));
        });
    }

    @Override
    public void clearWarns(String player) {
        CompletableFuture.runAsync(() -> {
            Map<String, Object> map = this.warns.getSection("Warn").getAllMap();
            map.remove(player);
            this.warns.set("Warn", map);
            this.warns.save();
            this.warns.reload();
            Server.getInstance().getPluginManager().callEvent(new ClearWarnlogEvent(player, "null"));
        });
    }

    @Override
    public void clearBanlog(String player, String executor) {
        CompletableFuture.runAsync(() -> {
            Map<String, Object> map = this.banlog.getSection("Banlog").getAllMap();
            map.remove(player);
            this.banlog.set("Banlog", map);
            this.banlog.save();
            this.banlog.reload();
            Server.getInstance().getPluginManager().callEvent(new ClearBanlogEvent(player, executor));
        });
    }

    @Override
    public void clearMutelog(String player, String executor) {
        CompletableFuture.runAsync(() -> {
            Map<String, Object> map = this.mutelog.getSection("Mutelog").getAllMap();
            map.remove(player);
            this.mutelog.set("Mutelog", map);
            this.mutelog.save();
            this.mutelog.reload();
            Server.getInstance().getPluginManager().callEvent(new ClearMutelogEvent(player, executor));
        });
    }

    @Override
    public void clearWarns(String player, String executor) {
        CompletableFuture.runAsync(() -> {
            Map<String, Object> map = this.warns.getSection("Warn").getAllMap();
            map.remove(player);
            this.warns.set("Warn", map);
            this.warns.save();
            this.warns.reload();
            Server.getInstance().getPluginManager().callEvent(new ClearWarnlogEvent(player, executor));
        });
    }

    @Override
    public void setBanReason(String player, String reason) {
        Server.getInstance().getPluginManager().callEvent(new EditBanReasonEvent(player, reason, "null"));
        this.bans.set("Ban." + player + ".Reason", reason);
        this.bans.save();
        this.bans.reload();
    }

    @Override
    public void setMuteReason(String player, String reason) {
        Server.getInstance().getPluginManager().callEvent(new EditMuteReasonEvent(player, reason, "null"));
        this.mutes.set("Mute." + player + ".Reason", reason);
        this.mutes.save();
        this.mutes.reload();
    }

    @Override
    public void setBanTime(String player, long time) {
        Server.getInstance().getPluginManager().callEvent(new EditBanTimeEvent(player, time, "null"));
        this.bans.set("Ban." + player + ".Time", time);
        this.bans.save();
        this.bans.reload();
    }

    @Override
    public void setMuteTime(String player, long time) {
        Server.getInstance().getPluginManager().callEvent(new EditMuteTimeEvent(player, time, "null"));
        this.mutes.set("Mute." + player + ".Time", time);
        this.mutes.save();
        this.mutes.reload();
    }

    @Override
    public void setBanReason(String player, String reason, String executor) {
        Server.getInstance().getPluginManager().callEvent(new EditBanReasonEvent(player, reason, executor));
        this.bans.set("Ban." + player + ".Reason", reason);
        this.bans.save();
        this.bans.reload();
    }

    @Override
    public void setMuteReason(String player, String reason, String executor) {
        Server.getInstance().getPluginManager().callEvent(new EditMuteReasonEvent(player, reason, executor));
        this.mutes.set("Mute." + player + ".Reason", reason);
        this.mutes.save();
        this.mutes.reload();
    }

    @Override
    public void setBanTime(String player, long time, String executor) {
        Server.getInstance().getPluginManager().callEvent(new EditBanTimeEvent(player, time, executor));
        this.bans.set("Ban." + player + ".Time", time);
        this.bans.save();
        this.bans.reload();
    }

    @Override
    public void setMuteTime(String player, long time, String executor) {
        Server.getInstance().getPluginManager().callEvent(new EditMuteTimeEvent(player, time, executor));
        this.mutes.set("Mute." + player + ".Time", time);
        this.mutes.save();
        this.mutes.reload();
    }

    @Override
    public void deleteBan(String id) {
        for (String s : this.banlog.getSection("Banlog").getAll().getKeys(false)) {
            if (this.banlog.exists("Banlog." + s + "." + id)) {
                Server.getInstance().getPluginManager().callEvent(new DeleteBanEvent(id, "null"));
                Map<String, Object> map = this.banlog.getSection("Banlog." + s).getAllMap();
                map.remove(id);
                this.banlog.set("Banlog." + s, map);
                this.banlog.save();
                this.banlog.reload();
            }
        }
    }

    @Override
    public void deleteMute(String id) {
        for (String s : this.mutelog.getSection("Mutelog").getAll().getKeys(false)) {
            if (this.mutelog.exists("Mutelog." + s + "." + id)) {
                Server.getInstance().getPluginManager().callEvent(new DeleteMuteEvent(id, "null"));
                Map<String, Object> map = this.mutelog.getSection("Mutelog." + s).getAllMap();
                map.remove(id);
                this.mutelog.set("Mutelog." + s, map);
                this.mutelog.save();
                this.mutelog.reload();
            }
        }
    }

    @Override
    public void deleteWarn(String id) {
        for (String s : this.warns.getSection("Warn").getAll().getKeys(false)) {
            if (this.warns.exists("Warn." + s + "." + id)) {
                Server.getInstance().getPluginManager().callEvent(new DeleteWarnEvent(id, "null"));
                Map<String, Object> map = this.warns.getSection("Warn." + s).getAllMap();
                map.remove(id);
                this.warns.set("Warn." + s, map);
                this.warns.save();
                this.warns.reload();
            }
        }
    }

    @Override
    public void deleteBan(String id, String executor) {
        for (String s : this.banlog.getSection("Banlog").getAll().getKeys(false)) {
            if (this.banlog.exists("Banlog." + s + "." + id)) {
                Server.getInstance().getPluginManager().callEvent(new DeleteBanEvent(id, executor));
                Map<String, Object> map = this.banlog.getSection("Banlog." + s).getAllMap();
                map.remove(id);
                this.banlog.set("Banlog." + s, map);
                this.banlog.save();
                this.banlog.reload();
            }
        }
    }

    @Override
    public void deleteMute(String id, String executor) {
        for (String s : this.mutelog.getSection("Mutelog").getAll().getKeys(false)) {
            if (this.mutelog.exists("Mutelog." + s + "." + id)) {
                Server.getInstance().getPluginManager().callEvent(new DeleteMuteEvent(id, executor));
                Map<String, Object> map = this.mutelog.getSection("Mutelog." + s).getAllMap();
                map.remove(id);
                this.mutelog.set("Mutelog." + s, map);
                this.mutelog.save();
                this.mutelog.reload();
            }
        }
    }

    @Override
    public void deleteWarn(String id, String executor) {
        for (String s : this.warns.getSection("Warn").getAll().getKeys(false)) {
            if (this.warns.exists("Warn." + s + "." + id)) {
                Server.getInstance().getPluginManager().callEvent(new DeleteWarnEvent(id, executor));
                Map<String, Object> map = this.warns.getSection("Warn." + s).getAllMap();
                map.remove(id);
                this.warns.set("Warn." + s, map);
                this.warns.save();
                this.warns.reload();
            }
        }
    }

    @Override
    public String getProvider() {
        return "Yaml";
    }

}
