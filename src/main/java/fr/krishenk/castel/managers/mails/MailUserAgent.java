package fr.krishenk.castel.managers.mails;

import com.google.common.base.Strings;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.GroupResolver;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.mails.CachedMail;
import fr.krishenk.castel.constants.mails.Mail;
import fr.krishenk.castel.constants.mails.MailRecipientType;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XItemStack;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageCompilerSettings;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderContextBuilder;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.ItemUtil;
import fr.krishenk.castel.utils.PlayerUtils;
import fr.krishenk.castel.utils.cooldown.BiCooldown;
import fr.krishenk.castel.utils.cooldown.Cooldown;
import fr.krishenk.castel.utils.internal.FastUUID;
import fr.krishenk.castel.utils.internal.MapUtil;
import fr.krishenk.castel.utils.nbt.ItemNBT;
import fr.krishenk.castel.utils.nbt.NBTType;
import fr.krishenk.castel.utils.nbt.NBTWrappers;
import fr.krishenk.castel.utils.nms.XBook;
import fr.krishenk.castel.utils.string.StringUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MailUserAgent implements Listener {
    private static final String MAILS_NBT = "Mail";
    private static final Cooldown<UUID> TOTAL_COOLDOWN = new Cooldown<>();
    private static final BiCooldown<UUID, UUID> GUILD_TO_GUILD_COOLDOWN = new BiCooldown<>();

    @EventHandler
    public void onMailProperties(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getHand() != EquipmentSlot.OFF_HAND) {
                Player player = event.getPlayer();
                if (player.isSneaking()) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (XMaterial.matchXMaterial(item) == XMaterial.WRITABLE_BOOK) {
                        DraftMail mail = getDraftMail(item);
                        if (mail != null) {
                            player.getInventory().setItemInMainHand(null);
                            openMailProperties(player, mail);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSign(PlayerEditBookEvent event) {
        if (event.isSigning()) {
            Player player = event.getPlayer();
            ItemStack item = PlayerUtils.getHotbarItem(player, event.getSlot());
            DraftMail draft = getDraftMail(item);
            if (draft.getSubject() != null && !draft.getSubject().isEmpty()) {
                if (draft.getRecipientsOfType(MailRecipientType.PRIMARY).isEmpty()) {
                    Lang.MAILS_SEND_MISSING_PRIMARY_RECIPIENT.sendError(player);
                    event.setCancelled(true);
                }
            } else {
                Lang.MAILS_SEND_MISSING_SUBJECT.sendError(player);
                event.setCancelled(true);
            }
        }
    }

    static String buildCCof(List<Group> cc) {
        if (cc.isEmpty()) return Lang.MAILS_CC_NONE.parse();
        StringBuilder builder = new StringBuilder();
        String sep = Lang.MAILS_CC_SEPARATOR.parse();
        int index = 0;
        for (Group group : cc) {
            builder.append(Lang.MAILS_CC_EACH.parse("cc", group.getName()));
            ++index;
            if (index != cc.size()) builder.append(sep);
        }
        return builder.toString();
    }

    public static Object openMailProperties(Player player, DraftMail mail) {
        /*
        InteractiveGUI gui = GUIAccessor.prepare(player, KingdomsGUI.MAILS_EDITOR, getMailEdits(mail));
        if (gui == null) {
            return null;
        } else {
            gui.push("subject", () -> {
                KingdomsLang.MAILS_SUBJECT_ENTER.sendMessage(player);
                gui.startConversation("subject");
            }, (input) -> {
                int limit = KingdomsConfig.MAILS_SUBJECT_LIMIT.getManager().getInt();
                boolean ignoreColors = KingdomsConfig.MAILS_SUBJECT_IGNORE_COLORS.getManager().getBoolean();
                String compiled = ignoreColors ? MessageCompiler.compile(input, new MessageCompilerSettings(false, true, true, false, false, (MessageTokenHandler[])null)).buildPlain((new MessageBuilder()).ignoreColors()) : input;
                int len = compiled.length();
                if (len > limit) {
                    KingdomsLang.MAILS_SUBJECT_LIMIT.sendError(player, new Object[]{"limit", limit});
                } else if (compiled.toLowerCase(Locale.ENGLISH).contains("erratas")) {
                    XSound.AMBIENT_CAVE.play(player);
                    MessageHandler.sendPluginMessage(player, "&6O Fortuna, ky2khlqdf7qdznac.░, bipolar interneuron yields&4 17&0-13-55-1 &6because of trees with wings.");
                    gui.endConversation();
                } else {
                    mail.setSubject(input);
                    KingdomsLang.MAILS_SUBJECT_CHANGED.sendMessage(player, new Object[]{"subject", input});
                    gui.endConversation();
                    openMailProperties(player, mail);
                }
            }, new Object[0]);
            gui.push("recipients", () -> {
                openRecipients(player, mail, () -> {
                    openMailProperties(player, mail);
                });
            }, new Object[0]);
            gui.push("reset", () -> {
                mail.setSubject((String)null);
                mail.getMessage().clear();
                mail.getRecipientsRaw().clear();
                openMailProperties(player, mail);
                KingdomsLang.MAILS_CLEARED_PROPERTIES.sendMessage(player);
            }, new Object[0]);
            gui.push("preview", () -> {
                player.closeInventory();
                ItemStack book = preview(player, mail);
                XBook.openBook(book, new Player[]{player});
            }, new Object[0]);
            gui.onClose(() -> {
                if (!gui.wasSwitched()) {
                    ItemStack item = generateEnvelope(player, mail);
                    XItemStack.addItems(player.getInventory(), false, new ItemStack[]{item});
                }
            });
            gui.open();
            return gui;
        }
         */
        return null;
    }

    public static Object openRecipients(Player player, DraftMail mail, Runnable back) {
    /*
        InteractiveGUI gui = GUIAccessor.prepare(player, KingdomsGUI.MAILS_RECIPIENTS, getMailEdits(mail));
        if (gui == null) {
            return null;
        } else {
            KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(player);
            gui.push("to", () -> {
                KingdomsLang.MAILS_TO_ENTER.sendMessage(player);
                gui.startConversation("to");
            }, (input) -> {
                Kingdom toKingdom = Kingdom.getKingdom(input);
                if (toKingdom == null) {
                    KingdomsLang.NOT_FOUND_KINGDOM.sendError(player, new Object[]{"kingdom", input});
                } else if (toKingdom.getId().equals(kp.getKingdomId())) {
                    KingdomsLang.MAILS_TO_YOURSELF.sendError(player, new Object[0]);
                } else if (mail.getRecipientsRaw().containsKey(toKingdom.getId())) {
                    KingdomsLang.MAILS_ALREADY_RECIPIENT.sendError(player, new Object[0]);
                } else {
                    removeLastPrimaryRecipient(mail);
                    mail.getRecipientsRaw().put(toKingdom.getId(), MailRecipientType.PRIMARY);
                    gui.endConversation();
                    openRecipients(player, mail, back);
                }
            }, new Object[0]);
            ReusableOptionHandler ccOpt = gui.getReusableOption("cc");
            List<Group> cc = mail.getRecipientsOfType(MailRecipientType.CC);
            Iterator var7 = cc.iterator();

            while(var7.hasNext()) {
                Group group = (Group)var7.next();
                ccOpt.setEdits(new Object[]{"singular-cc", group.getName()}).onNormalClicks(() -> {
                    KingdomsLang.MAILS_CC_REMOVED.sendMessage(player, new Object[]{"recipient", group.getName()});
                    mail.getRecipientsRaw().remove(group.getId());
                    openRecipients(player, mail, back);
                }).done();
                if (!ccOpt.hasNext()) {
                    break;
                }
            }

            gui.option("cc-add").onNormalClicks((context) -> {
                KingdomsLang.MAILS_CC_ENTER.sendMessage(player);
                context.startConversation();
            }).setConversation((input) -> {
                Kingdom toKingdom = Kingdom.getKingdom(input);
                if (toKingdom == null) {
                    KingdomsLang.NOT_FOUND_KINGDOM.sendError(player, new Object[]{"kingdom", input});
                } else if (toKingdom.getId().equals(kp.getKingdomId())) {
                    KingdomsLang.MAILS_TO_YOURSELF.sendError(player, new Object[0]);
                } else if (mail.getRecipientsRaw().containsKey(toKingdom.getId())) {
                    KingdomsLang.MAILS_ALREADY_RECIPIENT.sendError(player, new Object[0]);
                } else if (mail.getRecipientsOfType(MailRecipientType.CC).size() >= 5) {
                    KingdomsLang.MAILS_CC_MAX.sendError(player, new Object[]{"limit", 5});
                } else {
                    mail.getRecipientsRaw().put(toKingdom.getId(), MailRecipientType.CC);
                    openRecipients(player, mail, back);
                }
            }).done();
            gui.onClose(() -> {
                if (!gui.wasSwitched()) {
                    ItemStack item = generateEnvelope(player, mail);
                    XItemStack.addItems(player.getInventory(), false, new ItemStack[]{item});
                }
            });
            gui.push("back", back, new Object[0]);
            gui.open();
            return gui;
        }
     */
        return null;
    }

    static void removeLastPrimaryRecipient(DraftMail mail) {
        Iterator<MailRecipientType> it = mail.getRecipientsRaw().values().iterator();
        MailRecipientType type;
        do {
            if (!it.hasNext()) return;
            type = it.next();
        } while (type != MailRecipientType.PRIMARY);
        it.remove();
    }

    static MessageBuilder getMailEdits(DraftMail mail) {
        Group to = mail.getTo();
        return (new MessageBuilder()).parse("subject", Strings.isNullOrEmpty(mail.getSubject()) ? Lang.MAILS_SUBJECT_EMPTY.parse() : mail.getSubject()).raw("to", to == null ? Lang.MAILS_PRIMARY_NONE.parse() : to.getName()).parse("cc", buildCCof(mail.getRecipientsOfType(MailRecipientType.CC))).raw("replied-id", mail.getInReplyTo()).parse("summary", getSummaryOf(mail.getMessage()));
    }

    static MessageBuilder getMailEdits(Player sender, DraftMail mail) {
        return getMailEdits(mail).withContext(sender);
    }

    static String getSummaryOf(List<String> message) {
        if (StringUtils.areElementsEmpty(message)) return Lang.MAILS_CONTENT_EMPTY.parse();
        int summaryLimit = Config.MAILS_SUMMARY_LIMIT.getManager().getInt();
        String summary = message.get(0);
        return summary.substring(0, Math.min(summaryLimit, summary.length()))  + "...";
    }

    /*
    public static InteractiveGUI getMailbox(Player player, Kingdom kingdom, Consumer<InteractiveGUI> modifications) {
        KingdomPlayer kp = KingdomPlayer.getKingdomPlayer(player);
        if (!kp.hasPermission(StandardKingdomPermission.READ_MAILS) && !kp.hasPermission(StandardKingdomPermission.MANAGE_MAILS)) {
            StandardKingdomPermission.READ_MAILS.sendDeniedMessage(player);
            return null;
        } else {
            long envelopeRpCost = (long)MathUtils.eval(KingdomsConfig.MAILS_ENVELOPE_RESOURCE_POINTS.getManager().getString(), kingdom, new Object[0]);
            double moneyCost = MathUtils.eval(KingdomsConfig.MAILS_ENVELOPE_MONEY.getManager().getString(), kingdom, new Object[0]);
            InventoryInteractiveGUI gui = (InventoryInteractiveGUI)(new GUIBuilder(KingdomsGUI.MAILS_MAILBOX)).withSettings((new MessageBuilder()).raw("envelope-cost-resource-points", StringUtils.toFancyNumber((double)envelopeRpCost)).raw("envelope-cost-money", StringUtils.toFancyNumber(moneyCost)).raw("page", 1).raw("pages", 1)).inventoryGUIOnly().forPlayer(player).build();
            if (gui == null) {
                return null;
            } else {
                gui.push("get-envelope", () -> {
                    if (!kp.hasPermission(StandardKingdomPermission.MANAGE_MAILS)) {
                        StandardKingdomPermission.MANAGE_MAILS.sendDeniedMessage(player);
                    } else {
                        int slot = player.getInventory().firstEmpty();
                        if (slot < 0) {
                            KingdomsLang.MAILS_ENVELOPE_NO_FREE_SLOT.sendError(player, new Object[0]);
                        } else {
                            if (!kp.isAdmin()) {
                                Object[] costEdits = new Object[]{"rp", StringUtils.toFancyNumber((double)envelopeRpCost), "money", StringUtils.toFancyNumber(moneyCost)};
                                if (!kingdom.hasResourcePoints(envelopeRpCost)) {
                                    KingdomsLang.MAILS_ENVELOPE_NOT_ENOUGH_RESOURCES.sendError(player, costEdits);
                                    return;
                                }

                                if (SoftService.VAULT.isAvailable() && ServiceVault.isAvailable(Component.ECO) && !kingdom.hasMoney(moneyCost)) {
                                    KingdomsLang.MAILS_ENVELOPE_NOT_ENOUGH_RESOURCES.sendError(player, costEdits);
                                    return;
                                }

                                kingdom.addResourcePoints(-envelopeRpCost);
                                kingdom.addBank(-moneyCost);
                            }

                            player.getInventory().setItem(slot, generateEnvelope(player, new DraftMail(kingdom)));
                            KingdomsLang.MAILS_ENVELOPE_GIVE.sendMessage(player);
                            player.closeInventory();
                        }
                    }
                }, new Object[0]);
                ReusableOptionHandler sentOption = gui.getReusableOption("sent");
                Iterator var10 = kingdom.getSentMails().iterator();

                while(var10.hasNext()) {
                    Mail mail = (Mail)var10.next();
                    OfflinePlayer sender = mail.getPlayerSender();
                    getEditsForMail(sentOption.getSettings(), mail);
                    sentOption.onNormalClicks(() -> {
                        openMail(kingdom, player, mail);
                    }).pushHead(sender);
                    if (!sentOption.hasNext()) {
                        break;
                    }
                }

                ReusableOptionHandler inboxOption = gui.getReusableOption("inbox");
                Iterator var15 = kingdom.getReceivedMails().iterator();

                while(var15.hasNext()) {
                    Mail mail = (Mail)var15.next();
                    OfflinePlayer sender = mail.getPlayerSender();
                    getEditsForMail(inboxOption.getSettings(), mail);
                    inboxOption.onNormalClicks(() -> {
                        openMail(kingdom, player, mail);
                    }).pushHead(sender);
                    if (!inboxOption.hasNext()) {
                        break;
                    }
                }

                gui.onDelayedInteractableSlot((event) -> {
                    List<ItemStack> items = gui.getInteractableItems();
                    if (!items.isEmpty()) {
                        ItemStack item = (ItemStack)items.get(0);
                        DraftMail mail = getDraftMail(item);
                        if (mail == null) {
                            KingdomsLang.MAILS_NOT_AN_ENVELOPE.sendError(player, new Object[0]);
                            gui.returnItems();
                        } else if (mail.getSubject() == null) {
                            KingdomsLang.MAILS_SEND_MISSING_SUBJECT.sendError(player, new Object[0]);
                            gui.returnItems();
                        } else if (mail.getRecipientsOfType(MailRecipientType.PRIMARY).isEmpty()) {
                            KingdomsLang.MAILS_SEND_MISSING_PRIMARY_RECIPIENT.sendError(player, new Object[0]);
                            gui.returnItems();
                        } else if (!kp.hasPermission(StandardKingdomPermission.MANAGE_MAILS)) {
                            StandardKingdomPermission.MANAGE_MAILS.sendDeniedMessage(player);
                            gui.returnItems();
                        } else if (StringUtils.areElementsEmpty(mail.getMessage())) {
                            KingdomsLang.MAILS_SEND_MISSING_CONTENT.sendError(player, new Object[0]);
                            gui.returnItems();
                        } else {
                            long totalCd = TOTAL_COOLDOWN.getTimeLeft(kingdom.getId());
                            if (totalCd > 0L) {
                                KingdomsLang.MAILS_SEND_TOTAL_COOLDOWN.sendError(player, new Object[]{"cooldown", TimeFormatter.of(totalCd)});
                                gui.returnItems();
                            } else {
                                Group toGroup = (Group)mail.getRecipientsOfType(MailRecipientType.PRIMARY).get(0);
                                long perKingdomCd = KINGDOM_TO_KINGDOM_COOLDOWN.getTimeLeft(kingdom.getId(), toGroup.getId());
                                if (perKingdomCd > 0L) {
                                    KingdomsLang.MAILS_SEND_PER_GROUP_COOLDOWN.sendError(player, new Object[]{"kingdom", toGroup.getName(), "cooldown", TimeFormatter.of(perKingdomCd)});
                                    gui.returnItems();
                                } else {
                                    sendMail(player, mail);
                                    InteractiveGUI mailbox = getMailbox(player, kingdom, modifications);
                                    if (mailbox != null) {
                                        mailbox.open();
                                    }

                                }
                            }
                        }
                    }
                });
                modifications.accept(gui);
                return gui;
            }
        }
    }
     */

    public static void sendMail(Player sender, DraftMail draft) {
        Mail mail = draft.getFromGroup().sendMail(sender, draft);
        if (mail != null) {
            MessageBuilder settings = (MessageBuilder) getEditsForMail(mail).withContext(sender);
            for (Group group : draft.getRecipients().keySet()) {
                for (Player member : group.getOnlineMembers()) {
                    CastelPlayer cp = CastelPlayer.getCastelPlayer(member);
                    if (cp.hasPermission(StandardGuildPermission.READ_MAILS))
                        Lang.MAILS_NOTIFICATIONS_RECEIVERS.sendMessage(member, settings);
                }
            }
        }
    }

    public static PlaceholderContextBuilder getEditsForMail(Mail mail) {
        return getEditsForMail(new MessageBuilder(), mail);
    }

    public static PlaceholderContextBuilder getEditsForMail(MessageBuilder builder, Mail mail) {
        UUID to = mail.getRecipientsOfType(MailRecipientType.PRIMARY).get(0);
        Guild fromGroup = Guild.getGuild(mail.getFromGroup());
        Guild toGroup = Guild.getGuild(to);
        List<UUID> cc = mail.getRecipientsOfType(MailRecipientType.CC);
        List<Group> parsedCC = cc.stream().map(Guild::getGuild).filter(Objects::nonNull).collect(Collectors.toList());
        return builder.raw("sender", mail.getPlayerSender().getName()).raw("from", fromGroup == null ? Lang.UNKNOWN : fromGroup.getName()).parse("subject", mail.getSubject()).parse("summary", getSummaryOf(mail.getMessage())).raw("to", toGroup == null ? Lang.UNKNOWN : toGroup.getName()).parse("cc", buildCCof(parsedCC)).raw("sent", mail.getTime());
    }

    static ItemMeta setEnvelopeDescription(ItemStack item, boolean replying, MessageBuilder settings) {
        XItemStack.edit(item, (replying ? Config.MAILS_ENVELOPE_REPLY_ITEM : Config.MAILS_ENVELOPE_ITEM).getManager().getSection(), Function.identity(), null);
        ItemMeta meta = item.getItemMeta();
        ItemUtil.translate(meta, settings);
        return meta;
    }

    public static ItemStack generateEnvelopeInReplyTo(Player sender, Group fromGroup, Mail replyingMail, Class<? extends Group> communicationProtocol) {
        String subject = Lang.MAILS_REPLY_SUBJECT_FORMATTER.parse("subject", replyingMail.getSubject());
        Map<UUID, MailRecipientType> recipients = MapUtil.clone(replyingMail.getRecipients(), (k, v) -> v);
        DraftMail draft = new DraftMail(fromGroup, new ArrayList<>(), subject, recipients, replyingMail.getId(), communicationProtocol);
        return generateEnvelope(sender, draft);
    }

    public static ItemStack generateEnvelope(Player sender, DraftMail draft) {
        ItemStack mail = XMaterial.WRITTEN_BOOK.parseItem();
        BookMeta meta = (BookMeta) setEnvelopeDescription(mail, draft.getInReplyTo() != null, createSettings(sender, draft));
        meta.setAuthor(sender.getName());
        if (!draft.getMessage().isEmpty()) meta.setPages(draft.getMessage());

        mail.setItemMeta(meta);
        NBTWrappers.NBTTagCompound nbt = ItemNBT.getTag(mail);
        NBTWrappers.NBTTagCompound mailsContainer = new NBTWrappers.NBTTagCompound();
        NBTWrappers.NBTTagCompound nbtContainer = new NBTWrappers.NBTTagCompound();
        nbtContainer.set("fromGroup", NBTType.STRING, FastUUID.toString(draft.getFromGroup().getId()));
        nbtContainer.set("GroupType", NBTType.STRING, draft.getFromGroup().getClass().getName());
        nbtContainer.set("Created", NBTType.LONG, System.currentTimeMillis());
        nbtContainer.setCompound("Recipients", new NBTWrappers.NBTTagCompound());
        if (draft.getSubject() != null) nbtContainer.setString("Subject", draft.getSubject());
        if (!draft.getRecipientsRaw().isEmpty()) {
            NBTWrappers.NBTTagCompound recipientNBT = new NBTWrappers.NBTTagCompound();
            for (Map.Entry<UUID, MailRecipientType> recipient : draft.getRecipientsRaw().entrySet()) {
                recipientNBT.setString(FastUUID.toString(recipient.getKey()), recipient.getValue().name());
            }
            nbtContainer.setCompound("Recipients", recipientNBT);
        }

        mailsContainer.setCompound(MAILS_NBT, nbtContainer);
        nbt.setCompound("Castel", mailsContainer);
        return ItemNBT.setTag(mail, nbt);
    }

    static MessageBuilder createSettings(Player sender, DraftMail mail) {
        return getMailEdits(mail).withContext(sender);
    }

    private static DraftMail getDraftMail(ItemStack mail) {
        NBTWrappers.NBTTagCompound nbt = ItemNBT.getTag(mail);
        NBTWrappers.NBTTagCompound castelCompound = nbt.getCompound("Castel");
        if (castelCompound == null) return null;
        NBTWrappers.NBTTagCompound mailCompound = castelCompound.getCompound(MAILS_NBT);
        if (mailCompound == null) return null;
        UUID fromGroup = FastUUID.fromString(mailCompound.get("FromGroup", NBTType.STRING));
        String groupTypename = mailCompound.get("GroupType", NBTType.STRING);
        Class<? extends Group> groupType;
        try {
            groupType = (Class<? extends Group>) Class.forName(groupTypename);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        boolean isGuild = groupType == Guild.class;
        Group group = isGuild ? Guild.getGuild(fromGroup) : null;
        String subject = mailCompound.getString("Subject");
        String replyTo = mailCompound.getString("ReplyTo");
        UUID inReplyTo = replyTo == null ? null : FastUUID.fromString(replyTo);
        List<String> message = new ArrayList<>(((BookMeta)mail.getItemMeta()).getPages());
        Map<UUID, MailRecipientType> recipients = new HashMap<>();
        NBTWrappers.NBTTagCompound recipientsNBT = mailCompound.getCompound("Recipients");
        for (Map.Entry<String, NBTWrappers.NBTBase<?>> recipient : recipientsNBT.getValue().entrySet()) {
            UUID recipientId = FastUUID.fromString(recipient.getKey());
            MailRecipientType recipientType = MailRecipientType.valueOf(String.valueOf(recipient.getValue().getValue()));
            recipients.put(recipientId, recipientType);
        }
        return new DraftMail(group, message, subject, recipients, inReplyTo, groupType);
    }

    public static ItemStack getMailItem(CachedMail mail, boolean sent) {
        ItemStack draftItem = XMaterial.WRITABLE_BOOK.parseItem();
        BookMeta meta = (BookMeta) draftItem.getItemMeta();
        meta.setTitle(mail.getSubject());
        meta.setAuthor(mail.getSender().getName());
        List<Group> to = mail.getRecipientsOfType(MailRecipientType.PRIMARY);
        MessageBuilder settings = new MessageBuilder().raw("id", mail.getId() == null ? "none" : FastUUID.toString(mail.getId())).parse("subject", mail.getSubject()).raw("sent", mail.getSent()).raw("sender", mail.getSender().getName()).raw("from", mail.getFromGroup().getName()).raw("to", to.isEmpty() ? Lang.NONE : to.get(0).getName()).parse("cc", buildCCof(mail.getRecipientsOfType(MailRecipientType.CC)));
        List<BaseComponent[]> pages = new ArrayList<>(meta.getPageCount());
        BaseComponent[] header = (sent ? Lang.MAILS_HEADER_FORMAT_SENT : Lang.MAILS_HEADER_FORMAT_RECEIVED).getProvider(SupportedLanguage.EN).getMessage().build(settings).createSingular();
        pages.add(header);
        for (String page : mail.getMessage()) {
            BaseComponent[] msg = MessageCompiler.compile(page, new MessageCompilerSettings(false, true, true, false, true, null)).build(new MessageBuilder()).createSingular();
            pages.add(msg);
        }
        meta.spigot().setPages(pages);
        draftItem.setItemMeta(meta);
        return draftItem;
    }

    public static void openMail(Group fromGroup, Player player, Mail mail) {
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        cp.readMail(mail);
        ItemStack book = getMailItem(mail.toCached(GroupResolver.GUILDS_RESOLVER), mail.getFromGroup().equals(fromGroup.getId()));
        XBook.openBook(book, player);
    }

    public static ItemStack preview(Player sender, DraftMail draf) {
        return getMailItem(new CachedMail(null, draf.getFromGroup(), sender, draf.getMessage(), System.currentTimeMillis(), draf.getSubject(), draf.getRecipients(), draf.getInReplyTo() == null ? null : Mail.getMail(draf.getInReplyTo())), true);
    }
}
