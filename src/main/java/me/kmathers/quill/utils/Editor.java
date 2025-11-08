package me.kmathers.quill.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.kmathers.quill.Quill;

public class Editor {
    private final Quill plugin;
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final URI ENDPOINT = URI.create("https://quill-relay.kohanmathersmcgonnell.workers.dev/session/");
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("\"sessionId\"\\s*:\\s*\"([^\"]+)\"");

    public Editor(Quill plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<String> createSession() {
        String data = "Hello, World!";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(ENDPOINT)
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> {
                    Matcher matcher = SESSION_ID_PATTERN.matcher(response);
                    if (matcher.find()) {
                        return matcher.group(1);
                    } else {
                        plugin.getLogger().log(Level.SEVERE, plugin.translate("errors.editor.no-sessionid", response));
                        return null;
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
}
