package io.pillopl.library.lending.patronprofile.model;

import io.pillopl.library.catalogue.BookId;

import java.time.Instant;
import java.util.Objects;

public class Hold {

    private final BookId book;

    private final Instant till;

    public Hold(BookId book, Instant till) {
        this.book = Objects.requireNonNull(book);
        this.till = Objects.requireNonNull(till);
    }

    public BookId getBook() {
        return book;
    }

    public Instant getTill() {
        return till;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hold hold = (Hold) o;
        return Objects.equals(book, hold.book) && Objects.equals(till, hold.till);
    }

    @Override
    public int hashCode() {
        return Objects.hash(book, till);
    }

    @Override
    public String toString() {
        return "Hold{" +
                "book=" + book +
                ", till=" + till +
                '}';
    }
}
