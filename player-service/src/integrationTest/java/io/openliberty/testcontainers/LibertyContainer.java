/**
 *
 */
package io.openliberty.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * @author aguibert
 */
public class LibertyContainer extends GenericContainer<LibertyContainer> {

    public LibertyContainer(final String dockerImageName) {
        super(dockerImageName);
        waitingFor(Wait.forLogMessage("^.*CWWKF0011I.*$", 1)); // wait for smarter planet message by default
    }

    public LibertyContainer waitForMPHealth() {
        waitingFor(new MPHealthWaitStrategy());
        return self();
    }

}
