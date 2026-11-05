package com.evidence_engine.backend.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tiny JSON reader/writer, so we need no JSON library. Objects -> Map, arrays -> List. */
public final class Json {

    private Json() {
    }

    // ---------- writing ----------

    public static String write(Object value) {
        StringBuilder out = new StringBuilder();
        writeValue(out, value);
        return out.toString();
    }

    private static void writeValue(StringBuilder out, Object value) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String s) {
            writeString(out, s);
        } else if (value instanceof Number || value instanceof Boolean) {
            out.append(value);
        } else if (value instanceof Map<?, ?> map) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                writeString(out, String.valueOf(entry.getKey()));
                out.append(':');
                writeValue(out, entry.getValue());
            }
            out.append('}');
        } else if (value instanceof Collection<?> list) {
            out.append('[');
            boolean first = true;
            for (Object item : list) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                writeValue(out, item);
            }
            out.append(']');
        } else {
            writeString(out, value.toString());
        }
    }

    private static void writeString(StringBuilder out, String s) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }

    // ---------- reading ----------

    public static Object parse(String text) {
        Reader reader = new Reader(text);
        reader.skipSpace();
        Object value = reader.readValue();
        reader.skipSpace();
        if (reader.pos != text.length()) {
            throw new IllegalArgumentException("Unexpected text after the JSON at position " + reader.pos);
        }
        return value;
    }

    /** Value as a map, or empty map. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : Map.of();
    }

    /** Value as a list, or empty list. */
    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object value) {
        return value instanceof List<?> ? (List<Object>) value : List.of();
    }

    /** Value as a string, or "". */
    public static String asString(Object value) {
        return value == null || value instanceof Map || value instanceof List ? "" : value.toString();
    }

    /** First array item as a string (Primo wraps fields in arrays). */
    public static String first(Object value) {
        List<Object> list = asList(value);
        return list.isEmpty() ? asString(value) : asString(list.get(0));
    }

    private static final class Reader {
        private final String text;
        private int pos;

        Reader(String text) {
            this.text = text;
        }

        void skipSpace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        Object readValue() {
            if (pos >= text.length()) {
                throw error("Unexpected end of JSON");
            }
            char c = text.charAt(pos);
            return switch (c) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't' -> readWord("true", Boolean.TRUE);
                case 'f' -> readWord("false", Boolean.FALSE);
                case 'n' -> readWord("null", null);
                default -> readNumber();
            };
        }

        private Map<String, Object> readObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // {
            skipSpace();
            if (peek() == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipSpace();
                if (peek() != '"') {
                    throw error("Expected a key");
                }
                String key = readString();
                skipSpace();
                expect(':');
                skipSpace();
                map.put(key, readValue());
                skipSpace();
                char c = next();
                if (c == '}') {
                    return map;
                }
                if (c != ',') {
                    throw error("Expected , or }");
                }
            }
        }

        private List<Object> readArray() {
            List<Object> list = new ArrayList<>();
            pos++; // [
            skipSpace();
            if (peek() == ']') {
                pos++;
                return list;
            }
            while (true) {
                skipSpace();
                list.add(readValue());
                skipSpace();
                char c = next();
                if (c == ']') {
                    return list;
                }
                if (c != ',') {
                    throw error("Expected , or ]");
                }
            }
        }

        private String readString() {
            pos++; // opening quote
            StringBuilder out = new StringBuilder();
            while (true) {
                char c = next();
                if (c == '"') {
                    return out.toString();
                }
                if (c != '\\') {
                    out.append(c);
                    continue;
                }
                char escaped = next();
                switch (escaped) {
                    case '"', '\\', '/' -> out.append(escaped);
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case 'u' -> {
                        if (pos + 4 > text.length()) {
                            throw error("Bad \\u escape");
                        }
                        out.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                        pos += 4;
                    }
                    default -> throw error("Bad escape \\" + escaped);
                }
            }
        }

        private Object readNumber() {
            int start = pos;
            while (pos < text.length() && "+-0123456789.eE".indexOf(text.charAt(pos)) >= 0) {
                pos++;
            }
            String number = text.substring(start, pos);
            if (number.isEmpty()) {
                throw error("Unexpected character '" + text.charAt(start) + "'");
            }
            try {
                if (number.contains(".") || number.contains("e") || number.contains("E")) {
                    return Double.parseDouble(number);
                }
                return Long.parseLong(number);
            } catch (NumberFormatException e) {
                throw error("Bad number " + number);
            }
        }

        private Object readWord(String word, Object value) {
            if (!text.startsWith(word, pos)) {
                throw error("Unexpected text");
            }
            pos += word.length();
            return value;
        }

        private char peek() {
            if (pos >= text.length()) {
                throw error("Unexpected end of JSON");
            }
            return text.charAt(pos);
        }

        private char next() {
            char c = peek();
            pos++;
            return c;
        }

        private void expect(char c) {
            if (next() != c) {
                throw error("Expected " + c);
            }
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " (at position " + pos + ")");
        }
    }
}
