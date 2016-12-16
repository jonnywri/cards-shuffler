package example.cards.shuffler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import example.cards.entity.DeckEntity;

import java.util.List;

/**
 * Random shuffler for a given deck.
 */
public class RandomShuffler implements DeckShuffler {

    @Override
    public DeckEntity shuffleDeck(DeckEntity deck) {
        Preconditions.checkNotNull(deck, "deck cannot be null");
        List<String> cards = Lists.newArrayList(deck.getCards()); // create a copy so as not to modify original list
        List<String> shuffledCards = Lists.newArrayList();
        for (int i = cards.size(); i > 0; i--) {
            int card = (int) (Math.random() * i);
            shuffledCards.add(cards.remove(card));
        }

        // This is an extremely lazy way to do this - better would be to understand the underlying
        // data model to do this more appropriately (in the case of Hibernate possibly not so bad,
        // but in this case we should really be making a defensive copy of the deck and return a
        // brand new instance.
        return deck.setCards(shuffledCards);
    }
}
