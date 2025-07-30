package io.pillopl.library.lending.book.new_model.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookInformation;
import io.pillopl.library.lending.patron.application.hold.FindAvailableBook;
import io.vavr.control.Option;

public class FindAvailableBookAdapter implements FindAvailableBook {

    private final io.pillopl.library.lending.book.new_model.BookRepository newBookRepository;

    public FindAvailableBookAdapter(io.pillopl.library.lending.book.new_model.BookRepository newBookRepository) {
        this.newBookRepository = newBookRepository;
    }

    @Override
    public Option<AvailableBook> findAvailableBookBy(BookId bookId) {
        return newBookRepository.findBy(bookId)
                .filter(book -> "AVAILABLE".equals(book.getCurrentState()))
                .map(this::convertToAvailableBook);
    }

    private AvailableBook convertToAvailableBook(io.pillopl.library.lending.book.new_model.Book newBook) {
        BookInformation bookInfo = new BookInformation(newBook.getBookId(), newBook.getBookType());
        return new AvailableBook(bookInfo, newBook.getCurrentBranch(), newBook.getVersion());
    }
}
