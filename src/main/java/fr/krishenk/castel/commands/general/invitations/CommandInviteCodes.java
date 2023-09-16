package fr.krishenk.castel.commands.general.invitations;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.InviteCode;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XItemStack;
import fr.krishenk.castel.locale.SimpleMessenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.ItemUtil;
import fr.krishenk.castel.utils.internal.FastUUID;
import fr.krishenk.castel.utils.nbt.ItemNBT;
import fr.krishenk.castel.utils.nbt.NBTType;
import fr.krishenk.castel.utils.nbt.NBTWrappers;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CommandInviteCodes extends CastelCommand {
    private static final String INVITE_CODE_NBT = "InviteCode";

    public CommandInviteCodes() {
        super("inviteCodes", true);
    }

    @EventHandler
    public void onInviteAccept(PlayerInteractEvent event) {
        Objects.requireNonNull(event);
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getHand() != EquipmentSlot.OFF_HAND) {
                Player player = event.getPlayer();
                ItemStack item = player.getInventory().getItemInMainHand();

                Pair<UUID, String> inviteCodePair = getInviteCodeFrom(item);
                if (inviteCodePair != null) {
                    UUID guildId = inviteCodePair.getKey();
                    String code = inviteCodePair.getValue();
                    Guild guild = Guild.getGuild(guildId);
                    if (guild == null) {
                        Lang.INVITE_CODES_GUILD_DOESNT_EXIST.sendError(player);
                    } else {
                        SimpleMessenger messenger = new SimpleMessenger(player, new MessageBuilder().withContext(guild));
                        InviteCode inviteCode = guild.getInviteCodes().get(code);
                        if (inviteCode != null && !inviteCode.hasExpired()) {
                            if (inviteCode.isAllUsed()) {
                                messenger.sendError(Lang.INVITE_CODES_MAX_USES);
                            } else {
                                CastelPlayer kp = CastelPlayer.getCastelPlayer(player);
                                if (kp.hasGuild()) {
                                    messenger.sendError(Lang.COMMAND_JOIN_ALREADY_IN_GUILD);
                                } else {
                                    Lang requirement = CommandAccept.checkRequirementsToJoin(player, guild);
                                    if (requirement != null) {
                                        messenger.sendError(requirement);
                                    } else {
                                        inviteCode.getUsedBy().add(player.getUniqueId());
                                        kp.joinGuild(guild);
                                        List<Player> onlineMembers = guild.getOnlineMembers();
                                        for (Player x : onlineMembers) {
                                            Lang.INVITE_CODES_USED.sendMessage(x);
                                        }
                                        if (item.getAmount() == 1) {
                                            player.getInventory().setItemInMainHand(null);
                                        } else {
                                            int amount = item.getAmount();
                                            item.setAmount(amount - 1);
                                        }
                                    }
                                }
                            }
                        } else {
                            messenger.sendError(Lang.INVITE_CODES_NO_LONGER_VALID);
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (context.assertPlayer() || context.assertHasGuild()) return CommandResult.FAILED;
        Player player = context.senderAsPlayer();
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        if (!cp.hasPermission(StandardGuildPermission.INVITE)) {
            StandardGuildPermission.INVITE.sendDeniedMessage(player);
            return CommandResult.FAILED;
        } else {
//            openGUI(player, cp.getGuild());
            return CommandResult.SUCCESS;
        }
    }

    public static Pair<UUID, String> getInviteCodeFrom(ItemStack item) {
        Objects.requireNonNull(item);
        NBTWrappers.NBTTagCompound nbt = ItemNBT.getTag(item);
        NBTWrappers.NBTTagCompound castelCompound = nbt.getCompound("Castel");
        if (castelCompound == null) {
            return null;
        } else {
            castelCompound = castelCompound.getCompound("InviteCode");
            if (castelCompound == null) {
                return null;
            } else {
                UUID group = FastUUID.fromString(castelCompound.get("Group", NBTType.STRING));
                String code = castelCompound.get("Code", NBTType.STRING);
                return new Pair<>(group, code);
            }
        }
    }
/*
    public static void openGUI(Player player, Guild guild) {
        openGUI(player, guild, 0);
    }

    private final InteractiveGUI openGUI(Player player, Kingdom kingdom, int page) {
        InteractiveGUI gui = GUIAccessor.prepare(player, (GUIPathContainer) KingdomsGUI.INVITE$CODES);
        int maxInviteCodes = KingdomsConfig.INVITATIONS_CODES_MAX.getInt();
        long paperCost = KingdomsConfig.INVITATIONS_CODES_PAPER_COST_RESOURCE_POINTS.getLong();
        gui.getSettings().raw("invitecode-paper-cost", paperCost).raw("invitecode-max", maxInviteCodes);
        KingdomPlayer var10000 = KingdomPlayer.getKingdomPlayer((OfflinePlayer)player);
        Intrinsics.checkNotNullExpressionValue(var10000, "getKingdomPlayer(player)");
        KingdomPlayer kp = var10000;
        boolean hasInvitePerm = kp.hasPermission((KingdomPermission) StandardKingdomPermission.INVITE);
        org.kingdoms.data.Pair var10 = GUIPagination.paginate(gui, kingdom.getInviteCodes().values(), "invite-codes", page, Companion::openGUI$lambda-0);
        Intrinsics.checkNotNullExpressionValue(var10, "paginate(\n              …yer, kingdom, openPage) }");
        ReusableOptionHandler option = (ReusableOptionHandler)((Map.Entry)var10).getKey();
        Collection codes = (Collection)((Map.Entry)var10).getValue();
        Iterator var13 = codes.iterator();

        while(var13.hasNext()) {
            org.kingdoms.constants.group.model.InviteCode inviteCode = (org.kingdoms.constants.group.model.InviteCode)var13.next();
            org.kingdoms.locale.provider.MessageBuilder var16 = option.getSettings();
            Intrinsics.checkNotNullExpressionValue(inviteCode, "inviteCode");
            var16.addAll(this.getEditsOf(inviteCode));
            option.on(ClickType.LEFT, Companion::openGUI$lambda-1);
            option.on(ClickType.MIDDLE, Companion::openGUI$lambda-2).setConversation(Companion::openGUI$lambda-3);
            option.on(ClickType.RIGHT, Companion::openGUI$lambda-4);
            option.pushHead(inviteCode.getCreator());
            if (!option.hasNext()) {
                break;
            }
        }

        Ref.ObjectRef duration = new Ref.ObjectRef();
        gui.option("new-invite-code").onNormalClicks(Companion::openGUI$lambda-5).setConversation(Companion::openGUI$lambda-6).done();
        gui.option("delete-all").onNormalClicks(Companion::openGUI$lambda-7).done();
        Intrinsics.checkNotNullExpressionValue(gui, "gui");
        InteractiveGUI.open$default(gui, false, false, 3, (Object)null);
        return gui;
    }

        private static final void openGUI$lambda_0 (
    Player $player, Kingdom
    $kingdom,
    Integer openPage)

    {
        Intrinsics.checkNotNullParameter($player, "$player");
        Intrinsics.checkNotNullParameter($kingdom, "$kingdom");
        org.kingdoms.commands.general.invitations.CommandInviteCodes.Companion var10000 = org.kingdoms.commands.general.invitations.CommandInviteCodes.Companion;
        Intrinsics.checkNotNullExpressionValue(openPage, "openPage");
        var10000.openGUI($player, $kingdom, openPage);
    }

    private static final void openGUI$lambda_1(boolean $hasInvitePerm, KingdomPlayer $kp, long $paperCost, org.kingdoms.constants.group.Kingdom $kingdom, org.kingdoms.constants.group.model.InviteCode $inviteCode, Player $player, OptionHandler ctx) {
        Intrinsics.checkNotNullParameter($kp, "$kp");
        Intrinsics.checkNotNullParameter($kingdom, "$kingdom");
        Intrinsics.checkNotNullParameter($player, "$player");
        if (!$hasInvitePerm) {
            ctx.sendError((Messenger) KingdomsLang.COMMAND_INVITECODES_PERMISSION_GET, new Object[0]);
        } else {
            if (!$kp.isAdmin() && $paperCost > 0L) {
                if (!$kingdom.hasResourcePoints($paperCost)) {
                    ctx.sendError((Messenger) KingdomsLang.COMMAND_INVITECODES_PAPER_COST, new Object[0]);
                    return;
                }

                $kingdom.addResourcePoints(-$paperCost);
            }

            org.kingdoms.commands.general.invitations.CommandInviteCodes.Companion var10000 = org.kingdoms.commands.general.invitations.CommandInviteCodes.Companion;
            Intrinsics.checkNotNullExpressionValue($inviteCode, "inviteCode");
            ItemStack paper = var10000.generateInviteCodePaper($kingdom, $inviteCode);
            ItemStack[] var9 = new ItemStack[]{paper};
            XItemStack.giveOrDrop($player, var9);
            ctx.sendMessage((Messenger) KingdomsLang.COMMAND_INVITECODES_PAPER_GIVE, new Object[0]);
        }
    }

    private static final void openGUI$lambda_2(boolean $hasInvitePerm, OptionHandler ctx) {
        if (!$hasInvitePerm) {
            ctx.sendError((Messenger) KingdomsLang.COMMAND_INVITECODES_PERMISSION_REDEEM, new Object[0]);
        } else {
            ctx.sendMessage((Messenger) KingdomsLang.COMMAND_INVITECODES_REDEEM_ENTER, new Object[0]);
            ctx.startConversation();
        }
    }

    private static final void openGUI$lambda_3(org.kingdoms.constants.group.model.InviteCode $inviteCode, Player $player, Kingdom $kingdom, int $page, OptionHandler ctx, String input) {
        Intrinsics.checkNotNullParameter($player, "$player");
        Intrinsics.checkNotNullParameter($kingdom, "$kingdom");
        org.kingdoms.commands.general.invitations.CommandInviteCodes.Companion var10000 = org.kingdoms.commands.general.invitations.CommandInviteCodes.Companion;
        Intrinsics.checkNotNullExpressionValue(ctx, "ctx");
        Intrinsics.checkNotNullExpressionValue(input, "input");
        Long dur = var10000.durationOfInviteCode(ctx, input);
        if (dur != null) {
            ctx.endConversation();
            $inviteCode.redeemFor(TimeUtils.afterNow(Duration.ofMillis(dur)));
            org.kingdoms.commands.general.invitations.CommandInviteCodes.Companion.openGUI($player, $kingdom, $page);
        }

    }

    private static final void openGUI$lambda_4(boolean $hasInvitePerm, Kingdom $kingdom, org.kingdoms.constants.group.model.InviteCode $inviteCode, Player $player, int $page, OptionHandler ctx) {
        Intrinsics.checkNotNullParameter($kingdom, "$kingdom");
        Intrinsics.checkNotNullParameter($player, "$player");
        if (!$hasInvitePerm) {
            ctx.sendError((Messenger) KingdomsLang.COMMAND_INVITECODES_PERMISSION_DELETE, new Object[0]);
        } else {
            $kingdom.getInviteCodes().remove($inviteCode.getCode());
            org.kingdoms.commands.general.invitations.CommandInviteCodes.Companion.openGUI($player, $kingdom, $page);
        }
    }

    private static final void openGUI$lambda_5(boolean $hasInvitePerm, Kingdom $kingdom, int $maxInviteCodes, OptionHandler ctx) {
        Intrinsics.checkNotNullParameter($kingdom, "$kingdom");
        if (!$hasInvitePerm) {
            ctx.sendError((Messenger) KingdomsLang.COMMAND_INVITECODES_PERMISSION_CREATE, new Object[0]);
        } else if ($kingdom.getInviteCodes().size() >= $maxInviteCodes) {
            ctx.sendError((Messenger) KingdomsLang.COMMAND_INVITECODES_MAX, new Object[0]);
        } else {
            ctx.sendMessage((Messenger) KingdomsLang.COMMAND_INVITECODES_DURATION_ENTER, new Object[0]);
            ctx.startConversation();
        }
    }

    private static final void openGUI$lambda_6(Ref.ObjectRef $duration, Kingdom $kingdom, Player $player, OptionHandler ctx, String input) {
        Intrinsics.checkNotNullParameter($duration, "$duration");
        Intrinsics.checkNotNullParameter($kingdom, "$kingdom");
        Intrinsics.checkNotNullParameter($player, "$player");
        int uses = false;
        if ($duration.element == null) {
            org.kingdoms.commands.general.invitations.CommandInviteCodes.Companion var13 = org.kingdoms.commands.general.invitations.CommandInviteCodes.Companion;
            Intrinsics.checkNotNullExpressionValue(ctx, "ctx");
            Intrinsics.checkNotNullExpressionValue(input, "input");
            Long dur = var13.durationOfInviteCode(ctx, input);
            if (dur != null) {
                $duration.element = new AtomicLong(dur);
                ctx.sendMessage((Messenger) KingdomsLang.COMMAND_INVITECODES_USES_ENTER, new Object[0]);
            }

        } else {
            int uses;
            try {
                uses = Integer.parseInt(input);
            } catch (NumberFormatException var10) {
                ctx.getSettings().raw("arg", input);
                ctx.sendError((Messenger) KingdomsLang.INVALID_NUMBER, new Object[0]);
                return;
            }

            int minUses = RangesKt.coerceAtLeast(1, KingdomsConfig.INVITATIONS_CODES_USES_MIN.getInt());
            int maxUses = KingdomsConfig.INVITATIONS_CODES_USES_MAX.getInt();
            if ((uses != 0 || maxUses <= 0) && (uses == 0 || uses >= minUses) && (maxUses <= 0 || uses <= maxUses)) {
                Object var10001 = $duration.element;
                Intrinsics.checkNotNull(var10001);
                org.kingdoms.constants.group.model.InviteCode code = $kingdom.generateInviteCode(Duration.ofMillis(((AtomicLong) var10001).get()), $player.getUniqueId(), uses);
                Map var10000 = $kingdom.getInviteCodes();
                Intrinsics.checkNotNullExpressionValue(var10000, "kingdom.inviteCodes");
                Map var9 = var10000;
                var9.put(code.getCode(), code);
                ctx.endConversation();
                org.kingdoms.commands.general.invitations.CommandInviteCodes.Companion.openGUI($player, $kingdom);
            } else {
                ctx.getSettings().raw("uses-min", minUses).raw("uses-max", maxUses);
                ctx.sendError((Messenger) KingdomsLang.COMMAND_INVITECODES_USES_OUT_OF_RANGE, new Object[0]);
            }
        }
    }

    private static final void openGUI$lambda_7(Kingdom $kingdom, Player $player, OptionHandler ctx) {
        Intrinsics.checkNotNullParameter($kingdom, "$kingdom");
        Intrinsics.checkNotNullParameter($player, "$player");
        $kingdom.getInviteCodes().clear();
        org.kingdoms.commands.general.invitations.CommandInviteCodes.Companion.openGUI($player, $kingdom);
        ctx.sendMessage((Messenger) KingdomsLang.COMMAND_INVITECODES_DELETED_ALL, new Object[0]);
    }
 */

//    private final Long durationOfInviteCode(OptionHandler ctx, String input) {
//        Long dur = TimeUtils.parseTime(input, TimeUnit.DAYS);
//        if (dur == null) {
//            ctx.getSettings().raw("time", input);
//            ctx.sendError(Lang.INVALID_TIME);
//            return null;
//        } else {
//            long minDuration = Math.max(1000L, Config.INVITATIONS_CODES_EXPIRATION_MIN.getTimeMillis());
//            Long maxDuration = Config.INVITATIONS_CODES_EXPIRATION_MAX.getTimeMillis();
//            if (dur == 0L) {
//                if (maxDuration > 0L) {
//                    return dur;
//                }
//            }
//
//            if (dur == 0L || dur >= minDuration) {
//                if (maxDuration <= 0L || dur <= maxDuration) {
//                    return dur;
//                }
//            }
//
//            MessageBuilder var10000 = ctx.getSettings().raw("duration-min", TimeFormatter.of(minDuration));
//            var10000.raw("duration-max", TimeFormatter.of(maxDuration));
//            ctx.sendError(Lang.COMMAND_INVITECODES_DURATION_OUT_OF_RANGE);
//            return null;
//        }
//    }

    private static Map<String, Object> getEditsOf(InviteCode inviteCode) {
        Pair<String, Object>[] pairs = new Pair[]{
                new Pair<>("invitecode-code", inviteCode.getCode()),
                new Pair<>("invitecode-creator", inviteCode.getCreator().getName()),
                new Pair<>("invitecode-uses", inviteCode.getUses() == 0 ? Lang.UNLIMITED.parse() : inviteCode.getUses()),
                new Pair<>("invitecode-used", inviteCode.getUsedBy().size()),
                new Pair<>("invitecode-createdAt", TimeUtils.getDateAndTime(inviteCode.getCreatedAt())),
                new Pair<>("invitecode-expiration", inviteCode.getExpiration() == 0L ? Lang.UNLIMITED.parse() : TimeUtils.getDateAndTime(inviteCode.getExpiration()))
        };
        return new HashMap<>(Arrays.stream(pairs).collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
    }


    public static ItemStack generateInviteCodePaper(Guild guild, InviteCode inviteCode) {
        ItemStack item = XItemStack.deserialize(Config.INVITATIONS_CODES_PAPER_ITEM.getManager().getSection());
        ItemMeta meta = item.getItemMeta();
        ItemUtil.translate(meta, (new MessageBuilder()).addAll(getEditsOf(inviteCode)).withContext(guild));
        item.setItemMeta(meta);
        NBTWrappers.NBTTagCompound nbt = ItemNBT.getTag(item);
        NBTWrappers.NBTTagCompound castelNS = new NBTWrappers.NBTTagCompound();
        NBTWrappers.NBTTagCompound container = new NBTWrappers.NBTTagCompound();
        container.setString("Code", inviteCode.getCode());
        container.setString("Group", FastUUID.toString(guild.getDataKey()));
        castelNS.setCompound("InviteCode", container);
        nbt.setCompound("Castel", castelNS);
        item = ItemNBT.setTag(item, nbt);
        return item;
    }

}
