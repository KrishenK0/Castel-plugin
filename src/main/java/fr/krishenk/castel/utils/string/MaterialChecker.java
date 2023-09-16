package fr.krishenk.castel.utils.string;

import fr.krishenk.castel.libs.xseries.XMaterial;

public class MaterialChecker extends StringCheckerOptions {
    private final XMaterial matchedMaterial;

    public MaterialChecker(String text) {
        super(text);
        this.matchedMaterial = XMaterial.matchXMaterial(text).orElse(null);
    }

    @Override
    public boolean check(String text) {
        if (this.mode == StringCheckerOptions.StringCheckerMode.NORMAL && this.matchedMaterial != null) {
            return this.matchedMaterial == XMaterial.matchXMaterial(text).orElse(null);
        }
        return super.check(text);
    }
}
