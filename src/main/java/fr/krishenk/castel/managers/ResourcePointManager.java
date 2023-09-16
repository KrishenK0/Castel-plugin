package fr.krishenk.castel.managers;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.config.NewKeyedConfigAccessor;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.libs.xseries.XItemStack;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.managers.abstraction.CustomResourcePointDescription;
import fr.krishenk.castel.utils.cache.LazySupplier;
import fr.krishenk.castel.utils.nbt.ItemNBT;
import fr.krishenk.castel.utils.nbt.NBTType;
import fr.krishenk.castel.utils.nbt.NBTWrappers;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.BiFunction;

public class ResourcePointManager {
    private static final String RESOURCE_POINTS = "ResourcePoints";
    private static CustomResourcePointDescription[] customResourcePointDescriptions;

    public static void loadSettings() {
        ConfigurationSection section = Config.ResourcePoints.ADVANCED.getManager().getSection();
        if (section == null) {
            customResourcePointDescriptions = new CustomResourcePointDescription[0];
            CLogger.error("'advanced' section of resource-points.yml is not found.");
        } else {
            Set<String> keys = section.getKeys(false);
            customResourcePointDescriptions = new CustomResourcePointDescription[keys.size()];
            int i = 0;
            for (String key : keys) {
                ConfigurationSection customSection = section.getConfigurationSection(key);
                customResourcePointDescriptions[i++] = new CustomResourcePointDescription(key, customSection);
            }
        }
    }

    public static CustomResourcePointDescription[] getCustomResourcePointDescriptions() {
        return customResourcePointDescriptions;
    }

    public static String getResourcePointsNBTTag() {
        return RESOURCE_POINTS;
    }

    private static Long getCustomWorth(ItemStack item, ItemMeta meta, NBTWrappers.NBTTagCompound nbt) {
        for (CustomResourcePointDescription description : customResourcePointDescriptions) {
            if (description.matches(item, meta, LazySupplier.of(() -> nbt)))
                return (long) description.getWorth();
        }
        return null;
    }

    public static ItemStack injectWorth(ItemStack item, double resourcePoints) {
        NBTWrappers.NBTTagCompound nbt = ItemNBT.getTag(item);
        NBTWrappers.NBTTagCompound castel = nbt.getCompound("Castel");
        if (castel == null) {
            castel = new NBTWrappers.NBTTagCompound();
            nbt.set("Castel", castel);
        }
        castel.set(RESOURCE_POINTS, NBTType.DOUBLE, resourcePoints);
        return ItemNBT.setTag(item, nbt);
    }

    public static Double getInjectedWorth(ItemStack item) {
        return getInjectedWorth(ItemNBT.getTag(item));
    }

    public static Double getInjectedWorth(NBTWrappers.NBTTagCompound nbt) {
        NBTWrappers.NBTTagCompound castel = nbt.getCompound("Castel");
        return castel == null ? null : castel.get(RESOURCE_POINTS, NBTType.DOUBLE);
    }

    public static Double removeInjectedWorth(ItemStack item) {
        NBTWrappers.NBTTagCompound nbt = ItemNBT.getTag(item);
        NBTWrappers.NBTTagCompound castel = nbt.getCompound("Castel");
        if (castel == null) return null;
        NBTWrappers.NBTTagDouble worth = castel.remove(RESOURCE_POINTS);
        return worth.getValue();
    }

    public static Pair<ItemStack, Double> buildItem(String name) {
        ConfigurationSection section = Objects.requireNonNull(Config.ResourcePoints.CUSTOM_ITEMS.getManager().getSection(), "Cannot find the custom items section in the config");
        section = section.getConfigurationSection(name);
        if (section == null) return null;
        ItemStack item = XItemStack.deserialize(section, MessageHandler::colorize);
        double rp = section.getDouble("resource-points");
        item = injectWorth(item, rp);
        return Pair.of(item, rp);
    }

    public static long getWorth(ItemStack item) {
        return convertToResourcePoints(Collections.singleton(item), null).getKey();
    }

    public static Pair<Long, List<ItemStack>> convertToResourcePoints(Collection<ItemStack> donations, BiFunction<ItemStack, List<ItemStack>, Long> function) {
        return convertToResourcePoints(donations, function, null);
    }

    public static Pair<Long, List<ItemStack>> convertToResourcePoints(Collection<ItemStack> donations, BiFunction<ItemStack, List<ItemStack>, Long> function, Set<ResourcePointWorthType> exclude) {
        List<String> list = Config.ResourcePoints.GENERAL_FILTERS_MATERIAL_LIST.getManager().getStringList();
        boolean blacklist = Config.ResourcePoints.GENERAL_FILTERS_MATERIAL_BLACKLIST.getManager().getBoolean();
        NewKeyedConfigAccessor loreFilterOpt = Config.ResourcePoints.GENERAL_FILTERS_LORE.getManager();
        NewKeyedConfigAccessor enchantFilterOpt = Config.ResourcePoints.GENERAL_FILTERS_ENCHANTED.getManager();
        List<String> loreFilter = loreFilterOpt.isSet() ? loreFilterOpt.getStringList() : null;
        Boolean enchantFilter = enchantFilterOpt.isSet() ? enchantFilterOpt.getBoolean() : null;
        int forEach = Config.ResourcePoints.FOR_EACH.getManager().getInt();
        int giveAmnt = Config.ResourcePoints.GIVE.getManager().getInt();
        boolean hasNormalWorth = forEach != 0 && giveAmnt != 0 && (exclude == null || !exclude.contains(ResourcePointWorthType.NORMAL));
        boolean includeInjection = exclude == null || !exclude.contains(ResourcePointWorthType.INJECTED);
        boolean includeSpecials = exclude == null || !exclude.contains(ResourcePointWorthType.SPECIAL);
        ConfigurationSection customMaterials = Config.ResourcePoints.CUSTOM.getManager().getSection();
        List<ItemStack> leftOvers = new ArrayList<>();
        long total = 0L;

        for (ItemStack item : donations) {
            if (function != null) {
                Long amount = function.apply(item, leftOvers);
                if (amount != null) {
                    total += amount;
                    continue;
                }
            }

            int amount = item.getAmount();
            NBTWrappers.NBTTagCompound nbt = ItemNBT.getTag(item);
            if (includeInjection) {
                Double injected = getInjectedWorth(nbt);
                if (injected != null) {
                    total = (long) (total + injected * amount);
                    continue;
                }
            }

            if (includeSpecials) {
                ItemMeta meta = item.getItemMeta();
                Long advancedRp = getCustomWorth(item, meta, nbt);
                if (advancedRp != null) {
                    total += advancedRp * amount;
                    continue;
                }

                boolean hasLore = meta.hasLore();
                if (hasLore && loreFilter != null && !Objects.equals(meta.getLore(), loreFilter)) {
                    leftOvers.add(item);
                    continue;
                }

                boolean hasEnchants = meta.hasEnchants();
                if (hasEnchants && enchantFilter != null && hasEnchants != enchantFilter) {
                    leftOvers.add(item);
                    continue;
                }
            }

            XMaterial material = XMaterial.matchXMaterial(item);
            int special = includeSpecials ? customMaterials.getInt(material.name()) : 0;
            if (special != 0) {
                total += (long) special * amount;
            } else if (hasNormalWorth) {
                if (blacklist == material.isOneOf(list)) {
                    leftOvers.add(item);
                } else {
                    total += (long) (amount / forEach) * giveAmnt;
                    int remainder = amount % forEach;
                    if (remainder != 0) {
                        item.setAmount(remainder);
                        leftOvers.add(item);
                    }
                }
            }
        }
        return Pair.of(total, leftOvers);
    }

    static {
        loadSettings();
    }
}
