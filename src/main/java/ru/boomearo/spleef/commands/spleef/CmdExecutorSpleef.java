package ru.boomearo.spleef.commands.spleef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.command.TabCompleter;
import ru.boomearo.serverutils.utils.other.commands.AbstractExecutor;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefArena;

public class CmdExecutorSpleef extends AbstractExecutor implements TabCompleter {

    private static final List<String> empty = new ArrayList<>();

    public CmdExecutorSpleef() {
        super(new SpleefUse());
    }

    @Override
    public boolean zeroArgument(CommandSender sender) {
        sendUsageCommands(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> ss = new ArrayList<>(Arrays.asList("join", "leave", "list"));
            if (sender.hasPermission("spleef.admin")) {
                ss.add("createarena");
                ss.add("setspawnpoint");
            }
            List<String> matches = new ArrayList<>();
            String search = args[0].toLowerCase();
            for (String se : ss) {
                if (se.toLowerCase().startsWith(search)) {
                    matches.add(se);
                }
            }
            return matches;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("join")) {
                List<String> matches = new ArrayList<>();
                String search = args[1].toLowerCase();
                for (SpleefArena arena : Spleef.getInstance().getSpleefManager().getAllArenas()) {
                    if (arena.getName().toLowerCase().startsWith(search)) {
                        matches.add(arena.getName());
                    }
                }
                return matches;
            }
        }
        return empty;
    }

    @Override
    public String getPrefix() {
        return SpleefManager.prefix;
    }

    @Override
    public String getSuffix() {
        return " §8-" + SpleefManager.variableColor + " ";
    }
}
