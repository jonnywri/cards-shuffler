package example.cards.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.value.AutoValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@AutoValue
@ApiModel(description = "Definition of the data transfer object for a single Deck.")
@JsonPropertyOrder(alphabetic = true)
public abstract class DeckDto {
    /**
     * Factory method for creating instances of a DeckDto.
     */
    @JsonCreator
    public static DeckDto create(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("cards") List<String> cards
    ) {
        return new AutoValue_DeckDto(id, name, cards);
    }

    /**
     * Unique identifier for this deck.
     *
     * @return int representing the unique id for this deck.
     */
    @ApiModelProperty("Unique identifier for an individual deck.")
    public abstract int getId();

    /**
     * Human readable name for the deck.
     *
     * @return non-null name for the deck
     */
    @ApiModelProperty("Returns the name of the deck.")
    public abstract String getName();

    /**
     * Shuffled card list.
     *
     * @return list of shuffled cards
     */
    @ApiModelProperty("Returns a list of cards (order implied by list)")
    public abstract List<String> getCards();
}
