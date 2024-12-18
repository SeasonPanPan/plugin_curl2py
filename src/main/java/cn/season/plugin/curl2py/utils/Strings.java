package cn.season.plugin.curl2py.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * the Strings
 *
 * @author PanLongfei
 * @date 2024-11-24
 */
public class Strings {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    private Strings() {
    }

    public static int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen = length(cs);
        if (strLen != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static String convertToLF(String s) {
        return s.replaceAll("\r\n?", "\n");
    }

    public static String toMyJson(Map<String, String> kv) {
        if (kv == null || kv.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{\n");
        kv.forEach((k, v) -> {
            //    'k': 'v',\n
            v = v.replace("'", "\\'");
            sb.append(String.format("    '%s': '%s',\n", k, v));
        });
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append('}');
        return sb.toString();
    }

    // public static String toJson(Object src) {
    //     return src == null ? "null" : toJson(src, (Type)src.getClass());
    // }
    //
    // public static String toJson(Object obj, Type typeOfSrc) {
    //     try {
    //         StringWriter writer = new StringWriter();
    //         GSON.toJson(obj, typeOfSrc, newJsonWriter(writer));
    //         return writer.toString();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     return obj.toString();
    // }

    /**
     * toPrettyJson via Gson
     *
     * @param jsonStr
     * @return
     */
    public static String toPrettyJson(String jsonStr) {
        try {
            StringWriter writer = new StringWriter();
            GSON.toJson(JsonParser.parseString(jsonStr), newJsonWriter(writer));
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return jsonStr;
        }
    }

    public static JsonWriter newJsonWriter(Writer writer) {
        JsonWriter jsonWriter = new JsonWriter(writer);
        jsonWriter.setIndent("    ");
        jsonWriter.setSerializeNulls(true);
        return jsonWriter;
    }

    /**
     * formatJson does not dependency Gson
     *
     * @param json
     * @return
     */
    public static String formatJson(String json) {
        StringBuilder formattedJson = new StringBuilder();
        int indentLevel = 0;
        int endBooleanIdx = -1;
        boolean inQuotes = false;
        int length = json.length();

        for (int i = 0; i < length; i++) {
            char c = json.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
                formattedJson.append(c);
            } else if (!inQuotes) {
                switch (c) {
                    case '{':
                    case '[':
                        formattedJson.append(c).append("\n");
                        indentLevel++;
                        addIndentation(formattedJson, indentLevel);
                        break;
                    case '}':
                    case ']':
                        formattedJson.append("\n");
                        indentLevel--;
                        addIndentation(formattedJson, indentLevel);
                        formattedJson.append(c);
                        break;
                    case ',':
                        formattedJson.append(c).append("\n");
                        addIndentation(formattedJson, indentLevel);
                        break;
                    case ':':
                        formattedJson.append(c).append(" ");
                        break;
                    default:
                        if (Character.isWhitespace(c)) {
                            continue;
                        }
                        // true -> 'true'; false -> 'false'
                        if (c == 't' && i + 3 < length) {
                            String fs = json.substring(i, i + 4);
                            if ("true".equalsIgnoreCase(fs)) {
                                endBooleanIdx = i + 3;
                                formattedJson.append('"');
                            }
                        } else if (c == 'f' && i + 4 < length) {
                            String fs = json.substring(i, i + 5);
                            if ("false".equalsIgnoreCase(fs)) {
                                endBooleanIdx = i + 4;
                                formattedJson.append('"');
                            }
                        }
                        formattedJson.append(c);
                        if (i == endBooleanIdx) {
                            formattedJson.append('"');
                        }
                        break;
                }
            } else {
                formattedJson.append(c);
            }
        }

        return formattedJson.toString();
    }

    private static void addIndentation(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) {
            // 4 spaces per indent level
            sb.append("    ");
        }
    }
}
