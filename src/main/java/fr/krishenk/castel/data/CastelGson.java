package fr.krishenk.castel.data;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;

public class CastelGson {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().enableComplexMapKeySerialization().excludeFieldsWithModifiers(128, 8).create();
    private static final GsonContextImpl CONTEXT = new GsonContextImpl();

    private static JsonReader newJsonReader(Reader reader) {
        return new JsonReader(reader);
    }

    public static JsonElement fromString(String json) {
        try {
            JsonReader jsonReader = new JsonReader(new StringReader(json));
            JsonElement element = parseReader(jsonReader);
            if (!element.isJsonNull() && jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonSyntaxException("Did not consume the entire document.");
            } else {
                return element;
            }
        } catch (NumberFormatException | MalformedJsonException e) {
            throw new JsonSyntaxException(e);
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }

    private static JsonElement parseReader(JsonReader reader) throws JsonIOException, JsonSyntaxException {
        try {
            return Streams.parse(reader);
        } catch (OutOfMemoryError | StackOverflowError ex) {
            throw new JsonParseException("Failed parsing JSON source : " + reader + " to Json", ex);
        }
    }

    public static String toString(@NotNull JsonElement element) {
        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setLenient(false);
            jsonWriter.setSerializeNulls(false);
            jsonWriter.setHtmlSafe(false);
            Streams.write(element, jsonWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonWriter newJsonWritter(Writer writer) throws IOException {
        JsonWriter jsonWriter = new JsonWriter(writer);
        jsonWriter.setIndent("  ");
        jsonWriter.setLenient(false);
        jsonWriter.setHtmlSafe(false);
        jsonWriter.setSerializeNulls(false);
        return jsonWriter;
    }

    public static String toJson(Object src) {
        return GSON.toJson(src);
    }

    public static void toJson(JsonObject object, Appendable writter) throws IOException {
        JsonWriter jsonWriter = CastelGson.newJsonWritter(Streams.writerForAppendable(writter));
        Streams.write(object, jsonWriter);
    }

    private static void assertFullConsumption(Object obj, JsonReader reader) {
        try {
            if (obj != null && reader.peek() != JsonToken.END_DOCUMENT)
                throw new JsonIOException("JSON document was not fully consumed.");
        } catch (MalformedJsonException e) {
            throw new JsonSyntaxException(e);
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }

    public static JsonObject parse(Reader json) throws JsonIOException, JsonSyntaxException {
        JsonObject object;
        JsonReader reader = CastelGson.newJsonReader(json);
        reader.setLenient(false);
        try {
            reader.peek();
            object = (JsonObject) Streams.parse(reader);
        } catch (IllegalStateException e) {
            throw new JsonSyntaxException(e);
        } catch (IOException e) {
            throw new JsonSyntaxException(e);
        } catch (AssertionError e) {
            throw new AssertionError("AssertionError : " + e.getMessage(), e);
        }
        CastelGson.assertFullConsumption(object, reader);
        return object;
    }

    private static JsonElement parse(JsonReader reader) throws JsonParseException {
        boolean isEmpty = true;
        try {
            reader.peek();
            isEmpty = false;
            return TypeAdapters.JSON_ELEMENT.read(reader);
        } catch (EOFException e) {
            if (isEmpty) return JsonNull.INSTANCE;
            throw new JsonSyntaxException(e);
        } catch (MalformedJsonException e) {
            throw new JsonSyntaxException(e);
        } catch (IOException e) {
            throw new JsonIOException(e);
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
        }
    }

    private static class GsonContextImpl implements JsonSerializationContext, JsonDeserializationContext {
        private GsonContextImpl() {}

        @Override
        public JsonElement serialize(Object o) {
            return GSON.toJsonTree(o);
        }

        @Override
        public JsonElement serialize(Object o, Type type) {
            return GSON.toJsonTree(o, type);
        }

        @Override
        public <T> T deserialize(JsonElement jsonElement, Type type) throws JsonParseException {
            return GSON.fromJson(jsonElement, type);
        }
    }
}
