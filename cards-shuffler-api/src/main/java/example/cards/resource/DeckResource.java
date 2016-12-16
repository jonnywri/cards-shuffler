package example.cards.resource;

import example.cards.model.DeckDto;
import example.cards.model.DeckListDto;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Deck resource definition, see swagger documentation for more details.
 *
 * This is abstracted into an interface to allow a consumer to easily build both resource implementations
 * as well as client proxies.  The code for this is very straight forward with RestEasy client proxying.
 */
@Path("decks")
@Api(value = "Definition of deck resource.")
public interface DeckResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieves a list of all current decks.")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "Response indicating successful access to decks.",
                    response = DeckDto.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Limit provided was > 100."
            )
    })
    public DeckListDto getDecks(
            @ApiParam(name = "start", defaultValue = "0") @QueryParam("start") @DefaultValue("0") int start,
            @ApiParam(name = "limit", defaultValue = "20") @QueryParam("limit") @DefaultValue("20") int limit);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new shuffled deck.")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 204,
                    message = "Object successfully created.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "location",
                                    description = "location for created object",
                                    response = String.class
                            )
                    }

            )
    })
    public Response createDeck(DeckDto deck);

    @GET
    @Path("/{deckId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve an individual deck.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Deck with corresponding id found.",
                    response = DeckDto.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "No deck found for the corresponding id."
            )
    })
    public DeckDto getDeck(@PathParam("deckId") int id);

    @POST
    @Path("/{deckId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Shuffle an individual deck.")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Reshuffled deck is returned.",
                    response = DeckDto.class
            ),
            @ApiResponse(
                    code = 404,
                    message = "No deck found for the corresponding id."
            )
    })
    public DeckDto shuffleDeck(@PathParam("deckId") int id);

    @DELETE
    @Path("/{deckId}")
    @ApiOperation(value = "Delete an existing deck")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 204,
                    message = "Object successfully deleted."
            )
    })
    public Response deleteDeck(@PathParam("deckId") int id);
}
