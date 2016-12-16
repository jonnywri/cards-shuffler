package example.cards.entity;

import java.util.List;

/**
 * Model created to represent and underlying deck - separating our data storage concerns from our
 * top level API modeling concerns.  Allows for evolution of the data model without directly
 * impacting the API.
 * <p>
 * Mutability is left possible for this entity as numerous underlying data store technologies (such
 * as Hibernate) rely upon mutability.
 *
 * Generally speaking, implementing equals and hashcode would be appropriate, but that is being cut
 * for time considerations.  There's also a bit of laziness here in that nulls are ignored for things
 * like name; allowing for the top level dto to validate that the name is not coming in null.
 */
public class DeckEntity {

    private int id;
    private String name;
    private List<String> cards;

    public int getId() {
        return id;
    }

    public DeckEntity setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public DeckEntity setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getCards() {
        return cards;
    }

    public DeckEntity setCards(List<String> cards) {
        this.cards = cards;
        return this;
    }
}
