package example.cards.shuffler;

import example.cards.entity.DeckEntity;

/**
 * Interface for doing deck shuffling.
 */
public interface DeckShuffler {

    /**
     * Shuffles a deck of cards to return a newly ordered list.
     * @param deck non null deck of cards to be shuffled
     * @return DeckEntity that has a newly ordered cards list
     */
    public DeckEntity shuffleDeck(DeckEntity deck);
}
