package ru.boomearo.spleef.objects.state;

import org.bukkit.Location;
import org.bukkit.Sound;

import ru.boomearo.gamecontrol.objects.states.game.ICountable;
import ru.boomearo.gamecontrol.objects.states.game.IStartingState;
import ru.boomearo.serverutils.utils.other.DateUtil;
import ru.boomearo.serverutils.utils.other.DistanceUtils;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefArena;
import ru.boomearo.spleef.objects.SpleefPlayer;
import ru.boomearo.spleef.objects.SpleefTeam;
import ru.boomearo.spleef.objects.playertype.PlayingPlayer;

public class StartingState implements IStartingState, ICountable {

    private final SpleefArena arena;

    private int count = 30;

    private int cd = 20;

    public StartingState(SpleefArena arena) {
        this.arena = arena;
    }

    @Override
    public String getName() {
        return "§aНачало игры";
    }

    @Override
    public SpleefArena getArena() {
        return this.arena;
    }

    @Override
    public void initState() {
        this.arena.sendMessages(SpleefManager.prefix + "Начинаем игру!");
    }

    @Override
    public void autoUpdateHandler() {
        for (SpleefPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();

            if (!this.arena.getArenaRegion().isInRegionPoint(tp.getPlayer().getLocation())) {
                tp.getPlayerType().preparePlayer(tp);
            }
        }

        //Если на арене вообще нет игроков то переходим в ожидание. (малоли)
        if (this.arena.getAllPlayersType(PlayingPlayer.class).size() <= 0) {
            this.arena.setState(new WaitingState(this.arena));
            return;
        }

        handleCount(this.arena);

    }


    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    private void handleCount(SpleefArena arena) {
        if (this.cd <= 0) {
            this.cd = 20;

            //Если прошло 30 сек
            if (this.count <= 0) {

                if (!this.arena.isForceStarted()) {
                    //Если игроков не достаточно для игры, то возвращаемся в ожидание
                    if (this.arena.getAllPlayersType(PlayingPlayer.class).size() < this.arena.getMinPlayers()) {
                        this.arena.sendMessages(SpleefManager.prefix + "Не достаточно игроков для старта!");
                        this.arena.setState(new WaitingState(this.arena));
                        return;
                    }
                }


                arena.setState(new RunningState(arena, arena.getTimeLimit()));
                return;
            }

            if (this.count <= 5) {
                for (SpleefPlayer tp : this.arena.getAllPlayers()) {
                    handleTp(tp, false, true);
                }
            }

            arena.sendLevels(this.count);

            if (this.count <= 5) {
                arena.sendMessages(SpleefManager.prefix + "Игра начнется через " + SpleefManager.variableColor + DateUtil.formatedTime(this.count, false));
                arena.sendSounds(Sound.BLOCK_NOTE_BLOCK_PLING, 999, 2);
            }
            else {
                if ((this.count % 5) == 0) {
                    arena.sendMessages(SpleefManager.prefix + "Игра начнется через " + SpleefManager.variableColor + DateUtil.formatedTime(this.count, false));
                    arena.sendSounds(Sound.BLOCK_NOTE_BLOCK_PLING, 999, 2);
                }
            }

            this.count--;

            return;
        }

        this.cd--;
    }

    private void handleTp(SpleefPlayer tp, boolean force, boolean alsoOther) {
        //Обычных игроков ТОЛЬКО телепортируем
        if (tp.getPlayerType() instanceof PlayingPlayer) {

            SpleefTeam team = tp.getTeam();
            Location loc = team.getSpawnPoint();
            if (loc != null) {
                //Если игрок будет дальше одного блока то тепаем назад
                if (DistanceUtils.distance2DCircle(loc, tp.getPlayer().getLocation()) >= 0.8d || force) {
                    tp.getPlayer().teleport(loc);
                }
            }
            return;
        }

        if (alsoOther) {
            tp.getPlayerType().preparePlayer(tp);
        }
    }


}
