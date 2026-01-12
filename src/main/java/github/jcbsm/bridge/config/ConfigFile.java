package github.jcbsm.bridge.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class ConfigFile {


    private final Logger logger = LoggerFactory.getLogger(BridgeConfig.class.getName());

    public final String name;
    public final Path path;

    private final Map<String, Object> cache = new HashMap<>();

    protected ConfigFile(String name) {
        this.name = name.toLowerCase();
        path = BridgeConfig.getConfigDir().resolve("cfg_" + this.name + ".json");
    }

    public final boolean load() throws IOException {

        // Check if file exist already
        if (Files.exists(path)) {
            read();
        }

        return validate();
    }

    public final void save() throws IOException {

        validate();
        write();

    }

    @SuppressWarnings("unchecked")
    private void flatten(String prefix, Map<String, Object> map) {

        for (Map.Entry<String, Object> entry : map.entrySet()) {

            String key = prefix.isEmpty()
                    ? entry.getKey()
                    : prefix + "." + entry.getKey();

            Object value = entry.getValue();

            if (value instanceof Map<?, ?> nestedMap) {
                flatten(key, (Map<String, Object>) nestedMap);
            } else {
                cache.put(key, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deflatten() {

        Map<String, Object> root = new HashMap<>();

        // Iterate through cache
        for (Map.Entry<String, Object> entry : cache.entrySet()) {

            // Get parts
            String[] parts = entry.getKey().split("\\.");
            Map<String, Object> current = root;

            for (int i = 0; i < parts.length; i++) {

                String part = parts[i];

                // Go deeper
                if (i == parts.length - 1) {
                    current.put(part, entry.getValue());
                } else {
                    current = (Map<String, Object>) current.computeIfAbsent(
                            part, k -> new HashMap<>()
                    );
                }
            }
        }

        return root;
    }

    @SuppressWarnings("unchecked")
    protected void read() {

        // Clear the cache
        cache.clear();

        // Read the contents of the config file
        try (BufferedReader reader = Files.newBufferedReader(path)) {

            Map<String, Object> json = new Gson().fromJson(reader, Map.class);

            if (json != null) {
                flatten("", json);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    protected void write() {

        try {

            // Ensure file exists
            Files.createDirectories(path.getParent());

            // Write json to file
            try (var writer = Files.newBufferedWriter(path)) {

                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();

                gson.toJson(deflatten(), writer);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to write config file: " + path, e);
        }
    }

    private Object castValue(Object value, Class<?> type) {

        if (type == int.class || type == Integer.class) {
            return ((Number) value).intValue();
        }
        if (type == long.class || type == Long.class) {
            return ((Number) value).longValue();
        }
        if (type == double.class || type == Double.class) {
            return ((Number) value).doubleValue();
        }
        if (type == float.class || type == Float.class) {
            return ((Number) value).floatValue();
        }
        if (type == boolean.class || type == Boolean.class) {
            return (Boolean) value;
        }
        if (type == String.class) {
            return value.toString();
        }

        throw new IllegalArgumentException("Unsupported config type: " + type);
    }

    private boolean validateClass(String prefix, Class<?> cls) {

        boolean valid = true;

        // Validate static fields
        for (var field : cls.getDeclaredFields()) {

            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            String key = prefix.isEmpty()
                    ? field.getName()
                    : prefix + "." + field.getName();

            Object value = cache.get(key);

            try {
                Object defaultValue = field.get(null);

                if (value == null) {
                    cache.put(key, defaultValue);
                    continue;
                }

                Object casted = castValue(value, field.getType());
                cache.put(key, casted);

            } catch (Exception e) {
                logger.warn("Invalid value for '{}', resetting to default", key);
                try {
                    cache.put(key, field.get(null));
                } catch (IllegalAccessException ignored) {}
                valid = false;
            }
        }

        // Validate static inner classes
        for (Class<?> inner : cls.getDeclaredClasses()) {

            if (!java.lang.reflect.Modifier.isStatic(inner.getModifiers())) {
                continue;
            }

            String nextPrefix = prefix.isEmpty()
                    ? inner.getSimpleName().toLowerCase()
                    : prefix + "." + inner.getSimpleName().toLowerCase();

            valid &= validateClass(nextPrefix, inner);
        }

        return valid;
    }

    protected boolean validate() {
        return validateClass("", getClass());
    }

    public Object get(String key) {
        return cache.get(key);
    }

    public void set(String key, Object val) {
        cache.put(key, val);

        validate();
    }

}

