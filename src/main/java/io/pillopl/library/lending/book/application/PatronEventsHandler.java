package io.pillopl.library.lending.book.application;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.book.new_model.*;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.*;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.API;

import java.time.Instant;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

public class PatronEventsHandler {

    private final BookRepository bookRepository;
    private final DomainEvents domainEvents;

    public PatronEventsHandler(BookRepository bookRepository, DomainEvents domainEvents) {
        this.bookRepository = bookRepository;
        this.domainEvents = domainEvents;
    }

    void handle(BookPlacedOnHold bookPlacedOnHold) {
        bookRepository.findBy(new BookId(bookPlacedOnHold.getBookId()))
                .map(book -> handleBookPlacedOnHold(book, bookPlacedOnHold))
                .map(this::saveBook);
    }

    void handle(BookCheckedOut bookCheckedOut) {
        bookRepository.findBy(new BookId(bookCheckedOut.getBookId()))
                .map(book -> handleBookCheckedOut(book, bookCheckedOut))
                .map(this::saveBook);
    }

    void handle(BookHoldExpired holdExpired) {
        bookRepository.findBy(new BookId(holdExpired.getBookId()))
                .map(book -> handleBookHoldExpired(book, holdExpired))
                .map(this::saveBook);
    }

    void handle(BookHoldCanceled holdCanceled) {
        bookRepository.findBy(new BookId(holdCanceled.getBookId()))
                .map(book -> handleBookHoldCanceled(book,  holdCanceled))
                .map(this::saveBook);
    }

    void handle(BookReturned bookReturned) {
        bookRepository.findBy(new BookId(bookReturned.getBookId()))
                .map(book -> handleBookReturned(book, bookReturned))
                .map(this::saveBook);
    }


    private Book handleBookPlacedOnHold(Book book, BookPlacedOnHold bookPlacedOnHold) {
        if (book.getCurrentState().equals("AVAILABLE")) {
            book.placeOnHold(new PatronId(bookPlacedOnHold.getPatronId()), new LibraryBranchId(bookPlacedOnHold.getLibraryBranchId()), bookPlacedOnHold.getHoldTill());
            return book;
        } else if (book.getCurrentState().equals("ON_HOLD")) {
            return raiseDuplicateHoldFoundEvent(book, bookPlacedOnHold);
        }
        return book;
    }

    private Book raiseDuplicateHoldFoundEvent(Book book, BookPlacedOnHold bookPlacedOnHold) {
        if(book.getCurrentPatron() != null && book.getCurrentPatron().equals(new PatronId(bookPlacedOnHold.getPatronId()))) {
            return book;
        }
        domainEvents.publish(
                new BookDuplicateHoldFound(
                        Instant.now(),
                        book.getCurrentPatron() != null ? book.getCurrentPatron().getPatronId() : null,
                        bookPlacedOnHold.getPatronId(),
                        bookPlacedOnHold.getLibraryBranchId(),
                        bookPlacedOnHold.getBookId()));
        return book;
    }


    private Book handleBookHoldExpired(Book book, BookHoldExpired holdExpired) {
        if (book.getCurrentState().equals("ON_HOLD")) {
            book.expireHold();
            return book;
        }
        return book;
    }

    private Book handleBookHoldCanceled(Book book, BookHoldCanceled holdCanceled) {
        if (book.getCurrentState().equals("ON_HOLD")) {
            book.cancelHold();
            return book;
        }
        return book;
    }

    private Book handleBookCheckedOut(Book book, BookCheckedOut bookCheckedOut) {
        if (book.getCurrentState().equals("ON_HOLD")) {
            book.checkout(new PatronId(bookCheckedOut.getPatronId()), new LibraryBranchId(bookCheckedOut.getLibraryBranchId()));
            return book;
        }
        return book;
    }

    private Book handleBookReturned(Book book, BookReturned bookReturned) {
        if (book.getCurrentState().equals("CHECKED_OUT")) {
            book.returnBook(new LibraryBranchId(bookReturned.getLibraryBranchId()));
            return book;
        }
        return book;
    }

    private Book saveBook(Book book) {
        bookRepository.save(book);
        return book;
    }

}
