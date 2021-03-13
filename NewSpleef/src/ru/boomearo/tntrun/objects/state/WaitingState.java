package ru.boomearo.tntrun.objects.state;

import ru.boomearo.gamecontrol.objects.states.IWaitingState;
import ru.boomearo.tntrun.managers.SpleefManager;
import ru.boomearo.tntrun.objects.SpleefArena;
import ru.boomearo.tntrun.objects.SpleefPlayer;
import ru.boomearo.tntrun.objects.playertype.LosePlayer;
import ru.boomearo.tntrun.objects.playertype.PlayingPlayer;

public class WaitingState implements IWaitingState {

    private final SpleefArena arena;
    
    public WaitingState(SpleefArena arena) {
        this.arena = arena;
    }
    
    @Override
    public String getName() {
        return "§6Ожидание игроков";
    }
    
    @Override
    public SpleefArena getArena() {
        return this.arena;
    }
    
    @Override 
    public void initState() {
        this.arena.sendMessages(SpleefManager.prefix + "Ожидание игроков..");
        
        for (SpleefPlayer tp : this.arena.getAllPlayers()) {
            //Возвращаем умерших к жизни так сказать.
            if (tp.getPlayerType() instanceof LosePlayer) {
                tp.setPlayerType(new PlayingPlayer());
            }
            
            tp.getPlayerType().preparePlayer(tp);
        }
    }
    
    @Override
    public void autoUpdateHandler() {
        //Если мы набрали минимум то меняем статус
        if (this.arena.getAllPlayersType(PlayingPlayer.class).size() >= this.arena.getMinPlayers()) {
            this.arena.setState(new StartingState(this.arena));
        }
        
        for (SpleefPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();
            
            if (!this.arena.getArenaRegion().isInRegion(tp.getPlayer().getLocation())) {
                tp.getPlayerType().preparePlayer(tp);
            }
        }
    }


}
