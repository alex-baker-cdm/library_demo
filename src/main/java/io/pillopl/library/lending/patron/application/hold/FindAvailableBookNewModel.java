package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.new_model.Book;
import io.vavr.control.Option;

@FunctionalInterface
public interface FindAvailableBookNewModel {

    Option<Book> findAvailableBookBy(BookId bookId);
}
