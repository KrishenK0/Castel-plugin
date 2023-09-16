package fr.krishenk.castel.commands.admin.item;

import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XItemStack;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.nbt.ItemNBT;
import fr.krishenk.castel.utils.nbt.NBTWrappers;
import fr.krishenk.castel.utils.string.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemEditor {
    private final Player player;
    private final ItemStack item;
    private final NBTWrappers.NBTTagCompound rootNBT;

    public ItemEditor(Player player, ItemStack item) {
        this.player = player;
        this.item = item;
        this.rootNBT = ItemNBT.getTag(this.item);
    }

    private final MessageBuilder getEditForItem() {
        ItemMeta meta = this.item.getItemMeta();
        String name;
        if (meta.hasDisplayName()) {
            name = meta.getDisplayName().replace('§', '&');
        } else name = StringUtils.capitalize(this.item.getType().name());
        MessageBuilder settings = new MessageBuilder().placeholders("item_name", name).raw("item_count", this.item.getAmount()).raw("item_material", Lang.translateMaterial(XMaterial.matchXMaterial(this.item))).raw("item_custom_model_data", XMaterial.supports(14) && meta.hasCustomModelData() ? meta.getCustomModelData() : 0).raw("item_unbreakable", meta.isUnbreakable());
        for (ItemFlag flag : XItemStack.ITEM_FLAGS) {
            settings.raw("item_flag_" + flag.name().toLowerCase(Locale.ROOT), meta.hasItemFlag(flag) ? Lang.ENABLED.parse() : Lang.DISABLED.parse());
        }
        return settings;
    }

    private MessageBuilder getEditsForAttribute(Attribute attribute, AttributeModifier modifier) {
        return new MessageBuilder().raws("attribute_type", StringUtils.capitalize(attribute.name()), "attribute_uuid", modifier.getUniqueId(), "attribute_name", modifier.getName(), "attribute_amount", modifier.getAmount(), "attribute_operation", StringUtils.capitalize(modifier.getOperation().name()), "attribute_equipment_slot", StringUtils.capitalize(modifier.getSlot() != null ? modifier.getSlot().name() : "Any"));
    }

    private String getTypeOfNBT(NBTWrappers.NBTBase<?> nbt) {
        if (nbt instanceof NBTWrappers.NBTTagString) return "string";
        if (nbt instanceof NBTWrappers.NBTTagInt) return "int";
        if (nbt instanceof NBTWrappers.NBTTagDouble) return "double";
        if (nbt instanceof NBTWrappers.NBTTagByte) return "byte";
        if (nbt instanceof NBTWrappers.NBTTagLong) return "long";
        if (nbt instanceof NBTWrappers.NBTTagShort) return "short";
        if (nbt instanceof NBTWrappers.NBTTagEnd) return "end";
        if (nbt instanceof NBTWrappers.NBTTagFloat) return "float";
        if (nbt instanceof NBTWrappers.NBTTagCompound) return "compound";
        if (nbt instanceof NBTWrappers.NBTTagList) return "list";
        return nbt.getClass().getSimpleName();
    }

    private void buildPathsFrom(List<String> list, NBTWrappers.NBTTagCompound nbt, String currentPath) {
        if (nbt.getValue().isEmpty()) list.add(currentPath);
        else {
            for (Map.Entry<String, NBTWrappers.NBTBase<?>> nbtBase : nbt.getValue().entrySet()) {
                String key = nbtBase.getKey();
                NBTWrappers.NBTBase<?> value = nbtBase.getValue();
                String path = ((CharSequence) currentPath).length() == 0 ? key : currentPath + '/' + key;
                if (value instanceof NBTWrappers.NBTTagCompound) {
                    this.buildPathsFrom(list, (NBTWrappers.NBTTagCompound) value, path);
                } else {
                    list.add(path);
                }
            }
        }
    }

    public NBTWrappers.NBTBase<?> constructNBT(String type, Collection<String> value) {
        NBTWrappers.NBTBase<?> nBTBase;
        switch (type) {
            case "NBTTagCompound": {
                nBTBase = new NBTWrappers.NBTTagCompound();
                break;
            }
            case "NBTTagList": {
                List<NBTWrappers.NBTBase<?>> collection = new ArrayList<>(value.size());
                for (String name : value) {
                    collection.add(new NBTWrappers.NBTTagString(name));
                }
                nBTBase = new NBTWrappers.NBTTagList(collection);
                break;
            }
            case "NBTTagEnd": {
                nBTBase = new NBTWrappers.NBTTagEnd();
                break;
            }
            case "NBTTagByte": {
                nBTBase = new NBTWrappers.NBTTagByte(Byte.parseByte(StringUtils.join(value.toArray(), "")));
                break;
            }
            case "NBTTagShort": {
                nBTBase = new NBTWrappers.NBTTagShort(Short.parseShort(StringUtils.join(value.toArray(), "")));
                break;
            }
            case "NBTTagInt": {
                nBTBase = new NBTWrappers.NBTTagInt(Integer.parseInt(StringUtils.join(value.toArray(), "")));
                break;
            }
            case "NBTTagLong": {
                nBTBase = new NBTWrappers.NBTTagLong(Long.parseLong(StringUtils.join(value.toArray(), "")));
                break;
            }
            case "NBTTagFloat": {
                nBTBase = new NBTWrappers.NBTTagFloat(Float.parseFloat(StringUtils.join(value.toArray(), "")));
                break;
            }
            case "NBTTagDouble": {
                nBTBase = new NBTWrappers.NBTTagDouble(Double.parseDouble(StringUtils.join(value.toArray(), "")));
                break;
            }
            case "NBTTagString": {
                nBTBase = new NBTWrappers.NBTTagString(StringUtils.join(value.toArray(), ""));
                break;
            }
            case "NBTTagByteArray": {
                List<Byte> collection = new ArrayList<>(value.size());
                for (String x : value) {
                    collection.add(Byte.parseByte(x));
                }
                nBTBase = new NBTWrappers.NBTTagByteArray(ArrayUtils.toPrimitive(collection.toArray(new Byte[0])));
                break;
            }
            case "NBTTagIntArray": {
                List<Integer> collection = new ArrayList<>(value.size());
                for (String x : value) {
                    collection.add(Integer.parseInt(x));
                }
                nBTBase = new NBTWrappers.NBTTagIntArray(collection.stream().mapToInt(Integer::intValue).toArray());
                break;
            }
            case "NBTTagLongArray": {
                List<Long> collection = new ArrayList<>(value.size());
                for (String x : value) {
                    collection.add(Long.parseLong(x));
                }
                nBTBase = new NBTWrappers.NBTTagLongArray(collection.stream().mapToLong(Long::longValue).toArray());
                break;
            }
            default: {
                nBTBase = null;
            }
        }
        return nBTBase;
    }
}
