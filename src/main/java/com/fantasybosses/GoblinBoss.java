package com.fantasybosses;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class GoblinBoss implements Listener {

    private final JavaPlugin plugin;

    public GoblinBoss(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void spawnBoss(Location location) {
        Zombie goblin = location.getWorld().spawn(location, Zombie.class);
        goblin.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "Goblin Boss");
        goblin.setCustomNameVisible(true);
        goblin.setBaby(true);
        goblin.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(200);
        goblin.setHealth(200);
        goblin.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.25);

        goblin.getEquipment().setHelmet(createNBTGoblinHead());
        goblin.getEquipment().setChestplate(dyedLeather(Material.LEATHER_CHESTPLATE));
        goblin.getEquipment().setLeggings(dyedLeather(Material.LEATHER_LEGGINGS));
        goblin.getEquipment().setBoots(dyedLeather(Material.LEATHER_BOOTS));
        goblin.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));

        Bukkit.getScheduler().runTaskLater(plugin, () -> startAbilities(goblin), 1L);
    }

    private ItemStack dyedLeather(Material material) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(Color.fromRGB(40, 120, 40));
            meta.setDisplayName(ChatColor.DARK_GREEN + "Goblin Gear");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNBTGoblinHead() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        NBTItem nbtItem = new NBTItem(skull);

        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDMzZDMxZDc1NWZkNjViODFlYzA3OTk2MjA1M2Y4ZTI0YTI5ZmU3ZWIzODZmZDNkOTczMzdiMzgyYjg3MDMyIn19fQ==";

        NBTCompound skullOwner = nbtItem.addCompound("SkullOwner");
        skullOwner.setUUID("Id", UUID.randomUUID());
        NBTCompound properties = skullOwner.getOrCreateCompound("Properties");
        NBTCompoundList textures = properties.getCompoundList("textures");
        NBTCompound texture = textures.addCompound();
        texture.setString("Value", base64);

        skull = nbtItem.getItem();
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Goblin Crown");
            skull.setItemMeta(meta);
        }

        return skull;
    }

    private void startAbilities(Zombie goblin) {
        BossBar bar = Bukkit.createBossBar(ChatColor.GOLD + "Goblin Boss", BarColor.GREEN, BarStyle.SOLID);

        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                if (goblin == null || goblin.isDead()) {
                    bar.removeAll();
                    cancel();
                    return;
                }

                double max = goblin.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                bar.setProgress(Math.max(0.0, goblin.getHealth() / max));

                for (Player p : goblin.getWorld().getPlayers()) {
                    if (p.getLocation().distance(goblin.getLocation()) < 30) bar.addPlayer(p);
                    else bar.removePlayer(p);
                }

                if (timer % 160 == 0) {
                    TNTPrimed tnt = goblin.getWorld().spawn(goblin.getLocation().add(0, 1, 0), TNTPrimed.class);
                    tnt.setFuseTicks(40);
                    tnt.addScoreboardTag("goblin_tnt");

                    Player target = null;
                    double closest = Double.MAX_VALUE;

                    for (Player p : goblin.getWorld().getPlayers()) {
                        double dist = p.getLocation().distance(goblin.getLocation());
                        if (dist < closest) {
                            closest = dist;
                            target = p;
                        }
                    }

                    if (target != null) {
                        Vector direction = target.getLocation().toVector().subtract(goblin.getLocation().toVector()).normalize();
                        tnt.setVelocity(direction.multiply(1.2));
                    }

                    goblin.getWorld().playSound(goblin.getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                }

                if (timer % 300 == 0) {
                    goblin.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
                    goblin.getWorld().playSound(goblin.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1f, 1f);
                }

                timer++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onGoblinTntExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof TNTPrimed &&
                event.getEntity().getScoreboardTags().contains("goblin_tnt")) {
            event.blockList().clear();
        }
    }
}
