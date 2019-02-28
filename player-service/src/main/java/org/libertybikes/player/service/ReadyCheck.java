/**
 *
 */
package org.libertybikes.player.service;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

/**
 * @author aguibert
 *
 */
public class ReadyCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.builder().up().build();
    }

}
