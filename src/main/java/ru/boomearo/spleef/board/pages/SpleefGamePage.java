package ru.boomearo.spleef.board.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.boomearo.board.objects.boards.AbstractHolder;
import ru.boomearo.board.objects.boards.AbstractPage;
import ru.boomearo.board.objects.boards.AbstractPageList;
import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefPlayer;
import ru.boomearo.spleef.objects.playertype.LosePlayer;
import ru.boomearo.spleef.objects.playertype.PlayingPlayer;
import ru.boomearo.spleef.objects.state.EndingState;
import ru.boomearo.spleef.objects.state.RunningState;

public class SpleefGamePage extends AbstractPage {

    private final SpleefPlayer spPlayer;

    public SpleefGamePage(AbstractPageList pageList, SpleefPlayer spPlayer) {
        super(pageList);
        this.spPlayer = spPlayer;
    }

    @Override
    public int getTimeToChange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public String getTitle() {
        return SpleefManager.gameNameDys;
    }

    @Override
    protected List<AbstractHolder> createHolders() {
        List<AbstractHolder> holders = new ArrayList<AbstractHolder>();

        holders.add(new AbstractHolder(this) {

            @Override
            public String getText() {
                return SpleefManager.mainColor + new SimpleDateFormat("dd/MM/yyyy").format(new Date(System.currentTimeMillis()));
            }

        });

        holders.add(new AbstractHolder(this) {

            @Override
            public String getText() {
                return " ";
            }

        });

        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return SpleefManager.mainColor + "Карта: '" + SpleefManager.variableColor + spPlayer.getArena().getName() + SpleefManager.mainColor + "'";
            }

        });

        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return " ";
            }

        });

        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return SpleefManager.mainColor + "Статус: " + spPlayer.getArena().getState().getName();
            }

            @Override
            public long getMaxCacheTime() {
                return 0;
            }

        });

        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                IGameState state = spPlayer.getArena().getState();
                if (state instanceof RunningState) {
                    RunningState rs = (RunningState) state;

                    return SpleefManager.mainColor + "До конца: " + SpleefManager.variableColor + getFormattedTimeLeft(rs.getCount());
                }
                else if (state instanceof EndingState) {
                    EndingState es = (EndingState) state;
                    return SpleefManager.mainColor + "Новая игра: " + SpleefManager.variableColor + getFormattedTimeLeft(es.getCount());
                }
                return " ";
            }

            @Override
            public long getMaxCacheTime() {
                return 0;
            }

        });

        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return SpleefManager.mainColor + "Наблюдателей: " + SpleefManager.variableColor + spPlayer.getArena().getAllPlayersType(LosePlayer.class).size();
            }

        });

        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return " ";
            }

        });


        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return SpleefManager.mainColor + "Игроков: " + SpleefManager.variableColor + spPlayer.getArena().getAllPlayersType(PlayingPlayer.class).size();
            }

        });

        return holders;
    }

    private static String getFormattedTimeLeft(int time) {
        int min = 0;
        int sec = 0;
        String minStr = "";
        String secStr = "";

        min = (int) Math.floor(time / 60);
        sec = time % 60;

        minStr = (min < 10) ? "0" + String.valueOf(min) : String.valueOf(min);
        secStr = (sec < 10) ? "0" + String.valueOf(sec) : String.valueOf(sec);

        return minStr + ":" + secStr;
    }
}
