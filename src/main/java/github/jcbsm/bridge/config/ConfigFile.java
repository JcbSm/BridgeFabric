package github.jcbsm.bridge.config;

import blue.endless.jankson.*;
import blue.endless.jankson.api.SyntaxError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigFile<T> {

    private static final Jankson JANKSON = Jankson.builder()
            .build();

    protected final Path path;
    protected T config;

    protected ConfigFile(Path path, Class<T> clazz) {

        // Set path
        this.path = path;

        // If file exists, load
        if (Files.exists(path)) {
            this.config = load(clazz);

        // Otherwise, try and create a new one.
        } else {
            try {
                this.config = clazz.getDeclaredConstructor().newInstance();
                save();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private T load(Class<T> clazz) {
        try {
            String raw = Files.readString(path);
            JsonObject obj = JANKSON.load(raw);
            return JANKSON.fromJson(obj, clazz);
        } catch (IOException | SyntaxError e) {
            throw new RuntimeException("Failed to load config: " + path, e);
        }
    }

    public void save() {
        try {
            JsonElement json = JANKSON.toJson(config);
            Files.createDirectories(path.getParent());
            Files.writeString(path, json.toJson(true, true));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config: " + path, e);
        }
    }

    public T get() {
        return config;
    }
}
