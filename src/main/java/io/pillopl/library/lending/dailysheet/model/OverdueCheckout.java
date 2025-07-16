package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;
import io.pillopl.library.lending.patron.model.PatronId;

import java.util.Objects;

public class OverdueCheckout {
    private final BookId checkedOutBook;
    private final PatronId patron;
    private final LibraryBranchId library;

    public OverdueCheckout(BookId checkedOutBook, PatronId patron, LibraryBranchId library) {
        this.checkedOutBook = Objects.requireNonNull(checkedOutBook);
        this.patron = Objects.requireNonNull(patron);
        this.library = Objects.requireNonNull(library);
    }

    public BookId getCheckedOutBook() {
        return checkedOutBook;
    }

    public PatronId getPatron() {
        return patron;
    }

    public LibraryBranchId getLibrary() {
        return library;
    }

    OverdueCheckoutRegistered toEvent() {
        return OverdueCheckoutRegistered.now(this.patron, this.checkedOutBook, this.library);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverdueCheckout that = (OverdueCheckout) o;
        return Objects.equals(checkedOutBook, that.checkedOutBook) && Objects.equals(patron, that.patron) && Objects.equals(library, that.library);
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkedOutBook, patron, library);
    }

    @Override
    public String toString() {
        return "OverdueCheckout{" +
                "checkedOutBook=" + checkedOutBook +
                ", patron=" + patron +
                ", library=" + library +
                '}';
    }
}
