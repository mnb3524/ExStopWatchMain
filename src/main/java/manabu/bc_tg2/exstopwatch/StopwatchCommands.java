package manabu.bc_tg2.exstopwatch;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StopwatchCommands implements CommandExecutor {

    private ExStopWatch exStopWatch;

    public StopwatchCommands(ExStopWatch plugin) {
        this.exStopWatch = plugin;
    }

    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
        if (!s.hasPermission("stopwatch.admin")) {
            s.sendMessage(ChatColor.RED + "You have no permission!");
            return false;
        }

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("stop")) {
                exStopWatch.stopStopwatch(s);
            } else if (args[0].equalsIgnoreCase("start")) {
                exStopWatch.startStopwatch();
            } else if (args[0].equalsIgnoreCase("pause")) {
                exStopWatch.pauseStopwatch(s);
            }  else if (args[0].equalsIgnoreCase("show")) {
                exStopWatch.showStopwatch(s, args);
            }  else if (args[0].equalsIgnoreCase("showall")) {
                exStopWatch.showAllStopwatch(s);
            } else if (args[0].equalsIgnoreCase("help")) {
                exStopWatch.showHelp(s);
            } else if (args[0].equalsIgnoreCase("item")) {
                exStopWatch.getItems(s);
            } else if (args[0].equalsIgnoreCase("offset")) {
                exStopWatch.offsetSeconds(s, args);
            } else if (args[0].equalsIgnoreCase("message")) {
                exStopWatch.setMessage(s, args);
            } else if (args[0].equalsIgnoreCase("endsec")) {
                exStopWatch.setEndSec(s, args);
            }
        } else {
            s.sendMessage(ChatColor.RED + "Do /[stopwatch|st] pause/stop/start/help/item/message/offset/endsec");
        }
        return false;
    }
}
