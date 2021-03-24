package ru.boomearo.spleef.board;

import java.util.ArrayList;
import java.util.List;

import ru.boomearo.board.objects.PlayerBoard;
import ru.boomearo.board.objects.boards.AbstractPage;
import ru.boomearo.board.objects.boards.AbstractPageList;
import ru.boomearo.spleef.board.pages.SpleefGamePage;
import ru.boomearo.spleef.board.pages.SpleefLobbyPage;
import ru.boomearo.spleef.objects.SpleefPlayer;

public class SpleefPageList extends AbstractPageList {

    private final SpleefPlayer spPlayer;
    
    public SpleefPageList(PlayerBoard player, SpleefPlayer spPlayer) {
        super(player);
        this.spPlayer = spPlayer;
    }

    @Override
    protected List<AbstractPage> createPages() {
        List<AbstractPage> pages = new ArrayList<AbstractPage>();
        
        pages.add(new SpleefLobbyPage(this, this.spPlayer));
        pages.add(new SpleefGamePage(this, this.spPlayer));
        
        return pages;
    }

}
