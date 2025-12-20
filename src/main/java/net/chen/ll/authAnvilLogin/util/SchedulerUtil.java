package net.chen.ll.authAnvilLogin.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public final class SchedulerUtil {

    public static ScheduledTask runAsyncRepeating(
            Plugin plugin,
            Runnable task,
            long delayTicks,
            long periodTicks
    ) {
        if (Bukkit.getServer().getName().equalsIgnoreCase("Folia")) {
            // Folia 异步调度
            return Bukkit.getAsyncScheduler().runAtFixedRate(
                    plugin,
                    scheduledTask -> task.run(),
                    delayTicks * 50L,
                    periodTicks * 50L,
                    TimeUnit.MILLISECONDS
            );
        } else {
            // Paper / Spigot 传统异步
            Bukkit.getScheduler().runTaskTimerAsynchronously(
                    plugin,
                    task,
                    delayTicks,
                    periodTicks
            );
            return null;
        }
    }

    public static void runAsyncOnce(Plugin plugin, Runnable task) {
        if (Bukkit.getServer().getName().equalsIgnoreCase("Folia")) {
            Bukkit.getAsyncScheduler().runNow(
                    plugin,
                    t -> task.run()
            );
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(
                    plugin,
                    task
            );
        }
    }

}

