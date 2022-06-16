package de.buw.tmdt.plasma.utilities.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TickTock {

    private static final Logger logger = LoggerFactory.getLogger(TickTock.class);

    private static final ThreadLocal<List<Token>> timerLookup = new ThreadLocal<>();

    private TickTock() {
    }

    public static void tick() {
        tick(null);
    }

    public static void tick(String label) {
        long time = System.currentTimeMillis();
        if (timerLookup.get() == null) {
            timerLookup.set(new ArrayList<>());
        }
        timerLookup.get().add(new Token(label, time));
    }

    public static long tock() {
        long time = System.currentTimeMillis();
        List<Token> localTimerLookup = timerLookup.get();
        if (localTimerLookup == null || localTimerLookup.isEmpty()) {
            logger.warn("No tick for tock.");
        } else {
            Token last = localTimerLookup.remove(localTimerLookup.size() - 1);
            long result = time - last.time;
            logger.debug("Tock {} after {}ms.", last.key != null ? "for " + last.key : "", result);
            return result;
        }
        return -1;
    }

    private static final class Token {
        private final Object key;
        private final long time;

        public Token(Object key, long time) {
            this.key = key;
            this.time = time;
        }
    }
}
