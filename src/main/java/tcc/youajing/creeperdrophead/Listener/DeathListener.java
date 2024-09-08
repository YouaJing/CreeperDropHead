package tcc.youajing.creeperdrophead.Listener;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import tcc.youajing.creeperdrophead.CreeperDropHead;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DeathListener implements Listener {
    private final CreeperDropHead plugin;
    private final Random random = new Random();
    private final Map<UUID, UUID> entityHurtPlayerMap = new ConcurrentHashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DeathListener(CreeperDropHead plugin) {
        this.plugin = plugin;
    }

    // 玩家死亡事件处理
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        UUID killerUUID = entityHurtPlayerMap.get(p.getUniqueId());
        Entity killer = killerUUID != null ? Bukkit.getEntity(killerUUID) : null;

        // 检查杀手是否为充能苦力怕
        if (killer != null && killer.getType().equals(EntityType.CREEPER) && ((Creeper) killer).isPowered()) {
            Audience player = (Audience) event.getEntity();
            int level = p.getLevel();
            double dropChance = Math.min(0.05 + (0.95 / 300) * level, 1.0); // 每级增加0.33%的掉落几率，最高100%
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
                Component successMessage = miniMessage.deserialize("<bold><gradient:#DFFFCD:#deecdd>喜！掉了！---本次死亡不掉落！");
                player.sendMessage(successMessage);
            } else {
                Component fallMessage = miniMessage.deserialize("<bold><gradient:#fc6076:#ff9a44>哦悲悲---人货两空...");
                player.sendMessage(fallMessage);
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

    @EventHandler
    public void onPlayerThrowsTrident(PlayerLaunchProjectileEvent event) {
        if (event.getProjectile() instanceof Trident trident) {
            if (trident.getItemStack().containsEnchantment(Enchantment.CHANNELING)) {
                if (event.getPlayer().getWorld().hasStorm()) {
                    Random random1 = new Random();
                    if (random1.nextInt(100) < 5) {
                        Location playerLocation = event.getPlayer().getLocation();
                        World world = event.getPlayer().getWorld();
                        Location strikeLocation = playerLocation;

                        // 检测半径128格范围内是否有避雷针
                        boolean lightningRodFound = false;
                        for (int x = -128; x <= 128; x++) {
                            for (int y = -128; y <= 128; y++) {
                                for (int z = -128; z <= 128; z++) {
                                    Location checkLocation = playerLocation.clone().add(x, y, z);
                                    if (world.getBlockAt(checkLocation).getType() == Material.LIGHTNING_ROD) {
                                        strikeLocation = checkLocation.clone().add(0, -1, 0); // 在避雷针下方一个方块召唤雷击
                                        lightningRodFound = true;
                                        break;
                                    }
                                }
                                if (lightningRodFound) break;
                            }
                            if (lightningRodFound) break;
                        }

                        // 召唤雷击
                        world.strikeLightning(strikeLocation);
                        // 获取同世界的玩家
                        List<Player> nearbyPlayers =event.getPlayer().getWorld().getPlayers();

                        // 通知每个玩家
                        Component message = miniMessage.deserialize("<bold><gradient:#005bea:#00c6fb:#005bea>⚡⚡⚡⚡" + event.getPlayer().getName() + "装B遭雷劈⚡⚡⚡⚡");
                        for (Player nearbyPlayer : nearbyPlayers) {
                            nearbyPlayer.sendMessage(message);
                        }
                    }
                }
            }
        }
    }

}
