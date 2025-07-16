package io.pillopl.library.lending.patronprofile.model;

import io.pillopl.library.catalogue.BookId;

import java.time.Instant;
import java.util.Objects;

public class Checkout {

    private final BookId book;

    private final Instant till;

    public Checkout(BookId book, Instant till) {
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
        Checkout checkout = (Checkout) o;
        return Objects.equals(book, checkout.book) && Objects.equals(till, checkout.till);
    }

    @Override
    public int hashCode() {
        return Objects.hash(book, till);
    }

    @Override
    public String toString() {
        return "Checkout{" +
                "book=" + book +
                ", till=" + till +
                '}';
    }
}
