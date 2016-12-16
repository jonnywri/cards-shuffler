package example.cards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import example.cards.model.DeckDto;
import example.cards.model.DeckListDto;
import org.eclipse.jetty.server.Server;
import org.junit.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Tests running a jetty server from the application.
 *
 * This test servers as more of an integration test.  This should validate the entire service, mount points
 * and responses.
 *
 * Also just demonstrating the use of conditional checking here, versus equality checking.
 */
public class ApplicationTest {

    private static Server server;
    private static Client client;

    @BeforeClass
    public static void before() {
        try {
            server = Application.startJetty(8080, "jetty");
        } catch (Exception e) {
            Assert.fail("Server failed to start.");
        }
        client = ClientBuilder.newClient();
    }

    @AfterClass
    public static void after() throws Exception {
        if (server != null) {
            server.stop();
        }
        client.close();
    }

    @Test
    public void getSwaggerJson() {
        Response swaggerJson = client.target("http://localhost:8080/swagger.json")
                .request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue("Should have /swagger.json path mounted.", swaggerJson.getStatus() == 200);
        swaggerJson.close();
    }

    @Test
    public void getSwaggerYaml() {
        Response swaggerYaml = client.target("http://localhost:8080/swagger.json")
                .request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue("Should have /swagger.json path mounted.", swaggerYaml.getStatus() == 200);
        swaggerYaml.close();
    }

    @Test
    public void testGetEmptyDeckList() throws Exception {
        Response decks = client.target("http://localhost:8080/decks").request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue("Should have /decks path mounted", decks.getStatus() == 200);
        try {
            DeckListDto deckList = new ObjectMapper().readValue(decks.readEntity(String.class), DeckListDto.class);
            Assert.assertTrue("Should have no decks.", deckList.getTotal() == 0);
            Assert.assertTrue("Start should be 0.", deckList.getStart() == 0);
            Assert.assertTrue("Limit should be 20.", deckList.getLimit() == 20);
            Assert.assertTrue("Should have no more results.", !deckList.hasMoreResults());
        } catch (IOException ioe) {
            Assert.fail("Unexpected read exception during json translation.");
        }
    }

    @Test
    public void testFullWorkflow() throws Exception {
        DeckDto deck1 = DeckDto.create(0, "deck1", Lists.newArrayList("card1", "card2", "card3"));
        DeckDto deck2 = DeckDto.create(1, "spades-only", Lists.newArrayList("2-spades", "3-spades", "4-spades", "5-spades",
                "6-spades", "7-spades", "8-spades", "9-spades", "10-spades", "J-spades", "Q-spades", "K-spades",
                "A-spades"));
        Response response1 = client.target("http://localhost:8080/decks").request(MediaType.APPLICATION_JSON)
                .acceptEncoding(MediaType.APPLICATION_JSON)
                .put(Entity.json(new ObjectMapper().writeValueAsString(deck1)));
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response1.getStatus());
        Assert.assertEquals("http://localhost:8080/decks/0", response1.getHeaderString("Location"));
        response1.close();

        Response response2 = client.target("http://localhost:8080/decks").request(MediaType.APPLICATION_JSON)
                .acceptEncoding(MediaType.APPLICATION_JSON)
                .put(Entity.json(new ObjectMapper().writeValueAsString(deck2)));
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response2.getStatus());
        Assert.assertEquals("http://localhost:8080/decks/1", response2.getHeaderString("Location"));
        response2.close();

        Response decks = client.target("http://localhost:8080/decks")
                .queryParam("start", "0")
                .queryParam("limit", "1")
                .request(MediaType.APPLICATION_JSON).get();
        Assert.assertTrue("Should have /decks path mounted", decks.getStatus() == 200);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            DeckListDto deckList = objectMapper.readValue(decks.readEntity(String.class), DeckListDto.class);
            Assert.assertTrue("Should have no decks.", deckList.getTotal() == 2);
            Assert.assertTrue("Start should be 0.", deckList.getStart() == 0);
            Assert.assertTrue("Limit should be 20.", deckList.getLimit() == 1);
            Assert.assertTrue("Should have no more results.", deckList.hasMoreResults());
            DeckDto deck = deckList.getDecks().get(0);
            Assert.assertEquals(0, deck.getId());
            Assert.assertEquals(deck1.getName(), deck.getName());
            Assert.assertEquals(deck1.getCards(), deck.getCards());
            decks.close();
        } catch (IOException ioe) {
            Assert.fail("Unexpected read exception during json translation.");
        } finally {
            decks.close();
        }

        Response deck2Response = client.target("http://localhost:8080/decks/1")
                .request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), deck2Response.getStatus());
        DeckDto retrievedDeck2 = objectMapper.readValue(deck2Response.readEntity(String.class), DeckDto.class);
        Assert.assertEquals(deck2, retrievedDeck2);
        deck2Response.close();

        Response shuffleDeck2Response = client.target("http://localhost:8080/decks/1")
                .request(MediaType.APPLICATION_JSON).post(null);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), shuffleDeck2Response.getStatus());
        DeckDto shuffledDeck2 = objectMapper.readValue(shuffleDeck2Response.readEntity(String.class), DeckDto.class);
        Assert.assertEquals(1, shuffledDeck2.getId());
        Assert.assertEquals(deck2.getName(), shuffledDeck2.getName());
        Assert.assertNotEquals(deck2.getCards(), shuffledDeck2.getCards());
        for (String card : deck2.getCards()) {
            Assert.assertTrue(shuffledDeck2.getCards().contains(card));
        }
        shuffleDeck2Response.close();

        Response deleteDeck1Response = client.target("http://localhost:8080/decks/0")
                .request(MediaType.APPLICATION_JSON).delete();
        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteDeck1Response.getStatus());
        deleteDeck1Response.close();
        Response deleteDeck2Response = client.target("http://localhost:8080/decks/1")
                .request(MediaType.APPLICATION_JSON).delete();
        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteDeck2Response.getStatus());
        deleteDeck2Response.close();
        Response repeatDeleteDeck2Reponse = client.target("http://localhost:8080/decks/1")
                .request(MediaType.APPLICATION_JSON).delete();
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), repeatDeleteDeck2Reponse.getStatus());
        repeatDeleteDeck2Reponse.close();

        Response deck2NotFoundResponse = client.target("http://localhost:8080/decks/1")
                .request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), deck2NotFoundResponse.getStatus());
        deck2NotFoundResponse.close();
    }
}
