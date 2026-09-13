package org.CreadoresProgram.CreaTv.proxy;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.CreadoresProgram.CreaTv.utils.Util;

public class ProxyServer {

    private static final int PROXY_PORT = 9999;
    private static ServerSocket serverSocket;
    private static volatile boolean isRunning = false;

    private static final OkHttpClient client = Util.clientHtStream;

    public static synchronized void start() {
        if (isRunning) return;
        isRunning = true;

        new Thread(new Runnable() {
            public void run() {
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress("127.0.0.1", PROXY_PORT));

                    while (isRunning) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            new Thread(new ProxyHandler(clientSocket)).start();
                        } catch (IOException e) {
                            if (!isRunning) {
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    stop();
                }
            }
        }, "TlsProxyServerThread").start();
    }

    public static synchronized void stop() {
        isRunning = false;
        closeQuietly(serverSocket);
        serverSocket = null;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static String buildStreamUrl(String originalHttpsUrl) {
        try {
            String encodedUrl = URLEncoder.encode(originalHttpsUrl, "UTF-8");
            return "http://127.0.0.1:" + PROXY_PORT + "/proxy?target=" + encodedUrl;
        } catch (UnsupportedEncodingException e) {
            return originalHttpsUrl;
        }
    }

    private static class ProxyHandler implements Runnable {
        private final Socket vlcSocket;

        public ProxyHandler(Socket socket) {
            this.vlcSocket = socket;
        }

        public void run() {
            InputStream vlcIn = null;
            OutputStream vlcOut = null;
            Response response = null;

            try {
                vlcIn = vlcSocket.getInputStream();
                vlcOut = vlcSocket.getOutputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(vlcIn));
                String requestLine = reader.readLine();

                if (requestLine == null || !requestLine.startsWith("GET")) {
                    closeQuietly(vlcSocket);
                    return;
                }

                String targetUrlStr = extractTargetUrl(requestLine);
                if (targetUrlStr == null) {
                    send400BadRequest(vlcOut);
                    return;
                }

                Request request = new Request.Builder()
                        .url(targetUrlStr)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .build();

                response = client.newCall(request).execute();

                int responseCode = response.code();
                String contentType = response.header("Content-Type", "video/mp2t");
                if (contentType == null) {
                    contentType = "video/mp2t";
                }

                PrintWriter writer = new PrintWriter(vlcOut);
                writer.print("HTTP/1.1 " + responseCode + " OK\r\n");
                writer.print("Content-Type: " + contentType + "\r\n");
                writer.print("Connection: close\r\n\r\n");
                writer.flush();

                InputStream remoteIn = response.body().byteStream();

                if (targetUrlStr.contains(".m3u8") || contentType.contains("mpegurl") || contentType.contains("m3u8")) {
                    rewriteAndStreamM3u8(remoteIn, vlcOut, targetUrlStr);
                } else {
                    pipeStreamData(remoteIn, vlcOut);
                }

            } catch (Exception e) {
            } finally {
                if (response != null) {
                    response.close();
                }
                closeQuietly(vlcIn);
                closeQuietly(vlcOut);
                closeQuietly(vlcSocket);
            }
        }

        private void pipeStreamData(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
        }

        private void rewriteAndStreamM3u8(InputStream in, OutputStream out, String baseUrlStr) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out);
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#") && line.length() > 0) {
                    String absoluteUrl = resolveUrl(baseUrlStr, line);
                    line = buildStreamUrl(absoluteUrl);
                }
                writer.print(line + "\r\n");
            }
            writer.flush();
        }

        private String extractTargetUrl(String requestLine) throws Exception {
            int targetIndex = requestLine.indexOf("target=");
            if (targetIndex == -1) return null;
            int spaceIndex = requestLine.indexOf(" ", targetIndex);
            String encoded = requestLine.substring(targetIndex + 7, spaceIndex != -1 ? spaceIndex : requestLine.length());
            return URLDecoder.decode(encoded, "UTF-8");
        }

        private String resolveUrl(String baseUrl, String relativeOrAbsolute) {
            try {
                return new URL(new URL(baseUrl), relativeOrAbsolute).toString();
            } catch (Exception e) {
                return relativeOrAbsolute;
            }
        }

        private void send400BadRequest(OutputStream out) throws IOException {
            PrintWriter writer = new PrintWriter(out);
            writer.print("HTTP/1.1 400 Bad Request\r\n\r\n");
            writer.flush();
        }
    }

    private static void closeQuietly(Closeable c) {
        if (c != null) {
            try { c.close(); } catch (IOException ignored) {}
        }
    }

    private static void closeQuietly(ServerSocket ss) {
        if (ss != null) {
            try { ss.close(); } catch (IOException ignored) {}
        }
    }

    private static void closeQuietly(Socket s) {
        if (s != null) {
            try { s.close(); } catch (IOException ignored) {}
        }
    }
}
