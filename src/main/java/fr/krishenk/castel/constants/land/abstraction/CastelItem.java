package fr.krishenk.castel.constants.land.abstraction;

import fr.krishenk.castel.abstraction.LandOperator;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.constants.metadata.CastelObject;

public abstract class CastelItem<T extends CastelItemStyle<?, ?, ?>> extends CastelObject<SimpleLocation> implements LandOperator {
}
