package example.cards.shuffler;

import com.google.common.collect.Lists;
import example.cards.entity.DeckEntity;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests the random shuffler.  Typically I'd use a test data generator, but that would take
 * more time to setup.
 */
public class RandomShufflerTest {

    /**
     * Due to the random nature of this algorithm, setting up valid test cases for the workflows
     * is very difficult.  The main thing will be to look at whether shuffles are producing
     * consistently similar results.  This test will have a small statistical odds of failing for
     * the following reasons:
     * 1.) 2 decks shuffled into exactly the same order (very unlikely)
     * 2.) 3 deck shuffles produced the exact same number of matches
     *
     * These are mitigated by being statistically insignificant, but random is random.
     */
    @Test
    public void testValidShuffle() {
        RandomShuffler randomShuffler = new RandomShuffler();

        // Test data is built with a ten card deck.  This ensures that there are statistically
        // insignificant odds that a random shuffle will produce the same order.  The odds are
        // roughly (1/10)^10 of producing the same shuffle.
        List<String> cards = Lists.newArrayList("card1", "card2", "card3", "card4", "card5", "card6",
                "card7", "card8", "card9", "card10");
        List<String> cardCopy = Lists.newArrayList(cards);
        DeckEntity deck = new DeckEntity()
                .setId(1)
                .setName("deck1")
                .setCards(cards);

        DeckEntity shuffledDeck = randomShuffler.shuffleDeck(deck);
        Assert.assertEquals("Passed in list should not be modified", cards, cardCopy);
        Assert.assertEquals("Expected same ids.", deck.getId(), shuffledDeck.getId());
        Assert.assertEquals("Expected same name.", deck.getName(), shuffledDeck.getName());

        // Do card validation.
        Assert.assertEquals("Deck size should be the same.", cards.size(),
                shuffledDeck.getCards().size());
        for (String card : cards) {
            Assert.assertTrue("Shuffled deck should contain all cards.",
                    shuffledDeck.getCards().contains(card));
        }

        int firstShuffleMatches = findMatches(cards, shuffledDeck.getCards());
        DeckEntity secondShuffledDeck = randomShuffler.shuffleDeck(deck);
        int secondShuffleMatches = findMatches(cards, secondShuffledDeck.getCards());
        DeckEntity thirdShuffledDeck = randomShuffler.shuffleDeck(deck);
        int thirdShuffledMatches = findMatches(cards, thirdShuffledDeck.getCards());
        if ((firstShuffleMatches == secondShuffleMatches) && (secondShuffleMatches == thirdShuffledMatches)
                && firstShuffleMatches > 0) {
            Assert.fail("three shuffles should not produce the same number of matches.");
        }
    }

    private int findMatches(List<String> cards, List<String> otherCards) {
        int numMatches = 0;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).equals(otherCards.get(i))) {
                numMatches++;
            }
        }
        return numMatches;
    }

    @Test
    public void testEmptyDeck() {
        DeckEntity deck = new DeckEntity()
                .setId(1)
                .setName("deck1")
                .setCards(Lists.newArrayList());

        RandomShuffler randomShuffler = new RandomShuffler();
        DeckEntity shuffledDeck = randomShuffler.shuffleDeck(deck);

        Assert.assertEquals("Expected same ids.", deck.getId(), shuffledDeck.getId());
        Assert.assertEquals("Expected same name.", deck.getName(), shuffledDeck.getName());
        Assert.assertEquals("Deck should have empty list of cards.", (List<String>) Lists.<String> newArrayList(),
                shuffledDeck.getCards());
    }

    @Test
    public void testOneCardDeck() {
        List<String> cards = Lists.newArrayList("card1");
        DeckEntity deck = new DeckEntity()
                .setId(1)
                .setName("deck1")
                .setCards(cards);

        RandomShuffler randomShuffler = new RandomShuffler();
        DeckEntity shuffledDeck = randomShuffler.shuffleDeck(deck);
        Assert.assertEquals("Expected same ids.", deck.getId(), shuffledDeck.getId());
        Assert.assertEquals("Expected same name.", deck.getName(), shuffledDeck.getName());
        Assert.assertEquals("Deck should have empty list of cards.", cards,
                shuffledDeck.getCards());
    }

    @Test
    public void testDeckWithNullCards() {
        List<String> cards = Lists.newArrayList("card1", null);
        cards.add(null);
        DeckEntity deck = new DeckEntity()
                .setId(1)
                .setName("deck1")
                .setCards(cards);

        RandomShuffler randomShuffler = new RandomShuffler();
        DeckEntity shuffledDeck = randomShuffler.shuffleDeck(deck);
        Assert.assertEquals("Expected same ids.", deck.getId(), shuffledDeck.getId());
        Assert.assertEquals("Expected same name.", deck.getName(), shuffledDeck.getName());
    }

}
