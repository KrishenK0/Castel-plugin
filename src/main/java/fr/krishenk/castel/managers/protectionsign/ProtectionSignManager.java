package fr.krishenk.castel.managers.protectionsign;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.ProtectionSign;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.events.general.OpenProtectedBlockEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XBlock;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.libs.xseries.XSound;
import fr.krishenk.castel.managers.chat.ChatInputHandler;
import fr.krishenk.castel.managers.chat.ChatInputManager;
import fr.krishenk.castel.utils.LocationUtils;
import fr.krishenk.castel.utils.internal.ExpirableSet;
import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;
import fr.krishenk.castel.utils.string.StringUtils;
import fr.krishenk.castel.utils.time.TimeFormatter;
import fr.krishenk.castel.utils.versionsupport.VersionSupport;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ProtectionSignManager implements Listener {
    private static final ExpirableSet<UUID> PASSWORD_COOLDOWNS;
    private static final Map<UUID, Integer> PASSWORD_ATTEMPTS = new NonNullMap<>();

    public ProtectionSignManager(CastelPlugin plugin) {
        if (Config.ProtectionSigns.PROTECTIONS_CONTAINER_TRANSFERS_DISALLOW_ALL.getManager().getBoolean() || Config.ProtectionSigns.PROTECTIONS_CONTAINER_TRANSFERS_DISALLOW_CROSS_ORIGIN_CONTAINER_TRANSFERS.getManager().getBoolean()) {
            Bukkit.getPluginManager().registerEvents(new ItemTransferFactory(), plugin);
        }
    }

    protected static Block putSignOn(Block block, XMaterial signMaterial, BlockFace facing) {
        String name = StringUtils.replace(signMaterial.name(), "SIGN", "WALL_SIGN");
        signMaterial = XMaterial.matchXMaterial(name).orElseThrow(() -> new IllegalArgumentException("Unexpected sign material for protection: " + name));
        block = block.getRelative(facing);
        if (signMaterial.parseMaterial() != null) {
            block.setType(signMaterial.parseMaterial());
        }
        VersionSupport.putSign(block, facing);
        return block;
    }

    private static boolean canBeDouble(Block block) {
        return block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST;
    }

    public static void handleProtectedBlock(PlayerInteractEvent event, ProtectionSign protection) {
        Player player = event.getPlayer();
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        boolean admin = CastelPluginPermission.PROTECTION$SIGNS_OPEN.hasPermission(player, true);
        ProtectionSign.AccessResult accessLevel = protection.canAccess(player);
        boolean canOpen = admin || accessLevel == ProtectionSign.AccessResult.ACCEPTED;
        if (!canOpen) {
            Land land = protection.getLand();
            boolean isClaimed = land.isClaimed();
            if (!isClaimed && !Config.ProtectionSigns.PROTECT_UNCLAIMED.getManager().getBoolean()) canOpen = true;

            if (!canOpen && isClaimed)
                canOpen = land.getGuildId().equals(cp.getGuildId()) && cp.hasPermission(StandardGuildPermission.PROTECTION_SIGNS);
        }

        Block block = event.getClickedBlock();
        if (ProtectionSign.isSign(block) && Config.ProtectionSigns.GUI.getManager().getBoolean()) {
            if (!admin && !protection.isOwner(player)) {
                Lang.PROTECTED_SIGNS_CANT_MODIFY.sendMessage(player);
                deniedSound(player);
                return;
            }

//            ProtectionSignGuiManager.openMenu(player, protection);
        }

        if (!canOpen) {
            event.setCancelled(true);
            if (accessLevel == ProtectionSign.AccessResult.PASSWORD) {
                BlockState state = block.getState();
                if (state instanceof Container) {
                    requestPassword(player, protection, (Container) state);
                    return;
                }
            }

            Lang.PROTECTED_SIGNS_PROTECTED.sendMessage(player);
            deniedSound(player);
        }

        OpenProtectedBlockEvent openEvent = new OpenProtectedBlockEvent(player, block, protection, canOpen);
        Bukkit.getPluginManager().callEvent(openEvent);
        if (openEvent.isCancelled()) event.setCancelled(true);
    }

    private static void handlePistons(BlockPistonEvent event, Collection<Block> blocks) {
        boolean protect = Config.ProtectionSigns.PROTECTIONS_PISTON.getManager().getBoolean();
        for (Block block : blocks) {
            Optional<ProtectionSign> protectionOpt = ProtectionSign.getProtection(block);
            if (protectionOpt.isPresent()) {
                if (protect) {
                    event.setCancelled(true);
                    return;
                }

                ProtectionSign protection = protectionOpt.get();
                protection.getLand().getProtectedBlocks().remove(protection.getLocation());
            }
        }
    }

    private static boolean handleAlreadyProtected(Player player, Block block) {
        Optional<ProtectionSign> protectionOpt = ProtectionSign.getProtection(block);
        if (protectionOpt.isPresent()) {
            Lang.PROTECTED_SIGNS_ALREADY_PROTECTED.sendMessage(player, "owner", Bukkit.getOfflinePlayer(protectionOpt.get().getOwner()).getName());
            deniedSound(player);
            return true;
        }
        return false;
    }

    private static void deniedSound(Player player) {
        XSound.play(player, Config.ProtectionSigns.DENIED_SOUND.getManager().getString());
    }

    public static void requestPassword(Player player, ProtectionSign sign, Container container) {
        long cd = PASSWORD_COOLDOWNS.getTimeLeft(player.getUniqueId());
        if (cd > 0L) {
            Lang.PROTECTED_SIGNS_PASSWORD_IN_COOLDOWN.sendError(player, "cooldown", TimeFormatter.of(cd));
        } else {
            Lang.PROTECTED_SIGNS_PASSWORD_REQUIRED.sendError(player);
            ChatInputHandler<?> handler = new ChatInputHandler<>();
            handler.onInput((event -> {
                if (sign.verifyPassword(event.getMessage())) {
                    Bukkit.getScheduler().runTask(CastelPlugin.getInstance(), () -> player.openInventory(container.getInventory()));
                    sign.getTemporarilyTrusted().add(player.getUniqueId());
                    PASSWORD_ATTEMPTS.remove(player.getUniqueId());
                    return true;
                } else {
                    int maxAttempts = Config.ProtectionSigns.PASSWORDS_MAX_ATTEMPTS.getManager().getInt();
                    int attempts = PASSWORD_ATTEMPTS.getOrDefault(player.getUniqueId(), 0) + 1;
                    if (attempts >= maxAttempts) {
                        Lang.PROTECTED_SIGNS_PASSWORD_ERROR_429.sendError(player);
                        PASSWORD_COOLDOWNS.add(player.getUniqueId());
                        return true;
                    } else {
                        Lang.PROTECTED_SIGNS_PASSWORD_INVALID.sendError(player);
                        PASSWORD_ATTEMPTS.put(player.getUniqueId(), attempts);
                        return false;
                    }
                }
            }));
            handler.onAnyMove((event) -> {
                Lang.PROTECTED_SIGNS_PASSWORD_CANT_MOVE.sendError(player);
                return true;
            });
            ChatInputManager.startConversation(player, handler);
        }
    }

    public static List<String> getProtectionTypeSignLines(ProtectionSign.ProtectionType protectionType) {
        switch (protectionType) {
            case PROTECTED: return Config.ProtectionSigns.LINES.getManager().getStringList();
            case EVERYONE: return Config.ProtectionSigns.EVERYONE_LINES.getManager().getStringList();
            case EVERYONE_IN_GUILD: return Config.ProtectionSigns.EVERYONE_IN_GUILD_LINES.getManager().getStringList();
            default: throw new AssertionError("Unknown sign protection type: " + protectionType);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSignPut(SignChangeEvent event) {
        String line = event.getLine(0);
        ProtectionSign.ProtectionType protectionType = null;
        boolean caseSensitive = Config.ProtectionSigns.CASE_SENSITIVE_CODES.getManager().getBoolean();
        if (!caseSensitive) line = line.toLowerCase();

        if (Config.ProtectionSigns.CODES.getManager().getStringList().contains(line)) {
            protectionType = ProtectionSign.ProtectionType.PROTECTED;
        }

        if (protectionType == null && Config.ProtectionSigns.EVERYONE_IN_GUILD_ENABLED.getManager().getBoolean() && Config.ProtectionSigns.EVERYONE_IN_GUILD_CODES.getManager().getStringList().contains(line)) {
            protectionType = ProtectionSign.ProtectionType.EVERYONE_IN_GUILD;
        }

        if (protectionType == null && Config.ProtectionSigns.EVERYONE_ENABLED.getManager().getBoolean() && Config.ProtectionSigns.EVERYONE_CODES.getManager().getStringList().contains(line)) {
            protectionType = ProtectionSign.ProtectionType.EVERYONE;
        }

        if (protectionType != null) {
            Block sign = event.getBlock();
            Block block = VersionSupport.getAttachedBlock(sign);
            Player player = event.getPlayer();
            if (block == null) {
                Lang.PROTECTED_SIGNS_NOT_ATTACHED.sendMessage(player);
                deniedSound(player);
            } else if (!ProtectionSign.canBlockBeProtected(block)) {
                Lang.PROTECTED_SIGNS_INVALID_BLOCK.sendMessage(player);
                deniedSound(player);
            } else {
                List<String> signs = Config.ProtectionSigns.SIGNS.getManager().getStringList();
                if (!signs.isEmpty() && !XBlock.isOneOf(block, signs)) {
                    Lang.PROTECTED_SIGNS_INVALID_SIGN.sendMessage(player);
                    deniedSound(player);
                } else if (!handleAlreadyProtected(player, block)) {
                    SimpleLocation location = SimpleLocation.of(block);
                    Land land = Land.getLand(block);
                    if (land == null || !land.isClaimed()) {
                        if (Config.ProtectionSigns.PROTECT_UNCLAIMED.getManager().getBoolean()) {
                            Lang.PROTECTED_SIGNS_UNCLAIMED.sendError(player);
                            return;
                        }

                        if (land == null) {
                            land = new Land((UUID) null, location.toSimpleChunkLocation());
                        }
                    }

                    Lang.PROTECTED_SIGNS_GUILD_ITEMS.sendError(player);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onProtectedBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Optional<ProtectionSign> protectionOpt = ProtectionSign.getProtection(block);
        if (protectionOpt.isPresent()) {
            ProtectionSign protection = protectionOpt.get();
            boolean isSign = protection.getSign().equals(SimpleLocation.of(block));
            if (isSign || !ProtectionSign.isSign(block)) {
                Player player = event.getPlayer();
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                boolean admin = cp.isAdmin() || cp.hasGuild() && cp.getGuildId().equals(protection.getLand().getGuildId()) && cp.hasPermission(StandardGuildPermission.PROTECTION_SIGNS) || CastelPluginPermission.PROTECTION$SIGNS_BREAK.hasPermission(player, true);
                if (!admin && protection.canAccess(player) != ProtectionSign.AccessResult.ACCEPTED) {
                    Lang.PROTECTED_SIGNS_PROTECTED.sendMessage(player);
                    deniedSound(player);
                    event.setCancelled(true);
                } else if (!admin && !protection.getOwner().equals(player.getUniqueId())) {
                    Lang.PROTECTED_SIGNS_CANT_BREAK.sendMessage(player);
                    deniedSound(player);
                    event.setCancelled(true);
                } else {
                    if (isSign) Lang.PROTECTED_SIGNS_UNPROTECTED.sendMessage(player);
                    else Lang.PROTECTED_SIGNS_BROKE.sendMessage(player);

                    if (isSign || protection.shouldRemoveProtectionAfterBreak(block))
                        protection.removeProtection();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPistonExtended(BlockPistonExtendEvent event) {
        handlePistons(event, event.getBlocks());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPistonRectract(BlockPistonRetractEvent event) {
        handlePistons(event, event.getBlocks());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void doubleChestPortectedNotifier(BlockPlaceEvent event) {
        if (canBeDouble(event.getBlockPlaced())) {
            Bukkit.getScheduler().runTaskLater(CastelPlugin.getInstance(), () -> {
                Block block = event.getBlockPlaced();
                if (block.getState() instanceof Chest) {
                    Chest chest = (Chest) block.getState();
                    Inventory inventory = chest.getInventory();
                    if (inventory instanceof DoubleChestInventory) {
                        DoubleChest doubleChest = ((DoubleChestInventory) inventory).getHolder();
                        Chest leftChest = (Chest) doubleChest.getLeftSide();
                        Chest rightChest = (Chest) doubleChest.getRightSide();
                        Block target = rightChest.getBlock();
                        if (LocationUtils.equalsIgnoreWorld(block, target)) {
                            target = leftChest.getBlock();
                        }

                        if (ProtectionSign.isProtected(target)) {
                            if (!Config.ProtectionSigns.PROTECT_UNCLAIMED.getManager().getBoolean()) {
                                SimpleChunkLocation otherChunk = SimpleChunkLocation.of(block);
                                SimpleChunkLocation mainChunk = SimpleChunkLocation.of(target);
                                if (!otherChunk.equalsIgnoreWorld(mainChunk)) {
                                    Land otherLand = otherChunk.getLand();
                                    if (otherLand == null || !otherLand.isClaimed()) {
                                        Lang.PROTECTED_SIGNS_CANT_PROTECT_DOUBLE_CHEST.sendError(event.getPlayer());
                                        return;
                                    }
                                }
                            }

                            Lang.PROTECTED_SIGNS_DOUBLE_CHEST_PROTECTED.sendMessage(event.getPlayer());
                        }
                    }
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onQuickProtect(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (event.getBlockFace() != BlockFace.UP && event.getBlockFace() != BlockFace.DOWN) {
                    if (Config.ProtectionSigns.QUICK_PROTECT_ENABLED.getManager().getBoolean()) {
                        Player player = event.getPlayer();
                        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                        if (CastelPluginPermission.PROTECTION$SIGNS_USE.hasPermission(player, true)) {
                            boolean isCreative = player.getGameMode() == GameMode.CREATIVE;
                            if (!isCreative || CastelPluginPermission.PROTECTION$SIGNS_USE_CREATIVE.hasPermission(player, true)) {
                                if (player.isSneaking() || !Config.ProtectionSigns.QUICK_PROTECT_SNEAK.getManager().getBoolean()) {
                                    ItemStack itemSign = event.getItem();
                                    if (itemSign != null && itemSign.getType().name().endsWith("SIGN")) {
                                        Block block = event.getClickedBlock();
                                        if (ProtectionSign.canBlockBeProtected(block)) {
                                            List<String> signs = Config.ProtectionSigns.SIGNS.getManager().getStringList();
                                            XMaterial material = XMaterial.matchXMaterial(itemSign);
                                            if (!signs.isEmpty() && !material.isOneOf(signs)) {
                                                Lang.PROTECTED_SIGNS_INVALID_SIGN.sendError(player);
                                                event.setCancelled(true);
                                            } else {
                                                SimpleChunkLocation chunk = SimpleChunkLocation.of(block);
                                                Land land = chunk.getLand();
                                                if (land == null || !land.isClaimed()) {
                                                    if (!Config.ProtectionSigns.PROTECT_UNCLAIMED.getManager().getBoolean()) {
                                                        Lang.PROTECTED_SIGNS_UNCLAIMED.sendError(player);
                                                        return;
                                                    }

                                                    if (land == null) {
                                                        land = new Land((UUID) null, chunk);
                                                    }
                                                }

                                                SimpleLocation location = SimpleLocation.of(block);
                                                // TURRETS, STRUCTURES
                                                Lang.PROTECTED_SIGNS_GUILD_ITEMS.sendError(player);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static {
        long cooldown = Config.ProtectionSigns.PASSWORDS_MAX_ATTEMPTS_COOLDOWN.getManager().getTimeMillis();
        PASSWORD_COOLDOWNS = new ExpirableSet<>(cooldown, TimeUnit.MILLISECONDS, false);
    }

    public class ItemTransferFactory implements Listener {
        private final boolean DISALLOW_ALL = Config.ProtectionSigns.PROTECTIONS_CONTAINER_TRANSFERS_DISALLOW_ALL.getManager().getBoolean();
        private final boolean DISALLOW_CROSS_ORIGIN = Config.ProtectionSigns.PROTECTIONS_CONTAINER_TRANSFERS_DISALLOW_CROSS_ORIGIN_CONTAINER_TRANSFERS.getManager().getBoolean();

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
        private void onProtectedTransfer(InventoryMoveItemEvent event) {
            Location srcLocation = event.getSource().getLocation();
            Optional<ProtectionSign> srcOpt = srcLocation == null ? Optional.empty() : ProtectionSign.getProtection(srcLocation.getBlock());
            Location destLocation = event.getDestination().getLocation();
            Optional<ProtectionSign> destOpt = destLocation == null ? Optional.empty() : ProtectionSign.getProtection(destLocation.getBlock());
            if (DISALLOW_ALL) {
                if (srcOpt.isPresent() || destOpt.isPresent()) event.setCancelled(true);
            } else if (srcOpt.isPresent() && destOpt.isPresent()) {
                ProtectionSign src = srcOpt.get();
                ProtectionSign dest = destOpt.get();
                boolean disallowTransfers = DISALLOW_CROSS_ORIGIN ? !src.getOwner().equals(dest.getOwner()) : src.canAccess(dest.getOwner()) != ProtectionSign.AccessResult.ACCEPTED;
                if (disallowTransfers) event.setCancelled(true);
            } else {
                if (srcOpt.isPresent() || destOpt.isPresent()) event.setCancelled(true);
            }
        }
    }
}
