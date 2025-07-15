package io.pillopl.library.lending.book.application;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.book.model.*;
import io.pillopl.library.lending.book.new_model.Book;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.*;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.API;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;

import java.time.Instant;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@AllArgsConstructor
public class PatronEventsHandler {

    private final BookRepository bookRepository;
    private final DomainEvents domainEvents;

    @EventListener
    void handle(BookPlacedOnHold bookPlacedOnHold) {
        bookRepository.findBy(new BookId(bookPlacedOnHold.getBookId()))
                .map(book -> handleBookPlacedOnHold(book, bookPlacedOnHold))
                .map(this::saveBook);
    }

    @EventListener
    void handle(BookCheckedOut bookCheckedOut) {
        bookRepository.findBy(new BookId(bookCheckedOut.getBookId()))
                .map(book -> handleBookCheckedOut(book, bookCheckedOut))
                .map(this::saveBook);
    }

    @EventListener
    void handle(BookHoldExpired holdExpired) {
        bookRepository.findBy(new BookId(holdExpired.getBookId()))
                .map(book -> handleBookHoldExpired(book, holdExpired))
                .map(this::saveBook);
    }

    @EventListener
    void handle(BookHoldCanceled holdCanceled) {
        bookRepository.findBy(new BookId(holdCanceled.getBookId()))
                .map(book -> handleBookHoldCanceled(book,  holdCanceled))
                .map(this::saveBook);
    }

    @EventListener
    void handle(BookReturned bookReturned) {
        bookRepository.findBy(new BookId(bookReturned.getBookId()))
                .map(book -> handleBookReturned(book, bookReturned))
                .map(this::saveBook);
    }


    private io.pillopl.library.lending.book.model.Book handleBookPlacedOnHold(io.pillopl.library.lending.book.model.Book book, BookPlacedOnHold bookPlacedOnHold) {
        if (book instanceof Book) {
            Book newBook = (Book) book;
            if ("AVAILABLE".equals(newBook.getCurrentState())) {
                newBook.placeOnHold(new PatronId(bookPlacedOnHold.getPatronId()), 
                        new LibraryBranchId(bookPlacedOnHold.getLibraryBranchId()), 
                        bookPlacedOnHold.getHoldTill());
                return newBook;
            } else if ("ON_HOLD".equals(newBook.getCurrentState())) {
                return raiseDuplicateHoldFoundEvent(newBook, bookPlacedOnHold);
            }
        }
        return book;
    }

    private Book raiseDuplicateHoldFoundEvent(Book onHold, BookPlacedOnHold bookPlacedOnHold) {
        if(onHold.getCurrentPatron() != null && onHold.getCurrentPatron().equals(new PatronId(bookPlacedOnHold.getPatronId()))) {
            return onHold;
        }
        domainEvents.publish(
                new BookDuplicateHoldFound(
                        Instant.now(),
                        onHold.getCurrentPatron().getPatronId(),
                        bookPlacedOnHold.getPatronId(),
                        bookPlacedOnHold.getLibraryBranchId(),
                        bookPlacedOnHold.getBookId()));
        return onHold;
    }


    private io.pillopl.library.lending.book.model.Book handleBookHoldExpired(io.pillopl.library.lending.book.model.Book book, BookHoldExpired holdExpired) {
        if (book instanceof Book) {
            Book newBook = (Book) book;
            if ("ON_HOLD".equals(newBook.getCurrentState())) {
                newBook.expireHold();
            }
        }
        return book;
    }

    private io.pillopl.library.lending.book.model.Book handleBookHoldCanceled(io.pillopl.library.lending.book.model.Book book, BookHoldCanceled holdCanceled) {
        if (book instanceof Book) {
            Book newBook = (Book) book;
            if ("ON_HOLD".equals(newBook.getCurrentState())) {
                newBook.cancelHold();
            }
        }
        return book;
    }

    private io.pillopl.library.lending.book.model.Book handleBookCheckedOut(io.pillopl.library.lending.book.model.Book book, BookCheckedOut bookCheckedOut) {
        if (book instanceof Book) {
            Book newBook = (Book) book;
            if ("ON_HOLD".equals(newBook.getCurrentState())) {
                newBook.checkout(new PatronId(bookCheckedOut.getPatronId()), 
                        new LibraryBranchId(bookCheckedOut.getLibraryBranchId()));
            }
        }
        return book;
    }

    private io.pillopl.library.lending.book.model.Book handleBookReturned(io.pillopl.library.lending.book.model.Book book, BookReturned bookReturned) {
        if (book instanceof Book) {
            Book newBook = (Book) book;
            if ("CHECKED_OUT".equals(newBook.getCurrentState())) {
                newBook.returnBook(new LibraryBranchId(bookReturned.getLibraryBranchId()));
            }
        }
        return book;
    }

    private io.pillopl.library.lending.book.model.Book saveBook(io.pillopl.library.lending.book.model.Book book) {
        bookRepository.save(book);
        return book;
    }

}
