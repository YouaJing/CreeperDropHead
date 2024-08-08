package tcc.youajing.creeperdrophead.Listener;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathListener implements Listener {
    private final Random random = new Random();
    private final Map<UUID, UUID> entityHurtPlayerMap = new ConcurrentHashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // 玩家死亡事件处理
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        UUID killerUUID = entityHurtPlayerMap.get(p.getUniqueId());
        Entity killer = killerUUID != null ? Bukkit.getEntity(killerUUID) : null;

        // 检查杀手是否为充能苦力怕
        if (killer != null && killer.getType().equals(EntityType.CREEPER) && ((Creeper) killer).isPowered()) {
            int level = p.getLevel();
            double dropChance = Math.min(0.0033 * level, 1.0); // 每级增加0.33%的掉落几率，最高100%
            int levelsToDeduct = Math.min(level, 300); // 扣除的等级最多为300级

            // 根据计算的几率检查是否掉落头颅
            if (random.nextDouble() <= dropChance) {
                Location loc = p.getLocation();
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                skullMeta.setOwningPlayer(p);

                // 设置头颅名字
                skullMeta.setDisplayName(ChatColor.YELLOW + p.getName() + "的脑袋");

                // 获取当前日期并格式化
                String date = new SimpleDateFormat("yyyy年MM月dd日").format(new Date());
                String dropTime = ChatColor.LIGHT_PURPLE + "掉落时间：" + date;

                // 设置头颅的Lore
                skullMeta.setLore(Arrays.asList("会来纪念我的对吧....", "", dropTime));
                skull.setItemMeta(skullMeta);

                // 在玩家位置掉落头颅
                loc.getWorld().dropItemNaturally(loc, skull);

                // 扣除玩家的等级
                p.setLevel(level - levelsToDeduct);
                // 保持玩家死亡时的物品和等级
                event.setKeepInventory(true);
                event.setKeepLevel(true);
                event.getDrops().clear();

                // 使用MiniMessage通知玩家
                Audience player = (Audience) event.getEntity();
                Component message = miniMessage.deserialize("<yellow><bold>喜，掉了！</bold></yellow>");
                player.sendMessage(message);
            }
        }
    }

    // 玩家被实体伤害事件处理
    @EventHandler
    public void onPlayerHurtByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Entity damager = event.getDamager();
            entityHurtPlayerMap.put(player.getUniqueId(), damager.getUniqueId());
        }
    }
}
