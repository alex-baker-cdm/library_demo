package io.pillopl.library.lending.patron.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

import java.util.Objects;

class Hold {

    private final BookId bookId;
    private final LibraryBranchId libraryBranchId;

    Hold(BookId bookId, LibraryBranchId libraryBranchId) {
        this.bookId = Objects.requireNonNull(bookId);
        this.libraryBranchId = Objects.requireNonNull(libraryBranchId);
    }

    public BookId getBookId() {
        return bookId;
    }

    public LibraryBranchId getLibraryBranchId() {
        return libraryBranchId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hold hold = (Hold) o;
        return Objects.equals(bookId, hold.bookId) && Objects.equals(libraryBranchId, hold.libraryBranchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, libraryBranchId);
    }

    @Override
    public String toString() {
        return "Hold{" +
                "bookId=" + bookId +
                ", libraryBranchId=" + libraryBranchId +
                '}';
    }
}
