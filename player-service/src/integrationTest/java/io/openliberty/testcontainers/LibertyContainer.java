package io.openliberty.testcontainers;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * @author aguibert
 */
public class LibertyContainer extends GenericContainer<LibertyContainer> {

    static final Logger LOGGER = LoggerFactory.getLogger(LibertyContainer.class);

    private String baseURL;

    public LibertyContainer(final String dockerImageName) {
        super(dockerImageName);
        waitingFor(Wait.forLogMessage("^.*CWWKF0011I.*$", 1)); // wait for smarter planet message by default
    }

    public LibertyContainer waitForMPHealth() {
        waitingFor(new MPHealthWaitStrategy());
        return self();
    }

    public <T> T createRestClient(Class<T> clazz) {
        List<Class<?>> providers = new ArrayList<>();
        providers.add(JsonBProvider.class);
        return JAXRSClientFactory.create(getBaseURL(), clazz, providers);
    }

    public String getBaseURL() throws IllegalStateException {
        if (baseURL != null)
            return baseURL;
        if (!this.isRunning())
            throw new IllegalStateException("Container must be running to determine hostname and port");
        baseURL = "http://" + this.getContainerIpAddress() + ':' + this.getFirstMappedPort();
        return baseURL;
    }

}
