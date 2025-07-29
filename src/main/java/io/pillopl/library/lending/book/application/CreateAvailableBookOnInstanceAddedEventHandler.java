package io.pillopl.library.lending.book.application;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookInstanceAddedToCatalogue;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.new_model.Book;
import io.pillopl.library.lending.book.new_model.BookRepository;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

import java.util.UUID;

public class CreateAvailableBookOnInstanceAddedEventHandler {

    private final BookRepository bookRepository;

    public CreateAvailableBookOnInstanceAddedEventHandler(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    void handle(BookInstanceAddedToCatalogue event) {
        Book book = new Book(
                new BookId(event.getBookId()),
                event.getType(),
                ourLibraryBranch(),
                Version.zero()
        );
        bookRepository.save(book);
    }

    private LibraryBranchId ourLibraryBranch() {
        //from properties
        return new LibraryBranchId(UUID.randomUUID());
    }


}
