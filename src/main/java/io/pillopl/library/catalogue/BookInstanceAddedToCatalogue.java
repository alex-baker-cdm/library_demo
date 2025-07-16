package io.pillopl.library.catalogue;

import io.pillopl.library.commons.events.DomainEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class BookInstanceAddedToCatalogue implements DomainEvent {

    private final UUID eventId = UUID.randomUUID();
    private final String isbn;
    private final BookType type;
    private final UUID bookId;
    private final Instant when = Instant.now();

    BookInstanceAddedToCatalogue(String isbn, BookType type, UUID bookId) {
        this.isbn = Objects.requireNonNull(isbn);
        this.type = Objects.requireNonNull(type);
        this.bookId = Objects.requireNonNull(bookId);
    }

    BookInstanceAddedToCatalogue(BookInstance bookInstance) {
        this(bookInstance.getBookIsbn().getIsbn(), bookInstance.getBookType(), bookInstance.getBookId().getBookId());
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getIsbn() {
        return isbn;
    }

    public BookType getType() {
        return type;
    }

    public UUID getBookId() {
        return bookId;
    }

    @Override
    public Instant getWhen() {
        return when;
    }

    @Override
    public UUID getAggregateId() {
        return bookId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookInstanceAddedToCatalogue that = (BookInstanceAddedToCatalogue) o;
        return Objects.equals(eventId, that.eventId) && Objects.equals(isbn, that.isbn) && Objects.equals(type, that.type) && Objects.equals(bookId, that.bookId) && Objects.equals(when, that.when);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, isbn, type, bookId, when);
    }

    @Override
    public String toString() {
        return "BookInstanceAddedToCatalogue{" +
                "eventId=" + eventId +
                ", isbn='" + isbn + '\'' +
                ", type=" + type +
                ", bookId=" + bookId +
                ", when=" + when +
                '}';
    }
}
