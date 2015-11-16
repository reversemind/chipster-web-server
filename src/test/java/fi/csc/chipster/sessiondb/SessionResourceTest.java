package fi.csc.chipster.sessiondb;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fi.csc.chipster.rest.Config;
import fi.csc.chipster.rest.RestUtils;
import fi.csc.chipster.rest.TestServerLauncher;
import fi.csc.chipster.sessiondb.model.Session;

public class SessionResourceTest {

	private static TestServerLauncher launcher;
	private static SessionDbClient user1Client;
	private static SessionDbClient user2Client;
	private static SessionDbClient schedulerClient;
	private static SessionDbClient compClient;
	private static SessionDbClient unparseableTokenClient;
	private static SessionDbClient tokenFailClient;
	private static SessionDbClient authFailClient;
	private static SessionDbClient noAuthClient;

    @BeforeClass
    public static void setUp() throws Exception {
    	Config config = new Config();
    	launcher = new TestServerLauncher(config);
        
		user1Client 			= new SessionDbClient(launcher.getServiceLocator(), launcher.getUser1Token());
		user2Client 			= new SessionDbClient(launcher.getServiceLocator(), launcher.getUser2Token());
		schedulerClient 		= new SessionDbClient(launcher.getServiceLocator(), launcher.getSchedulerToken());
		compClient 				= new SessionDbClient(launcher.getServiceLocator(), launcher.getCompToken());
		unparseableTokenClient 	= new SessionDbClient(launcher.getServiceLocator(), launcher.getUnparseableToken());
		tokenFailClient 		= new SessionDbClient(launcher.getServiceLocator(), launcher.getWrongToken());
		authFailClient 			= new SessionDbClient(launcher.getServiceLocator(), launcher.getUsernameAndPassword());
		noAuthClient 			= new SessionDbClient(launcher.getServiceLocator(), null);
    }

    @AfterClass
    public static void tearDown() throws Exception {
    	launcher.stop();
    }

    @Test
    public void post() throws IOException, RestException {
    	
    	user1Client.createSession(RestUtils.getRandomSession());
    	
    	testCreateSession(401, unparseableTokenClient);
    	testCreateSession(403, tokenFailClient);
    	testCreateSession(401, authFailClient);
    	testCreateSession(401, noAuthClient);
    }

	private void testCreateSession(int expected, SessionDbClient client) {
		try {
    		client.createSession(RestUtils.getRandomSession());
    		assertEquals(true, false);
    	} catch (RestException e) {
    		assertEquals(expected, e.getResponse().getStatus());
    	}
	}

	@Test
    public void get() throws IOException, RestException {
		
		Session session1 = RestUtils.getRandomSession();
		Session session2 = RestUtils.getRandomSession();
		
		UUID sessionId1 = user1Client.createSession(session1);
		UUID sessionId2 = user2Client.createSession(session2);
		
		Session getSession1 = user1Client.getSession(sessionId1);
		
		assertEquals(sessionId1, getSession1.getSessionId());
		assertEquals(session1.getName(), getSession1.getName());
		// check that user2Client works, other tests rely on it
		assertEquals(true, user2Client.getSession(sessionId2) != null);
		// servers can read any session
		assertEquals(true, schedulerClient.getSession(sessionId1) != null);
		assertEquals(true, compClient.getSession(sessionId1) != null);
		
		// wrong user
		testGetSession(403, sessionId2, user1Client);
		testGetSession(403, sessionId1, user2Client);
		
		// auth tests
		testGetSession(401, sessionId1, unparseableTokenClient);
		testGetSession(403, sessionId1, tokenFailClient);
		testGetSession(401, sessionId1, authFailClient);
		testGetSession(401, sessionId1, noAuthClient);
    }
	
	private void testGetSession(int expected, UUID id, SessionDbClient client) {
		try {
    		client.getSession(id);
    		assertEquals(true, false);
    	} catch (RestException e) {
    		assertEquals(expected, e.getResponse().getStatus());
    	}
	}
	
	@Test
    public void getAll() throws RestException {
		
		Session session1 = RestUtils.getRandomSession();
		Session session2 = RestUtils.getRandomSession();
		
		UUID sessionId1 = user1Client.createSession(session1);
		UUID sessionId2 = user1Client.createSession(session2);
		
		assertEquals(true, user1Client.getSessions().containsKey(sessionId1));
		assertEquals(true, user1Client.getSessions().containsKey(sessionId2));
		
		// wrong user
		assertEquals(false, user2Client.getSessions().containsKey(sessionId1));
		
		// auth tests
		testGetSessions(401, unparseableTokenClient);
		testGetSessions(403, tokenFailClient);
		testGetSessions(401, authFailClient);
		testGetSessions(401, noAuthClient);
    }
	
	private void testGetSessions(int expected, SessionDbClient client) {
		try {
    		client.getSessions();
    		assertEquals(true, false);
    	} catch (RestException e) {
    		assertEquals(expected, e.getResponse().getStatus());
    	}
	}
	
	@Test
    public void put() throws RestException {
		
		Session session1 = RestUtils.getRandomSession();
		UUID sessionId1 = user1Client.createSession(session1);
		
		session1.setName("new name");
		user1Client.updateSession(session1);
		assertEquals("new name", user1Client.getSession(sessionId1).getName());
		
        // servers can modify any session
		session1.setName("new name2");
		schedulerClient.updateSession(session1);
		assertEquals("new name2", user1Client.getSession(sessionId1).getName());
		
		session1.setName("new name3");
		compClient.updateSession(session1);
		assertEquals("new name3", user1Client.getSession(sessionId1).getName());
		
		
        // wrong user
		testUpdateSession(403, session1, user2Client);
		
		testUpdateSession(401, session1, unparseableTokenClient);
		testUpdateSession(403, session1, tokenFailClient);
		testUpdateSession(401, session1, authFailClient);
		testUpdateSession(401, session1, noAuthClient);
    }
	
	private void testUpdateSession(int expected, Session newSession, SessionDbClient client) {
		try {
    		client.updateSession(newSession);
    		assertEquals(true, false);
    	} catch (RestException e) {
    		assertEquals(expected, e.getResponse().getStatus());
    	}
	}
	
	@Test
    public void delete() throws RestException {
		
		Session session1 = RestUtils.getRandomSession();
		UUID sessionId1 = user1Client.createSession(session1);
		
		// wrong user
		testDeleteSession(403, sessionId1, user2Client);

		// auth errors
		testDeleteSession(401, sessionId1, unparseableTokenClient);
		testDeleteSession(403, sessionId1, tokenFailClient);
		testDeleteSession(401, sessionId1, authFailClient);
		testDeleteSession(401, sessionId1, noAuthClient);

		// delete
		user1Client.deleteSession(sessionId1);
		
		// doesn't exist anymore
		testGetSession(404, sessionId1, user1Client);
    }
	
	private void testDeleteSession(int expected, UUID sessionId, SessionDbClient client) {
		try {
    		client.deleteSession(sessionId);
    		assertEquals(true, false);
    	} catch (RestException e) {
    		assertEquals(expected, e.getResponse().getStatus());
    	}
	}
}
