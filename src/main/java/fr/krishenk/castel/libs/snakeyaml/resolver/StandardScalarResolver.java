
package fr.krishenk.castel.libs.snakeyaml.resolver;

import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.nodes.Tag;

import java.util.regex.Pattern;

public class StandardScalarResolver
implements ScalarResolver {
    public static final Pattern FLOAT = Pattern.compile("-?(0?\\.[0-9]+|[1-9][0-9]*(\\.[0-9]*)?)(e[-+]?[0-9]+)?");
    public static final Pattern INT = Pattern.compile("-?(?:(?:0x|#)[A-Fa-f0-9]+|[1-9](?:[0-9],?)*)|0");
    public static final Pattern MERGE = Pattern.compile("^(?:<<)$");

    @Override
    public Tag resolve(ResolverContext context) {
        if (context.getScalarStyle() == ScalarStyle.PLAIN) {
            String value = context.getValue();
            if (value.equalsIgnoreCase("null") || value.length() == 1 && value.charAt(0) == '~') {
                return Tag.NULL;
            }
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return Tag.BOOL;
            }
            if (value.equals("<<")) {
                return Tag.MERGE;
            }
            if (INT.matcher(value).matches()) {
                return Tag.INT;
            }
            if (FLOAT.matcher(value).matches()) {
                return Tag.FLOAT;
            }
        }
        return Tag.STR;
    }
}

