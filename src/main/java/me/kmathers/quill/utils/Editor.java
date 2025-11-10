package me.kmathers.quill.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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

    public CompletableFuture<String> createSession(String data) {

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
                        plugin.getLogger().log(Level.SEVERE, plugin.translate("quill.error.runtime.editor.no-sessionid", response));
                        return null;
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    public CompletableFuture<String> waitForEdits(String sessionId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        pollRecursively(sessionId, future);
        
        return future;
    }

    private void pollRecursively(String sessionId, CompletableFuture<String> resultFuture) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT + sessionId + "/wait"))
                .timeout(Duration.ofSeconds(26))
                .GET()
                .build();
        
        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.body() == null || response.body().isEmpty()) {
                        pollRecursively(sessionId, resultFuture);
                    } else {
                        resultFuture.complete(response.body());
                    }
                })
                .exceptionally(ex -> {
                    resultFuture.completeExceptionally(ex);
                    return null;
                });
    }

    public void deleteSession(String sessionId, CompletableFuture<String> resultFuture) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT + sessionId))
                .DELETE()
                .build();
        
        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.body() != null && !(response.body().isEmpty())) {
                        resultFuture.complete(response.body());
                    }
                })
                .exceptionally(ex -> {
                    resultFuture.completeExceptionally(ex);
                    return null;
                });
    }

    public void writeFile(String name, String content, CompletableFuture<String> resultFuture) {
        try {
            java.nio.file.Path dataFolder = plugin.getDataFolder().toPath();
            java.nio.file.Path scriptsDir = dataFolder.resolve("scripts");

            java.nio.file.Path filePath = scriptsDir.resolve(name);

            java.nio.file.Files.writeString(filePath, content, java.nio.charset.StandardCharsets.UTF_8);

            String result = filePath.toString();
            plugin.getLogger().info(plugin.translate("quill.editor.wrote-file", result));
            resultFuture.complete(result);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, plugin.translate("quill.error.runtime.editor.write-failed", e.getMessage()), e);
            resultFuture.completeExceptionally(e);
        }
    }

    public CompletableFuture<String> readFile(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                java.nio.file.Path dataFolder = plugin.getDataFolder().toPath();
                java.nio.file.Path scriptsDir = dataFolder.resolve("scripts");

                java.nio.file.Path filePath = scriptsDir.resolve(name);

                String result = java.nio.file.Files.readString(filePath);

                return result;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, plugin.translate("quill.error.runtime.editor.read-failed", e.getMessage()), e);
                throw new RuntimeException(e);
            }
        });
    }
}