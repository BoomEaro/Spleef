package ru.boomearo.tntrun.runnable;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.tntrun.Spleef;
import ru.boomearo.tntrun.objects.SpleefArena;

public class ArenasRunnable extends BukkitRunnable {
    
    public ArenasRunnable() {
        runnable();
    }
    
    private void runnable() {
        this.runTaskTimer(Spleef.getInstance(), 1, 1);
    }
    
    @Override
    public void run() {
        for (SpleefArena arena : Spleef.getInstance().getTntRunManager().getAllArenas()) {
            
            IGameState state = arena.getState();
            
            state.autoUpdateHandler();
        }
    }
}
