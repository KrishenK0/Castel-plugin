package fr.krishenk.castel.managers;

import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.libs.xseries.XEnchantment;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.utils.cache.LazySupplier;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import fr.krishenk.castel.utils.nbt.NBTWrappers;
import fr.krishenk.castel.utils.string.StringCheckerOptions;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemMatcher {
    private final StringCheckerOptions name;
    private final StringCheckerOptions lore;
    private final StringCheckerOptions material;
    private final int customModelData;
    private final List<Pair<XEnchantment, ConditionalCompiler.LogicalOperand>> enchants;
    private final Pair<String[], StringCheckerOptions>[] NBT;
    private final ConfigurationSection section;

    public ItemMatcher(ConfigurationSection section) {
        Objects.requireNonNull(section, "Cannot parse item matcher of a null section");
        this.section = section;
        this.material = section.isSet("material") ? new StringCheckerOptions(section.getString("material")) : null;
        this.customModelData = section.getInt("custom-model-data");
        this.name = section.isSet("name") ? new StringCheckerOptions(section.getString("name")) : null;
        this.lore = section.isSet("lore") ? new StringCheckerOptions(section.getString("lore")) : null;
        if (section.isSet("enchants")) {
            this.enchants = new ArrayList<>();
            ConfigurationSection enchantsSection = section.getConfigurationSection("enchants");
            XEnchantment enchant;
            ConditionalCompiler.LogicalOperand condition;
            for (Iterator<String> it = enchantsSection.getKeys(false).iterator(); it.hasNext(); this.enchants.add(Pair.of(enchant, condition))) {
                String key = it.next();
                String conditionStr = enchantsSection.getString(key);
                int asLvl = enchantsSection.getInt(key);
                enchant = XEnchantment.matchXEnchantment(key).orElseThrow(() -> new IllegalStateException("Unknown enchantement: " + key));
                if (conditionStr.equals("*")) condition = ConditionalCompiler.ConstantLogicalOperand.TRUE;
                else if (asLvl == 0 && !conditionStr.equals("0")) condition = ConditionalCompiler.compile(conditionStr).evaluate();
                else condition = new ConditionalCompiler.BiLogicalOperator(new ConditionalCompiler.LogicalVariableOperand("lvl"), ConditionalCompiler.LogicalOperator.EQUALS, new ConditionalCompiler.ArithmeticOperand(new MathCompiler.ConstantExpr(asLvl, MathCompiler.ConstantExprType.NUMBER)));
            }
        } else this.enchants = null;

        if (section.isSet("nbt")) {
            ConfigurationSection nbt = section.getConfigurationSection("nbt");
            Set<String> keys = nbt.getKeys(false);
            this.NBT = new Pair[keys.size()];
            int i = 0;
            StringCheckerOptions checker;
            String[] splits;
            for (Iterator<String> it = keys.iterator(); it.hasNext(); this.NBT[i++] = Pair.of(splits, checker)) {
                String key = it.next();
                splits = StringUtils.splitArray(key, '.');
                checker = new StringCheckerOptions(nbt.getString(key));
            }
        } else this.NBT = new Pair[0];
    }

    public ConfigurationSection getSection() {
        return section;
    }
    public boolean matchesMaterial(ItemStack item) {
        return this.material == null || this.material.check(XMaterial.matchXMaterial(item).name());
    }

    public boolean matches(ItemStack item) {
        return this.matches(item, null);
    }

    public boolean matches(ItemStack item, LazySupplier<NBTWrappers.NBTTagCompound> nbt) {
        return this.matches(item, item.getItemMeta(), nbt);
    }

    public boolean matches(ItemStack itemStack, ItemMeta meta, LazySupplier<NBTWrappers.NBTTagCompound> nbt) {
        if (!this.matchesMaterial(itemStack)) return false;
        else if (!this.matchesCustomModel(meta)) return false;
        else if (!this.matchesEnchants(meta == null ? null : meta.getEnchants())) return false;
        else if (this.matchesName(meta) && this.matchesLore(meta)) return nbt == null || this.needsNBT() || !this.matchesNBT(nbt.get());
        else return false;
    }

    private boolean matchesEnchants(Map<Enchantment, Integer> enchants) {
        if (this.enchants == null) return true;
        if (this.enchants.isEmpty()) return true;
        Iterator<Pair<XEnchantment, ConditionalCompiler.LogicalOperand>> it = this.enchants.iterator();
        boolean passed;
        do {
            if (!it.hasNext()) return true;
            Pair<XEnchantment, ConditionalCompiler.LogicalOperand> enchant = it.next();
            Integer lvl = enchants.get(enchant.getKey().getEnchant());
            if (lvl == null) return false;

            ConditionalCompiler.LogicalOperand requiredLvl = enchant.getValue();
            passed = (boolean) requiredLvl.eval((x) -> x.equals("lvl") ? lvl : null);
        } while (passed);
        return false;
    }

    public boolean needsNBT() {
        return this.NBT != null;
    }

    public boolean matchesName(ItemMeta meta) {
        if (this.name == null) return true;
        return meta != null && meta.hasDisplayName() && this.name.check(meta.getDisplayName());
    }

    public boolean matchesLore(ItemMeta meta) {
        if (this.lore == null) return true;
        if (meta.hasLore()) {
            Iterator<String> it = meta.getLore().iterator();
            String line;
            do {
                if (!it.hasNext()) return false;
                line = it.next();
            } while (!this.lore.check(line));
            return true;
        }
        return false;
    }

    public boolean matchesCustomModel(ItemMeta meta) {
        if (!XMaterial.supports(14)) return true;
        if (this.customModelData == 0) return true;
        if (meta == null) return false;
        return meta.hasCustomModelData() && meta.getCustomModelData() == this.customModelData;
    }

    public boolean matchesNBT(NBTWrappers.NBTTagCompound nbt) {
        for (Pair<String[], StringCheckerOptions> tag : this.NBT) {
            NBTWrappers.NBTTagCompound last = null;
            int times = 0;
            for (String name : tag.getKey()) {
                ++times;
                NBTWrappers.NBTBase<?> current = (last == null ? nbt : last).get(name);
                if (current == null) return false;

                if (current instanceof NBTWrappers.NBTTagCompound) {
                    last = (NBTWrappers.NBTTagCompound) current;
                } else {
                    if (times != tag.getKey().length) return false;

                    Object value = current.getValue();
                    if (value == null) return false;
                    if (!(tag.getValue().check(value.toString()))) return false;
                }
            }
        }
        return true;
    }
}
