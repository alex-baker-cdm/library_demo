package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.model.*;
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

    io.pillopl.library.lending.book.new_model.Book toNewModelBook() {
        io.pillopl.library.lending.book.new_model.Book book = new io.pillopl.library.lending.book.new_model.Book(
                new BookId(book_id), 
                book_type, 
                new LibraryBranchId(available_at_branch != null ? available_at_branch : 
                        (on_hold_at_branch != null ? on_hold_at_branch : checked_out_at_branch)), 
                new Version(version)
        );
        
        Match(book_state).of(
                Case($(Available), () -> { return book; }),
                Case($(OnHold), () -> { 
                    book.placeOnHold(new PatronId(on_hold_by_patron), new LibraryBranchId(on_hold_at_branch), on_hold_till);
                    return book;
                }),
                Case($(CheckedOut), () -> { 
                    book.checkout(new PatronId(checked_out_by_patron), new LibraryBranchId(checked_out_at_branch));
                    return book;
                })
        );
        
        return book;
    }

    private AvailableBook toAvailableBook() {
        return new AvailableBook(new BookId(book_id), book_type,  new LibraryBranchId(available_at_branch), new Version(version));
    }

    private BookOnHold toBookOnHold() {
        return new BookOnHold(new BookId(book_id), book_type, new LibraryBranchId(on_hold_at_branch), new PatronId(on_hold_by_patron), on_hold_till, new Version(version));
    }

    private CheckedOutBook toCheckedOutBook() {
        return new CheckedOutBook(new BookId(book_id), book_type,  new LibraryBranchId(checked_out_at_branch), new PatronId(checked_out_by_patron), new Version(version));
    }
}

