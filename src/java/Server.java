import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/** Local Java HTTP server for the Smart Parking Lot browser prototype. */
public final class Server {
    private static final Path ROOT = findWebRoot();
    private static final String COOKIE = "parkside_session";
    private static final long SESSION_LIFETIME_SECONDS = 8 * 60 * 60;
    private static final String ADMIN_USER = setting("PARKSIDE_ADMIN_USER", "admin");
    private static final String ADMIN_PASSWORD = setting("PARKSIDE_ADMIN_PASSWORD", "admin123");
    private static final Map<String, Long> SESSIONS = new ConcurrentHashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<String, String> CONTENT_TYPES = Map.of(
        ".html", "text/html; charset=utf-8",
        ".css", "text/css; charset=utf-8",
        ".js", "text/javascript; charset=utf-8"
    );

    private Server() {}

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(setting("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        // Route browser requests through the shared request handler.
        server.createContext("/", Server::handle);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Parkside is running at http://localhost:" + port);
        System.out.println("Serving website files from " + ROOT);
        System.out.println("Demo admin username: " + ADMIN_USER);
        if (System.getenv("PARKSIDE_ADMIN_PASSWORD") == null) {
            System.out.println("Demo password: admin123 (set PARKSIDE_ADMIN_PASSWORD to change it)");
        }
    }

    private static Path findWebRoot() {
        try {
            Path classLocation = Path.of(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toAbsolutePath().normalize();
            Path classParent = classLocation.getParent();
            if (hasWebFiles(classParent)) return classParent;
            if (classParent != null && hasWebFiles(classParent.getParent())) return classParent.getParent();
        } catch (Exception ignored) {
            // Fall back to common launch locations below.
        }

        Path workingDirectory = Path.of(".").toAbsolutePath().normalize();
        if (hasWebFiles(workingDirectory)) return workingDirectory;
        Path nestedSource = workingDirectory.resolve("src");
        if (hasWebFiles(nestedSource)) return nestedSource;
        return workingDirectory;
    }

    private static boolean hasWebFiles(Path root) {
        return root != null && Files.isRegularFile(root.resolve("html/index.html"));
    }

    private static void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try {
            if (path.equals("/api/session") && method.equals("GET")) {
                json(exchange, 200, "{\"authenticated\":" + isAuthenticated(exchange) + "}");
                return;
            }
            if (path.equals("/api/login") && method.equals("POST")) {
                login(exchange);
                return;
            }
            if (path.equals("/api/logout") && method.equals("POST")) {
                logout(exchange);
                return;
            }
            if (!(method.equals("GET") || method.equals("HEAD"))) {
                text(exchange, 405, "Method not allowed");
                return;
            }

            String name;
            switch (path) {
                case "/":
                case "/index.html": name = "html/index.html"; break;
                case "/admin.html": name = "html/admin.html"; break;
                case "/login.html": name = "html/login.html"; break;
                default: name = path.startsWith("/") ? path.substring(1) : path;
            }
            if (!allowedFile(name)) {
                text(exchange, 404, "Not found");
                return;
            }
            if (name.equals("html/admin.html") && !isAuthenticated(exchange)) {
                exchange.getResponseHeaders().set("Location", "/login.html");
                exchange.getResponseHeaders().set("Cache-Control", "no-store");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }
            if (name.equals("html/login.html") && isAuthenticated(exchange)) {
                exchange.getResponseHeaders().set("Location", "/admin.html");
                exchange.getResponseHeaders().set("Cache-Control", "no-store");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }

            Path file = ROOT.resolve(name).normalize();
            if (!file.startsWith(ROOT) || !Files.isRegularFile(file)) {
                text(exchange, 404, "Not found");
                return;
            }
            byte[] body = Files.readAllBytes(file);
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", CONTENT_TYPES.get(extension(name)));
            headers.set("X-Content-Type-Options", "nosniff");
            headers.set("Referrer-Policy", "same-origin");
            headers.set("Cache-Control", "no-cache");
            if (method.equals("HEAD")) {
                exchange.sendResponseHeaders(200, -1);
            } else {
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            }
            exchange.close();
        } catch (Exception exception) {
            text(exchange, 500, "Server error");
        }
    }

    private static void login(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase().startsWith("application/x-www-form-urlencoded")) {
            json(exchange, 415, "{\"error\":\"Expected form-encoded credentials\"}");
            return;
        }
        byte[] raw = exchange.getRequestBody().readNBytes(8193);
        if (raw.length == 0 || raw.length > 8192) {
            json(exchange, 400, "{\"error\":\"Invalid request\"}");
            return;
        }
        Map<String, String> values = parseForm(new String(raw, StandardCharsets.UTF_8));
        boolean validUser = constantTimeEquals(ADMIN_USER, values.getOrDefault("username", ""));
        boolean validPassword = constantTimeEquals(ADMIN_PASSWORD, values.getOrDefault("password", ""));
        if (!validUser || !validPassword) {
            json(exchange, 401, "{\"error\":\"Invalid username or password\"}");
            return;
        }

        byte[] tokenBytes = new byte[32];
        RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        SESSIONS.put(token, Instant.now().getEpochSecond() + SESSION_LIFETIME_SECONDS);
        exchange.getResponseHeaders().add("Set-Cookie", COOKIE + "=" + token
            + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=" + SESSION_LIFETIME_SECONDS);
        json(exchange, 200, "{\"authenticated\":true}");
    }

    private static void logout(HttpExchange exchange) throws IOException {
        String token = cookie(exchange, COOKIE);
        if (token != null) SESSIONS.remove(token);
        exchange.getResponseHeaders().add("Set-Cookie", COOKIE + "=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0");
        json(exchange, 200, "{\"authenticated\":false}");
    }

    private static boolean isAuthenticated(HttpExchange exchange) {
        String token = cookie(exchange, COOKIE);
        if (token == null) return false;
        Long expiresAt = SESSIONS.get(token);
        if (expiresAt == null) return false;
        if (expiresAt <= Instant.now().getEpochSecond()) {
            SESSIONS.remove(token);
            return false;
        }
        return true;
    }

    private static String cookie(HttpExchange exchange, String name) {
        String header = exchange.getRequestHeaders().getFirst("Cookie");
        if (header == null) return null;
        for (String item : header.split(";")) {
            String[] pair = item.trim().split("=", 2);
            if (pair.length == 2 && pair[0].equals(name)) return pair[1];
        }
        return null;
    }

    private static Map<String, String> parseForm(String form) {
        Map<String, String> values = new ConcurrentHashMap<>();
        for (String part : form.split("&")) {
            String[] pair = part.split("=", 2);
            String key = decode(pair[0]);
            String value = pair.length == 2 ? decode(pair[1]) : "";
            values.putIfAbsent(key, value);
        }
        return values;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean allowedFile(String name) {
        return name.equals("html/index.html") || name.equals("html/admin.html") || name.equals("html/login.html")
            || name.equals("css/styles.css") || name.equals("js/auth.js") || name.equals("js/driver.js");
    }

    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot);
    }

    private static String setting(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static void json(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        send(exchange, status, body.getBytes(StandardCharsets.UTF_8));
    }

    private static void text(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        send(exchange, status, body.getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange exchange, int status, byte[] body) throws IOException {
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
