package ru.boomearo.tntrun.objects.playertype;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.tntrun.Spleef;
import ru.boomearo.tntrun.objects.ItemButton;
import ru.boomearo.tntrun.objects.SpleefArena;
import ru.boomearo.tntrun.objects.SpleefPlayer;
import ru.boomearo.tntrun.utils.ExpFix;

public class PlayingPlayer implements IPlayerType {
    
    private String killer;
    
    @Override
    public void preparePlayer(SpleefPlayer player) {
        if (Bukkit.isPrimaryThread()) {
            task(player);
        }
        else {
            Bukkit.getScheduler().runTask(Spleef.getInstance(), () -> {
                task(player);
            });
        }
    }
    
    public String getKiller() {
        return this.killer;
    }
    
    public void setKiller(String killer) {
        this.killer = killer;
    }
    
    private void task(SpleefPlayer player) {
        Player pl = player.getPlayer();
        
        pl.setFoodLevel(20);
        pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        
        pl.setGameMode(GameMode.ADVENTURE);
        pl.setFlying(false);
        pl.setAllowFlight(false);
        
        ExpFix.setTotalExperience(player.getPlayer(), 0);
        
        Inventory inv = pl.getInventory();
        inv.clear();
        
        for (ItemButton ib : ItemButton.values()) {
            inv.setItem(ib.getSlot(), ib.getItem());
        }

        SpleefArena arena = player.getArena();
        
        GameControl.getInstance().asyncTeleport(pl, arena.getRandomSpawnLocation());
    }
    
}