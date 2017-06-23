package fi.csc.chipster.proxy.other;

import fi.csc.chipster.proxy.ConnectionManager;
import fi.csc.chipster.proxy.WebSocketProxyServlet;
import fi.csc.chipster.proxy.model.Connection;
import fi.csc.chipster.proxy.model.Route;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

        connectToTarget(targetUri, sess.getUpgradeResponse().getHeaders());

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


//    /**
//     * Jetty  WebSocket client
//     * @param targetUri
//     */
//    private void connectToTarget(String targetUri) {
//
//        CountDownLatch connectLatch = new CountDownLatch(1);
//
//        this.proxyClient = new WebSocketProxyBinaryClient(this, connectLatch, targetUri.replace("/websockify/websockify", "/websockify"));
//
//        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
////        ClientManager client = ClientManager.createClient();
//
//        WebSocketClient client = new WebSocketClient();
//
//
//
////            client.connectToServer(proxyClient, cec, new URI(targetUri.replace("/websockify/websockify", "/websockify")));
//        try {
//            client.start();
//            ClientUpgradeRequest request = new ClientUpgradeRequest();
////            client.connect(proxyClient, new URI(targetUri.replace("/websockify/websockify", "/websockify")), request);
////            client.connect(proxyClient.getProxySocket(), new URI(targetUri.replace("/websockify/websockify", "/websockify")), request);
//            client.connect(proxyClient.getProxySocket(), new URI(targetUri.replace("/websockify/websockify", "/websockify")), request);
//            connectLatch.await();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            closeSocketSession(WebSocketProxyServlet.toCloseReason(e));
//        }
////            client. .connectToServer(proxyClient, cec, new URI(targetUri.replace("/websockify/websockify", "/websockify")));
//
//    }

    /**
     * Tyrus WebSocket client
     * @param targetUri
     */
    private void connectToTarget(String targetUri, Map<String, List<String>> __headers) {

        System.out.println("=================================================");
        System.out.println("=================================================");
        System.out.println("headers:" + __headers);

        CountDownLatch connectLatch = new CountDownLatch(1);

        this.proxyClient = new WebSocketProxyBinaryClient(this, connectLatch, targetUri.replace("/websockify/websockify", "/websockify"));


//        List<String> protocols = Arrays.asList("binary", "base64");
//        List<String> protocols = Arrays.asList("binary");
        List<String> protocols = Arrays.asList("base64");

//        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().preferredSubprotocols(protocols).build();
        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
                .configurator(new ClientEndpointConfig.Configurator() {
                    @Override
                    public void beforeRequest(Map<String, List<String>> headers) {
                        super.beforeRequest(headers);

                        System.out.println("////////////////////////////////");
                        System.out.println("inner headers:" + headers);


                        List<String> cookieList = headers.get("Cookie");
                        if (null == cookieList) {
                            cookieList = new ArrayList<>();
                        }
                        cookieList.add("token=6d932f54-7e88-4425-a96d-c86da27c6cf7");     // set your cookie value here
                        headers.put("Cookie", cookieList);

                        List<String> swa = __headers.get("Sec-WebSocket-Accept");
                        if(swa != null && !swa.isEmpty()){
                            System.out.println("swa:" + swa);
                            headers.put("Sec-WebSocket-Accept", swa);
                        }

                        List<String> values = __headers.get("Sec-WebSocket-Protocol");
                        if(values != null && !values.isEmpty()){
                            System.out.println("Sec-WebSocket-Protocol values:" + values);
                            headers.put("Sec-WebSocket-Protocol", values);
                        }

                        values = __headers.get("Sec-WebSocket-Extensions");
                        if(values != null && !values.isEmpty()){
                            System.out.println("Sec-WebSocket-Extensions values:" + values);
                            headers.put("Sec-WebSocket-Extensions", values);
                        }

                        headers.put("Origin", Arrays.asList("http://ctrl.vstage.rcdn.ru:6080"));
                        headers.put("Connection", Arrays.asList("keep-alive","Upgrade"));
                        headers.put("User-Agent", Arrays.asList("Mozilla/5.0 (X11; Linux x86_64; rv:52.0) Gecko/20100101 Firefox/52.0"));
                        headers.put("Upgrade", Arrays.asList("websocket"));
                        headers.put("Pragma", Arrays.asList("no-cache"));
                        headers.put("Cache-Control", Arrays.asList("no-cache"));

                        System.out.println("\n\nafter upgrade:" + headers);

                    }
                })
                .build();

        ClientManager client = ClientManager.createClient();
        client.setDefaultMaxBinaryMessageBufferSize(500000);

        try {
            client.connectToServer(proxyClient, cec, new URI(targetUri.replace("/websockify/websockify", "/websockify")));

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

        System.out.println("\n\nrequestPath:" + requestPath);
        System.out.println("\n\nprefix + \"/\":" + prefix + "/");
        System.out.println("\n\nis starts :" + !requestPath.startsWith(prefix + "/"));

//        if (!requestPath.startsWith(prefix + "/")) {
        if (!requestPath.startsWith(prefix)) {
            throw new IllegalArgumentException("path " + requestPath + " doesn't start with prefix " + prefix);
        } else {
            requestPath = requestPath.replaceFirst(prefix + "/", "");
        }

        UriBuilder targetUriBuilder = UriBuilder.fromUri(proxyTo);
        targetUriBuilder.path(requestPath);
        targetUriBuilder.replaceQuery(requestUri.getQuery());

        return targetUriBuilder.build().toString();
    }

//    private String getTargetUri(Session sourceSession) {
//
//        URI requestUri = sourceSession.getUpgradeRequest().getRequestURI();
//        String requestPath = requestUri.getPath();
//
//        System.out.println("\n\nrequestPath:" + requestPath);
//
////        if (!requestPath.startsWith(prefix + "/")) {
////            throw new IllegalArgumentException("path " + requestPath + " doesn't start with prefix " + prefix);
////        } else {
////            requestPath = requestPath.replaceFirst(prefix + "/", "");
////        }
//
//        requestPath = requestPath.replaceFirst(prefix + "/", "");
//        UriBuilder targetUriBuilder = UriBuilder.fromUri(proxyTo);
////        targetUriBuilder.path(requestPath);
//        targetUriBuilder.replaceQuery(requestUri.getQuery());
//
//        return targetUriBuilder.build().toString();
//    }

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

    public void sendByteBuffer(ByteBuffer message){

        System.out.println("\n\n" +
                "" +
                "SEND BYTES #1" +
                "" +
                "" +
                "\n\n\n");

            try {
                socketSession.getRemote().sendBytes(message);
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
