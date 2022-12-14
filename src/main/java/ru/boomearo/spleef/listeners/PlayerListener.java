package ru.boomearo.spleef.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import ru.boomearo.gamecontrol.objects.states.game.IGameState;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefPlayer;
import ru.boomearo.spleef.objects.SpleefTeam;
import ru.boomearo.spleef.objects.playertype.LosePlayer;
import ru.boomearo.spleef.objects.state.RunningState;
import ru.boomearo.spleef.objects.state.RunningState.BlockOwner;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e) {
        Player pl = e.getEntity();

        SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
        if (tp != null) {
            LosePlayer lp = new LosePlayer();
            tp.setPlayerType(lp);

            e.setDroppedExp(0);
            e.getDrops().clear();
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent e) {
        Player pl = e.getPlayer();

        SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
        if (tp != null) {
            SpleefTeam team = tp.getTeam();
            Location loc = team.getSpawnPoint();
            if (loc != null) {
                e.setRespawnLocation(loc);
            }
            tp.getPlayerType().preparePlayer(tp);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player pl = e.getPlayer();

        String msg = e.getMessage();
        if (msg.equalsIgnoreCase("/spleef leave") || msg.equalsIgnoreCase("/lobby") || msg.equalsIgnoreCase("/spawn")) {
            return;
        }

        SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
        if (tp != null) {
            e.setCancelled(true);
            pl.sendMessage(SpleefManager.prefix + "???? ???? ???????????? ???????????????????????? ?????? ?????????????? ?? ????????!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreakEvent(BlockBreakEvent e) {
        Player pl = e.getPlayer();

        SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
        if (tp != null) {

            //???????? ?????????? ???????????? ?? ?????????? ???????? ????????, ???? ??????????????????
            //?? ???????? ???? ?????????? ???????? ????????, ???? ????????????
            Block b = e.getBlock();
            if (isAllowedToBreak(b.getType())) {

                IGameState state = tp.getArena().getState();
                if (state instanceof RunningState rs) {

                    BlockOwner bo = rs.getBlockByLocation(b.getLocation());
                    if (bo == null) {
                        rs.addBlock(b, tp);
                    }

                    e.setExpToDrop(0);
                    e.setDropItems(false);
                    e.setCancelled(false);
                    return;
                }
            }

            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            Player pl = e.getPlayer();
            SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
            if (tp != null) {

                IGameState state = tp.getArena().getState();
                if (state instanceof RunningState) {
                    e.setCancelled(false);
                    return;
                }

                e.setCancelled(true);
            }
        }
    }

    private static boolean isAllowedToBreak(Material mat) {
        return mat == Material.SNOW_BLOCK || mat == Material.PODZOL;
    }
}
