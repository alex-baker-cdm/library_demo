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

    private AvailableBook toAvailableBook() {
        return new AvailableBook(new BookId(book_id), book_type,  new LibraryBranchId(available_at_branch), new Version(version));
    }

    private BookOnHold toBookOnHold() {
        return new BookOnHold(new BookId(book_id), book_type, new LibraryBranchId(on_hold_at_branch), new PatronId(on_hold_by_patron), on_hold_till, new Version(version));
    }

    private CheckedOutBook toCheckedOutBook() {
        return new CheckedOutBook(new BookId(book_id), book_type,  new LibraryBranchId(checked_out_at_branch), new PatronId(checked_out_by_patron), new Version(version));
    }

    io.pillopl.library.lending.book.new_model.Book toNewBook() {
        LibraryBranchId branch = available_at_branch != null ? new LibraryBranchId(available_at_branch) :
                                on_hold_at_branch != null ? new LibraryBranchId(on_hold_at_branch) :
                                new LibraryBranchId(checked_out_at_branch);
        
        io.pillopl.library.lending.book.new_model.Book book = 
            new io.pillopl.library.lending.book.new_model.Book(
                new BookId(book_id),
                book_type,
                branch,
                new Version(version)
            );
        
        if (book_state == OnHold) {
            book.setState(new io.pillopl.library.lending.book.new_model.OnHoldState(
                book,
                new LibraryBranchId(on_hold_at_branch),
                new PatronId(on_hold_by_patron),
                on_hold_till
            ));
        } else if (book_state == CheckedOut) {
            book.setState(new io.pillopl.library.lending.book.new_model.CheckedOutState(
                book,
                new LibraryBranchId(checked_out_at_branch),
                new PatronId(checked_out_by_patron)
            ));
        }
        
        return book;
    }
}

