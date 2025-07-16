package io.pillopl.library.lending.patron.model;

import java.util.Objects;

import static io.pillopl.library.lending.patron.model.PatronType.Regular;

class PatronInformation {

    private final PatronId patronId;

    private final PatronType type;

    PatronInformation(PatronId patronId, PatronType type) {
        this.patronId = Objects.requireNonNull(patronId);
        this.type = Objects.requireNonNull(type);
    }

    public PatronId getPatronId() {
        return patronId;
    }

    public PatronType getType() {
        return type;
    }

    boolean isRegular() {
        return type.equals(Regular);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatronInformation that = (PatronInformation) o;
        return Objects.equals(patronId, that.patronId) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patronId, type);
    }

    @Override
    public String toString() {
        return "PatronInformation{" +
                "patronId=" + patronId +
                ", type=" + type +
                '}';
    }
}

