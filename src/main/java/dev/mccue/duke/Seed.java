package dev.mccue.duke;

import java.util.Optional;

/// Wrapper class for a seed, useful for browser embedding
/// where CheerpJ currently does not directly support longs
public record Seed(long value) {
    public static Optional<Seed> tryParse(String input) {
        try {
            return Optional.of(new Seed(Long.parseUnsignedLong(input)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public String toString() {
        return Long.toUnsignedString(value);
    }
}
