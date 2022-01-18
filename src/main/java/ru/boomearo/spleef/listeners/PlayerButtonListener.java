package ru.boomearo.spleef.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.objects.ItemButton;
import ru.boomearo.spleef.objects.SpleefPlayer;
import ru.boomearo.spleef.objects.ItemButton.ButtonClick;

public class PlayerButtonListener implements Listener {

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        Player pl = e.getPlayer();
        SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
        if (tp != null) {
            
            Action ac = e.getAction();
            if (ac == Action.RIGHT_CLICK_AIR || ac == Action.RIGHT_CLICK_BLOCK) {
                ItemStack item = e.getItem();
                if (item != null) {
                    ItemButton ib = ItemButton.getButtonByItem(item);
                    if (ib != null) {
                        ButtonClick click = ib.getClick();
                        if (click != null) {
                            click.click(tp);
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    
}
