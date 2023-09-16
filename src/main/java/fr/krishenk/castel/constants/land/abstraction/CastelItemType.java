package fr.krishenk.castel.constants.land.abstraction;

public abstract class CastelItemType<I extends CastelItem<S>, S extends CastelItemStyle<I, S, T>, T extends CastelItemType<I, S, T>> {
    protected final String name;

    public CastelItemType(String name) {
        this.name = name;
    }

//    public static Pair<CastelItemStyle<?, ?, ?>, Integer> getTypeOf(ItemStack item) {
//        NBTWrappers.NBTTagCompound nbt = ItemNBT.getTag(item);
//        nbt = nbt.getCompound("Castel");
//        if (nbt != null) {
//            String tag = nbt.get("Strucute")
//        }
//    }
}
