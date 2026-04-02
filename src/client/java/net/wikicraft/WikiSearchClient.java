package net.wikicraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WikiSearchClient {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    public enum WikiLanguage {
        EN("English",    "https://minecraft.wiki"),
        DE("Deutsch",    "https://de.minecraft.wiki"),
        FR("Français",   "https://fr.minecraft.wiki"),
        ES("Español",    "https://es.minecraft.wiki"),
        PT("Português",  "https://pt.minecraft.wiki"),
        RU("Русский",    "https://ru.minecraft.wiki"),
        ZH("中文",        "https://zh.minecraft.wiki"),
        JA("日本語",      "https://ja.minecraft.wiki"),
        PL("Polski",     "https://pl.minecraft.wiki"),
        NL("Nederlands", "https://nl.minecraft.wiki"),
        IT("Italiano",   "https://it.minecraft.wiki"),
        UK("Українська", "https://uk.minecraft.wiki"),
        KO("한국어",      "https://ko.minecraft.wiki");

        public final String displayName;
        public final String baseUrl;

        WikiLanguage(String displayName, String baseUrl) {
            this.displayName = displayName;
            this.baseUrl     = baseUrl;
        }
    }

    public static void search(String query,
                              WikiLanguage language,
                              Consumer<List<SpotlightScreen.WikiResult>> callback) {
        if (query == null || query.isBlank()) {
            callback.accept(new ArrayList<>());
            return;
        }

        String encodedQuery = query.replace(" ", "+").replace("&", "%26");
        String url = language.baseUrl
                + "/api.php?action=opensearch&search="
                + encodedQuery
                + "&limit=8&format=json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "WikicraftSpotlight/1.0")
                .GET()
                .build();

        HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    List<SpotlightScreen.WikiResult> results = parseResponse(response.body());
                    Minecraft.getInstance().execute(() -> callback.accept(results));
                })
                .exceptionally(e -> {
                    Minecraft.getInstance().execute(() -> callback.accept(new ArrayList<>()));
                    return null;
                });
    }

    private static List<SpotlightScreen.WikiResult> parseResponse(String json) {
        List<SpotlightScreen.WikiResult> results = new ArrayList<>();
        try {
            JsonArray root   = JsonParser.parseString(json).getAsJsonArray();
            JsonArray titles = root.get(1).getAsJsonArray();
            JsonArray descs  = root.get(2).getAsJsonArray();
            JsonArray urls   = root.get(3).getAsJsonArray();
            for (int i = 0; i < titles.size(); i++) {
                String title = titles.get(i).getAsString();
                String desc  = descs.get(i).getAsString();
                String url   = urls.get(i).getAsString();
                if (desc.isBlank()) desc = "Minecraft Wiki";
                results.add(new SpotlightScreen.WikiResult(title, desc, url));
            }
        } catch (Exception ignored) {}
        return results;
    }
}
