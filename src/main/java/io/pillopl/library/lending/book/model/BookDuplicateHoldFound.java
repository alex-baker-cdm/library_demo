package io.pillopl.library.lending.book.model;

import io.pillopl.library.commons.events.DomainEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class BookDuplicateHoldFound implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant when;
    private final UUID firstPatronId;
    private final UUID secondPatronId;
    private final UUID libraryBranchId;
    private final UUID bookId;

    public BookDuplicateHoldFound(Instant when, UUID firstPatronId, UUID secondPatronId, UUID libraryBranchId, UUID bookId) {
        this.when = Objects.requireNonNull(when);
        this.firstPatronId = Objects.requireNonNull(firstPatronId);
        this.secondPatronId = Objects.requireNonNull(secondPatronId);
        this.libraryBranchId = Objects.requireNonNull(libraryBranchId);
        this.bookId = Objects.requireNonNull(bookId);
    }

    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getWhen() {
        return when;
    }

    public UUID getFirstPatronId() {
        return firstPatronId;
    }

    public UUID getSecondPatronId() {
        return secondPatronId;
    }

    public UUID getLibraryBranchId() {
        return libraryBranchId;
    }

    public UUID getBookId() {
        return bookId;
    }

    @Override
    public UUID getAggregateId() {
        return bookId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookDuplicateHoldFound that = (BookDuplicateHoldFound) o;
        return Objects.equals(eventId, that.eventId) && Objects.equals(when, that.when) && Objects.equals(firstPatronId, that.firstPatronId) && Objects.equals(secondPatronId, that.secondPatronId) && Objects.equals(libraryBranchId, that.libraryBranchId) && Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, when, firstPatronId, secondPatronId, libraryBranchId, bookId);
    }

    @Override
    public String toString() {
        return "BookDuplicateHoldFound{" +
                "eventId=" + eventId +
                ", when=" + when +
                ", firstPatronId=" + firstPatronId +
                ", secondPatronId=" + secondPatronId +
                ", libraryBranchId=" + libraryBranchId +
                ", bookId=" + bookId +
                '}';
    }
}
