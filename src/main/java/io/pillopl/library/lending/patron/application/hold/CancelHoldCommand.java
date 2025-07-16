package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.patron.model.PatronId;

import java.time.Instant;
import java.util.Objects;

public class CancelHoldCommand {
    private final Instant timestamp;
    private final PatronId patronId;
    private final BookId bookId;

    public CancelHoldCommand(Instant timestamp, PatronId patronId, BookId bookId) {
        this.timestamp = Objects.requireNonNull(timestamp);
        this.patronId = Objects.requireNonNull(patronId);
        this.bookId = Objects.requireNonNull(bookId);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public PatronId getPatronId() {
        return patronId;
    }

    public BookId getBookId() {
        return bookId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CancelHoldCommand that = (CancelHoldCommand) o;
        return Objects.equals(timestamp, that.timestamp) && Objects.equals(patronId, that.patronId) && Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, patronId, bookId);
    }

    @Override
    public String toString() {
        return "CancelHoldCommand{" +
                "timestamp=" + timestamp +
                ", patronId=" + patronId +
                ", bookId=" + bookId +
                '}';
    }
}
