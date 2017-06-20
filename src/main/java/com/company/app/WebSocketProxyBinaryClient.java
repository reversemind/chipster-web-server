package com.company.app;

import fi.csc.chipster.proxy.WebSocketProxyServlet;
import fi.csc.chipster.proxy.WebSocketProxySocket;

import javax.websocket.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class WebSocketProxyBinaryClient extends Endpoint {

    // TODO replace on lombok
//	private static final Logger logger = LogManager.getLogger();

    private WebSocketProxyBinarySocket proxySocket;
    private Session clientSession;
    private String targetUri;
    private CountDownLatch connectLatch;

    public WebSocketProxyBinaryClient(WebSocketProxyBinarySocket jettyWebSocketSourceEndpoint, CountDownLatch openLatch, String targetUri) {
        this.proxySocket = jettyWebSocketSourceEndpoint;
        this.connectLatch = openLatch;
        this.targetUri = targetUri;
    }

    @Override
    public void onOpen(Session targetSession, EndpointConfig config) {
        this.clientSession = targetSession;

        targetSession.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                proxySocket.sendText(message);
            }
        });
        connectLatch.countDown();
    }

    @Override
    public void onClose(Session session, CloseReason reason) {
        connectLatch.countDown();
        proxySocket.closeSocketSession(reason);
    }

    @Override
    public void onError(Session session, Throwable thr) {
        connectLatch.countDown();
        proxySocket.closeSocketSession(WebSocketProxyServlet.toCloseReason(thr));
    }

    public void sendText(String message) {
        try {
            clientSession.getBasicRemote().sendText(message);
        } catch (IOException e) {
//			logger.error("failed to send a message to " + targetUri, e);
            proxySocket.closeSocketSession(WebSocketProxyServlet.toCloseReason(e));
            closeClientSession(WebSocketProxyServlet.toCloseReason(e));
        }
    }

    public void sendBinary(byte[] arg0, int a1, int a2){
        try {
            clientSession.getBasicRemote().sendText(null);
        } catch (IOException e) {
//			logger.error("failed to send a message to " + targetUri, e);
            proxySocket.closeSocketSession(WebSocketProxyServlet.toCloseReason(e));
            closeClientSession(WebSocketProxyServlet.toCloseReason(e));
        }
    }

    public void closeClientSession(CloseReason closeReason) {
        try {
            if (clientSession != null) {
                clientSession.close(closeReason);
            }
        } catch (IOException e) {
//			logger.error("failed to close the target websocket to " + targetUri, e);
        }
    }
}
