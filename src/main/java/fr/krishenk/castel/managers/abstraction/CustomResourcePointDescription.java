package fr.krishenk.castel.managers.abstraction;

import fr.krishenk.castel.managers.ItemMatcher;
import org.bukkit.configuration.ConfigurationSection;

public class CustomResourcePointDescription extends ItemMatcher {
    private final String node;
    private final double worth;

    public CustomResourcePointDescription(String node, ConfigurationSection section) {
        super(section);
        this.node = node;
        this.worth = section.getDouble("resource-points");
    }

    public double getWorth() {
        return worth;
    }

    public String getNode() {
        return node;
    }
}
