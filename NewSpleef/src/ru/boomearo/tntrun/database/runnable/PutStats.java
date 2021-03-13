package ru.boomearo.tntrun.database.runnable;

import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.gamecontrol.objects.statistics.StatsPlayer;
import ru.boomearo.tntrun.Spleef;
import ru.boomearo.tntrun.database.Sql;
import ru.boomearo.tntrun.objects.statistics.SpleefStatsType;

public class PutStats extends BukkitRunnable {

    private final SpleefStatsType type;
	private final StatsPlayer player;
	
	public PutStats(SpleefStatsType type, StatsPlayer player) {
		this.player = player;
		this.type = type;
		runnable();
	}
	
	private void runnable() {
		this.runTaskAsynchronously(Spleef.getInstance());
	}
	
	@Override
	public void run() {
		try {
			Sql.getInstance().putStatsData(this.type, this.player.getName(), this.player.getValue());
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
