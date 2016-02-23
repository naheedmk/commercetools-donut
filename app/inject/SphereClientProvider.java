package inject;

import com.google.inject.Provider;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientFactory;
import play.Configuration;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.Objects.requireNonNull;

@Singleton
class SphereClientProvider implements Provider<SphereClient> {

    private static final Logger.ALogger LOG = Logger.of(SphereClientProvider.class);

    private final Configuration configuration;

    @Inject
    public SphereClientProvider(final Configuration configuration) {
        this.configuration = requireNonNull(configuration);
    }

    @Override
    public SphereClient get() {
        final String projectKey = requireNonNull(configuration.getString("sphere.project"));
        final String clientId =  requireNonNull(configuration.getString("sphere.clientId"));
        final String clientSecret = requireNonNull(configuration.getString("sphere.clientSecret"));
        final SphereClientFactory factory = SphereClientFactory.of();
        final SphereClient sphereClient = factory.createClient(projectKey, clientId, clientSecret);
        LOG.debug("Created SphereClient");
        return sphereClient;
    }
}
