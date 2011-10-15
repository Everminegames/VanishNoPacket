package to.joe.vanish;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;

public class VanishAnnounceManipulator {
    private ArrayList<String> delayedAnnounce;
    private final Object syncLogin = new Object();
    private final VanishPlugin plugin;
    private final String fakeJoin;
    private final String fakeQuit;
    private final boolean delayedJoinTracking;
    private final HashMap<String, Boolean> status;

    public VanishAnnounceManipulator(VanishPlugin plugin, String fakeJoin, String fakeQuit, boolean delayedJoinTracking) {
        this.plugin = plugin;
        this.fakeJoin = fakeJoin.replace("&&", "\167");
        this.fakeQuit = fakeQuit.replace("&&", "\167");
        this.delayedJoinTracking = delayedJoinTracking;
        this.status = new HashMap<String, Boolean>();
        synchronized (this.syncLogin) {
            this.delayedAnnounce = new ArrayList<String>();
        }
    }

    public void addToDelayedAnnounce(String player) {
        this.status.put(player, false);
        if (!this.delayedJoinTracking) {
            return;
        }
        synchronized (this.syncLogin) {
            this.delayedAnnounce.add(player);
        }
    }

    public boolean delayedAnnounceKill(String player) {
        if (this.delayedAnnounce.contains(player)) {
            this.delAnnounce(player);
            return true;
        }
        return false;
    }

    public void fakeJoin(String player) {
        if (this.status.containsKey(player) && this.status.get(player)) {
            return;
        }
        this.plugin.getServer().broadcastMessage(ChatColor.YELLOW + this.fakeJoin.replace("%p", player));
        this.plugin.log(player + " faked joining");
        this.status.put(player, true);
    }

    public void fakeQuit(String player) {
        if (this.status.containsKey(player) && !this.status.get(player)) {
            return;
        }
        this.plugin.getServer().broadcastMessage(ChatColor.YELLOW + this.fakeQuit.replace("%p", player));
        this.plugin.log(player + " faked quitting");
        this.status.put(player, false);
    }

    public void toggled(String player) {
        if (!this.delayedJoinTracking || !this.delayedAnnounce.contains(player)) {
            return;
        }
        this.fakeJoin(player);
        this.delAnnounce(player);
        this.status.put(player, true);
    }

    public boolean wasPlayerMarkedOnline(String player) {
        if (this.status.containsKey(player)) {
            return this.status.remove(player);
        }
        return true;
    }

    private void delAnnounce(String player) {
        synchronized (this.syncLogin) {
            this.delayedAnnounce.remove(player);
        }
    }
}
