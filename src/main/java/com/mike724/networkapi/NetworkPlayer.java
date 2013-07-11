package com.mike724.networkapi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

public class NetworkPlayer {
    private String player;
    private Integer tokens;
    private Integer cash;
    private Boolean isBanned;
    private Boolean isOnline;
    private NetworkRank rank;
    private long joinDate;

    public NetworkPlayer(String player, Integer tokens, Integer cash, Boolean isBanned, Boolean isOnline, NetworkRank rank, long joinDate) {
        this.player = player;
        this.tokens = tokens;
        this.cash = cash;
        this.isBanned = isBanned;
        this.isOnline = isOnline;
        this.rank = rank;
        this.joinDate = joinDate;
    }

    public NetworkPlayer(String player) {
        this(player, 0, 0, false, false, NetworkRank.USER,System.currentTimeMillis());
    }

    public Integer getTokens() {
        return tokens;
    }

    public String getPlayer() {
        return player;
    }

    public Boolean isBanned() {
        return isBanned;
    }

    public Boolean isOnline() {
        return isOnline;
    }

    public NetworkRank getRank() {
        return rank;
    }

    public void setOnline(Boolean b) {
        isOnline = b;
    }

    public void setRank(NetworkRank nr) {
        rank = nr;
    }

    public void setTokens(Integer i) {
        tokens = i;
    }

    public void setPlayer(String s) {
        player = s;
    }

    public void setBanned(Boolean b) {
        isBanned = b;
    }

    public Integer getCash() {
        return cash;
    }

    public void setCash(Integer cash) {
        this.cash = cash;
    }

    public void updateWallet() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("currency", "display");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("Wallet");
        Score tokens = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Tokens:"));
        tokens.setScore(this.getTokens());
        Score cash = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "Cash:"));
        cash.setScore(this.getCash());
        Bukkit.getPlayer(this.getPlayer()).setScoreboard(board);
    }
}