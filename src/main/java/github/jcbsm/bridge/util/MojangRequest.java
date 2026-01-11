package github.jcbsm.bridge.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class MojangRequest {

    private static final Logger logger = LoggerFactory.getLogger(MojangRequest.class.getSimpleName());


    private static Map<String, Object> execute(String url) throws IOException {

        OkHttpClient httpClient = new OkHttpClient();
        Gson gson = new Gson();

        Request request = new Request.Builder().url(url).build();
        Call call = httpClient.newCall(request);
        ResponseBody response = call.execute().body();

        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();  // TODO: beta feature apparently - find stable version
        return gson.fromJson(response.string(), mapType);

    }

    /**
     * Get the UUID to a corresponding username.
     * @param username of the minecraft account you want to query the uuid for.
     * @return UUID of the queried minecraft account.
     * @throws IOException Unexpected results - i.e. no internet.
     */
    @Nullable
    public static String usernameToUUID(String username) throws IOException {
        Map<String, Object> response = execute(String.format("https://api.mojang.com/users/profiles/minecraft/%s", username));

        if (response.containsKey("errorMessage")) { return null; }  // If error (no such player exists) return null
        return (String) response.get("id");
    }

    /**
     * Gets the username of a UUID
     * @param uuid UUID of the Minecraft user
     * @return Username of the player
     * @throws IOException
     */
    @Nullable
    public static String uuidToUsername(String uuid) throws IOException {
        Map<String, Object> response = execute(String.format("https://api.mojang.com/user/profile/%s", uuid));

        if (response.containsKey("errorMessage")) { return null; }
        return (String) response.get("name");
    }
}
