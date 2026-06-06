/*
 * Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package de.avanux.smartapplianceenabler.semp.discovery;

import org.fourthline.cling.model.message.Connection;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.fourthline.cling.model.message.UpnpHeaders;
import org.fourthline.cling.model.message.UpnpRequest;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.impl.apache.StreamServerConfigurationImpl;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.StreamServer;
import org.fourthline.cling.transport.spi.UpnpStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A Java-standard-library-based UPnP StreamServer replacing the Apache HTTP 4.x implementation,
 * which is incompatible with Java 24+ due to the removal of SecurityManager.
 */
public class SimpleStreamServerImpl implements StreamServer<StreamServerConfigurationImpl> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleStreamServerImpl.class);

    private static final int SOCKET_TIMEOUT_MS = 10_000;

    private final StreamServerConfigurationImpl configuration;
    private ServerSocket serverSocket;
    private Router router;

    public SimpleStreamServerImpl(StreamServerConfigurationImpl configuration) {
        this.configuration = configuration;
    }

    @Override
    public StreamServerConfigurationImpl getConfiguration() {
        return configuration;
    }

    @Override
    public synchronized void init(InetAddress bindAddress, Router router) throws InitializationException {
        this.router = router;
        try {
            int port = configuration.getListenPort();
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new java.net.InetSocketAddress(bindAddress, port), configuration.getTcpConnectionBacklog());
            configuration.setListenPort(serverSocket.getLocalPort());
            logger.info("SEMP UPnP stream server listening on {}:{}", bindAddress.getHostAddress(), serverSocket.getLocalPort());
        } catch (IOException e) {
            throw new InitializationException("Could not open server socket: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized int getPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : configuration.getListenPort();
    }

    @Override
    public void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.debug("Error closing server socket", e);
        }
    }

    @Override
    public void run() {
        int socketTimeoutMs = configuration.getDataWaitTimeoutSeconds() * 1000;
        while (!serverSocket.isClosed()) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                router.received(new SimpleUpnpStream(router.getProtocolFactory(), socket, socketTimeoutMs));
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    logger.warn("Error accepting connection on SEMP UPnP stream server", e);
                }
            } catch (RuntimeException e) {
                // executor rejected the task (e.g. during shutdown); close the socket
                if (socket != null) {
                    try { socket.close(); } catch (IOException ignored) {}
                }
            }
        }
    }

    // -----------------------------------------------------------------------

    private static class SimpleUpnpStream extends UpnpStream {

        private static final Logger log = LoggerFactory.getLogger(SimpleUpnpStream.class);

        private final Socket socket;
        private final int socketTimeoutMs;

        SimpleUpnpStream(ProtocolFactory protocolFactory, Socket socket, int socketTimeoutMs) {
            super(protocolFactory);
            this.socket = socket;
            this.socketTimeoutMs = socketTimeoutMs;
        }

        @Override
        public void run() {
            try {
                socket.setSoTimeout(socketTimeoutMs > 0 ? socketTimeoutMs : SOCKET_TIMEOUT_MS);
                log.trace("Incoming connection from: {}", socket.getInetAddress());

                StreamRequestMessage requestMessage = readRequest();
                if (requestMessage == null) {
                    log.warn("Could not parse HTTP request from {}", socket.getInetAddress());
                    return;
                }

                requestMessage.setConnection(new Connection() {
                    @Override
                    public boolean isOpen() {
                        return !socket.isClosed();
                    }

                    @Override
                    public InetAddress getRemoteAddress() {
                        return socket.getInetAddress();
                    }

                    @Override
                    public InetAddress getLocalAddress() {
                        return socket.getLocalAddress();
                    }
                });

                log.trace("Received synchronous stream: ({})", requestMessage);

                StreamResponseMessage responseMessage = process(requestMessage);
                if (responseMessage == null) {
                    responseMessage = new StreamResponseMessage(new UpnpResponse(
                            UpnpResponse.Status.NOT_FOUND.getStatusCode(),
                            UpnpResponse.Status.NOT_FOUND.getStatusMsg()));
                }

                writeResponse(responseMessage);
                responseSent(responseMessage);

            } catch (Throwable t) {
                log.error("Error processing UPnP stream from {}", socket.getInetAddress(), t);
                responseException(t);
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }

        private StreamRequestMessage readRequest() throws IOException {
            InputStream is = socket.getInputStream();

            // Read headers byte-by-byte until we detect end of headers (\r\n\r\n)
            ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
            int b;
            int prev3 = -1, prev2 = -1, prev1 = -1;
            while ((b = is.read()) != -1) {
                headerBuffer.write(b);
                if (prev3 == '\r' && prev2 == '\n' && prev1 == '\r' && b == '\n') {
                    break;
                }
                prev3 = prev2;
                prev2 = prev1;
                prev1 = b;
            }

            byte[] rawHeaders = headerBuffer.toByteArray();
            if (rawHeaders.length == 0) {
                return null;
            }

            // Parse request line from raw bytes
            ByteArrayInputStream bais = new ByteArrayInputStream(rawHeaders);
            BufferedReader lineReader = new BufferedReader(new InputStreamReader(bais, StandardCharsets.ISO_8859_1));
            String requestLine = lineReader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return null;
            }

            String[] parts = requestLine.split(" ", 3);
            if (parts.length < 2) {
                log.warn("Invalid HTTP request line: {}", requestLine);
                return null;
            }

            UpnpRequest.Method method = UpnpRequest.Method.getByHttpName(parts[0]);
            if (method == null) {
                method = UpnpRequest.Method.UNKNOWN;
            }
            URI uri = URI.create(parts[1]);

            // Reconstruct remaining header bytes for Cling's header parser
            int consumed = requestLine.getBytes(StandardCharsets.ISO_8859_1).length + 2; // +2 for \r\n
            byte[] remainingHeaders = new byte[rawHeaders.length - consumed];
            System.arraycopy(rawHeaders, consumed, remainingHeaders, 0, remainingHeaders.length);
            UpnpHeaders headers = new UpnpHeaders(new ByteArrayInputStream(remainingHeaders));

            StreamRequestMessage requestMessage = new StreamRequestMessage(method, uri);
            requestMessage.setHeaders(headers);
            return requestMessage;
        }

        private void writeResponse(StreamResponseMessage response) throws IOException {
            OutputStream os = socket.getOutputStream();

            UpnpResponse operation = response.getOperation();
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 ")
                    .append(operation.getStatusCode())
                    .append(" ")
                    .append(operation.getStatusMessage())
                    .append("\r\n");

            byte[] body = response.getBodyBytes();
            boolean hasContentLength = false;

            for (Map.Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
                if ("Content-Length".equalsIgnoreCase(entry.getKey())) {
                    hasContentLength = true;
                }
                for (String value : entry.getValue()) {
                    sb.append(entry.getKey()).append(": ").append(value).append("\r\n");
                }
            }
            if (!hasContentLength && body != null && body.length > 0) {
                sb.append("Content-Length: ").append(body.length).append("\r\n");
            }
            // RFC 7231 requires two-digit day; DateTimeFormatter.RFC_1123_DATE_TIME may produce single-digit days
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            String httpDate = String.format("%s, %02d %s %04d %02d:%02d:%02d GMT",
                    now.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    now.getDayOfMonth(),
                    now.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    now.getYear(), now.getHour(), now.getMinute(), now.getSecond());
            sb.append("Date: ").append(httpDate).append("\r\n");
            sb.append("Connection: close\r\n");
            sb.append("\r\n");

            String headers = sb.toString();
            log.trace("Sending HTTP response headers to {}:\n{}", socket.getInetAddress(), headers.replace("\r\n", "\\r\\n\n"));
            os.write(headers.getBytes(StandardCharsets.ISO_8859_1));
            if (body != null && body.length > 0) {
                log.trace("Sending HTTP response body to {} ({} bytes):\n{}", socket.getInetAddress(), body.length, new String(body, StandardCharsets.UTF_8));
                os.write(body);
            }
            os.flush();
        }
    }
}
