package fi.csc.chipster.proxy;

import java.net.URI;

import javax.servlet.ServletException;
import javax.websocket.DeploymentException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fi.csc.chipster.rest.Config;

public class ChipsterProxyServer {
	
	private final Logger logger = LogManager.getLogger();
	
    private ProxyServer proxy;

	public ChipsterProxyServer(Config config) {
    	
    	this.proxy = new ProxyServer(URI.create(config.getString("proxy-bind")));
    	
    	try {
    		proxy.addHttpProxyRule(		"sessiondb", 		config.getString("session-db"));
    		proxy.addWebSocketProxyRule("sessiondbevents", 	config.getString("session-db-events"), 2);
    		proxy.addHttpProxyRule(		"auth", 			config.getString("authentication-service"));
    		proxy.addHttpProxyRule(		"filebroker", 		config.getString("file-broker"));
    		
    	} catch (ServletException | DeploymentException e) {
    		logger.error("failed to add a proxy rule", e);
    	}
    }
	
	public void startServer() {
		proxy.startServer();
	}
	
	public void close() {
		proxy.close();
	}
}
