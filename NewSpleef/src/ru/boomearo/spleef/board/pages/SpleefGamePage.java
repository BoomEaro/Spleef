package ru.boomearo.spleef.board.pages;

import java.util.ArrayList;
import java.util.List;

import ru.boomearo.board.objects.boards.AbstractHolder;
import ru.boomearo.board.objects.boards.AbstractPage;
import ru.boomearo.board.objects.boards.AbstractPageList;
import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.gamecontrol.utils.DateUtil;
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
            protected String getText() {
                return "§7Арена: §7'§b" + spPlayer.getArena().getName() + "§7'";
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
                return "§7Статус: " + spPlayer.getArena().getState().getName();
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
                    
                    return "§7Игра закончится через: §b" + DateUtil.formatedTime(rs.getCount(), false, true);
                }
                else if (state instanceof EndingState) {
                    EndingState es = (EndingState) state;
                    return "§7Новая игра через: §b" + DateUtil.formatedTime(es.getCount(), false, true);
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
                return "§7Наблюдателей: §b" + spPlayer.getArena().getAllPlayersType(LosePlayer.class).size();
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
                return "§7Игроков: §b" + spPlayer.getArena().getAllPlayersType(PlayingPlayer.class).size();
            }

        });
        
        return holders;
    }

}
