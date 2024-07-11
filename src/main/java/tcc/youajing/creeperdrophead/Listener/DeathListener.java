package tcc.youajing.creeperdrophead.Listener;


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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        UUID killerUUID = entityHurtPlayerMap.get(p.getUniqueId());
        Entity killer = killerUUID != null ? Bukkit.getEntity(killerUUID) : null;
        if (killer != null && killer.getType().equals(EntityType.CREEPER) && ((Creeper) killer).isPowered()) {
            int level = p.getLevel();
            if (level >= 50) {
                double dropChance = 0;
                int levelsToDeduct = 0;

                if (level >= 300) {
                    dropChance = 1.0;
                    levelsToDeduct = 300;
                } else if (level >= 250) {
                    dropChance = 0.95;
                    levelsToDeduct = 250;
                } else if (level >= 200) {
                    dropChance = 0.88;
                    levelsToDeduct = 200;
                } else if (level >= 150) {
                    dropChance = 0.66;
                    levelsToDeduct = 150;
                }else if (level >= 100) {
                    dropChance = 0.44;
                    levelsToDeduct = 100;
                } else {
                    dropChance = 0.22;
                    levelsToDeduct = 50;
                }

                if (random.nextDouble() <= dropChance) {
                    Location loc = p.getLocation();
                    String loc1 = loc.toString();
                    ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
                    SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                    skullMeta.setOwningPlayer(p);

                    // 设置头颅名字
                    skullMeta.setDisplayName(ChatColor.YELLOW  + p.getName() + "的脑袋");

                    // 获取当前时间并格式化
                    String date = new SimpleDateFormat("yyyy年MM月dd日").format(new Date());
                    String dropTime = ChatColor.LIGHT_PURPLE + "掉落时间：" + date;

                    // 设置Lore
                    skullMeta.setLore(Arrays.asList("会来纪念我的对吧....", "", dropTime));
                    skull.setItemMeta(skullMeta);

                    loc.getWorld().dropItemNaturally(loc, skull);

                    // 扣除玩家的经验等级
                    p.setLevel(level - levelsToDeduct);
                    // 单次死亡不掉落
                    event.setKeepInventory(true);
                    event.setKeepLevel(true);
                    event.getDrops().clear();
                    p.sendMessage( ChatColor.YELLOW.toString() + ChatColor.BOLD + "喜，掉了！");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerHurtByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Entity damager = event.getDamager();
            entityHurtPlayerMap.put(player.getUniqueId(), damager.getUniqueId());
        }
    }
}
