package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExpired;
import io.pillopl.library.lending.patron.model.PatronId;

import java.util.Objects;

public class ExpiredHold {
    private final BookId heldBook;
    private final PatronId patron;
    private final LibraryBranchId library;

    public ExpiredHold(BookId heldBook, PatronId patron, LibraryBranchId library) {
        this.heldBook = Objects.requireNonNull(heldBook);
        this.patron = Objects.requireNonNull(patron);
        this.library = Objects.requireNonNull(library);
    }

    public BookId getHeldBook() {
        return heldBook;
    }

    public PatronId getPatron() {
        return patron;
    }

    public LibraryBranchId getLibrary() {
        return library;
    }

    BookHoldExpired toEvent() {
        return BookHoldExpired.now(this.heldBook, this.patron, this.library);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpiredHold that = (ExpiredHold) o;
        return Objects.equals(heldBook, that.heldBook) && Objects.equals(patron, that.patron) && Objects.equals(library, that.library);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heldBook, patron, library);
    }

    @Override
    public String toString() {
        return "ExpiredHold{" +
                "heldBook=" + heldBook +
                ", patron=" + patron +
                ", library=" + library +
                '}';
    }
}
