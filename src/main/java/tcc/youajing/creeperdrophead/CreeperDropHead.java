package tcc.youajing.creeperdrophead;

import crypticlib.BukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import tcc.youajing.creeperdrophead.Listener.DeathListener;

public class CreeperDropHead extends BukkitPlugin {

    @Override
    public void enable() {
        // 启动插件时的日志信息
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[CreeperDropHead] 插件已启用！++++++++++++++++++++++++");
        register();
    }

    @Override
    public void disable() {
        // 停止插件时的日志信息
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[CreeperDropHead] 插件已禁用！-------------------------");
    }

    public void register() {
        // 注册事件监听器
        this.getServer().getPluginManager().registerEvents(new DeathListener(this), this);
    }
}


