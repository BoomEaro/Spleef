package ru.boomearo.spleef.objects.state;

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.objects.states.game.AbstractRegenState;
import ru.boomearo.gamecontrol.objects.states.perms.SpectatorFirst;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefArena;
import ru.boomearo.spleef.objects.SpleefPlayer;

public class RegenState extends AbstractRegenState implements SpectatorFirst {

    private final SpleefArena arena;

    public RegenState(SpleefArena arena) {
        this.arena = arena;
    }

    @Override
    public String getName() {
        return "§6Регенерация арены";
    }

    @Override
    public SpleefArena getArena() {
        return this.arena;
    }

    @Override
    public void initState() {
        this.arena.sendMessages(SpleefManager.prefix + "Начинаем регенерацию арены..");

        try {
            setWaitingRegen(true);
            GameControl.getInstance().getGameManager().queueRegenArena(this.arena);
        }
        catch (ConsoleGameException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void autoUpdateHandler() {
        for (SpleefPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();

            if (!this.arena.getArenaRegion().isInRegionPoint(tp.getPlayer().getLocation())) {
                tp.getPlayerType().preparePlayer(tp);
            }
        }

        if (!isWaitingRegen()) {
            this.arena.setState(new WaitingState(arena));
        }
    }

}
