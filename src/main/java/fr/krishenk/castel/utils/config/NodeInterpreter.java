package fr.krishenk.castel.utils.config;

import fr.krishenk.castel.libs.snakeyaml.api.LoadSettings;
import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.constructor.BaseConstructor;
import fr.krishenk.castel.libs.snakeyaml.constructor.StandardConstructor;
import fr.krishenk.castel.libs.snakeyaml.nodes.*;
import fr.krishenk.castel.libs.snakeyaml.resolver.StandardScalarResolver;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import fr.krishenk.castel.utils.compilers.PlaceholderContextProvider;
import fr.krishenk.castel.utils.string.StringUtils;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface NodeInterpreter<T> {

    Companion Companion = new Companion();
    
    @NotNull BaseConstructor DEFAULT_CTOR = new StandardConstructor(new LoadSettings());
    
    @NotNull StandardScalarResolver DEFAULT_RESOLVER = new StandardScalarResolver();
    
    @NotNull NodeInterpreter<String> STRING = (it) -> {
        if (it == null) {
            return null;
        }
        if (it.getNodeType() != NodeType.SCALAR) {
            return null;
        }
        return it.getParsed() == null ? null : ((ScalarNode)it).getValue();
    };
    
    @NotNull NodeInterpreter<List<Integer>> INT_LIST = (it) -> {
        if (it == null || it.getNodeType() != NodeType.SEQUENCE) {
            return new ArrayList();
        }
        SequenceNode seq = (SequenceNode)it;
        List list2 = new ArrayList(seq.getValue().size());
        for (Node item : seq.getValue()) {
            if (!(item.getParsed() instanceof Number)) continue;
            Object object = item.getParsed();
            list2.add(((Number)object).intValue());
        }
        return list2;
    };
    
    @NotNull NodeInterpreter<List<String>> STRING_LIST = (it) -> {
        if (it == null) {
            return new ArrayList<>();
        }
        if (it.getNodeType() != NodeType.SEQUENCE) {
            if (it.getNodeType() == NodeType.SCALAR) {
                ScalarNode scalarNode = (ScalarNode)it;
                return StringUtils.split(scalarNode.getValue(), '\n', true);
            }
            return new ArrayList<>();
        }
        SequenceNode sequenceNode = (SequenceNode)it;
        List<String> strings = new ArrayList<>(sequenceNode.getValue().size());
        for (Node element : sequenceNode.getValue()) {
            String parsed = STRING.parse(element);
            if (parsed == null) continue;
            strings.add(parsed);
        }
        return strings;
    };
    
    @NotNull NodeInterpreter<Float> FLOAT = (it) -> {
        float f;
        if (it == null) {
            return 0.0f;
        }
        if (!(it.getParsed() instanceof Number)) {
            f = 0.0f;
        } else {
            Object object = it.getParsed();
            f = ((Number)object).floatValue();
        }
        return f;
    };
    
    @NotNull NodeInterpreter<Long> LONG = (it) -> {
        long l;
        if (it == null) {
            return 0L;
        }
        if (!(it.getParsed() instanceof Number)) {
            l = 0L;
        } else {
            Object object = it.getParsed();
            l = ((Number)object).longValue();
        }
        return l;
    };
    
    @NotNull NodeInterpreter<Double> DOUBLE = (it) -> {
        double d;
        if (it == null) {
            return 0.0;
        }
        if (!(it.getParsed() instanceof Number)) {
            d = 0.0;
        } else {
            Object object = it.getParsed();
            d = ((Number)object).doubleValue();
        }
        return d;
    };
    
    @NotNull NodeInterpreter<Boolean> BOOLEAN = (it) -> {
        boolean bl;
        if (it == null) {
            return false;
        }
        if (!(it.getParsed() instanceof Boolean)) {
            bl = false;
        } else {
            Object object = it.getParsed();
            bl = (Boolean)object;
        }
        return bl;
    };
    
    @NotNull NodeInterpreter<Integer> INT = (it) -> {
        int n;
        if (it == null) {
            return 0;
        }
        if (!(it.getParsed() instanceof Number)) {
            n = 0;
        } else {
            Object object = it.getParsed();
            n = ((Number)object).intValue();
        }
        return n;
    };
    
    @NotNull NodeInterpreter<ConditionalCompiler.LogicalOperand> CONDITION = (it) -> {
        if (it == null) {
            return null;
        }
        if (it.getNodeType() != NodeType.SCALAR) {
            return null;
        }
        if (!(it.getParsed() instanceof ConditionalCompiler.LogicalOperand)) {
            ScalarNode scalarNode = (ScalarNode)it;
            it.cacheConstructed(ConditionalCompiler.compile(scalarNode.getValue()).evaluate());
        }
        Object object = it.getParsed();
        return (ConditionalCompiler.LogicalOperand)object;
    };
    
    @NotNull NodeInterpreter<MathCompiler.Expression> MATH = (it) -> {
        if (it == null) {
            return MathCompiler.DEFAULT_VALUE;
        }
        if (it.getNodeType() != NodeType.SCALAR) {
            return MathCompiler.DEFAULT_VALUE;
        }
        if (!(it.getParsed() instanceof MathCompiler.Expression)) {
            ScalarNode scalarNode = (ScalarNode)it;
            it.cacheConstructed(MathCompiler.Companion.compile(scalarNode.getValue()));
        }
        Object object = it.getParsed();
        return (MathCompiler.Expression)object;
    };
    
//    @NotNull NodeInterpreter<LanguageEntry> MESSAGE_ENTRY = (it) -> {
//        if (it == null) {
//            return KingdomsLang.UNKNOWN_MESSAGE_ENTRY.getLanguageEntry();
//        }
//        if (it.getNodeType() != NodeType.SCALAR) {
//            return KingdomsLang.UNKNOWN_MESSAGE_ENTRY.getLanguageEntry();
//        }
//        if (!(it.getParsed() instanceof LanguageEntry)) {
//            ScalarNode scalarNode = (ScalarNode)it;
//            ((ScalarNode)it).cacheConstructed(LanguageEntry.fromConfig(scalarNode.getValue()));
//        }
//        Object object = it.getParsed();
//        return (LanguageEntry)object;
//    };

    T parse(@Nullable Node var1);

    @Nullable
    static Long getTime(@Nullable Node node, @Nullable PlaceholderContextProvider ctx) {
        return Companion.getTime(node, ctx);
    }

    @NotNull
    static Node nodeOfObject(@Nullable Object obj) {
        return Companion.nodeOfObject(obj);
    }
    
    final class Companion {
        private Companion() {
        }
        
        @Nullable
        public Long getTime(@Nullable Node node, @Nullable PlaceholderContextProvider ctx) {
            if (node == null) {
                return null;
            }
            Object cache = null;
            if (node.getTag() != CustomConfigValidators.PERIOD) {
                Object object;
                ScalarNode scalarNode = (ScalarNode)node;
                try {
                    object = MathCompiler.Companion.compile(scalarNode.getValue());
                }
                catch (Throwable ignored) {
                    object = TimeUtils.parseTime(scalarNode.getValue());
                }
                cache = object;
                if (cache == null) {
                    return null;
                }
                node.setTag(CustomConfigValidators.PERIOD);
                node.cacheConstructed(cache);
            } else {
                cache = node.getParsed();
            }
            return cache instanceof Number ? Long.valueOf(((Number)cache).longValue()) : Long.valueOf((long) MathUtils.eval((MathCompiler.Expression)cache, ctx));
        }

        @NotNull
        public Node nodeOfObject(@Nullable Object obj) {
            if (obj == null) {
                return new ScalarNode(Tag.NULL, "~", ScalarStyle.PLAIN);
            }
            if (obj instanceof Node) {
                return (Node)obj;
            }
            if (obj instanceof Collection) {
                List nodes = new ArrayList(((Collection)obj).size());
                for (Object item : (Collection)obj) {
                    nodes.add(this.nodeOfObject(item));
                }
                return new SequenceNode(Tag.SEQ, nodes, FlowStyle.AUTO);
            }
            if (obj instanceof Map) {
                throw new UnsupportedOperationException("Mapping from objects");
            }
            ScalarNode node = new ScalarNode(DEFAULT_RESOLVER.resolve(obj.toString()), obj.toString(), ScalarStyle.AUTO);
            node.cacheConstructed(DEFAULT_CTOR.constructObject(node));
            return node;
        }
    }
}
