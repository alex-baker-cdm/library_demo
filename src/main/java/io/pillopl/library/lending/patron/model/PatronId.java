package io.pillopl.library.lending.patron.model;


import java.util.Objects;
import java.util.UUID;

public class PatronId {
    private final UUID patronId;

    public PatronId(UUID patronId) {
        this.patronId = Objects.requireNonNull(patronId);
    }

    public UUID getPatronId() {
        return patronId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatronId patronId1 = (PatronId) o;
        return Objects.equals(patronId, patronId1.patronId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patronId);
    }

    @Override
    public String toString() {
        return "PatronId{" +
                "patronId=" + patronId +
                '}';
    }
}
