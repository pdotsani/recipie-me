package org.vaadin.example;

import com.google.gson.reflect.TypeToken;
import com.nimbusds.jose.shaded.gson.Gson;
import dev.langchain4j.internal.Json;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OpenAIImageGen {
    private final String OPENAIKEY = (EnvUtils.get("OPEN_AI_KEY") != null) ? EnvUtils.get("OPEN_AI_KEY") : "demo";
    private String url = "https://api.openai.com/v1/images/generations";

    public String generate(String prompt) {
        Gson gson = new Gson();
        HashMap<String, Object> body = new HashMap<>();
        body.put("model", "dall-e-3");
        body.put("prompt", prompt);
        body.put("n", 1);
        body.put("size", "1024x1024");

        String jsonString = Json.toJson(body);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(15, TimeUnit.MINUTES)
                .readTimeout(15, TimeUnit.MINUTES)
                .writeTimeout(15, TimeUnit.MINUTES)
                .build();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody bodyJson = RequestBody.create(JSON, jsonString);
        Request request = new Request.Builder()
                .url(url)
                .post(bodyJson)
                .header("Authorization", "Bearer " + OPENAIKEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Response Code: " + response.code());
            String responseJsonString = response.body().string();

            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> responseMap = gson.fromJson(responseJsonString, type);

            ArrayList<Map<String,String>> data = (ArrayList<Map<String,String>>) responseMap.get("data");
            Object returnUrl = data.get(0).get("url");

            return returnUrl.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
