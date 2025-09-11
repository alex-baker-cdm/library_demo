package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.new_model.*;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.*;
import static io.vavr.API.*;

class BookDatabaseEntity {

    enum BookState {
        Available, OnHold, CheckedOut
    }

    UUID book_id;
    BookType book_type;
    BookState book_state;
    UUID available_at_branch;
    UUID on_hold_at_branch;
    UUID on_hold_by_patron;
    Instant on_hold_till;
    UUID checked_out_at_branch;
    UUID checked_out_by_patron;
    int version;

    Book toDomainModel() {
        return Match(book_state).of(
                Case($(Available), this::toAvailableBook),
                Case($(OnHold), this::toBookOnHold),
                Case($(CheckedOut), this::toCheckedOutBook)
        );
    }

    private Book toAvailableBook() {
        return new Book(new BookId(book_id), book_type, new LibraryBranchId(available_at_branch), new Version(version));
    }

    private Book toBookOnHold() {
        Book book = new Book(new BookId(book_id), book_type, new LibraryBranchId(on_hold_at_branch), new Version(version));
        book.placeOnHold(new PatronId(on_hold_by_patron), new LibraryBranchId(on_hold_at_branch), on_hold_till);
        return book;
    }

    private Book toCheckedOutBook() {
        Book book = new Book(new BookId(book_id), book_type, new LibraryBranchId(checked_out_at_branch), new Version(version));
        book.placeOnHold(new PatronId(checked_out_by_patron), new LibraryBranchId(checked_out_at_branch), on_hold_till != null ? on_hold_till : java.time.Instant.now().plus(java.time.Duration.ofDays(1)));
        book.checkout(new PatronId(checked_out_by_patron), new LibraryBranchId(checked_out_at_branch));
        return book;
    }
}

