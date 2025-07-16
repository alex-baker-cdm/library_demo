package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCanceled;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExpired;
import io.pillopl.library.lending.patron.model.PatronEvent.BookReturned;
import io.pillopl.library.lending.patron.model.PatronId;

import java.util.Objects;

import java.time.Instant;

public class BookOnHold implements Book {

    private final BookInformation bookInformation;

    private final LibraryBranchId holdPlacedAt;

    private final PatronId byPatron;

    private final Instant holdTill;

    private final Version version;

    BookOnHold(BookInformation bookInformation, LibraryBranchId holdPlacedAt, PatronId byPatron, Instant holdTill, Version version) {
        this.bookInformation = Objects.requireNonNull(bookInformation);
        this.holdPlacedAt = Objects.requireNonNull(holdPlacedAt);
        this.byPatron = Objects.requireNonNull(byPatron);
        this.holdTill = Objects.requireNonNull(holdTill);
        this.version = Objects.requireNonNull(version);
    }

    public BookOnHold(BookId bookId, BookType type, LibraryBranchId libraryBranchId, PatronId patronId, Instant holdTill, Version version) {
        this(new BookInformation(bookId, type), libraryBranchId, patronId, holdTill, version);
    }

    public AvailableBook handle(BookReturned bookReturned) {
        return new AvailableBook(
                bookInformation, new LibraryBranchId(bookReturned.getLibraryBranchId()),
                version);
    }

    public AvailableBook handle(BookHoldExpired bookHoldExpired) {
        return new AvailableBook(
                bookInformation,
                new LibraryBranchId(bookHoldExpired.getLibraryBranchId()),
                version);
    }

    public CheckedOutBook handle(BookCheckedOut bookCheckedOut) {
        return new CheckedOutBook(
                bookInformation,
                new LibraryBranchId(bookCheckedOut.getLibraryBranchId()),
                new PatronId(bookCheckedOut.getPatronId()),
                version);
    }

    public AvailableBook handle(BookHoldCanceled bookHoldCanceled) {
        return new AvailableBook(
                bookInformation, new LibraryBranchId(bookHoldCanceled.getLibraryBranchId()),
                version);
    }


    public BookId getBookId() {
        return bookInformation.getBookId();
    }

    public BookInformation getBookInformation() {
        return bookInformation;
    }

    public LibraryBranchId getHoldPlacedAt() {
        return holdPlacedAt;
    }

    public PatronId getByPatron() {
        return byPatron;
    }

    public Instant getHoldTill() {
        return holdTill;
    }

    public Version getVersion() {
        return version;
    }

    public boolean by(PatronId patronId) {
        return byPatron.equals(patronId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookOnHold that = (BookOnHold) o;
        return Objects.equals(bookInformation, that.bookInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookInformation);
    }

    @Override
    public String toString() {
        return "BookOnHold{" +
                "bookInformation=" + bookInformation +
                ", holdPlacedAt=" + holdPlacedAt +
                ", byPatron=" + byPatron +
                ", holdTill=" + holdTill +
                ", version=" + version +
                '}';
    }
}

