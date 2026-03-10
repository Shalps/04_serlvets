package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private static final String PUBLIC_ROOT = "public";
    private static final Set<String> VALID_PATHS = Set.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js"
    );

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket socket = serverSocket.accept();
                    threadPool.execute(() -> handleClient(socket));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            threadPool.shutdownNow();
        }
    }

    private void handleClient(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                sendResponseErr(out, 400, "Bad Request", "Empty request");
                return;
            }
            String[] parts = requestLine.split(" ");
            if (parts.length != 3) {
                sendResponseErr(out, 400, "Not Found", "Malformed request line");
                return;
            }
            String method = parts[0];
            String path = parts[1];
            String version = parts[2];

            if (!"GET".equals(method)) {
                sendResponseErr(out, 405, "Method Not Allowed", "Only GET supported");
                return;
            }
            if (VALID_PATHS.contains(path)) {
                sendResponse(out,path);
                return;
            }else sendResponseErr(out,404,"Not Found","Не найдено");

            String body = "<h1>Server is working!</h1>";
            sendResponseErr(out, 200, "OK", body);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendResponseErr(BufferedOutputStream out, int statusCode, String statusText, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        String response =
                "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                        "Content-Type: text/html; charset=utf-8\r\n" +
                        "Content-Length: " + bytes.length + "\r\n" +
                        "\r\n";
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.write(bytes);
        out.flush();
    }
    void sendResponse(BufferedOutputStream out,String path) throws IOException {
        Path filePath = Path.of(".",PUBLIC_ROOT, path);
        String mimeType = Files.probeContentType(filePath);
        long length = Files.size(filePath);
        String response = "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                   "Content-Length: " + length + "\r\n" +
                                   "Connection: close\r\n" +
                                   "\r\n";
        out.write(response.getBytes());
        Files.copy(filePath, out);
        out.flush();

    }

//    public void start2() {
//        try ( ServerSocket serverSocket = new ServerSocket(9999)) {
//            while (true) {
//                try (
//                        final var socket = serverSocket.accept();
//                        final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                        final var out = new BufferedOutputStream(socket.getOutputStream());
//                ) {
//                    // read only request line for simplicity
//                    // must be in form GET /path HTTP/1.1
//                    final var requestLine = in.readLine();
//                    final var parts = requestLine.split(" ");
//
//                    if (parts.length != 3) {
//                        // just close socket
//                        continue;
//                    }
//
//                    final var path = parts[1];
//                    if (!validPaths.contains(path)) {
//                        out.write((
//                                "HTTP/1.1 404 Not Found\r\n" +
//                                        "Content-Length: 0\r\n" +
//                                        "Connection: close\r\n" +
//                                        "\r\n"
//                        ).getBytes());
//                        out.flush();
//                        continue;
//                    }
//
//                    final var filePath = Path.of(".", "public", path);
//                    final var mimeType = Files.probeContentType(filePath);
//
//                    // special case for classic
//                    if (path.equals("/classic.html")) {
//                        final var template = Files.readString(filePath);
//                        final var content = template.replace(
//                                "{time}",
//                                LocalDateTime.now().toString()
//                        ).getBytes();
//                        out.write((
//                                "HTTP/1.1 200 OK\r\n" +
//                                        "Content-Type: " + mimeType + "\r\n" +
//                                        "Content-Length: " + content.length + "\r\n" +
//                                        "Connection: close\r\n" +
//                                        "\r\n"
//                        ).getBytes());
//                        out.write(content);
//                        out.flush();
//                        continue;
//                    }
//
//                    final var length = Files.size(filePath);
//                    out.write((
//                            "HTTP/1.1 200 OK\r\n" +
//                                    "Content-Type: " + mimeType + "\r\n" +
//                                    "Content-Length: " + length + "\r\n" +
//                                    "Connection: close\r\n" +
//                                    "\r\n"
//                    ).getBytes());
//                    Files.copy(filePath, out);
//                    out.flush();
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
