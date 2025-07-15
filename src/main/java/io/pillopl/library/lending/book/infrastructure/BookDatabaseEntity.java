package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.model.*;
import io.pillopl.library.lending.book.new_model.*;
import io.pillopl.library.lending.book.new_model.Book;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.*;
import static io.vavr.API.*;

@NoArgsConstructor
@Data
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

    io.pillopl.library.lending.book.model.Book toDomainModel() {
        return Match(book_state).of(
                Case($(Available), this::toAvailableBook),
                Case($(OnHold), this::toBookOnHold),
                Case($(CheckedOut), this::toCheckedOutBook)
        );
    }

    private Book toAvailableBook() {
        AvailableState state = new AvailableState(null, new LibraryBranchId(available_at_branch));
        Book book = new Book(new BookId(book_id), book_type, state, new Version(version));
        state.setBook(book);
        return book;
    }

    private Book toBookOnHold() {
        OnHoldState state = new OnHoldState(null, new LibraryBranchId(on_hold_at_branch), 
                new PatronId(on_hold_by_patron), on_hold_till);
        Book book = new Book(new BookId(book_id), book_type, state, new Version(version));
        state.setBook(book);
        return book;
    }

    private Book toCheckedOutBook() {
        CheckedOutState state = new CheckedOutState(null, new LibraryBranchId(checked_out_at_branch), 
                new PatronId(checked_out_by_patron));
        Book book = new Book(new BookId(book_id), book_type, state, new Version(version));
        state.setBook(book);
        return book;
    }
}

