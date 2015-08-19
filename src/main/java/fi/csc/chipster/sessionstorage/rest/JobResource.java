package fi.csc.chipster.sessionstorage.rest;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.hibernate.ObjectNotFoundException;

import fi.csc.chipster.rest.RestUtils;
import fi.csc.chipster.rest.hibernate.Hibernate;
import fi.csc.chipster.rest.hibernate.Transaction;
import fi.csc.chipster.sessionstorage.model.Job;
import fi.csc.chipster.sessionstorage.model.Session;
import fi.csc.chipster.sessionstorage.model.SessionEvent;
import fi.csc.chipster.sessionstorage.model.SessionEvent.EventType;

public class JobResource {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(JobResource.class.getName());
	
	final private String sessionId;

	private SessionResource sessionResource;
	
	public JobResource() {
		sessionId = null;
	}
	
	public JobResource(SessionResource sessionResource, String id) {
		this.sessionResource = sessionResource;
		this.sessionId = id;
	}
	
    // CRUD
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transaction
    public Response get(@PathParam("id") String id, @Context SecurityContext sc) {

    	sessionResource.checkSessionReadAuthorization(sc.getUserPrincipal().getName(), sessionId);
    	Job result = (Job) getHibernate().session().get(Job.class, id);
    	
    	if (result == null) {
    		throw new NotFoundException();
    	}

   		return Response.ok(result).build();
    }
    
	@GET
    @Produces(MediaType.APPLICATION_JSON)
	@Transaction
    public Response getAll(@Context SecurityContext sc) {

		sessionResource.checkSessionReadAuthorization(sc.getUserPrincipal().getName(), sessionId);
		List<Job> result = getSession().getJobs();
		result.size(); // trigger lazy loading before the transaction is closed

		// if nothing is found, just return 200 (OK) and an empty list
		return Response.ok(toJaxbList(result)).build();
    }	

	@POST
    @Consumes(MediaType.APPLICATION_JSON)
	@Transaction
    public Response post(Job job, @Context UriInfo uriInfo, @Context SecurityContext sc) {	
    	        	
		job = RestUtils.getRandomJob();
		job.setJobId(null);
		
		if (job.getJobId() != null) {
			throw new BadRequestException("session already has an id, post not allowed");
		}
		
		job.setJobId(RestUtils.createId());

		sessionResource.checkSessionWriteAuthorization(sc.getUserPrincipal().getName(), sessionId);
		getSession().getJobs().add(job);

		URI uri = uriInfo.getAbsolutePathBuilder().path(job.getJobId()).build();
		Events.broadcast(new SessionEvent(job.getJobId(), EventType.CREATE));
		return Response.created(uri).build();
    }

	@PUT
	@Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
	@Transaction
    public Response put(Job job, @PathParam("id") String id, @Context SecurityContext sc) {
				    		
		// override the url in json with the id in the url, in case a 
		// malicious client has changed it
		job.setJobId(id);

		sessionResource.checkSessionWriteAuthorization(sc.getUserPrincipal().getName(), sessionId);
		if (getHibernate().session().get(Job.class, id) == null) {
			// transaction will commit, but we haven't changed anything
			return Response.status(Status.NOT_FOUND)
					.entity("job doesn't exist").build();
		}
		getHibernate().session().merge(job);

		// more fine-grained events are needed, like "job added" and "job removed"
		Events.broadcast(new SessionEvent(id, EventType.UPDATE));
		return Response.noContent().build();
    }

	@DELETE
    @Path("{id}")
	@Transaction
    public Response delete(@PathParam("id") String jobId, @Context SecurityContext sc) {

		try {
			// remove from session, hibernate will take care of the actual job table
			sessionResource.checkSessionWriteAuthorization(sc.getUserPrincipal().getName(), sessionId);
			Job job = (Job) getHibernate().session().load(Job.class, jobId);
			getSession().getJobs().remove(job);

			Events.broadcast(new SessionEvent(jobId, EventType.DELETE));
			return Response.noContent().build();
		} catch (ObjectNotFoundException e) {
			return Response.status(Status.NOT_FOUND).build();
		}
    }

	/**
	 * Call inside a transaction
	 * 
	 * @return
	 */
	private Session getSession() {
		return (Session) getHibernate().session().load(Session.class, sessionId);
	}
	
    /**
	 * Make a list compatible with JSON conversion
	 * 
	 * Default Java collections don't define the XmlRootElement annotation and 
	 * thus can't be converted directly to JSON. 
	 * @param <T>
	 * 
	 * @param result
	 * @return
	 */
	private GenericEntity<List<Job>> toJaxbList(List<Job> result) {
		return new GenericEntity<List<Job>>(result) {};
	}
	
	private Hibernate getHibernate() {
		return sessionResource.getHibernate();
	}
}
