/**
 *
 */
package org.libertybikes.player.data;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class PlayerDBProducer {

    @Produces
    @ApplicationScoped
    public PlayerDB createDB() {
        try {
            PersistentPlayerDB db = new PersistentPlayerDB();
            if (db.isAvailable()) {
                System.out.println("Using persistent player DB.");
                return db;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Unable to create persistent player DB. Falling back to in-memory storage");
        return new InMemPlayerDB();
    }

}
