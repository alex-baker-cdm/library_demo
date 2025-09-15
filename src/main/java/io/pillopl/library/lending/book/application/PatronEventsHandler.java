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
        PatronId patronId = new PatronId(bookPlacedOnHold.getPatronId());
        LibraryBranchId branchId = new LibraryBranchId(bookPlacedOnHold.getLibraryBranchId());
        
        if (book.getState().canBePutOnHold(patronId)) {
            book.placeOnHold(patronId, branchId, bookPlacedOnHold.getHoldTill());
        } else {
            if ("ON_HOLD".equals(book.getCurrentState()) && 
                !patronId.equals(book.getCurrentPatron())) {
                domainEvents.publish(
                        new BookDuplicateHoldFound(
                                Instant.now(),
                                book.getCurrentPatron().getPatronId(),
                                bookPlacedOnHold.getPatronId(),
                                bookPlacedOnHold.getLibraryBranchId(),
                                bookPlacedOnHold.getBookId()));
            }
        }
        return book;
    }


    private Book handleBookHoldExpired(Book book, BookHoldExpired holdExpired) {
        if ("ON_HOLD".equals(book.getCurrentState())) {
            book.expireHold();
        }
        return book;
    }

    private Book handleBookHoldCanceled(Book book, BookHoldCanceled holdCanceled) {
        if ("ON_HOLD".equals(book.getCurrentState())) {
            book.cancelHold();
        }
        return book;
    }

    private Book handleBookCheckedOut(Book book, BookCheckedOut bookCheckedOut) {
        PatronId patronId = new PatronId(bookCheckedOut.getPatronId());
        LibraryBranchId branchId = new LibraryBranchId(bookCheckedOut.getLibraryBranchId());
        
        if (book.getState().canBeCheckedOut(patronId)) {
            book.checkout(patronId, branchId);
        }
        return book;
    }

    private Book handleBookReturned(Book book, BookReturned bookReturned) {
        LibraryBranchId branchId = new LibraryBranchId(bookReturned.getLibraryBranchId());
        
        if (book.getState().canBeReturned()) {
            book.returnBook(branchId);
        }
        return book;
    }

    private Book saveBook(Book book) {
        bookRepository.save(book);
        return book;
    }

}
