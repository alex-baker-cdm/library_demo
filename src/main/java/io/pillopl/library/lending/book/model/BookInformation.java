package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;

import java.util.Objects;

public class BookInformation {

    private final BookId bookId;

    private final BookType bookType;

    public BookInformation(BookId bookId, BookType bookType) {
        this.bookId = Objects.requireNonNull(bookId);
        this.bookType = Objects.requireNonNull(bookType);
    }

    public BookId getBookId() {
        return bookId;
    }

    public BookType getBookType() {
        return bookType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookInformation that = (BookInformation) o;
        return Objects.equals(bookId, that.bookId) && Objects.equals(bookType, that.bookType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, bookType);
    }

    @Override
    public String toString() {
        return "BookInformation{" +
                "bookId=" + bookId +
                ", bookType=" + bookType +
                '}';
    }
}
