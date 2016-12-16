package example.cards.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.value.AutoValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Definition of a list resource returned by a deck.
 */
@AutoValue
@ApiModel(description = "Definition of the data transfer object for multiple Decks.")
@JsonPropertyOrder(alphabetic = true)
public abstract class DeckListDto {

    /**
     * Factory method for creating instances of a DeckDto.
     */
    @JsonCreator
    public static DeckListDto create(
            @JsonProperty(value = "total", required = true) int total,
            @JsonProperty(value = "start", required = true) int start,
            @JsonProperty(value = "limit", required = true) int limit,
            @JsonProperty(value = "moreResults", required = true) Boolean hasMoreResults,
            @JsonProperty(value = "decks", required = true) List<DeckDto> decks
    ) {
        return new AutoValue_DeckListDto(total, start, limit, hasMoreResults, decks);
    }

    @ApiModelProperty(value = "Total number of decks.", required = true)
    @JsonProperty(value = "total", required = true)
    public abstract int getTotal();

    @ApiModelProperty(value = "Pagination start.", required = true)
    @JsonProperty(value = "start", required = true)
    public abstract int getStart();

    @ApiModelProperty(value = "Pagination limit.", required = true)
    @JsonProperty(value = "limit", required = true)
    public abstract int getLimit();

    @ApiModelProperty(value = "If more results can be retrieved on next page.", required = true)
    @JsonProperty(value = "moreResults", required = true)
    public abstract Boolean hasMoreResults();

    @ApiModelProperty(value = "List of retrieved deck.", required = true)
    @JsonProperty(value = "decks", required = true)
    public abstract List<DeckDto> getDecks();

}
