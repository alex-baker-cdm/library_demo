package io.pillopl.library.lending.book.new_model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;

@FunctionalInterface
public interface FindBookOnHoldNewModel {

    Option<Book> findBookOnHold(BookId bookId, PatronId patronId);
}
