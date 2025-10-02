package io.pillopl.library.lending.book.application;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.book.new_model.*;
import io.pillopl.library.lending.book.model.BookDuplicateHoldFound;
import io.pillopl.library.lending.patron.model.PatronEvent.*;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

import java.time.Instant;

public class PatronEventsHandler {

    private final io.pillopl.library.lending.book.new_model.BookRepository bookRepository;
    private final DomainEvents domainEvents;

    public PatronEventsHandler(io.pillopl.library.lending.book.new_model.BookRepository bookRepository, DomainEvents domainEvents) {
        this.bookRepository = bookRepository;
        this.domainEvents = domainEvents;
    }

    void handle(BookPlacedOnHold bookPlacedOnHold) {
        bookRepository.findBy(new BookId(bookPlacedOnHold.getBookId()))
                .peek(book -> handleBookPlacedOnHold(book, bookPlacedOnHold))
                .peek(this::saveBook);
    }

    void handle(BookCheckedOut bookCheckedOut) {
        bookRepository.findBy(new BookId(bookCheckedOut.getBookId()))
                .peek(book -> handleBookCheckedOut(book, bookCheckedOut))
                .peek(this::saveBook);
    }

    void handle(BookHoldExpired holdExpired) {
        bookRepository.findBy(new BookId(holdExpired.getBookId()))
                .peek(book -> handleBookHoldExpired(book, holdExpired))
                .peek(this::saveBook);
    }

    void handle(BookHoldCanceled holdCanceled) {
        bookRepository.findBy(new BookId(holdCanceled.getBookId()))
                .peek(book -> handleBookHoldCanceled(book, holdCanceled))
                .peek(this::saveBook);
    }

    void handle(BookReturned bookReturned) {
        bookRepository.findBy(new BookId(bookReturned.getBookId()))
                .peek(book -> handleBookReturned(book, bookReturned))
                .peek(this::saveBook);
    }


    private void handleBookPlacedOnHold(Book book, BookPlacedOnHold bookPlacedOnHold) {
        BookState state = book.getState();
        
        if (state instanceof OnHoldState) {
            OnHoldState onHoldState = (OnHoldState) state;
            PatronId requestingPatron = new PatronId(bookPlacedOnHold.getPatronId());
            
            if (!onHoldState.getByPatron().equals(requestingPatron)) {
                domainEvents.publish(
                    new BookDuplicateHoldFound(
                        Instant.now(),
                        onHoldState.getByPatron().getPatronId(),
                        bookPlacedOnHold.getPatronId(),
                        bookPlacedOnHold.getLibraryBranchId(),
                        bookPlacedOnHold.getBookId()));
            }
        } else {
            book.placeOnHold(
                new PatronId(bookPlacedOnHold.getPatronId()),
                new LibraryBranchId(bookPlacedOnHold.getLibraryBranchId()),
                bookPlacedOnHold.getHoldTill());
        }
    }

    private void handleBookHoldExpired(Book book, BookHoldExpired holdExpired) {
        book.expireHold();
    }

    private void handleBookHoldCanceled(Book book, BookHoldCanceled holdCanceled) {
        book.cancelHold();
    }

    private void handleBookCheckedOut(Book book, BookCheckedOut bookCheckedOut) {
        book.checkout(
            new PatronId(bookCheckedOut.getPatronId()),
            new LibraryBranchId(bookCheckedOut.getLibraryBranchId()));
    }

    private void handleBookReturned(Book book, BookReturned bookReturned) {
        book.returnBook(new LibraryBranchId(bookReturned.getLibraryBranchId()));
    }

    private void saveBook(Book book) {
        bookRepository.save(book);
    }

}
