/**
 *
 */
package io.openliberty.testcontainers;

import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;

/**
 * @author aguibert
 */
public class MPHealthWaitStrategy extends HttpWaitStrategy {

    public MPHealthWaitStrategy() {
        super();
        forPath("/health");
        forResponsePredicate(response -> response.contains("\"outcome\":\"UP\""));
    }

    @Override
    public void waitUntilReady(WaitStrategyTarget waitStrategyTarget) {
        super.waitUntilReady(waitStrategyTarget);
        // TODO: heuristic to work around MP Health endpoint being available slightly before the app is
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
    }
}
