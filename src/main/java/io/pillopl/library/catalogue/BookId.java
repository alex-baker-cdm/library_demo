package io.pillopl.library.catalogue;


import java.util.Objects;
import java.util.UUID;

public class BookId {

    private final UUID bookId;

    public BookId(UUID bookId) {
        this.bookId = Objects.requireNonNull(bookId);
    }

    public UUID getBookId() {
        return bookId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookId bookId1 = (BookId) o;
        return Objects.equals(bookId, bookId1.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }

    @Override
    public String toString() {
        return "BookId{" +
                "bookId=" + bookId +
                '}';
    }
}
