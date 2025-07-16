package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHold;
import io.pillopl.library.lending.patron.model.PatronId;

import java.util.Objects;

public class AvailableBook implements Book {

    private final BookInformation bookInformation;

    private final LibraryBranchId libraryBranch;

    private final Version version;

    public AvailableBook(BookInformation bookInformation, LibraryBranchId libraryBranch, Version version) {
        this.bookInformation = Objects.requireNonNull(bookInformation);
        this.libraryBranch = Objects.requireNonNull(libraryBranch);
        this.version = Objects.requireNonNull(version);
    }

    public AvailableBook(BookId bookId, BookType type, LibraryBranchId libraryBranchId, Version version) {
        this(new BookInformation(bookId, type), libraryBranchId, version);
    }

    public boolean isRestricted() {
        return bookInformation.getBookType().equals(BookType.Restricted);
    }

    public BookId getBookId() {
        return bookInformation.getBookId();
    }

    public BookInformation getBookInformation() {
        return bookInformation;
    }

    public LibraryBranchId getLibraryBranch() {
        return libraryBranch;
    }

    public Version getVersion() {
        return version;
    }

    public BookOnHold handle(BookPlacedOnHold bookPlacedOnHold) {
        return new BookOnHold(
                bookInformation,
                new LibraryBranchId(bookPlacedOnHold.getLibraryBranchId()),
                new PatronId(bookPlacedOnHold.getPatronId()),
                bookPlacedOnHold.getHoldTill(),
                version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvailableBook that = (AvailableBook) o;
        return Objects.equals(bookInformation, that.bookInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookInformation);
    }

    @Override
    public String toString() {
        return "AvailableBook{" +
                "bookInformation=" + bookInformation +
                ", libraryBranch=" + libraryBranch +
                ", version=" + version +
                '}';
    }
}

