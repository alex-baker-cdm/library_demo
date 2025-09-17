package io.pillopl.library.lending.book.application;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.book.new_model.Book;
import io.pillopl.library.lending.book.new_model.BookRepository;
import io.pillopl.library.lending.book.new_model.OnHoldState;
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
        if (book.getState().canBePutOnHold(new PatronId(bookPlacedOnHold.getPatronId()))) {
            book.placeOnHold(
                new PatronId(bookPlacedOnHold.getPatronId()),
                new LibraryBranchId(bookPlacedOnHold.getLibraryBranchId()),
                bookPlacedOnHold.getHoldTill()
            );
            return book;
        } else {
            return raiseDuplicateHoldFoundEvent(book, bookPlacedOnHold);
        }
    }

    private Book raiseDuplicateHoldFoundEvent(Book book, BookPlacedOnHold bookPlacedOnHold) {
        if (book.getState() instanceof OnHoldState) {
            OnHoldState onHoldState = (OnHoldState) book.getState();
            if (onHoldState.getByPatron().equals(new PatronId(bookPlacedOnHold.getPatronId()))) {
                return book;
            }
            domainEvents.publish(
                    new BookDuplicateHoldFound(
                            Instant.now(),
                            onHoldState.getByPatron().getPatronId(),
                            bookPlacedOnHold.getPatronId(),
                            bookPlacedOnHold.getLibraryBranchId(),
                            bookPlacedOnHold.getBookId()));
        }
        return book;
    }


    private Book handleBookHoldExpired(Book book, BookHoldExpired holdExpired) {
        book.expireHold();
        return book;
    }

    private Book handleBookHoldCanceled(Book book, BookHoldCanceled holdCanceled) {
        book.cancelHold();
        return book;
    }

    private Book handleBookCheckedOut(Book book, BookCheckedOut bookCheckedOut) {
        book.checkout(
            new PatronId(bookCheckedOut.getPatronId()),
            new LibraryBranchId(bookCheckedOut.getLibraryBranchId())
        );
        return book;
    }

    private Book handleBookReturned(Book book, BookReturned bookReturned) {
        book.returnBook(new LibraryBranchId(bookReturned.getLibraryBranchId()));
        return book;
    }

    private Book saveBook(Book book) {
        bookRepository.save(book);
        return book;
    }

}
