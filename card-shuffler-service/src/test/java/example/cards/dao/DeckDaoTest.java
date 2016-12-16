package example.cards.dao;

import com.google.common.collect.Lists;
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

import java.util.List;

/**
 * Tests for the deck persistence layer.
 *
 * Lacks multithreading tests due to time constraints.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeckDaoTest {

    @Mock
    DeckShuffler deckShuffler;

    /**
     * Tests happy path functionality for get decks.  This method could potentially be split into several, but
     * the mental simplicity of walking through the workflow makes it logical to keep this as a single integrated test.
     * This helps keep the idea of the state management within the dao as a single entity in the test without it
     * spilling over across multiple tests, enforcing things like additional test suites and ordering.
     */
    @Test
    public void testDaoGetDecks() {
        DeckDao dao = new DeckDao();
        DeckListDto deckListDto = dao.getDecks(0, 1);

        Assert.assertEquals("", 0, deckListDto.getTotal());
        Assert.assertEquals("", 0, deckListDto.getStart());
        Assert.assertEquals("", 1, deckListDto.getLimit());
        Assert.assertEquals("", false, deckListDto.hasMoreResults());

        // add decks - need to do this because the dao itself holds state
        List<String> cards = Lists.newArrayList("card1", "card2");
        DeckDto deck1 = DeckDto.create(1, "deck1", cards);
        DeckDto deck2 = DeckDto.create(2, "deck2", cards);
        DeckDto deck3 = DeckDto.create(3, "deck3", Lists.newArrayList());
        dao.createDeck(deck1);
        dao.createDeck(deck2);
        dao.createDeck(deck3);

        DeckListDto updatedDeckList = dao.getDecks(0, 1);

        Assert.assertEquals("Should have 3 decks.", 3, updatedDeckList.getTotal());
        Assert.assertEquals("Should have start 0.", 0, updatedDeckList.getStart());
        Assert.assertEquals("Should have limit 1", 1, updatedDeckList.getLimit());
        Assert.assertEquals("Should have more results.", true, updatedDeckList.hasMoreResults());

        // validate decks
        List<DeckDto> decks = updatedDeckList.getDecks();
        Assert.assertEquals(1, decks.size());
        DeckDto deckDto1 = decks.get(0);
        Assert.assertEquals(0, deckDto1.getId()); // not what we gave on the dto, on purpose
        Assert.assertEquals(deck1.getName(), deckDto1.getName());
        Assert.assertEquals(deck1.getCards(), deckDto1.getCards());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDaoGetDecksNegativeStart() {
        new DeckDao().getDecks(-1, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDaoGetDecksNegativeLimit() {
        new DeckDao().getDecks(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDaoGetDecksLimitZero() {
        new DeckDao().getDecks(0, 0);
    }

    @Test
    public void testCreateDeck() {
        DeckDao dao = new DeckDao();

        List<String> cards = Lists.newArrayList("card1", "card2", "card3", "card4");
        DeckEntity deck = dao.createDeck(DeckDto.create(10, "deck1", cards));

        Assert.assertEquals(0, deck.getId());
        Assert.assertEquals("deck1", deck.getName());
        Assert.assertEquals(cards, deck.getCards());

        DeckEntity deck2 = dao.createDeck(DeckDto.create(10, "deck1", Lists.newArrayList()));

        Assert.assertEquals(1, deck2.getId());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateDeckNullDeck() {
        new DeckDao().createDeck(null);
    }

    @Test
    public void testShuffleDeck() {
        DeckDao dao = new DeckDao();
        DeckEntity deck = dao.createDeck(DeckDto.create(0, "deck1", Lists.newArrayList("card1", "card2", "card3")));

        DeckEntity preShuffledDeck = new DeckEntity()
                .setId(0)
                .setName("deck1")
                .setCards(Lists.newArrayList("card2", "card1", "card3"));
        Mockito.when(deckShuffler.shuffleDeck(deck)).thenReturn(preShuffledDeck);

        DeckDto shuffled = dao.shuffleDeck(0, deckShuffler);
        Assert.assertEquals(preShuffledDeck.getCards(), shuffled.getCards());
    }

    @Test
    public void testShuffleDeckInvalidId() {
        DeckDto shuffled = new DeckDao().shuffleDeck(0, deckShuffler);
        Assert.assertNull(shuffled);
        Mockito.verifyNoMoreInteractions(deckShuffler);
    }

    @Test(expected = NullPointerException.class)
    public void testShuffleDeckNullShuffler() {
        new DeckDao().shuffleDeck(0, null);
    }

    @Test
    public void testRemove() {
        DeckDao dao = new DeckDao();
        DeckEntity deck = dao.createDeck(DeckDto.create(0, "deck1", Lists.newArrayList("card1", "card2", "card3")));

        DeckEntity removed = dao.remove(deck.getId());
        Assert.assertEquals(deck, removed); // works by virtue of being the same object, better would be to implement equals
    }

    @Test
    public void testRemoveInvalidId() {
        Assert.assertNull(new DeckDao().remove(0));
    }

    @Test
    public void testGetDeck() {
        DeckDao dao = new DeckDao();
        DeckEntity deck = dao.createDeck(DeckDto.create(0, "deck1", Lists.newArrayList("card1", "card2", "card3")));

        DeckDto retrievedDeck = dao.getDeck(0);
        Assert.assertEquals(deck.getId(), retrievedDeck.getId());
        Assert.assertEquals(deck.getName(), retrievedDeck.getName());
        Assert.assertEquals(deck.getCards(), retrievedDeck.getCards());
    }

    @Test
    public void testGetDeckInvalidId() {
        Assert.assertNull(new DeckDao().getDeck(0));
    }
}
