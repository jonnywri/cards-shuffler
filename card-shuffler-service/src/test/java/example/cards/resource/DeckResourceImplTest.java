package example.cards.resource;

import com.google.common.collect.Lists;
import example.cards.dao.DeckDao;
import example.cards.entity.DeckEntity;
import example.cards.model.DeckDto;
import example.cards.model.DeckListDto;
import example.cards.shuffler.DeckShuffler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Test for the resource impl class.  Some repeat tests exist here and the ApplicationTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeckResourceImplTest {

    @Mock
    DeckDao deckDao;

    @Mock
    DeckShuffler deckShuffler;

    @Test
    public void testGetDecks() {
        DeckResourceImpl deckResource = new DeckResourceImpl(deckDao, deckShuffler);
        DeckDto deck = DeckDto.create(0, "deck1", Lists.newArrayList("card1", "card2"));
        Mockito.when(deckDao.getDecks(0, 1))
                .thenReturn(DeckListDto.create(1, 0, 1, false, Lists.newArrayList(deck)));
        deckResource.getDecks(0, 1);
        Mockito.verify(deckDao, Mockito.times(1)).getDecks(0, 1);
    }

    @Test
    public void testGetDecksInvalidStart() {
        try {
            DeckResourceImpl deckResource = new DeckResourceImpl(deckDao, deckShuffler);
            deckResource.getDecks(-1, 20);
            Assert.fail("Exception should have been thrown.");
        } catch (WebApplicationException wae) {
            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), wae.getResponse().getStatus());
            Assert.assertEquals(DeckResourceImpl.START_PARAM_ERROR, wae.getMessage());
        }
        Mockito.verifyNoMoreInteractions(deckDao);
    }

    @Test
    public void testGetDecksInvalidLimit() {
        try {
            DeckResourceImpl deckResource = new DeckResourceImpl(deckDao, deckShuffler);
            deckResource.getDecks(0, -1);
            Assert.fail("Exception should have been thrown.");
        } catch (WebApplicationException wae) {
            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), wae.getResponse().getStatus());
            Assert.assertEquals(DeckResourceImpl.LIMIT_MUST_BE_A_POSITIVE_INTEGER, wae.getMessage());
        }
        Mockito.verifyNoMoreInteractions(deckDao);
    }

    @Test
    public void testGetDecksZeroLimit() {
        try {
            DeckResourceImpl deckResource = new DeckResourceImpl(deckDao, deckShuffler);
            deckResource.getDecks(0, 0);
            Assert.fail("Exception should have been thrown.");
        } catch (WebApplicationException wae) {
            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), wae.getResponse().getStatus());
            Assert.assertEquals(DeckResourceImpl.LIMIT_MUST_BE_A_POSITIVE_INTEGER, wae.getMessage());
        }
        Mockito.verifyNoMoreInteractions(deckDao);
    }

    @Test
    public void testGetDecksGreaterThan100() {
        try {
            DeckResourceImpl deckResource = new DeckResourceImpl(deckDao, deckShuffler);
            deckResource.getDecks(0, 101);
            Assert.fail("Exception should have been thrown.");
        } catch (WebApplicationException wae) {
            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), wae.getResponse().getStatus());
            Assert.assertEquals(DeckResourceImpl.LIMIT_MAY_NOT_EXCEED_100, wae.getMessage());
        }
        Mockito.verifyNoMoreInteractions(deckDao);
    }

    @Test
    public void testCreateDeck() {
        DeckResourceImpl deckResource = new DeckResourceImpl(deckDao, deckShuffler);
        DeckDto deck = DeckDto.create(0, "deck1", Lists.newArrayList());
        Mockito.when(deckDao.createDeck(deck)).thenReturn(new DeckEntity()
            .setId(0).setName("deck1").setCards(Lists.newArrayList()));
        Response response = deckResource.createDeck(deck);
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Assert.assertEquals("/decks/0", response.getHeaderString("Location")); // host info not included
    }

    @Test
    public void testGetDeck() {
        DeckResourceImpl deckResource = new DeckResourceImpl(deckDao, deckShuffler);
        DeckDto mockDeck = DeckDto.create(0, "deck1", Lists.newArrayList());
        Mockito.when(deckDao.getDeck(0)).thenReturn(mockDeck);
        DeckDto deck = deckResource.getDeck(0);
        Assert.assertEquals(mockDeck, deck);
    }

    @Test
    public void testGetDeckInvalidId() {
        try {
            DeckResourceImpl deckResource = new DeckResourceImpl(deckDao, deckShuffler);
            deckResource.getDeck(0);
            Assert.fail("Exception should have been thrown.");
        } catch (WebApplicationException wae) {
            Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), wae.getResponse().getStatus());
            Assert.assertEquals(DeckResourceImpl.ENTITY_NOT_FOUND, wae.getMessage());
        }
    }

    @Test
    public void testShuffleDeck() {
        DeckResourceImpl deckResource = new DeckResourceImpl(deckDao, deckShuffler);
        DeckDto mockDeck = DeckDto.create(0, "deck1", Lists.newArrayList());
        Mockito.when(deckDao.shuffleDeck(0, deckShuffler)).thenReturn(mockDeck);
        DeckDto deck = deckResource.shuffleDeck(0);
        Assert.assertEquals(mockDeck, deck);
    }

    @Test
    public void testShuffleDeckInvalidId() {
        try {
            DeckResourceImpl deckResource = new DeckResourceImpl(deckDao, deckShuffler);
            deckResource.shuffleDeck(0);
            Assert.fail("Exception should have been thrown.");
        } catch (WebApplicationException wae) {
            Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), wae.getResponse().getStatus());
            Assert.assertEquals(DeckResourceImpl.ENTITY_NOT_FOUND, wae.getMessage());
        }
    }

    @Test
    public void testRemoveDeck() {
        DeckResourceImpl deckResource = new DeckResourceImpl(deckDao, deckShuffler);
        Mockito.when(deckDao.remove(0)).thenReturn(new DeckEntity()
            .setId(0)
            .setName("name")
            .setCards(Lists.newArrayList()));
        Response response = deckResource.deleteDeck(0);
        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void testRemoveDeckInvalidId() {
        try {
            DeckResourceImpl deckResource = new DeckResourceImpl(deckDao, deckShuffler);
            deckResource.deleteDeck(0);
            Assert.fail("Exception should have been thrown.");
        } catch (WebApplicationException wae) {
            Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), wae.getResponse().getStatus());
            Assert.assertEquals(DeckResourceImpl.ENTITY_NOT_FOUND, wae.getMessage());
        }
    }

}
