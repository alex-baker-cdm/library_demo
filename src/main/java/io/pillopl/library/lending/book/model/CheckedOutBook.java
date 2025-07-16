package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent;
import io.pillopl.library.lending.patron.model.PatronId;

import java.util.Objects;

public class CheckedOutBook implements Book {

    private final BookInformation bookInformation;

    private final LibraryBranchId checkedOutAt;

    private final PatronId byPatron;

    private final Version version;

    CheckedOutBook(BookInformation bookInformation, LibraryBranchId checkedOutAt, PatronId byPatron, Version version) {
        this.bookInformation = Objects.requireNonNull(bookInformation);
        this.checkedOutAt = Objects.requireNonNull(checkedOutAt);
        this.byPatron = Objects.requireNonNull(byPatron);
        this.version = Objects.requireNonNull(version);
    }

    public CheckedOutBook(BookId bookId, BookType type, LibraryBranchId libraryBranchId, PatronId patronId, Version version) {
        this(new BookInformation(bookId, type), libraryBranchId, patronId, version);
    }

    public BookId getBookId() {
        return bookInformation.getBookId();
    }

    public BookInformation getBookInformation() {
        return bookInformation;
    }

    public LibraryBranchId getCheckedOutAt() {
        return checkedOutAt;
    }

    public PatronId getByPatron() {
        return byPatron;
    }

    public Version getVersion() {
        return version;
    }

    public AvailableBook handle(PatronEvent.BookReturned bookReturnedByPatron) {
        return new AvailableBook(
                bookInformation,
                new LibraryBranchId(bookReturnedByPatron.getLibraryBranchId()),
                version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckedOutBook that = (CheckedOutBook) o;
        return Objects.equals(bookInformation, that.bookInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookInformation);
    }

    @Override
    public String toString() {
        return "CheckedOutBook{" +
                "bookInformation=" + bookInformation +
                ", checkedOutAt=" + checkedOutAt +
                ", byPatron=" + byPatron +
                ", version=" + version +
                '}';
    }
}

