package fr.krishenk.castel.constants.land.abstraction;

import fr.krishenk.castel.constants.group.upgradable.Upgrade;

public abstract class CastelItemStyle<I extends CastelItem<S>, S extends CastelItemStyle<I, S, T>, T extends CastelItemType<I, S, T>> implements Upgrade {
}
