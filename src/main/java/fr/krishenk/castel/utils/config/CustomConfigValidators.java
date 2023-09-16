package fr.krishenk.castel.utils.config;

import com.google.common.base.Enums;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.player.GuildPermission;
import fr.krishenk.castel.libs.snakeyaml.nodes.NodeType;
import fr.krishenk.castel.libs.snakeyaml.nodes.ScalarNode;
import fr.krishenk.castel.libs.snakeyaml.nodes.Tag;
import fr.krishenk.castel.libs.snakeyaml.validation.NodeValidator;
import fr.krishenk.castel.libs.snakeyaml.validation.ValidationContext;
import fr.krishenk.castel.libs.snakeyaml.validation.ValidationFailure;
import fr.krishenk.castel.libs.xseries.*;
import fr.krishenk.castel.locale.LanguageEntry;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import fr.krishenk.castel.utils.config.adapters.YamlContainer;
import fr.krishenk.castel.utils.string.MaterialChecker;
import fr.krishenk.castel.utils.string.StringUtils;
import fr.krishenk.castel.utils.time.TimeUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CustomConfigValidators {
    private static final Map<String, NodeValidator> VALIDATORS = new HashMap<String, NodeValidator>();
    public static final Tag MATH = new Tag("Math");
    public static final Tag MATERIAL = new Tag("Material");
    public static final Tag BIOME = new Tag("Biome");
    public static final Tag CONDITION = new Tag("Condition");
    public static final Tag ENCHANT = new Tag("Enchant");
    public static final Tag MATERIAL_MATCHER = new Tag("MaterialMatcher");
    public static final Tag STRING_CHECKER_OPTIONS = new Tag("StringCheckerOptions");
    public static final Tag ITEM_MATCHER = new Tag("ItemMatcher");
    public static final Tag ITEM_STACK = new Tag("ItemStack");
    public static final Tag ENTITY = new Tag("Entity");
    public static final Tag ITEM_FLAG = new Tag("ItemFlag");
    public static final Tag WORLD = new Tag("World");
    public static final Tag KINGDOM_PERMISSION = new Tag("KingdomPermission");
    public static final Tag SOUND = new Tag("Sound");
    public static final Tag PERIOD = new Tag("Period");
    public static final Tag STRUCTURE_TYPE = new Tag("StructureType");
    public static final Tag TURRET_TYPE = new Tag("TurretType");
    public static final Tag POTION = CustomConfigValidators.register("Potion", new Potion());
    public static final Tag MESSAGE_ENTRY = CustomConfigValidators.register("MessageEntry", new MessageEntry());
    public static final Tag PARTICLE = CustomConfigValidators.register("Particle", new Particle());
    private static final NodeValidator ITEM_STACK_VALIDATOR;
    private static final NodeValidator ITEM_MATCHER_VALIDATOR;
    private static final NodeValidator ENTITY_VALIDATOR;

    private CustomConfigValidators() {
    }

    public static Map<String, NodeValidator> getValidators() {
        return VALIDATORS;
    }

    public static Tag register(String name, NodeValidator validator) {
        Tag tag = new Tag(name);
        VALIDATORS.put(name, validator);
        return tag;
    }

    static {
        VALIDATORS.put(MATH.getValue(), new Math());
        VALIDATORS.put(MATERIAL.getValue(), new Material());
        VALIDATORS.put(ENTITY.getValue(), new Entity());
        VALIDATORS.put(BIOME.getValue(), new Biome());
        VALIDATORS.put(CONDITION.getValue(), new Condition());
        VALIDATORS.put(STRING_CHECKER_OPTIONS.getValue(), new StringCheckerOptions());
        VALIDATORS.put(MATERIAL_MATCHER.getValue(), new MaterialMatcher());
        VALIDATORS.put(KINGDOM_PERMISSION.getValue(), new KingdomPermission());
        VALIDATORS.put(SOUND.getValue(), new Sound());
        VALIDATORS.put(PERIOD.getValue(), new Period());
        VALIDATORS.put(ENCHANT.getValue(), new Enchant());
        VALIDATORS.put(ITEM_STACK.getValue(), new ItemStack());
        VALIDATORS.put(ITEM_MATCHER.getValue(), new ItemMatcher());
        VALIDATORS.put(ITEM_FLAG.getValue(), new ItemFlag());
        VALIDATORS.put(WORLD.getValue(), new World());
//        VALIDATORS.put(STRUCTURE_TYPE.getValue(), new StructureType());
//        VALIDATORS.put(TURRET_TYPE.getValue(), new TurretType());
        ITEM_STACK_VALIDATOR = YamlContainer.parseValidator("Item Stack", "schemas/item-stack.yml");
        ITEM_MATCHER_VALIDATOR = YamlContainer.parseValidator("Item Matcher", "schemas/item-stack.yml");
        ENTITY_VALIDATOR = YamlContainer.parseValidator("Entity", "schemas/entity.yml");
    }

    private static final class Potion
            implements NodeValidator {
        private Potion() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            if (context.getNode().getTag() == POTION) {
                return null;
            }
            if (context.getNode().getTag() != Tag.STR) {
                return context.err("Expected a potion effect");
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            Optional<XPotion> potion = XPotion.matchXPotion(scalarNode.getValue());
            if (!potion.isPresent()) {
                return context.err("Unknown potion effect '" + scalarNode.getValue() + '\'');
            }
            scalarNode.setTag(POTION);
            scalarNode.cacheConstructed((Object)potion.get());
            return null;
        }
    }

    private static final class MessageEntry
            implements NodeValidator {
        private MessageEntry() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            Tag tag = context.getNode().getTag();
            if (tag == MESSAGE_ENTRY) {
                return null;
            }
            if (tag != Tag.STR) {
                return context.err("Expected a message entry, instead got " + tag);
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            if (!LanguageEntry.isValidConfigLanguageEntry(scalarNode.getValue())) {
                return context.err("Malformed message entry: '" + scalarNode.getValue() + '\'');
            }
            scalarNode.setTag(MESSAGE_ENTRY);
            scalarNode.cacheConstructed(LanguageEntry.fromConfig(scalarNode.getValue()));
            return null;
        }
    }

    private static final class Particle
            implements NodeValidator {
        private Particle() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            Tag tag = context.getNode().getTag();
            if (tag == STRUCTURE_TYPE) {
                return null;
            }
            if (tag != Tag.STR) {
                return context.err("Expected a particle name, instead got " + tag);
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            com.google.common.base.Optional particle = Enums.getIfPresent(org.bukkit.Particle.class, (String)scalarNode.getValue());
            if (!particle.isPresent()) {
                return context.err("Unknown particle '" + scalarNode.getValue() + '\'');
            }
            return null;
        }
    }

    private static final class Math
            implements NodeValidator {
        private Math() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            MathCompiler.Expression compiled;
            Tag tag = context.getNode().getTag();
            if (tag == MATH) {
                return null;
            }
            if (tag != Tag.STR && tag != Tag.INT && tag != Tag.FLOAT) {
                return context.err("Expected math equation, instead got " + tag);
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            try {
                compiled = MathCompiler.compile(scalarNode.getValue());
            }
            catch (Exception ex) {
                return context.err(ex.getMessage());
            }
            scalarNode.setTag(MATH);
            scalarNode.cacheConstructed(compiled);
            return null;
        }
    }

    private static final class Material
            implements NodeValidator {
        private Material() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            if (context.getNode().getTag() == MATERIAL) {
                return null;
            }
            if (context.getNode().getTag() != Tag.STR) {
                return context.err("Expected a material");
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            Optional<XMaterial> mat = XMaterial.matchXMaterial(scalarNode.getValue());
            if (!mat.isPresent()) {
                return context.err("Unknown material '" + scalarNode.getValue() + '\'');
            }
            scalarNode.setTag(MATERIAL);
            scalarNode.cacheConstructed((Object)mat.get());
            return null;
        }
    }

    private static final class Entity
            implements NodeValidator {
        private Entity() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            Tag tag = context.getNode().getTag();
            if (tag == ENTITY) {
                return null;
            }
            if (context.getNode().getNodeType() != NodeType.MAPPING) {
                return context.err("Expected an entity section, instead got " + tag);
            }
            return ENTITY_VALIDATOR.validate(context);
        }
    }

    private static final class Biome
            implements NodeValidator {
        private Biome() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            if (context.getNode().getTag() == BIOME) {
                return null;
            }
            if (context.getNode().getTag() != Tag.STR) {
                return context.err("Expected a biome");
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            Optional<XBiome> mat = XBiome.matchXBiome(scalarNode.getValue());
            if (!mat.isPresent()) {
                return context.err("Unknown biome '" + scalarNode.getValue() + '\'');
            }
            return null;
        }
    }

    private static final class Condition
            implements NodeValidator {
        private Condition() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            ConditionalCompiler.LogicalOperand compiled;
            Tag tag = context.getNode().getTag();
            if (tag == MATH) {
                return null;
            }
            if (tag != Tag.STR && tag != Tag.BOOL) {
                return context.err("Expected a condition, instead got " + tag);
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            try {
                compiled = ConditionalCompiler.compile(scalarNode.getValue()).evaluate();
            }
            catch (Exception ex) {
                return context.err(ex.getMessage());
            }
            scalarNode.setTag(CONDITION);
            scalarNode.cacheConstructed(compiled);
            return null;
        }
    }

    private static final class StringCheckerOptions
            implements NodeValidator {
        private StringCheckerOptions() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            Tag tag = context.getNode().getTag();
            if (tag == STRING_CHECKER_OPTIONS) {
                return null;
            }
            if (context.getNode().getNodeType() != NodeType.SCALAR) {
                return context.err("Expected a string matcher text here, got '" + tag.getValue() + "' instead. ");
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            String val = scalarNode.getValue();
            String str = val.toUpperCase(Locale.ENGLISH);
            if (str.startsWith("REGEX:")) {
                str = val.substring("REGEX:".length());
                try {
                    Pattern.compile(str);
                }
                catch (PatternSyntaxException ex) {
                    return context.err("Failed to parse regex '" + val + "' " + ex.getMessage());
                }
            }
            return null;
        }
    }

    private static final class MaterialMatcher
            implements NodeValidator {
        private MaterialMatcher() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            if (context.getNode().getTag() == MATERIAL_MATCHER) {
                return null;
            }
            if (context.getNode().getTag() != Tag.STR) {
                return context.err("Expected a material");
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            String val = scalarNode.getValue();
            String material = val.toUpperCase(Locale.ENGLISH);
            if (material.startsWith("CONTAINS:")) {
                material = val.substring("CONTAINS:".length());
                for (char c : material.toCharArray()) {
                    if (c == '_' || StringUtils.isEnglishLetterOrDigit(c)) continue;
                    return context.err("Materials cannot possibly contain the character '" + c + '\'');
                }
            } else if (material.startsWith("REGEX:")) {
                material = val.substring("REGEX:".length());
                try {
                    Pattern.compile(material);
                }
                catch (PatternSyntaxException ex) {
                    return context.err("Failed to parse regex '" + material + "' " + ex.getMessage());
                }
            } else {
                Optional<XMaterial> mat = XMaterial.matchXMaterial(val);
                if (!mat.isPresent()) {
                    return context.err("Unknown material matcher '" + val + '\'');
                }
            }
            scalarNode.setTag(MATERIAL_MATCHER);
            scalarNode.cacheConstructed(new MaterialChecker(val));
            return null;
        }
    }

    private static final class KingdomPermission
            implements NodeValidator {
        private KingdomPermission() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            if (context.getNode().getTag() == KINGDOM_PERMISSION) {
                return null;
            }
            if (context.getNode().getTag() != Tag.STR) {
                return context.err("Expected a kingdom permission");
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            GuildPermission perm = CastelPlugin.getInstance().getPermissionRegistry().getRegistered(Namespace.fromString(scalarNode.getValue()));
            if (perm == null) {
                return context.err("Unknown kingdom permission '" + scalarNode.getValue() + '\'');
            }
            scalarNode.setTag(KINGDOM_PERMISSION);
            scalarNode.cacheConstructed(perm);
            return null;
        }
    }

    private static final class Sound
            implements NodeValidator {
        private Sound() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            XSound.Record record;
            if (context.getNode().getTag() == SOUND) {
                return null;
            }
            if (context.getNode().getTag() != Tag.STR) {
                return context.err("Expected a sound");
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            if (scalarNode.getValue().equals("default")) {
                record = new XSound.Record(XSound.BLOCK_LEVER_CLICK); //XSound.parse(KingdomsConfig.GUIS_DEFAULT_CLICK_SOUND.getString());
            } else {
                try {
                    record = XSound.parse(scalarNode.getValue());
                }
                catch (IllegalArgumentException ex) {
                    return context.err(ex.getMessage());
                }
            }
            scalarNode.setTag(SOUND);
            scalarNode.cacheConstructed(record);
            return null;
        }
    }

    private static final class Period
            implements NodeValidator {
        private Period() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            if (context.getNode().getTag() == PERIOD) {
                return null;
            }
            if (context.getNode().getTag() != Tag.STR && context.getNode().getTag() != Tag.INT) {
                return context.err("Expected a time period");
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            if (!(scalarNode.getParsed() instanceof Number)) {
                try {
                    MathCompiler.Expression compiled = MathCompiler.compile(scalarNode.getValue());
                    if (!compiled.contains(MathCompiler.ConstantExpr.class, x -> x.getType() == MathCompiler.ConstantExprType.TIME)) {
                        context.warn("The provided math equation doesn't seem to contain any time periods");
                    }
                    scalarNode.setTag(PERIOD);
                    scalarNode.cacheConstructed(compiled);
                    return null;
                }
                catch (Exception compiled) {
                    Long time = TimeUtils.parseTime(scalarNode.getValue());
                    if (time == null) {
                        return context.err("Cannot parse time period '" + scalarNode.getValue() + '\'');
                    }
                    scalarNode.cacheConstructed(time);
                }
            } else {
                int num = ((Number)scalarNode.getParsed()).intValue();
                if (num < 0) {
                    return context.err("Cannot parse time period '" + scalarNode.getValue() + '\'');
                }
                if (num > 0) {
                    context.err("Time period without any time suffix.");
                }
            }
            scalarNode.setTag(PERIOD);
            return null;
        }
    }

    private static final class Enchant
            implements NodeValidator {
        private Enchant() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            if (context.getNode().getTag() == ENCHANT) {
                return null;
            }
            if (context.getNode().getTag() != Tag.STR) {
                return context.err("Expected an enchantment");
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(scalarNode.getValue());
            if (!enchant.isPresent()) {
                return context.err("Unknown enchantment '" + scalarNode.getValue() + '\'');
            }
            scalarNode.setTag(ENCHANT);
            scalarNode.cacheConstructed((Object)enchant.get());
            return null;
        }
    }

    private static final class ItemStack
            implements NodeValidator {
        private ItemStack() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            Tag tag = context.getNode().getTag();
            if (tag == ITEM_STACK) {
                return null;
            }
            if (context.getNode().getNodeType() != NodeType.MAPPING) {
                return context.err("Expected an item, instead got " + tag);
            }
            return ITEM_STACK_VALIDATOR.validate(context);
        }
    }

    private static final class ItemMatcher
            implements NodeValidator {
        private ItemMatcher() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            Tag tag = context.getNode().getTag();
            if (tag == ITEM_MATCHER) {
                return null;
            }
            if (context.getNode().getNodeType() != NodeType.MAPPING) {
                return context.err("Expected an item matcher section, instead got " + tag);
            }
            return ITEM_MATCHER_VALIDATOR.validate(context);
        }
    }

    private static final class ItemFlag
            implements NodeValidator {
        private ItemFlag() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            Tag tag = context.getNode().getTag();
            if (tag == STRUCTURE_TYPE) {
                return null;
            }
            if (tag != Tag.STR) {
                return context.err("Expected an item flag, instead got " + tag);
            }
            ScalarNode scalarNode = (ScalarNode)context.getNode();
            com.google.common.base.Optional itemFlag = Enums.getIfPresent(org.bukkit.inventory.ItemFlag.class, (String)scalarNode.getValue());
            if (!itemFlag.isPresent() && !scalarNode.getValue().equalsIgnoreCase("ALL")) {
                return context.err("Unknown item flag '" + scalarNode.getValue() + '\'');
            }
            return null;
        }
    }

    private static final class World
            implements NodeValidator {
        private World() {
        }

        @Override
        public ValidationFailure validate(ValidationContext context) {
            return null;
        }
    }

//    private static final class StructureType
//            implements NodeValidator {
//        private StructureType() {
//        }
//
//        @Override
//        public ValidationFailure validate(ValidationContext context) {
//            Tag tag = context.getNode().getTag();
//            if (tag == STRUCTURE_TYPE) {
//                return null;
//            }
//            if (tag != Tag.STR) {
//                return context.err("Expected a structure type name, instead got " + tag);
//            }
//            ScalarNode scalarNode = (ScalarNode)context.getNode();
//            org.kingdoms.constants.land.structures.StructureType type = StructureRegistry.getType(scalarNode.getValue());
//
//            if (type == null) {
//                return context.err("Unknown structure type with name '" + scalarNode.getValue() + '\'');
//            }
//            return null;
//        }
//    }
//
//    private static final class TurretType
//            implements NodeValidator {
//        private TurretType() {
//        }
//
//        @Override
//        public ValidationFailure validate(ValidationContext context) {
//            Tag tag = context.getNode().getTag();
//            if (tag == STRUCTURE_TYPE) {
//                return null;
//            }
//            if (tag != Tag.STR) {
//                return context.err("Expected a turret type name, instead got " + tag);
//            }
//            ScalarNode scalarNode = (ScalarNode)context.getNode();
//            org.kingdoms.constants.land.turrets.TurretType type = TurretRegistry.getType(scalarNode.getValue());
//            if (type == null) {
//                return context.err("Unknown turret type with name '" + scalarNode.getValue() + '\'');
//            }
//            return null;
//        }
//    }
}


