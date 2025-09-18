package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.new_model.Book;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;
import org.junit.Test;
import org.junit.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.*;

public class NewBookDatabaseRepositoryTest {

    private JdbcTemplate jdbcTemplate;

    private NewBookDatabaseRepository repository;
    private BookId bookId;
    private LibraryBranchId branchId;
    private PatronId patronId;

    @Before
    public void setUp() {
        DataSource dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("testdb_" + System.currentTimeMillis())
                .addScript("classpath:create_lending_book_db.sql")
                .ignoreFailedDrops(true)
                .build();
        jdbcTemplate = new JdbcTemplate(dataSource);
        repository = new NewBookDatabaseRepository(jdbcTemplate);
        bookId = new BookId(UUID.randomUUID());
        branchId = new LibraryBranchId(UUID.randomUUID());
        patronId = new PatronId(UUID.randomUUID());
    }

    @Test
    public void shouldSaveAndRetrieveAvailableBook() {
        Book book = new Book(bookId, BookType.Circulating, branchId, new Version(0));
        
        repository.save(book);
        Option<Book> retrieved = repository.findBy(bookId);
        
        assertTrue(retrieved.isDefined());
        Book retrievedBook = retrieved.get();
        assertEquals("AVAILABLE", retrievedBook.getCurrentState());
        assertEquals(bookId, retrievedBook.getBookId());
        assertEquals(BookType.Circulating, retrievedBook.getBookType());
        assertEquals(branchId, retrievedBook.getCurrentBranch());
        assertNull(retrievedBook.getCurrentPatron());
    }

    @Test
    public void shouldSaveAndRetrieveBookOnHold() {
        Book book = new Book(bookId, BookType.Circulating, branchId, new Version(0));
        Instant holdTill = Instant.now().plusSeconds(3600);
        book.placeOnHold(patronId, branchId, holdTill);
        
        repository.save(book);
        Option<Book> retrieved = repository.findBy(bookId);
        
        assertTrue(retrieved.isDefined());
        Book retrievedBook = retrieved.get();
        assertEquals("ON_HOLD", retrievedBook.getCurrentState());
        assertEquals(bookId, retrievedBook.getBookId());
        assertEquals(BookType.Circulating, retrievedBook.getBookType());
        assertEquals(branchId, retrievedBook.getCurrentBranch());
        assertEquals(patronId, retrievedBook.getCurrentPatron());
    }

    @Test
    public void shouldSaveAndRetrieveCheckedOutBook() {
        Book book = new Book(bookId, BookType.Circulating, branchId, new Version(0));
        book.checkout(patronId, branchId);
        
        repository.save(book);
        Option<Book> retrieved = repository.findBy(bookId);
        
        assertTrue(retrieved.isDefined());
        Book retrievedBook = retrieved.get();
        assertEquals("CHECKED_OUT", retrievedBook.getCurrentState());
        assertEquals(bookId, retrievedBook.getBookId());
        assertEquals(BookType.Circulating, retrievedBook.getBookType());
        assertEquals(branchId, retrievedBook.getCurrentBranch());
        assertEquals(patronId, retrievedBook.getCurrentPatron());
    }

    @Test
    public void shouldFindAvailableBookOnly() {
        Book availableBook = new Book(bookId, BookType.Circulating, branchId, new Version(0));
        repository.save(availableBook);
        
        Option<Book> found = repository.findAvailableBookBy(bookId);
        
        assertTrue(found.isDefined());
        assertEquals("AVAILABLE", found.get().getCurrentState());
    }

    @Test
    public void shouldNotFindAvailableBookWhenOnHold() {
        Book book = new Book(bookId, BookType.Circulating, branchId, new Version(0));
        book.placeOnHold(patronId, branchId, Instant.now().plusSeconds(3600));
        repository.save(book);
        
        Option<Book> found = repository.findAvailableBookBy(bookId);
        
        assertTrue(found.isEmpty());
    }

    @Test
    public void shouldFindBookOnHoldForCorrectPatron() {
        Book book = new Book(bookId, BookType.Circulating, branchId, new Version(0));
        book.placeOnHold(patronId, branchId, Instant.now().plusSeconds(3600));
        repository.save(book);
        
        Option<Book> found = repository.findBookOnHold(bookId, patronId);
        
        assertTrue(found.isDefined());
        assertEquals("ON_HOLD", found.get().getCurrentState());
        assertEquals(patronId, found.get().getCurrentPatron());
    }

    @Test
    public void shouldNotFindBookOnHoldForWrongPatron() {
        Book book = new Book(bookId, BookType.Circulating, branchId, new Version(0));
        book.placeOnHold(patronId, branchId, Instant.now().plusSeconds(3600));
        repository.save(book);
        
        PatronId wrongPatronId = new PatronId(UUID.randomUUID());
        
        Option<Book> found = repository.findBookOnHold(bookId, wrongPatronId);
        
        assertTrue(found.isEmpty());
    }
}
