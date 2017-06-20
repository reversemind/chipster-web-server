package com.company.app;

import fi.csc.chipster.proxy.ConnectionManager;
import fi.csc.chipster.proxy.WebSocketProxyClient;
import fi.csc.chipster.proxy.WebSocketProxyServlet;
import fi.csc.chipster.proxy.WebSocketProxySocket;
import fi.csc.chipster.proxy.model.Connection;
import fi.csc.chipster.proxy.model.Route;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class WebSocketProxyBinarySocket extends WebSocketAdapter {

    // TODO replace on lombok
//	public static final Logger logger = LogManager.getLogger();

    private Session socketSession;
    protected WebSocketProxyBinaryClient proxyClient;
    private String prefix;
    private String proxyTo;

    private ConnectionManager connectionManager;

    private Connection connection;

    public WebSocketProxyBinarySocket(String prefix, String proxyTo, ConnectionManager connectionManager) {
        this.prefix = prefix;
        this.proxyTo = proxyTo;
        this.connectionManager = connectionManager;
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        this.socketSession = sess;

        String targetUri = getTargetUri(socketSession);
//		logger.debug("proxy " + socketSession.getUpgradeRequest().getRequestURI() + " \t -> " + targetUri);

        connectToTarget(targetUri);

        connection = new Connection();
        connection.setSourceAddress(socketSession.getRemoteAddress().getHostString().toString());
        connection.setRequestURI(socketSession.getUpgradeRequest().getRequestURI().toString());
        connection.setRoute(new Route(prefix.substring(1), proxyTo));
        connectionManager.addConnection(connection);

    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        proxyClient.closeClientSession(new CloseReason(CloseReason.CloseCodes.getCloseCode(statusCode), reason));
        connectionManager.removeConnection(connection);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        proxyClient.closeClientSession(WebSocketProxyServlet.toCloseReason(cause));
        connectionManager.removeConnection(connection);
    }

    private void connectToTarget(String targetUri) {

        CountDownLatch connectLatch = new CountDownLatch(1);

        this.proxyClient = new WebSocketProxyBinaryClient(this, connectLatch, targetUri);

        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
        ClientManager client = ClientManager.createClient();
        try {
            client.connectToServer(proxyClient, cec, new URI(targetUri));

            connectLatch.await();

        } catch (DeploymentException e) {
            // authentication error or bad request, no need to log
            closeSocketSession(WebSocketProxyServlet.toCloseReason(e));
        } catch (IOException | URISyntaxException | InterruptedException e) {
//			logger.error("failed to connect to " + targetUri, e);
            closeSocketSession(WebSocketProxyServlet.toCloseReason(e));
        }
    }

    private String getTargetUri(Session sourceSession) {

        URI requestUri = sourceSession.getUpgradeRequest().getRequestURI();
        String requestPath = requestUri.getPath();

        if (!requestPath.startsWith(prefix + "/")) {
            throw new IllegalArgumentException("path " + requestPath + " doesn't start with prefix " + prefix);
        } else {
            requestPath = requestPath.replaceFirst(prefix + "/", "");
        }

        UriBuilder targetUriBuilder = UriBuilder.fromUri(proxyTo);
        targetUriBuilder.path(requestPath);
        targetUriBuilder.replaceQuery(requestUri.getQuery());

        return targetUriBuilder.build().toString();
    }

    public void closeSocketSession(CloseReason closeReason) {
        socketSession.close(closeReason.getCloseCode().getCode(), closeReason.getReasonPhrase());
    }

    public void sendText(String message) {
        try {
            socketSession.getRemote().sendString(message);
        } catch (IOException e) {
//			logger.error("failed to send a message", e);
            proxyClient.closeClientSession(WebSocketProxyServlet.toCloseReason(e));
            closeSocketSession(WebSocketProxyServlet.toCloseReason(e));
        }
    }

    /**
     * A WebSocket binary frame has been received.
     *
     * @param payload
     *            the raw payload array received
     * @param offset
     *            the offset in the payload array where the data starts
     * @param len
     *            the length of bytes in the payload
     */
//    void onWebSocketBinary(byte payload[], int offset, int len);
    @Override
    public void onWebSocketBinary(byte[] arg0, int arg1, int arg2){
        super.onWebSocketBinary(arg0, arg1, arg2);

        System.out.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

        System.out.println("length:" + arg0);
        System.out.println("length:" + arg0.length);
        System.out.println("arg1:" + arg1);
        System.out.println("arg2:" + arg2);

        System.out.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

        proxyClient.sendBinary(arg0, arg1, arg2);
    }
}
