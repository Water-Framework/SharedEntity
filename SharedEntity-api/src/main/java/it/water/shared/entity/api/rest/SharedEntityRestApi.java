package it.water.shared.entity.api.rest;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.*;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.service.rest.FrameworkRestApi;
import it.water.core.api.service.rest.RestApi;
import it.water.core.api.service.rest.WaterJsonView;
import it.water.service.rest.api.security.LoggedIn;
import it.water.shared.entity.model.WaterSharedEntity;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Set;

/**
 * @Generated by Water Generator
 * Rest Api Interface for SharedEntity entity.
 * This interfaces exposes all CRUD methods with default JAXRS annotations.
 */
@Path("/entities/shared")
@Api(produces = MediaType.APPLICATION_JSON, tags = "SharedEntity API")
@FrameworkRestApi
public interface SharedEntityRestApi extends RestApi {

    @LoggedIn
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/", notes = "SharedEntity Save API", httpMethod = "POST", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    WaterSharedEntity save(WaterSharedEntity sharedentity);


    @LoggedIn
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/{id}", notes = "SharedEntity Find API", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    WaterSharedEntity find(@PathParam("id") long id);

    @LoggedIn
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/", notes = "SharedEntity Find All API", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    PaginableResult<WaterSharedEntity> findAll();

    @LoggedIn
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/", notes = "Service for deleting a shared entity entity", httpMethod = "DELETE", consumes = "application/json")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 404, message = "Entity not found"),
            @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(WaterJsonView.Public.class)
    void removeSharedEntityByPK(@ApiParam(value = "Shared entity which must be deleted", required = true) WaterSharedEntity sharedEntity);

    @LoggedIn
    @GET
    @Path("/findByPK")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/findByPK",
            notes = "Service for finding sharedentity", httpMethod = "GET", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Entity not found")})
    @JsonView(WaterJsonView.Public.class)
    WaterSharedEntity findByPK
            (@ApiParam(value = "SharedEntity entity which must find ", required = true) WaterSharedEntity sharedEntity);

    @LoggedIn
    @GET
    @Path("/findByEntity")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/findByEntity",
            notes = "Service for finding SharedEntity objects", httpMethod = "GET", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Entity not found")})
    @JsonView(WaterJsonView.Public.class)
    Collection<WaterSharedEntity> findByEntity(@QueryParam("entityResourceName") String entityResourceName, @QueryParam("entityId") long entityId);

    @LoggedIn
    @GET
    @Path("/findByUser/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/findByUser/{userId}",
            notes = "Service for finding shared entity", httpMethod = "GET", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Entity not found")})
    @JsonView(WaterJsonView.Public.class)
    Collection<WaterSharedEntity> findByUser(@PathParam("userId") long userId);


    @LoggedIn
    @GET
    @Path("/sharingUsers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/getUsers",
            notes = "Service for finding sharing users by entity", httpMethod = "GET", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Entity not found")})
    @JsonView(WaterJsonView.Public.class)
    Set<Long> getUsers(@QueryParam("entityResourceName") String entityResourceName,
                       @QueryParam("entityId") long entityId);
}
