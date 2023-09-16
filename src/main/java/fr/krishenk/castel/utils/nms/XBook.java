package fr.krishenk.castel.utils.nms;

import com.google.common.base.Strings;
import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class XBook {
    private static final Object MAIN_HAND;
    private static final MethodHandle CHAT_SERIALIZER = null;
    private static final MethodHandle OPEN_BOOK_METHOD;
    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle PAGES = null;
    private static final boolean SUPPORTED;
    private static final boolean ADD_PAGES;

    public static void openBook(ItemStack book, Player ... players) {
        Objects.requireNonNull(book, "Cannot open null book");
        Objects.requireNonNull(players, "Cannot open book to null players");
        if (SUPPORTED) {
            for (Player player : players) {
                player.openBook(book);
            }
        } else {
            Object nmsBook;
            try {
                nmsBook = AS_NMS_COPY.invoke(book);
            } catch (Throwable e) {
                e.printStackTrace();
                return;
            }

            for (Player player : players) {
                PlayerInventory inventory = player.getInventory();
                int slot = inventory.getHeldItemSlot();
                ItemStack previous = inventory.getItem(slot);
                inventory.setItem(slot, book);
                Object nmsPlayer = ReflectionUtils.getHandle(player);
                try {
                    OPEN_BOOK_METHOD.invoke(nmsPlayer, nmsBook, MAIN_HAND);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                inventory.setItem(slot, previous);
            }
        }
    }

    public static ItemStack getBook(List<String> pages, Player player, String title, boolean editable, boolean raw) {
        return getBook(pages, player.getName(), title, editable, raw ? null : (String page) -> MessageCompiler.compile(page).build(new MessageBuilder().withContext(player)).createSingular());
    }

    public static ItemStack getBook(List<String> pages, String author, String title, boolean editable, Function<String, BaseComponent[]> pageTranslator) {
        ItemStack book = (editable ? XMaterial.WRITABLE_BOOK : XMaterial.WRITABLE_BOOK).parseItem();
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        bookMeta.setAuthor(author);
        bookMeta.setTitle(title);
        if (pageTranslator == null) bookMeta.setPages(pages.toArray(new String[0]));
        else {
            for (String page : pages) {
                if (Strings.isNullOrEmpty(page)) bookMeta.addPage("");
                else {
                    BaseComponent[] msg = pageTranslator.apply(page);
                    bookMeta.spigot().addPage(msg);
                }
            }
        }

        book.setItemMeta(bookMeta);
        return book;
    }

    static {
        boolean addPages;
        try {
            Player.class.getDeclaredMethod("openBook", ItemStack.class);
            addPages = true;
        } catch (NoSuchMethodException e) {
            addPages = false;
        }
        SUPPORTED = addPages;

        try {
            BookMeta.class.getDeclaredMethod("spigot");
            addPages = true;
        } catch (NoSuchMethodException e) {
            addPages = false;
        }
        ADD_PAGES = addPages;

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle openBookMethod = null;
        MethodHandle asNMSCopy = null;
        MethodHandle chatSerializer = null;
        MethodHandle pages = null;
        Object mainHand = null;
        if (!SUPPORTED) {
            Class<?> hand = ReflectionUtils.getNMSClass("EnumHand");
            Class<?> entityPlayer = ReflectionUtils.getNMSClass("EntityPlayer");
            Class<?> nmsItemStack = ReflectionUtils.getNMSClass("ItemStack");
            Class<?> craftItemStack = ReflectionUtils.getNMSClass("inventory.CraftItemStack");

            try {
                mainHand = hand.getField("MAIN_HAND").get(null);
                asNMSCopy = lookup.findStatic(craftItemStack, "asNMSCopy", MethodType.methodType(nmsItemStack, ItemStack.class));
                if (ReflectionUtils.supports(9)) {
                    openBookMethod = lookup.findVirtual(entityPlayer, "a", MethodType.methodType(Void.TYPE, nmsItemStack, hand));
                } else {
                    openBookMethod = lookup.findVirtual(entityPlayer, "openBook", MethodType.methodType(Void.TYPE, nmsItemStack));
                }
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        MAIN_HAND = mainHand;
        OPEN_BOOK_METHOD = openBookMethod;
        AS_NMS_COPY = asNMSCopy;
    }
}
