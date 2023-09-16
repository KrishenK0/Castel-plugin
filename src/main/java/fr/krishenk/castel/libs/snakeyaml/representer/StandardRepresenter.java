
package fr.krishenk.castel.libs.snakeyaml.representer;

import fr.krishenk.castel.libs.snakeyaml.api.DumpSettings;
import fr.krishenk.castel.libs.snakeyaml.api.RepresentToNode;
import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.exceptions.YamlEngineException;
import fr.krishenk.castel.libs.snakeyaml.nodes.Node;
import fr.krishenk.castel.libs.snakeyaml.nodes.Tag;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

public class StandardRepresenter
extends BaseRepresenter {
    protected final Map<Class<?>, Tag> classTags;
    protected final DumpSettings settings;
    private static final Pattern MULTILINE_PATTERN = Pattern.compile("[\n\u0085\u2028\u2029]");

    public StandardRepresenter(DumpSettings settings) {
        this.nullRepresenter = new RepresentNull();
        this.representers.put(String.class, new RepresentString());
        this.representers.put(Boolean.class, new RepresentBoolean());
        this.representers.put(Character.class, new RepresentString());
        RepresentPrimitiveArray primitiveArray = new RepresentPrimitiveArray();
        this.representers.put(int[].class, primitiveArray);
        this.representers.put(long[].class, primitiveArray);
        this.representers.put(float[].class, primitiveArray);
        this.representers.put(double[].class, primitiveArray);
        this.representers.put(char[].class, primitiveArray);
        this.parentClassRepresenters.put(Number.class, new RepresentNumber());
        this.parentClassRepresenters.put(List.class, new RepresentList());
        this.parentClassRepresenters.put(Map.class, new RepresentMap());
        this.parentClassRepresenters.put(Iterator.class, new RepresentIterator());
        this.parentClassRepresenters.put(Object[].class, new RepresentArray());
        this.parentClassRepresenters.put(Enum.class, new RepresentEnum());
        this.classTags = new HashMap();
        this.settings = settings;
    }

    protected Tag getTag(Class<?> clazz, Tag defaultTag) {
        return this.classTags.getOrDefault(clazz, defaultTag);
    }

    public Tag addClassTag(Class<?> clazz, Tag tag) {
        if (tag == null) {
            throw new NullPointerException("Tag must be provided.");
        }
        return this.classTags.put(clazz, tag);
    }

    protected final class RepresentNull
    implements RepresentToNode {
        protected RepresentNull() {
        }

        @Override
        public Node representData(Object data) {
            return StandardRepresenter.this.representScalar(Tag.NULL, "null");
        }
    }

    protected final class RepresentString
    implements RepresentToNode {
        protected RepresentString() {
        }

        @Override
        public Node representData(Object data) {
            ScalarStyle style = ScalarStyle.PLAIN;
            String value = data.toString();
            if (MULTILINE_PATTERN.matcher(value).find()) {
                style = ScalarStyle.LITERAL;
            }
            return StandardRepresenter.this.representScalar(Tag.STR, value, style);
        }
    }

    protected final class RepresentBoolean
    implements RepresentToNode {
        protected RepresentBoolean() {
        }

        @Override
        public Node representData(Object data) {
            String value = Boolean.TRUE.equals(data) ? "true" : "false";
            return StandardRepresenter.this.representScalar(Tag.BOOL, value);
        }
    }

    protected final class RepresentPrimitiveArray
    implements RepresentToNode {
        protected RepresentPrimitiveArray() {
        }

        @Override
        public Node representData(Object data) {
            Class<?> type = data.getClass().getComponentType();
            if (Integer.TYPE == type) {
                return StandardRepresenter.this.representSequence(Tag.SEQ, this.asIntList(data), FlowStyle.AUTO);
            }
            if (Long.TYPE == type) {
                return StandardRepresenter.this.representSequence(Tag.SEQ, this.asLongList(data), FlowStyle.AUTO);
            }
            if (Float.TYPE == type) {
                return StandardRepresenter.this.representSequence(Tag.SEQ, this.asFloatList(data), FlowStyle.AUTO);
            }
            if (Double.TYPE == type) {
                return StandardRepresenter.this.representSequence(Tag.SEQ, this.asDoubleList(data), FlowStyle.AUTO);
            }
            throw new YamlEngineException("Unexpected primitive '" + type.getCanonicalName() + '\'');
        }

        private List<Integer> asIntList(Object in) {
            int[] array = (int[])in;
            ArrayList<Integer> list2 = new ArrayList<Integer>(array.length);
            for (int j : array) {
                list2.add(j);
            }
            return list2;
        }

        private List<Long> asLongList(Object in) {
            long[] array = (long[])in;
            ArrayList<Long> list2 = new ArrayList<Long>(array.length);
            for (long l : array) {
                list2.add(l);
            }
            return list2;
        }

        private List<Float> asFloatList(Object in) {
            float[] array = (float[])in;
            ArrayList<Float> list2 = new ArrayList<Float>(array.length);
            for (float v : array) {
                list2.add(Float.valueOf(v));
            }
            return list2;
        }

        private List<Double> asDoubleList(Object in) {
            double[] array = (double[])in;
            ArrayList<Double> list2 = new ArrayList<Double>(array.length);
            for (double v : array) {
                list2.add(v);
            }
            return list2;
        }
    }

    protected final class RepresentNumber
    implements RepresentToNode {
        protected RepresentNumber() {
        }

        @Override
        public Node representData(Object data) {
            String value;
            Tag tag;
            if (data instanceof Byte || data instanceof Short || data instanceof Integer || data instanceof Long || data instanceof BigInteger) {
                tag = Tag.INT;
                value = data.toString();
            } else {
                Number number = (Number)data;
                tag = Tag.FLOAT;
                value = number.toString();
            }
            return StandardRepresenter.this.representScalar(StandardRepresenter.this.getTag(data.getClass(), tag), value);
        }
    }

    protected class RepresentList
    implements RepresentToNode {
        protected RepresentList() {
        }

        @Override
        public Node representData(Object data) {
            return StandardRepresenter.this.representSequence(StandardRepresenter.this.getTag(data.getClass(), Tag.SEQ), (List)data, FlowStyle.AUTO);
        }
    }

    protected final class RepresentMap
    implements RepresentToNode {
        protected RepresentMap() {
        }

        @Override
        public Node representData(Object data) {
            return StandardRepresenter.this.representMapping(StandardRepresenter.this.getTag(data.getClass(), Tag.MAP), (Map)data, FlowStyle.AUTO);
        }
    }

    protected class RepresentIterator
    implements RepresentToNode {
        protected RepresentIterator() {
        }

        @Override
        public Node representData(Object data) {
            Iterator iter = (Iterator)data;
            return StandardRepresenter.this.representSequence(StandardRepresenter.this.getTag(data.getClass(), Tag.SEQ), new IteratorWrapper(iter), FlowStyle.AUTO);
        }
    }

    protected final class RepresentArray
    implements RepresentToNode {
        protected RepresentArray() {
        }

        @Override
        public Node representData(Object data) {
            Object[] array = (Object[])data;
            List<Object> list2 = Arrays.asList(array);
            return StandardRepresenter.this.representSequence(Tag.SEQ, list2, FlowStyle.AUTO);
        }
    }

    protected final class RepresentEnum
    implements RepresentToNode {
        protected RepresentEnum() {
        }

        @Override
        public Node representData(Object data) {
            Tag tag = new Tag(data.getClass());
            return StandardRepresenter.this.representScalar(StandardRepresenter.this.getTag(data.getClass(), tag), ((Enum)data).name());
        }
    }

    private static final class IteratorWrapper
    implements Iterable<Object> {
        private final Iterator<Object> iter;

        public IteratorWrapper(Iterator<Object> iter) {
            this.iter = iter;
        }

        @Override
        public Iterator<Object> iterator() {
            return this.iter;
        }
    }
}

