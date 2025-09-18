package io.pillopl.library.lending.book.new_model;

import io.pillopl.library.catalogue.BookId;
import io.vavr.control.Option;

@FunctionalInterface
public interface FindAvailableBookNewModel {

    Option<Book> findAvailableBookBy(BookId bookId);
}
