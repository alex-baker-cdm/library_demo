package io.pillopl.library.lending.book.application;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.book.model.*;
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
        return API.Match(book).of(
                Case($(instanceOf(AvailableBook.class)), availableBook -> availableBook.handle(bookPlacedOnHold)),
                Case($(instanceOf(BookOnHold.class)), bookOnHold -> raiseDuplicateHoldFoundEvent(bookOnHold, bookPlacedOnHold)),
                Case($(), () -> book)
        );
    }

    private BookOnHold raiseDuplicateHoldFoundEvent(BookOnHold onHold, BookPlacedOnHold bookPlacedOnHold) {
        if(onHold.by(new PatronId(bookPlacedOnHold.getPatronId()))) {
            return onHold;
        }
        domainEvents.publish(
                new BookDuplicateHoldFound(
                        Instant.now(),
                        onHold.getByPatron().getPatronId(),
                        bookPlacedOnHold.getPatronId(),
                        bookPlacedOnHold.getLibraryBranchId(),
                        bookPlacedOnHold.getBookId()));
        return onHold;
    }


    private Book handleBookHoldExpired(Book book, BookHoldExpired holdExpired) {
        return API.Match(book).of(
                Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(holdExpired)),
                Case($(), () -> book)
        );
    }

    private Book handleBookHoldCanceled(Book book, BookHoldCanceled holdCanceled) {
        return API.Match(book).of(
                Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(holdCanceled)),
                Case($(), () -> book)
        );
    }

    private Book handleBookCheckedOut(Book book, BookCheckedOut bookCheckedOut) {
        return API.Match(book).of(
                Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(bookCheckedOut)),
                Case($(), () -> book)
        );
    }

    private Book handleBookReturned(Book book, BookReturned bookReturned) {
        return API.Match(book).of(
                Case($(instanceOf(CheckedOutBook.class)), checkedOut -> checkedOut.handle(bookReturned)),
                Case($(), () -> book)
        );
    }

    private Book saveBook(Book book) {
        bookRepository.save(book);
        return book;
    }

}
