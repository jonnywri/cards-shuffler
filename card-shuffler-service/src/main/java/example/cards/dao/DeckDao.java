package example.cards.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import example.cards.entity.DeckEntity;
import example.cards.model.DeckDto;
import example.cards.model.DeckListDto;
import example.cards.shuffler.DeckShuffler;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Data access layer, injectable at runtime to allow for different deck access/persist/deletion operations.
 * <p>
 * This DAO is a very inefficient implementation to deal with multiple threads.  It basically locks interactions
 * to the underlying data store (a map) for ease of implementation. A better implementation would use a real
 * scalable data store, or would use better concurrency mechanisms to do sub copies of the lists for paging
 * while managed better access to the index variable (locks around index for creations).
 */
public class DeckDao {

    // Starting index definition - static and volatile for multiple thread access.
    private volatile int index = 0;

    /**
     * Decks is defined as a tree map, as order must be preserved by id for pagination purposes, while a map
     * provides the simplest look up mechanism while preserving the natural order.  If a list were chosen
     * for this implementation we would need to maintain a correlation of id => index which would require
     * more complex logic in additions/deletions, plus additional memory for storing the index to id mapping.
     * <p>
     * The tradeoff is longer lookup times when performing pagination.
     */
    private final TreeMap<Integer, DeckEntity> decks = Maps.newTreeMap();

    /**
     * Retrieval of deck, using basic pagination parameters.
     *
     * @param start start index from which to retrieve, should be 0 or a positive integer
     * @param limit total limit, should be a positive integer
     * @return a list of sorted deck entities
     * @throws IllegalArgumentException start or limit is a negative number
     */
    public synchronized DeckListDto getDecks(int start, int limit) {
        // fail fast
        Preconditions.checkArgument(start >= 0, "start must be 0 or a positive integer");
        Preconditions.checkArgument(limit > 0, "limit must be a positive integer");

        // return an empty array if the start is greater than the size.
        if (start >= decks.size()) {
            return DeckListDto.create(decks.size(), start, limit, false, Lists.newArrayList());
        }

        List<DeckDto> pagedDeckDtos = decks.entrySet().stream()
                .skip(start)
                .limit(limit)
                .map(e -> DeckConverter.convertToDto(e.getValue()))
                .collect(Collectors.toList());
        return DeckListDto.create(decks.size(), start, limit, (start+limit)<decks.size(), pagedDeckDtos);
    }

    /**
     * Creates a new deck from a deck API definition.
     *
     * @param dto non-null dto to be translated and stored as a deck entity.
     * @return created DeckEntity
     * @throws NullPointerException if dto is null
     */
    public synchronized DeckEntity createDeck(DeckDto dto) {
        Preconditions.checkNotNull(dto);
        DeckEntity entity = DeckConverter.convertToEntity(dto);
        int deckId = index++;
        decks.put(deckId, entity.setId(deckId));
        // return true always, no real chance of failure here
        return entity;
    }

    public synchronized DeckDto shuffleDeck(int deckId, DeckShuffler deckShuffler) {
        Preconditions.checkNotNull(deckShuffler);
        DeckEntity deck = decks.get(deckId);
        if (deck == null) {
            return null;
        }
        return DeckConverter.convertToDto(deckShuffler.shuffleDeck(deck));
    }

    /**
     * Removes a deck from the underlying map.
     *
     * @param deckId deck id to be removed
     * @return DeckDto that is removed - this is to have an implementation consistent with general "remove"
     * mechanisms whereby the consumer can act on the removed object
     */
    public synchronized DeckEntity remove(int deckId) {
        return decks.remove(deckId);
    }

    /**
     * Lookup deck by identifier.
     *
     * @param id id of the deck
     * @return null if provided id is null or does not exist in storage
     */
    public synchronized DeckDto getDeck(int id) {
        return DeckConverter.convertToDto(decks.get(id));
    }

    /**
     * Static utility class for converting between {@link DeckDto} objects and {@link DeckEntity} objects.
     */
    public static class DeckConverter {
        /**
         * Utility method will not set an identifier - should be generated elsewhere.
         *
         * @param dto dto to convertToDto into an entity
         * @return DeckEntity object representing the old DeckDto, null if provided DeckDto is null
         */
        public static DeckEntity convertToEntity(DeckDto dto) {
            if (dto == null) {
                return null;
            }
            return new DeckEntity()
                    .setName(dto.getName())
                    .setCards(dto.getCards());
        }

        /**
         * @param deckEntity entity to be converted into Dto
         * @return DeckDto object based on the original DeckEntity, null if provided entity was null
         */
        public static DeckDto convertToDto(DeckEntity deckEntity) {
            if (deckEntity == null) {
                return null;
            }
            return DeckDto.create(deckEntity.getId(), deckEntity.getName(), deckEntity.getCards());
        }
    }
}
