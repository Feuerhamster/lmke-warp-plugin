package dev.lmke.mc.warps.services;

import dev.lmke.mc.warps.LMKEWarps;
import dev.lmke.mc.warps.utils.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public class EconomyService {
    public static boolean economyAvailable() {
        Economy eco = LMKEWarps.getEconomy();
        return eco != null;
    }

    public static boolean warpEconomyEnabled() {
        if (!economyAvailable()) return false;

        return ConfigManager.getConfig().getBoolean("warp.enable_economy");
    }

    public static boolean POIEconomyEnabled() {
        if (!economyAvailable()) return false;

        return ConfigManager.getConfig().getBoolean("poi.enable_economy");
    }

    public static boolean playerHasMoney(Player p, double money) {
        Economy eco = LMKEWarps.getEconomy();

        double bal = eco.getBalance(p);

        return bal >= money;
    }

    /**
     * Modify the balance of a player
     *
     * @param p     Player
     * @param money Amount of money (negative for deposit, positive for withdraw)
     */
    public static void modifyBalance(Player p, double money) {
        Economy eco = LMKEWarps.getEconomy();

        if (money > 0) {
            EconomyResponse res = eco.withdrawPlayer(p, money);
            res.transactionSuccess();
        } else if (money < 0) {
            EconomyResponse res = eco.depositPlayer(p, Math.abs(money));
            res.transactionSuccess();
        }

    }

    public static double getWarpCreatePrice() {
        return ConfigManager.getConfig().getDouble("warp.create_cost");
    }

    public static double getWarpDeletePrice() {
        return ConfigManager.getConfig().getDouble("warp.delete_cost");
    }

    public static double getPOICreatePrice() {
        return ConfigManager.getConfig().getDouble("poi.create_cost");
    }

    public static double getPOIDeletePrice() {
        return ConfigManager.getConfig().getDouble("poi.delete_cost");
    }
}
