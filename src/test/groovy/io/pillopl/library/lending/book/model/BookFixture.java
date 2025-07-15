package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.new_model.Book;
import io.pillopl.library.lending.book.new_model.AvailableState;
import io.pillopl.library.lending.book.new_model.OnHoldState;
import io.pillopl.library.lending.book.new_model.CheckedOutState;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.catalogue.BookType.Circulating;
import static io.pillopl.library.catalogue.BookType.Restricted;
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch;

public class BookFixture {

    public static Book bookOnHold(BookId bookId, LibraryBranchId libraryBranchId) {
        OnHoldState state = new OnHoldState(null, libraryBranchId, anyPatronId(), Instant.now());
        Book book = new Book(bookId, Circulating, state, version0());
        state.setBook(book);
        return book;
    }

    public static Book circulatingBook() {
        AvailableState state = new AvailableState(null, anyBranch());
        Book book = new Book(anyBookId(), Circulating, state, version0());
        state.setBook(book);
        return book;
    }

    public static Book bookOnHold() {
        OnHoldState state = new OnHoldState(null, anyBranch(), anyPatronId(), Instant.now());
        Book book = new Book(anyBookId(), Circulating, state, version0());
        state.setBook(book);
        return book;
    }

    public static Book circulatingAvailableBookAt(LibraryBranchId libraryBranchId) {
        AvailableState state = new AvailableState(null, libraryBranchId);
        Book book = new Book(anyBookId(), Circulating, state, version0());
        state.setBook(book);
        return book;
    }

    public static Book circulatingAvailableBookAt(BookId bookId, LibraryBranchId libraryBranchId) {
        AvailableState state = new AvailableState(null, libraryBranchId);
        Book book = new Book(bookId, Circulating, state, version0());
        state.setBook(book);
        return book;
    }

    public static Book aBookAt(LibraryBranchId libraryBranchId) {
        AvailableState state = new AvailableState(null, libraryBranchId);
        Book book = new Book(anyBookId(), Circulating, state, version0());
        state.setBook(book);
        return book;
    }

    public static Version version0() {
        return new Version(0);
    }

    public static Book circulatingAvailableBook() {
        return circulatingAvailableBookAt(anyBranch());
    }

    public static Book checkedOutBook() {
        CheckedOutState state = new CheckedOutState(null, anyBranch(), anyPatronId());
        Book book = new Book(anyBookId(), Circulating, state, version0());
        state.setBook(book);
        return book;
    }

    public static Book restrictedBook() {
        AvailableState state = new AvailableState(null, anyBranch());
        Book book = new Book(anyBookId(), Restricted, state, version0());
        state.setBook(book);
        return book;
    }

    public static BookId anyBookId() {
        return new BookId(UUID.randomUUID());
    }


    private static PatronId anyPatronId() {
        return new PatronId(UUID.randomUUID());
    }


}
