package io.pillopl.library.lending.book.new_model.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.Book;
import io.pillopl.library.lending.book.model.BookRepository;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.book.model.CheckedOutBook;
import io.pillopl.library.lending.book.model.BookInformation;
import io.pillopl.library.lending.book.new_model.OnHoldState;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;

public class BookRepositoryAdapter implements BookRepository {

    private final io.pillopl.library.lending.book.new_model.BookRepository newBookRepository;

    public BookRepositoryAdapter(io.pillopl.library.lending.book.new_model.BookRepository newBookRepository) {
        this.newBookRepository = newBookRepository;
    }

    @Override
    public Option<Book> findBy(BookId bookId) {
        return newBookRepository.findBy(bookId)
                .map(this::convertToOldModel);
    }

    @Override
    public void save(Book book) {
        throw new UnsupportedOperationException("Saving old model books is not supported during migration. Use new model instead.");
    }

    private Book convertToOldModel(io.pillopl.library.lending.book.new_model.Book newBook) {
        BookInformation bookInfo = new BookInformation(newBook.getBookId(), newBook.getBookType());
        LibraryBranchId branch = newBook.getCurrentBranch();
        PatronId patron = newBook.getCurrentPatron();
        Version version = newBook.getVersion();

        switch (newBook.getCurrentState()) {
            case "AVAILABLE":
                return new AvailableBook(bookInfo, branch, version);
            case "ON_HOLD":
                java.time.Instant holdTill = getHoldExpiration(newBook);
                return new BookOnHold(newBook.getBookId(), newBook.getBookType(), branch, patron, holdTill, version);
            case "CHECKED_OUT":
                return new CheckedOutBook(newBook.getBookId(), newBook.getBookType(), branch, patron, version);
            default:
                throw new IllegalStateException("Unknown state: " + newBook.getCurrentState());
        }
    }

    private java.time.Instant getHoldExpiration(io.pillopl.library.lending.book.new_model.Book book) {
        if (book.getState() instanceof OnHoldState) {
            return ((OnHoldState) book.getState()).getHoldTill();
        }
        return java.time.Instant.now().plus(7, java.time.temporal.ChronoUnit.DAYS);
    }
}
