package io.pillopl.library.catalogue;


import java.util.Objects;
import java.util.UUID;

class BookInstance {

    private final ISBN bookIsbn;
    private final BookId bookId;
    private final BookType bookType;

    private BookInstance(ISBN bookIsbn, BookId bookId, BookType bookType) {
        this.bookIsbn = Objects.requireNonNull(bookIsbn);
        this.bookId = Objects.requireNonNull(bookId);
        this.bookType = Objects.requireNonNull(bookType);
    }

    static BookInstance instanceOf(Book book, BookType bookType) {
        return new BookInstance(book.getBookIsbn(), new BookId(UUID.randomUUID()), bookType);
    }

    public ISBN getBookIsbn() {
        return bookIsbn;
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
        BookInstance that = (BookInstance) o;
        return Objects.equals(bookIsbn, that.bookIsbn) && 
               Objects.equals(bookId, that.bookId) && 
               Objects.equals(bookType, that.bookType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookIsbn, bookId, bookType);
    }

    @Override
    public String toString() {
        return "BookInstance{" +
                "bookIsbn=" + bookIsbn +
                ", bookId=" + bookId +
                ", bookType=" + bookType +
                '}';
    }
}
