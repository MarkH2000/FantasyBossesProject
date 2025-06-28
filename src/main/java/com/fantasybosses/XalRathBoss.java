package com.fantasybosses;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * XalRathBoss - Custom boss logic for Xal Rath, Sovereign of the Abyss.
 * <p>
 * To use this class, register it as a listener in your plugin's onEnable method:
 * <pre>
 * getServer().getPluginManager().registerEvents(new XalRathBoss(this), this);
 * </pre>
 */
@SuppressWarnings("deprecation")
public class XalRathBoss implements Listener {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    private BossBar bossBar;
    private boolean voidPillarPhase = false;
    private boolean voidboundActive = false;
    private boolean voidboundCompleted = false;

    private int abilityTick = 0;

    private List<Location> pillarLocations = new ArrayList<>();
    private List<Location> sigilLocations = new ArrayList<>();

    public XalRathBoss(JavaPlugin plugin) {
        this.plugin = plugin;
        // Signature log on plugin enable
        Bukkit.getLogger().info("[FantasyBosses] XalRathBoss by GitHub Copilot loaded successfully.");
    }

    // Example: Boss spawn logic (call this method to spawn the boss)
    public WitherSkeleton spawnXalRath(Location location) {
        World world = location.getWorld();
        WitherSkeleton xalRath = world.spawn(location, WitherSkeleton.class);
        xalRath.setCustomName(ChatColor.DARK_PURPLE + "Xal'Rath, Sovereign of the Abyss");
        xalRath.setCustomNameVisible(true);
        xalRath.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);
        xalRath.setHealth(500.0);
        xalRath.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        xalRath.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        xalRath.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
        xalRath.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        xalRath.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        xalRath.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, true, false));
        xalRath.setRemoveWhenFarAway(false);
        bossBar = Bukkit.createBossBar(ChatColor.DARK_PURPLE + "Xal'Rath, Sovereign of the Abyss", BarColor.PURPLE, BarStyle.SEGMENTED_20);
        bossBar.setProgress(1.0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }

        startAbilityTask(xalRath);

        return xalRath;
    }

    // Periodic ability logic
    private void startAbilityTask(WitherSkeleton xalRath) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (xalRath.isDead() || xalRath.getHealth() <= 0) {
                    bossBar.removeAll();
                    cancel();
                    return;
                }
                abilityTick++;

                // Example: Every 10 seconds, cast a random ability
                if (abilityTick % 200 == 0) {
                    int ability = random.nextInt(6);
                    switch (ability) {
                        case 0:
                            castVoidPillar(xalRath.getLocation());
                            break;
                        case 1:
                            castVoidbound(xalRath.getLocation());
                            break;
                        case 2:
                            castAbyssalSigil(xalRath.getLocation());
                            break;
                        case 3:
                            castSoulChain(xalRath.getLocation());
                            break;
                        case 4:
                            castVoidPulse(xalRath.getLocation());
                            break;
                        case 5:
                            castFlickerCurse(xalRath.getLocation());
                            break;
                    }
                }

                // Update boss bar health
                bossBar.setProgress(xalRath.getHealth() / xalRath.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // Example ability: Void Pillar
    private void castVoidPillar(Location center) {
        if (voidPillarPhase) return;
        voidPillarPhase = true;
        pillarLocations.clear();

        World world = center.getWorld();
        double radius = 6.0;
        int pillars = 6;
        for (int i = 0; i < pillars; i++) {
            double angle = 2 * Math.PI * i / pillars;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location pillarLoc = new Location(world, x, center.getY(), z);
            pillarLocations.add(pillarLoc);

            // Visual effect
            world.spawnParticle(Particle.PORTAL, pillarLoc, 50, 0.5, 2, 0.5, 0.1);
            world.playSound(pillarLoc, Sound.ENTITY_ENDERMAN_SCREAM, 1, 1);

            // Place obsidian pillar
            for (int y = 0; y < 5; y++) {
                Block block = world.getBlockAt(pillarLoc.getBlockX(), pillarLoc.getBlockY() + y, pillarLoc.getBlockZ());
                block.setType(Material.OBSIDIAN);
            }
        }

        // Remove pillars after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : pillarLocations) {
                    for (int y = 0; y < 5; y++) {
                        Block block = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + y, loc.getBlockZ());
                        if (block.getType() == Material.OBSIDIAN) {
                            block.setType(Material.AIR);
                        }
                    }
                }
                pillarLocations.clear();
                voidPillarPhase = false;
            }
        }.runTaskLater(plugin, 200L);
    }

    // Example ability: Voidbound (debuff nearby players)
    private void castVoidbound(Location center) {
        if (voidboundActive) return;
        voidboundActive = true;

        World world = center.getWorld();
        List<Player> affected = new ArrayList<>();
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(center) < 10) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                player.sendMessage(ChatColor.DARK_PURPLE + "You are bound by the void!");
                affected.add(player);
            }
        }

        // Remove effect after 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : affected) {
                    player.removePotionEffect(PotionEffectType.WITHER);
                }
                voidboundActive = false;
            }
        }.runTaskLater(plugin, 100L);
    }

    // Example ability: Abyssal Sigil (summon sigils that explode)
    private void castAbyssalSigil(Location center) {
        sigilLocations.clear();
        World world = center.getWorld();
        double radius = 8.0;
        int sigils = 4;
        for (int i = 0; i < sigils; i++) {
            double angle = 2 * Math.PI * i / sigils;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location sigilLoc = new Location(world, x, center.getY(), z);
            sigilLocations.add(sigilLoc);

            world.spawnParticle(Particle.EFFECT, sigilLoc, 100, 1, 0.5, 1, 0.2);
            world.spawnParticle(Particle.WITCH, sigilLoc, 100, 1, 0.5, 1, 0.2);
            world.spawnParticle(Particle.ENTITY_EFFECT, sigilLoc, 100, 1, 0.5, 1, 0.2);
            world.spawnParticle(Particle.ENTITY_EFFECT, sigilLoc, 100, 1, 0.5, 1, 0.2);
            world.playSound(sigilLoc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
        }
        // Explode sigils after 3 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : sigilLocations) {
                    world.createExplosion(loc, 2.0F, false, false);
                }
                sigilLocations.clear();
            }
        }.runTaskLater(plugin, 60L);
    }

    // Boss death event
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof WitherSkeleton && entity.getCustomName() != null &&
                entity.getCustomName().contains("Xal'Rath")) {
            bossBar.removeAll();
            event.getDrops().clear();

            // Drop custom loot
            ItemStack voidBlade = new ItemStack(Material.NETHERITE_SWORD);
            ItemMeta meta = voidBlade.getItemMeta();
            meta.setDisplayName(ChatColor.DARK_PURPLE + "Voidblade of Xal'Rath");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Forged in the Abyss.");
            meta.setLore(lore);
            voidBlade.setItemMeta(meta);
            event.getDrops().add(voidBlade);

            Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "Xal'Rath, Sovereign of the Abyss has been defeated!");
        }
    }
    // Utility: Summon Xal'Rath with dramatic effects
    // Utility: Summon Xal'Rath with dramatic effects
    public void summonXalRathWithEffects(Location location) {
        World world = location.getWorld();

        // 1. Darken sky and thunder
        world.setStorm(true);
        world.setThundering(true);
        world.setWeatherDuration(20 * 30); // 30 seconds

        // 2. Thunder sound and hush
        world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10f, 0.7f);
        for (Player player : world.getPlayers()) {
            player.sendTitle(ChatColor.DARK_PURPLE + "A hush falls over the world...", "", 10, 60, 20);
        }

        // 3. Swirl of particles
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                    world.spawnParticle(Particle.INSTANT_EFFECT, location, 30, 1, 1, 1, 0.2);
                    world.spawnParticle(Particle.PORTAL, location, 60, 1.5, 1.5, 1.5, 0.1);
                    world.spawnParticle(Particle.INSTANT_EFFECT, location, 30, 1, 1, 1, 0.2);
                    world.spawnParticle(Particle.PORTAL, location, 60, 1.5, 1.5, 1.5, 0.1);
                    world.spawnParticle(Particle.ENTITY_EFFECT, location, 30, 1, 1, 1, 0.2);
                    ticks++;
                    if (ticks >= 30) {
                        // 4. Obsidian burst
                        for (int i = 0; i < 8; i++) {
                            double angle = 2 * Math.PI * i / 8;
                            double x = location.getX() + 2 * Math.cos(angle);
                            double z = location.getZ() + 2 * Math.sin(angle);
                            Location obsLoc = new Location(world, x, location.getY(), z);
                            for (int y = 0; y < 3; y++) {
                                Block block = world.getBlockAt(obsLoc.getBlockX(), obsLoc.getBlockY() + y, obsLoc.getBlockZ());
                                block.setType(Material.OBSIDIAN);
                            }
                            world.spawnParticle(Particle.SMOKE, obsLoc, 20, 0.5, 1, 0.5, 0.1);
                            world.spawnParticle(Particle.LARGE_SMOKE, obsLoc, 20, 0.5, 1, 0.5, 0.1);
                        }
                        WitherSkeleton xalRath = spawnXalRath(location);

                        // 6. Dialogue
                        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "Xal Rath: " + ChatColor.LIGHT_PURPLE + "“From the ink between stars… I awaken.”");
                        xalRath.getWorld().playSound(location, Sound.ENTITY_WITHER_SPAWN, 2f, 0.5f);

                        // Remove obsidian burst after 10 seconds
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < 8; i++) {
                                    double angle = 2 * Math.PI * i / 8;
                                    double x = location.getX() + 2 * Math.cos(angle);
                                    double z = location.getZ() + 2 * Math.sin(angle);
                                    Location obsLoc = new Location(world, x, location.getY(), z);
                                    for (int y = 0; y < 3; y++) {
                                        Block block = world.getBlockAt(obsLoc.getBlockX(), obsLoc.getBlockY() + y, obsLoc.getBlockZ());
                                        if (block.getType() == Material.OBSIDIAN) {
                                            block.setType(Material.AIR);
                                        }
                                    }
                                }
                            }
                        }.runTaskLater(plugin, 200L);

                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 2L);
    }
    // --- Xal Rath: Additional Abilities ---
    private void castVoidPulse(Location center) {
        World world = center.getWorld();
        world.spawnParticle(Particle.DRAGON_BREATH, center, 100, 2, 1, 2, 0.2);
        world.spawnParticle(Particle.DRAGON_BREATH, center, 100, 2, 1, 2, 0.2);
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(center) < 8) {
                player.damage(6.0);
                player.setVelocity(player.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.5).setY(0.7));
            }
        }
    }
    // 2. Phase Rift: Teleport near random player
    private void castPhaseRift(WitherSkeleton xalRath) {
        List<Player> players = xalRath.getWorld().getPlayers();
        xalRath.getWorld().spawnParticle(Particle.BLOCK, xalRath.getLocation(), 40, 0.5, 1, 0.5, 0.1, Material.OBSIDIAN.createBlockData());
        Player target = players.get(random.nextInt(players.size()));
        Location dest = target.getLocation().clone().add(random.nextDouble() * 2 - 1, 0, random.nextDouble() * 2 - 1);
        xalRath.getWorld().spawnParticle(Particle.BLOCK, xalRath.getLocation(), 40, 0.5, 1, 0.5, 0.1, Material.OBSIDIAN.createBlockData());
        xalRath.getWorld().playSound(xalRath.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 0.5f);
        xalRath.teleport(dest);
        xalRath.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1.2f);
    }

    // 3. Soul Chain: Link two players
    private void castSoulChain(Location center) {
        List<Player> players = center.getWorld().getPlayers();
        if (players.size() < 2) return;
        Player p1 = players.get(random.nextInt(players.size()));
        Player p2 = p1;
        while (p2 == p1 && players.size() > 1) {
            p2 = players.get(random.nextInt(players.size()));
        }
        final Player chainP1 = p1;
        final Player chainP2 = p2;
        chainP1.sendMessage(ChatColor.LIGHT_PURPLE + "You feel a chain bind you to another soul...");
        chainP2.sendMessage(ChatColor.LIGHT_PURPLE + "You feel a chain bind you to another soul...");
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!chainP1.isOnline() || !chainP2.isOnline() || ticks > 100) {
                    cancel();
                    return;
                }
                double dist = chainP1.getLocation().distance(chainP2.getLocation());
                if (dist > 10) {
                    chainP1.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                    chainP2.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                    chainP2.damage(2.0);
                }
                Location mid = chainP1.getLocation().clone().add(chainP2.getLocation()).multiply(0.5);
                chainP1.getWorld().spawnParticle(Particle.INSTANT_EFFECT, mid, 5, 0.2, 0.2, 0.2, 0.1);
                ticks += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void castFlickerCurse(Location center) {
        String[] whispers = {
            "You are alone.",
            "One of you is doomed.",
            "The void hungers.",
            "Your hope is a lie."
        };
        for (Player player : center.getWorld().getPlayers()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 1));
            player.sendMessage(ChatColor.DARK_PURPLE + "Xal Rath: " + ChatColor.GRAY + "Do you see what I see?");
            if (random.nextBoolean()) {
                player.sendMessage(ChatColor.DARK_GRAY + "[Whisper] " + whispers[random.nextInt(whispers.length)]);
                if (random.nextBoolean()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 128, false, false, false));
                }
            }
        }
        World world = center.getWorld();
        int count = 2 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            Location spawnLoc = center.clone().add(random.nextDouble() * 6 - 3, 0, random.nextDouble() * 6 - 3);
            WitherSkeleton echo = world.spawn(spawnLoc, WitherSkeleton.class);
            echo.setCustomName(ChatColor.GRAY + "Echo of the Void");
            echo.setCustomNameVisible(true);
            echo.setHealth(10.0);
            echo.getEquipment().setHelmet(new ItemStack(Material.BLACK_WOOL));
            echo.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
            echo.setRemoveWhenFarAway(true);
        }
        world.playSound(center, Sound.ENTITY_PHANTOM_AMBIENT, 2, 0.7f);
    }
    private void castMidnightBinding(Location center) {
        Player target = null;
        double maxHealth = 0;
        for (Player player : center.getWorld().getPlayers()) {
            if (player.getHealth() > maxHealth) {
                maxHealth = player.getHealth();
                target = player;
            }
        }
        if (target == null) return;
        Location loc = target.getLocation();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 2; y++) {
                    if (y == 2 || x == -1 || x == 1 || z == -1 || z == 1) {
                        Block block = loc.getWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                        block.setType(Material.OBSIDIAN);
                    }
                }
            }
        }
        target.sendMessage(ChatColor.DARK_PURPLE + "You are trapped by Midnight Binding!");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        for (int y = 0; y <= 2; y++) {
                            Block block = loc.getWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                            if (block.getType() == Material.OBSIDIAN) block.setType(Material.AIR);
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 160L);
    }

    // 8. Collapse: Enrage if only one player remains
    private void castCollapse(WitherSkeleton xalRath) {
        List<Player> players = xalRath.getWorld().getPlayers();
        if (players.size() == 1) {
            Player lone = players.get(0);
            xalRath.getWorld().playSound(xalRath.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 2, 0.5f);
            Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "Xal Rath begins to collapse reality!");
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (xalRath.isDead() || xalRath.getHealth() <= 0 || xalRath.getWorld().getPlayers().size() > 1) {
                        cancel();
                        return;
                    }
                    ticks += 20;
                    if (ticks >= 100) {
                        xalRath.getWorld().createExplosion(xalRath.getLocation(), 8.0f, false, false);
                        lone.damage(100.0);
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }

    // 9. Shattered Ground: Animate terrain under players
    private void castShatteredGround(Location center, WitherSkeleton xalRath) {
        for (Player player : center.getWorld().getPlayers()) {
            Location loc = player.getLocation();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Location blockLoc = loc.clone().add(x, -1, z);
                    blockLoc.getWorld().spawnParticle(Particle.CRIT, blockLoc, 8, 0.2, 0.1, 0.2, 0.05);
                    if (random.nextBoolean()) {
                        Block block = blockLoc.getBlock();
                        Material old = block.getType();
                        block.setType(Material.AIR);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                block.setType(old);
                            }
                        }.runTaskLater(plugin, 40L);
                    }
                }
            }
        }
        // Animate "shattered ground" patches that damage players
        World world = center.getWorld();
        for (int i = 0; i < 4; i++) {
            Location patch = center.clone().add(random.nextDouble() * 8 - 4, 0, random.nextDouble() * 8 - 4);
            world.spawnParticle(Particle.LARGE_SMOKE, patch, 40, 1, 0.2, 1, 0.05);
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    for (Player player : world.getPlayers()) {
                        if (player.getLocation().distance(patch) < 1.5) {
                            player.damage(1.0);
                        }
                    }
                    ticks += 10;
                    if (ticks > 60) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 10L);
        }

        // Animate obsidian spikes in a ring
        double radius = 5 + random.nextInt(3);
        List<Block> spikeBlocks = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            double angle = 2 * Math.PI * i / 12;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location spikeLoc = new Location(world, x, center.getY(), z);
            for (int y = 0; y < 3; y++) {
                Block block = world.getBlockAt(spikeLoc.getBlockX(), spikeLoc.getBlockY() + y, spikeLoc.getBlockZ());
                block.setType(Material.OBSIDIAN);
                spikeBlocks.add(block);
            }
            world.spawnParticle(Particle.BLOCK, spikeLoc, 10, 0.2, 0.5, 0.2, 0.1, Material.OBSIDIAN.createBlockData());
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : spikeBlocks) {
                    if (block.getType() == Material.OBSIDIAN) block.setType(Material.AIR);
                }
            }
        }.runTaskLater(plugin, 80L);

        List<Location> pillarLocs = new ArrayList<>();
        double[][] offsets = {{6,6},{-6,6},{6,-6},{-6,-6}};
        for (double[] offset : offsets) {
            Location pillar = center.clone().add(offset[0], 0, offset[1]);
            pillarLocs.add(pillar);
            for (int y = 0; y < 7; y++) {
                Block block = world.getBlockAt(pillar.getBlockX(), pillar.getBlockY() + y, pillar.getBlockZ());
                block.setType(Material.OBSIDIAN);
            }
            Location crystalLoc = pillar.clone().add(0, 7, 0);
            world.spawnParticle(Particle.END_ROD, crystalLoc, 20, 0.2, 0.5, 0.2, 0.05);
            // Place End Crystal (pseudo, as Bukkit API may need NMS or plugin for real entity)
        }
        xalRath.setInvulnerable(true);
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "Xal Rath is shielded by the Pillars of the Void!");
        // You'd need to listen for crystal break events and set xalRath.setInvulnerable(false) when all are gone.
    }

    // 14. Aetheric Shock: Damage/knockback if hit while shielded
    private void castAethericShock(Player attacker, WitherSkeleton xalRath) {
        attacker.damage(4.0);
        attacker.setVelocity(attacker.getLocation().toVector().subtract(xalRath.getLocation().toVector()).normalize().multiply(1.2).setY(0.5));
        attacker.sendMessage(ChatColor.DARK_PURPLE + "Aetheric energy repels your attack!");
        xalRath.getWorld().playSound(xalRath.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 0.7f);
    }

    // 15. Boss Bar Messages (Void Taunts)
    private void sendVoidTaunt() {
        String[] taunts = {
            "You are temporary.",
            "Even your silence screams.",
            "The void is patient.",
            "Your courage is delicious.",
            "You cannot win."
        };
        if (bossBar != null) {
            bossBar.setTitle(ChatColor.DARK_PURPLE + "Xal'Rath: " + ChatColor.LIGHT_PURPLE + taunts[random.nextInt(taunts.length)]);
        }
    }

    public void spawnBoss(Location location) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'spawnBoss'");
    }
}