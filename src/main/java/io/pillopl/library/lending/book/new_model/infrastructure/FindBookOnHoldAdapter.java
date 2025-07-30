package io.pillopl.library.lending.book.new_model.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.book.model.BookInformation;
import io.pillopl.library.lending.book.new_model.OnHoldState;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;

public class FindBookOnHoldAdapter implements FindBookOnHold {

    private final io.pillopl.library.lending.book.new_model.BookRepository newBookRepository;

    public FindBookOnHoldAdapter(io.pillopl.library.lending.book.new_model.BookRepository newBookRepository) {
        this.newBookRepository = newBookRepository;
    }

    @Override
    public Option<BookOnHold> findBookOnHold(BookId bookId, PatronId patronId) {
        return newBookRepository.findBy(bookId)
                .filter(book -> "ON_HOLD".equals(book.getCurrentState()))
                .filter(book -> patronId.equals(book.getCurrentPatron()))
                .map(this::convertToBookOnHold);
    }

    private BookOnHold convertToBookOnHold(io.pillopl.library.lending.book.new_model.Book newBook) {
        PatronId patron = newBook.getCurrentPatron();
        java.time.Instant holdTill = java.time.Instant.now().plus(7, java.time.temporal.ChronoUnit.DAYS);
        
        if (newBook.getState() instanceof OnHoldState) {
            holdTill = ((OnHoldState) newBook.getState()).getHoldTill();
        }
        
        return new BookOnHold(newBook.getBookId(), newBook.getBookType(), newBook.getCurrentBranch(), patron, holdTill, newBook.getVersion());
    }
}
