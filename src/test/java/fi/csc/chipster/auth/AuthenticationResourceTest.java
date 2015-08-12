package fi.csc.chipster.auth;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import fi.csc.chipster.auth.model.Token;
import fi.csc.chipster.auth.rest.AuthenticationService;
import fi.csc.chipster.rest.TestServer;

public class AuthenticationResourceTest {

    public static final String path = "tokens";
	private static final MediaType JSON = MediaType.APPLICATION_JSON_TYPE;
    private WebTarget target;
	private TestServer server;

    @Before
    public void setUp() throws Exception {
    	server = new TestServer(new AuthenticationService());
        target = server.getTarget();
    }

    @After
    public void tearDown() throws Exception {
    	server.stop(this);
    }

    @Test
    public void postPassword() throws JsonGenerationException, JsonMappingException, IOException {	
    	postClientToken(target);
    	// no authorized header
    	assertEquals(401, postTokenResponse(server.getTarget(false), null, null).getStatus());
    	assertEquals(403, postTokenResponse(target, "client", "wrongPasword").getStatus());
    	assertEquals(403, postTokenResponse(target, "wrongUsername", "wrongPasword").getStatus());
    }
    
    @Test
    public void postServer() throws JsonGenerationException, JsonMappingException, IOException {	
    	postServerToken(target);                          
    }

	@Test
    public void validate() throws JsonGenerationException, JsonMappingException, IOException {
        String clientToken = postClientToken(target);
        String serverToken = postServerToken(target);
        
        getToken(target, "token", serverToken, clientToken);
        
        assertEquals(403, getTokenResponse(target, "token", "wrongServerToken", clientToken).getStatus());
        assertEquals(404, getTokenResponse(target, "token", serverToken, "wrongClientToken").getStatus());
     // no authorized header
        assertEquals(401, getTokenResponse(server.getTarget(false), null, null, clientToken).getStatus());
    }
	
	@Test
    public void delete() {
        
		String clientToken = postClientToken(target);
        String serverToken = postServerToken(target);
        
        getToken(target, "token", serverToken, clientToken);
     
        // no authorized header
        assertEquals(401, deleteTokenResponse(server.getTarget(false), "token", clientToken).getStatus());
        assertEquals(403, deleteTokenResponse(target, "token", "wrongClientToken").getStatus());
        assertEquals(204, deleteTokenResponse(target, "token", clientToken).getStatus());
        assertEquals(403, deleteTokenResponse(target, "token", clientToken).getStatus());
        
        assertEquals(404, getTokenResponse(target, "token", serverToken, clientToken).getStatus());
    }
    
    public static String postClientToken(WebTarget target) {
    	return postToken(target, "client", "clientPassword");
	}
    
    public static String postServerToken(WebTarget target) {
    	return postToken(target, "sessionStorage", "sessionStoragePassword");
	}

    public static String getToken(WebTarget target, String username, String password, String clientToken) {
    	Token token = target
    			.path(path)
    			.request(JSON)
    			.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username)
    		    .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password)
    		    .header("chipster-token", clientToken)
    		    .get(Token.class);
    	
        assertEquals(false, token == null);
        
        return token.getTokenKey();
	}
    
    public static Response deleteTokenResponse(WebTarget target, String username, String password) {
    	Response response = target
    			.path(path)
    			.request(JSON)
    			.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username)
    		    .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password)
    		    .delete(Response.class);
    	
        return response;
	}
    
    public static Response getTokenResponse(WebTarget target, String username, String password, String clientToken) {
    	Response response = target
    			.path(path)
    			.request(JSON)
    			.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username)
    		    .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password)
    		    .header("chipster-token", clientToken)
    		    .get(Response.class);
    	
        return response;
	}
    
    public static String postToken(WebTarget target, String username, String password) {
    	Token token = target
    			.path(path)
    			.request(JSON)
    			.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username)
    		    .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password)
    		    .post(null, Token.class);
    	
        assertEquals(false, token == null);
        
        return token.getTokenKey();
	}
    
    public static Response postTokenResponse(WebTarget target, String username, String password) {
    	Response response = target
    			.path(path)
    			.request(JSON)
    			.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username)
    		    .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password)
    		    .post(null, Response.class);
    	
        return response;
	}	
}