package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.BookAdapter;
import io.pillopl.library.lending.book.new_model.BookRepository;
import io.vavr.control.Option;

class NewModelBookRepositoryImpl implements BookRepository {

    private final BookDatabaseRepository databaseRepository;

    NewModelBookRepositoryImpl(BookDatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    @Override
    public Option<io.pillopl.library.lending.book.new_model.Book> findBy(BookId bookId) {
        return databaseRepository.findBy(bookId)
                .map(BookAdapter::toNewModel);
    }

    @Override
    public void save(io.pillopl.library.lending.book.new_model.Book book) {
        io.pillopl.library.lending.book.model.Book oldModelBook = BookAdapter.toOldModel(book);
        databaseRepository.save(oldModelBook);
    }
}
