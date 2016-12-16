package example.cards;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import example.cards.dao.DeckDao;
import example.cards.shuffler.DeckShuffler;
import example.cards.resource.DeckResourceImpl;
import example.cards.shuffler.RandomShuffler;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

import javax.inject.Named;

/**
 * Module created for service injection.
 */
public class ServiceModule extends AbstractModule {

    public static final String ALGORITHM_PROPERTY = "algorithm";

    @Override
    public void configure() {
        // Creates the swagger configuration - should be made configurable.
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.2");
        beanConfig.setSchemes(new String[]{"http"}); // should support https in future.
        beanConfig.setHost("localhost:8080"); // should be configured to DNS in future.
        beanConfig.setBasePath("/api");
        beanConfig.setResourcePackage("com.nike.cards.resource");
        beanConfig.setScan(true);

        bind(SwaggerSerializers.class);
        bind(JacksonJsonProvider.class);
        bind(ApiListingResource.class);
        bind(DeckResourceImpl.class);
    }

    @Provides
    @Named(ALGORITHM_PROPERTY)
    public String getAlgorithmConfig() {
        return System.getProperty(ALGORITHM_PROPERTY, "random");
    }

    /**
     * Deck shuffler which loads up a deck shuffler based on algorithm.
     * @param algorithm algorithm used to shuffle cards
     * @return non null DeckShuffler to shuffle the card
     */
    @Provides
    public DeckShuffler deckShuffler(@Named(ALGORITHM_PROPERTY) String algorithm) {
        switch(algorithm) {
            default:
                return new RandomShuffler();
        }
    }

    @Provides
    @Singleton
    public DeckDao getDeckDao() {
        return new DeckDao();
    }
}
