package manabu.bc_tg2.exstopwatch;
import manabu.bc_tg2.exstopwatch.actionbar.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.apache.commons.lang.time.StopWatch;
import java.util.Timer;
import java.util.TimerTask;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class ExStopWatch extends JavaPlugin {

    private boolean isRunning, isPaused;
    private StopWatch stopWatch;
    private Timer timer;
    private File configFile;
    private YamlConfiguration config;
    private String timeMessage;
    private int offsetSec;
    private int startSec = 70;
    private int endSec = Integer.MAX_VALUE;
    private Actionbar actionbar;
    private UUID curPlayer;

    @Override
    public void onEnable() {
        if (!setupActionbar()) {
            getLogger().severe("Failed to setup Actionbar!");
            getLogger().severe("Your server version is not compatible with this plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        this.configFile = getConfig("config", this);
        this.reload();
        if (!this.config.isSet("timerMessage")) {
            this.config.set("timerMessage", "&6{0}");
            this.save();
        }
        timeMessage = this.config.getString("timerMessage");
        stopWatch = new StopWatch();

        isRunning = false;
        curPlayer = null;
        isPaused = false;
        getCommand("stopwatch").setExecutor(new StopwatchCommands(this));
        Bukkit.getPluginManager().registerEvents(new ItemListener(this), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (isRunning) {

//                  int totalSecs = (int) (stopWatch.getTime()/1000.0) + offsetSec;
                    int totalSecs = (int) ((stopWatch.getTime())/1000.0) + offsetSec;
                    int countdownSecs = startSec - totalSecs;
                    int hours = (int)(countdownSecs/3600);
                    int minutes = (int)((countdownSecs % 3600) / 60);
                    int seconds = countdownSecs % 60;

                    String time;
                    if (hours > 0) {
                        time = String.format("%01d:%02d:%02d", hours, minutes, seconds);
                    } else {
                        time = String.format("%01d:%02d", minutes, seconds,startSec);
                    }
                    String timerMessage = Util.replace(timeMessage, time);

                    //System.out.println(timerMessage);
                    if (curPlayer != null) {
                        if (Bukkit.getOfflinePlayer(curPlayer).isOnline()) {
                            if (Bukkit.getPlayer(curPlayer).hasPermission("stopwatch.default")) {
                                actionbar.sendActionbar(Bukkit.getPlayer(curPlayer), timerMessage);
                            }
                        } else {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.hasPermission("stopwatch.default")) {
                                    actionbar.sendActionbar(p, timerMessage);
                                }
                            }
                        }
                    } else {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.hasPermission("stopwatch.default")) {
                                actionbar.sendActionbar(p, timerMessage);
                            }
                        }
                    }
                    if (totalSecs >= endSec || countdownSecs <= 0) {
                        if(countdownSecs == 0) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.resetTitle();
                                p.sendTitle("??????", "???????????????", 20, 50, 20);
                                p.playSound(p.getLocation(),"entity.generic.explode",(float)1.0,(float)1.0);
                                p.playSound(p.getLocation(),"random.explode",(float)1.0,(float)1.0);
                            }
                        }

                        stopStopwatch(Bukkit.getConsoleSender());
                    }
                }
            }
        }.runTaskTimer(this, 1L, 1L);
    }

    //Command methods

    public void showHelp(CommandSender s) {
        s.sendMessage(ChatColor.GREEN + "/st pause will either pause or unpause the timer\n" +
                "/st start will start the timer and restart if already started\n" +
                "/st stop will stop the timer\n" +
                "/st showall will show the timer message to everyone (default)\n" +
                "/st show [player] will show the timer to the chosen player to show all do st showall\n" +
                "/st item will give you items to use the 3 time control commands\n" +
                "/st message [message] will set the message of the time, make sure to include {0} in it which represents time\n" +
                "/st offset [seconds] will add to the offset seconds by the amount given until next stopwatch reset\n" +
                "/st endsec ????????????\n" +
                "/st startsec ??????????????????????????????(??????????????????1:10)");
    }

    public void startStopwatch() {
        stopWatch.reset();
        offsetSec = 0;
        endSec = Integer.MAX_VALUE;
        //System.out.println("started");
        stopWatch.start();
        isRunning = true;
        isPaused = false;
    }

    public void showAllStopwatch(CommandSender s) {
        curPlayer = null;
        s.sendMessage(ChatColor.GREEN + "?????????????????????????????????????????????????????????");
    }

    public void showStopwatch(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(ChatColor.RED + "????????????????????????????????????????????????????????????????????? /st show [playername]");
            return;
        }
        String playername = args[1];
        Player player = Bukkit.getPlayer(playername);
        if (player == null) {
            s.sendMessage(ChatColor.RED + "?????????????????????????????????????????????");
            return;
        }
        curPlayer = player.getUniqueId();
        s.sendMessage(ChatColor.GREEN + "??????????????? " + player.getName() + "????????????????????????");
    }

    public void stopStopwatch(CommandSender s) {
        if (!isRunning) {
            s.sendMessage(ChatColor.RED + "????????????????????????????????????????????????");

        } else {
            // System.out.println("stopped");
            stopWatch.stop();
            isRunning = false;
            isPaused = false;
        }
    }

    public void pauseStopwatch(CommandSender s) {
        if (!isRunning) {
            s.sendMessage(ChatColor.RED + "??????????????????????????????????????????");
        } else {
            if (isPaused) {
                stopWatch.resume();
                //System.out.println("un paused");
            } else {
                stopWatch.suspend();
                //System.out.println("paused");
            }
            isPaused = !isPaused;
        }
    }

    public void getItems(CommandSender s) {
        if (!(s instanceof Player)) {
            s.sendMessage(ChatColor.RED + "??????????????????????????????????????????????????????????????????");
            return;
        }
        Player p = (Player) s;
        p.getInventory().setItem(0, ItemListener.getStartItem());
        p.getInventory().setItem(1, ItemListener.getStopItem());
        p.getInventory().setItem(2, ItemListener.getPauseItem());

    }

    public void setMessage(CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(ChatColor.RED + "Specify a message, like this /st message &6Time: {0}");
            return;
        }

        String message = args[1];

        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                message = message + " " + args[i];
            }
        }

        if (!Util.isPlaceholderInMessage(message)) {
            s.sendMessage(ChatColor.RED + "The message " + message + " must contain {0}!");
            return;
        }
        timeMessage = message;
        this.config.set("timerMessage", message);
        s.sendMessage("Timer message set to " + Util.replace(Util.colour(timeMessage), "20"));
        this.save();
    }

    public void offsetSeconds (CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(ChatColor.RED + "Specify offset seconds like 40 or -50, the offset seconds go back to 0 once timer is reset, current offset is " + offsetSec);
            return;
        }
        try {
            int offsetArg = Integer.parseInt(args[1]);
            offsetSec += offsetArg;
            //s.sendMessage(ChatColor.GREEN + "Successfully offset by " + offsetArg +" current offset now is " + offsetSec);
            s.sendMessage(ChatColor.GREEN + "?????????????????? " + offsetArg +" ???????????? ??????????????????????????? " + offsetSec);
        } catch (IllegalArgumentException e) {
            s.sendMessage(ChatColor.RED + "??????(integer)??????????????????????????????");
            return;
        }
    }

    public void setEndSec (CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(ChatColor.RED + "???????????????????????????????????????, ???????????? " + endSec);
            return;
        }
        try {
            //int offsetArg = Integer.parseInt(args[1]);
            //endSec = offsetArg;
            s.sendMessage(ChatColor.RED + "?????????????????????????????????????????????end??????????????????????????????");
        } catch (IllegalArgumentException e) {
            s.sendMessage(ChatColor.RED + "??????(integer)??????????????????????????????");
            return;
        }
    }

    public void setStartSec (CommandSender s, String[] args) {
        if (args.length == 1) {
            s.sendMessage(ChatColor.RED + "???????????????(??????40)???????????????????????????, ???????????? " + startSec);
            return;
        }
        try {
            int offsetArg = Integer.parseInt(args[1]);
            startSec = offsetArg;
            s.sendMessage(ChatColor.GREEN + "??????????????? " + startSec + "????????????????????????");
        } catch (IllegalArgumentException e) {
            s.sendMessage(ChatColor.RED + "??????(integer)??????????????????????????????");
            return;
        }
    }

    //Config methods

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
    }

    public void save() {
        try {
            this.config.save(this.configFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getConfig(final String name, final Plugin plugin) {
        final File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        final File configFile = new File(plugin.getDataFolder() + File.separator + name + ".yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return configFile;
    }

    private boolean setupActionbar() {
        String version;
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        } catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
            return false;
        }
        getLogger().info("????????????????????????????????? " + version + "?????????1.10???????????????????????????");
//            //  we are running 1.10+ where you can use ChatMessageType
        actionbar = new ActionbarModern();

//        if (version.equals("v1_7_R4")) {
//
//        } else if (version.equals("v1_8_R1")) {
//            //server is running 1.8-1.8.1 so we need to use the 1.8 R1 NMS class
//            actionbar = new Actionbar_1_8_R1();
//        } else if (version.equals("v1_8_R2")) {
//            //server is running 1.8.3 so we need to use the 1.8 R2 NMS class
//            actionbar = new Actionbar_1_8_R2();
//        } else if (version.equals("v1_8_R3")) {
//            //server is running 1.8.4 - 1.8.8 so we need to use the 1.8 R3 NMS class
//            actionbar = new Actionbar_1_8_R3();
//        } else if (version.equals("v1_9_R1")) {
//            //server is running 1.9 - 1.9.2 so we need to use the 1.9 R1 NMS class
//            actionbar = new Actionbar_1_9_R1();
//        } else if (version.equals("v1_9_R2")) {
//            //server is running 1.9.4 so we need to use the 1.9 R2 NMS class
//            actionbar = new Actionbar_1_9_R2();
//        } else  {
//
//            //  we are running 1.10+ where you can use ChatMessageType
//            actionbar = new ActionbarModern();
//        }
        return actionbar != null;
    }
}

class CountDownTimerTask extends TimerTask{
    public void run(){

    }
}