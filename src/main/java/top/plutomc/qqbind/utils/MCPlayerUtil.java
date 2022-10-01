package top.plutomc.qqbind.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import top.plutomc.qqbind.QQBindPlugin;

import java.io.IOException;
import java.util.UUID;

public final class MCPlayerUtil {
    private MCPlayerUtil() {
    }

    public static UUID getUUID(String name) throws IOException {
        String requestUrl = "https://api.mojang.com/users/profiles/minecraft/" + name;
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();
        Call call = QQBindPlugin.OK_HTTP_CLIENT.newCall(request);
        Response response = call.execute();
        String responseBody = response.body().string();
        JsonObject jsonObject = new JsonParser().parse(responseBody).getAsJsonObject();
        return UUIDUtil.trimmedToFull(jsonObject.get("id").getAsString());
    }
}
