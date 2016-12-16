package example.cards.resource;

import example.cards.dao.DeckDao;
import example.cards.shuffler.DeckShuffler;
import example.cards.entity.DeckEntity;
import example.cards.model.DeckDto;
import example.cards.model.DeckListDto;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * Implementation of deck resource.
 *
 * Note: There's no check for null in this class because it is redundant. The top level framework will throw errors
 * appropriately and this class should not be directly consumed in any fashion.  If you do consume this class,
 * note that passing in null for any parameter in the class will result in NullPointerExceptions.
 */
public class DeckResourceImpl implements DeckResource {

    public static final String LIMIT_MAY_NOT_EXCEED_100 = "Limit may not exceed 100.";
    public static final String LIMIT_MUST_BE_A_POSITIVE_INTEGER = "Limit must be a positive integer";
    public static final String START_PARAM_ERROR = "Start must be 0 or a positive integer";
    public static final String ENTITY_NOT_FOUND = "No entity found for id.";
    private final DeckDao deckDao;
    private final DeckShuffler deckShuffler;

    /**
     * To note: the DeckShuffler is injected here and not in the dao so that we could manage this
     * more easily a per request basis with different semantics from the dao.
     *
     * Do not consume this outside of testing or Guice integration.  Not meant for reuse.
     */
    @Inject
    public DeckResourceImpl(DeckDao deckDao, DeckShuffler deckShuffler) {
        this.deckDao = deckDao;
        this.deckShuffler = deckShuffler;
    }

    @Override
    public DeckListDto getDecks(int start, int limit) {
        if (limit > 100) {
            throw new WebApplicationException(LIMIT_MAY_NOT_EXCEED_100, Response.Status.BAD_REQUEST);  // these messages should really be at least static or, better yet, localized variables
        } else if (limit <= 0) {
            throw new WebApplicationException(LIMIT_MUST_BE_A_POSITIVE_INTEGER, Response.Status.BAD_REQUEST);
        } else if (start < 0) {
            throw new WebApplicationException(START_PARAM_ERROR, Response.Status.BAD_REQUEST);
        }
        return deckDao.getDecks(start, limit);
    }

    @Override
    public Response createDeck(DeckDto deck) {
        DeckEntity created = deckDao.createDeck(deck);
        if (created != null) {
            return Response.created(URI.create("/decks/" + Integer.toString(created.getId()))).build();
        }
        return Response.status(500).entity("Unable to build entity").build();
    }

    @Override
    public DeckDto getDeck(int id) {
        DeckDto deck = deckDao.getDeck(id);
        if (deck == null) {
            throw new WebApplicationException(ENTITY_NOT_FOUND, Response.Status.NOT_FOUND);
        }
        return deck;
    }

    @Override
    public DeckDto shuffleDeck(int id) {
        DeckDto shuffled = deckDao.shuffleDeck(id, deckShuffler);
        if (shuffled == null) {
            throw new WebApplicationException(ENTITY_NOT_FOUND, Response.Status.NOT_FOUND);
        }
        return deckDao.shuffleDeck(id, deckShuffler);
    }

    @Override
    public Response deleteDeck(int id) {
        DeckEntity deck = deckDao.remove(id);
        if (deck == null) {
            throw new WebApplicationException(ENTITY_NOT_FOUND, Response.Status.NOT_FOUND);
        }

        return Response.noContent().build();
    }
}
