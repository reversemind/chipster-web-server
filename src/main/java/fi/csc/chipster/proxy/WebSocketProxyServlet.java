package fi.csc.chipster.proxy;

import javax.inject.Singleton;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import com.company.app.WebSocketProxyBinarySocket;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Websocket proxy implementation. Creates a WebSocketProxySocket for listening
 * connections, which will create a WebSocketProxyClient to relay it.
 * <p>
 * The standard websocket API JSR 356 doesn't seem to allow dynamically adding and
 * removing sockets (called endpoint in JSR 356), so we are using Jetty's own API for the
 * socket side. This has and additional benefit of being a servlet, which makes it more similar
 * to the HTTP implementation. The JSR 356 didn't allow us to fail the initial upgrade request with
 * HTTP errors, but most likely that would be also possible now in this servlet implementation.
 *
 * @author klemela
 */
@Singleton
public class WebSocketProxyServlet extends WebSocketServlet {
    private ConnectionManager connectionManager;

    public WebSocketProxyServlet(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(WebSocketProxySocket.class);
        final String proxyTo = this.getInitParameter(ProxyServer.PROXY_TO);
        final String prefix = this.getInitParameter(ProxyServer.PREFIX);
        factory.setCreator(new WebSocketCreator() {

            @Override
            public Object createWebSocket(ServletUpgradeRequest request, ServletUpgradeResponse response) {

                System.out.println("\n\n===================================");
                System.out.println("headers:" + request.getHeaders());
                System.out.println("protocols:" + request.getSubProtocols());
                System.out.println("maps:" + request.getCookies());
                System.out.println("path:" + request.getRequestPath());
                System.out.println("\n\n===================================");

                for (String subprotocol : request.getSubProtocols()) {
                    if ("binary".equals(subprotocol)) {

                        System.out.println("\n\n ##^^^ -- binary ---\n\n");

                        response.setAcceptedSubProtocol(subprotocol);
                        // this will create a new instance for each connection
                        return new fi.csc.chipster.proxy.other.WebSocketProxyBinarySocket(prefix, proxyTo, connectionManager);
//                        return binaryEcho;
                    }

                    if ("base64".equals(subprotocol)) {

                        System.out.println("\n\n ##^^^ -- base64 ---\n\n");

                        response.setAcceptedSubProtocol(subprotocol);
//                        return textEcho;
                        // this will create a new instance for each connection
                        return new WebSocketProxySocket(prefix, proxyTo, connectionManager);
                    }

                    if ("text".equals(subprotocol)) {

                        System.out.println("\n\n ##^^^ -- text ---\n\n");

                        response.setAcceptedSubProtocol(subprotocol);
//                        return textEcho;
                        // this will create a new instance for each connection
                        return new WebSocketProxySocket(prefix, proxyTo, connectionManager);
                    }
                }

//                // No valid subprotocol in request, ignore the request
//                return null;
//
//


                System.out.println("\n\n ##^^^ -- default - text ---\n\n");
//                // this will create a new instance for each connection
                return new WebSocketProxySocket(prefix, proxyTo, connectionManager);
            }
        });
    }

    public static CloseReason toCloseReason(Throwable e) {
        String msg = "proxy error: " + e.getClass().getSimpleName() + " " + e.getMessage();
        if (e.getCause() != null) {
            msg += " Caused by: " + e.getCause().getClass().getSimpleName() + " " + e.getCause().getMessage();
        }
        return new CloseReason(CloseCodes.UNEXPECTED_CONDITION, msg);
    }
}