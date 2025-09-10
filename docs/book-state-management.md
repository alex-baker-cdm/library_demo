# book-state-management guide

This guide provides a comprehensive overview of book state management in the library system, covering both the original state model and the new state pattern implementation. The library system demonstrates two different architectural approaches to managing book states, each serving specific design purposes within the Domain-Driven Design framework.

## Overview

The library system implements book state management through two distinct approaches:

1. **Original State Model**: Concrete state classes (`AvailableBook`, `BookOnHold`, `CheckedOutBook`) that implement the `Book` interface
2. **New State Pattern**: A `BookState` interface with state implementations (`AvailableState`, `OnHoldState`, `CheckedOutState`) managed by a `Book` aggregate

Both approaches handle the same core business states but differ in their architectural patterns and integration with the domain model.

## Book States

Books in the library system can exist in three primary states:

- **Available**: Book is available for hold or direct checkout
- **On Hold**: Book is reserved by a patron with an expiration date
- **Checked Out**: Book is currently borrowed by a patron

### State Transitions

The following state transitions are valid in the system:

```
Available → On Hold → Checked Out → Available
    ↓           ↓
    ↓      (cancel/expire)
    ↓           ↓
    ←───────────←
```

Additional transition rules:
- Available books can be directly checked out (bypassing hold)
- Holds can be canceled or expire, returning the book to Available
- Only the patron who placed a hold can check out that book
- Books must be returned to become Available again

## Original State Model

The original implementation uses concrete classes that implement the `Book` interface, following an event-driven architecture pattern.

### Book Interface

```java
public interface Book {
    default BookId bookId() {
        return getBookInformation().getBookId();
    }
    
    default BookType type() {
        return getBookInformation().getBookType();
    }
    
    BookInformation getBookInformation();
    Version getVersion();
}
```

### State Classes

#### AvailableBook

Represents a book that is available for hold or checkout:

```java
public class AvailableBook implements Book {
    private final BookInformation bookInformation;
    private final LibraryBranchId libraryBranch;
    private final Version version;
    
    public BookOnHold handle(BookPlacedOnHold bookPlacedOnHold) {
        return new BookOnHold(
            bookInformation,
            new LibraryBranchId(bookPlacedOnHold.getLibraryBranchId()),
            new PatronId(bookPlacedOnHold.getPatronId()),
            bookPlacedOnHold.getHoldTill(),
            version);
    }
    
    public boolean isRestricted() {
        return bookInformation.getBookType().equals(BookType.Restricted);
    }
}
```

#### BookOnHold

Represents a book that is on hold by a patron:

```java
public class BookOnHold implements Book {
    private final BookInformation bookInformation;
    private final LibraryBranchId holdPlacedAt;
    private final PatronId byPatron;
    private final Instant holdTill;
    private final Version version;
    
    public CheckedOutBook handle(BookCheckedOut bookCheckedOut) {
        return new CheckedOutBook(
            bookInformation,
            new LibraryBranchId(bookCheckedOut.getLibraryBranchId()),
            new PatronId(bookCheckedOut.getPatronId()),
            version);
    }
    
    public AvailableBook handle(BookHoldCanceled bookHoldCanceled) {
        return new AvailableBook(
            bookInformation, 
            new LibraryBranchId(bookHoldCanceled.getLibraryBranchId()),
            version);
    }
    
    public AvailableBook handle(BookHoldExpired bookHoldExpired) {
        return new AvailableBook(
            bookInformation,
            new LibraryBranchId(bookHoldExpired.getLibraryBranchId()),
            version);
    }
}
```

#### CheckedOutBook

Represents a book that is currently checked out:

```java
public class CheckedOutBook implements Book {
    private final BookInformation bookInformation;
    private final LibraryBranchId checkedOutAt;
    private final PatronId byPatron;
    private final Version version;
    
    public AvailableBook handle(BookReturned bookReturnedByPatron) {
        return new AvailableBook(
            bookInformation,
            new LibraryBranchId(bookReturnedByPatron.getLibraryBranchId()),
            version);
    }
}
```

### Event-Driven State Transitions

The original model integrates with domain events through the `PatronEventsHandler`, which uses pattern matching to handle state transitions:

```java
public class PatronEventsHandler {
    private Book handleBookPlacedOnHold(Book book, BookPlacedOnHold bookPlacedOnHold) {
        return API.Match(book).of(
            Case($(instanceOf(AvailableBook.class)), 
                 availableBook -> availableBook.handle(bookPlacedOnHold)),
            Case($(instanceOf(BookOnHold.class)), 
                 bookOnHold -> raiseDuplicateHoldFoundEvent(bookOnHold, bookPlacedOnHold)),
            Case($(), () -> book)
        );
    }
    
    private Book handleBookCheckedOut(Book book, BookCheckedOut bookCheckedOut) {
        return API.Match(book).of(
            Case($(instanceOf(BookOnHold.class)), 
                 onHold -> onHold.handle(bookCheckedOut)),
            Case($(), () -> book)
        );
    }
}
```

## New State Pattern Implementation

The new implementation follows the classic State pattern with a `BookState` interface and concrete state implementations.

### BookState Interface

```java
public interface BookState {
    // Core state transitions
    BookState placeOnHold(PatronId patronId, LibraryBranchId branchId, Instant holdTill);
    BookState checkout(PatronId patronId, LibraryBranchId branchId);
    BookState returnBook(LibraryBranchId branchId);
    BookState cancelHold();
    BookState expireHold();
    
    // Validation methods
    boolean canBeCheckedOut(PatronId patronId);
    boolean canBePutOnHold(PatronId patronId);
    boolean canBeReturned();
    
    // State information
    String getStateName();
    Version getVersion();
    LibraryBranchId getCurrentBranch();
    PatronId getCurrentPatron();
    
    // Default implementations for invalid state transitions
    default BookState invalidTransition(String message) {
        throw new IllegalStateException("Invalid state transition: " + message);
    }
}
```

### State Implementations

#### AvailableState

```java
public class AvailableState implements BookState {
    private final Book book;
    private final LibraryBranchId branch;
    
    @Override
    public BookState placeOnHold(PatronId patronId, LibraryBranchId branchId, Instant holdTill) {
        return new OnHoldState(book, branchId, patronId, holdTill);
    }
    
    @Override
    public BookState checkout(PatronId patronId, LibraryBranchId branchId) {
        return new CheckedOutState(book, branchId, patronId);
    }
    
    @Override
    public boolean canBeCheckedOut(PatronId patronId) {
        return true;
    }
    
    @Override
    public boolean canBePutOnHold(PatronId patronId) {
        return true;
    }
}
```

#### OnHoldState

```java
public class OnHoldState implements BookState {
    private final Book book;
    private final LibraryBranchId holdPlacedAt;
    private final PatronId byPatron;
    private final Instant holdTill;
    
    @Override
    public BookState checkout(PatronId patronId, LibraryBranchId branchId) {
        if (!patronId.equals(byPatron)) {
            return invalidTransition("Only the patron who placed the hold can check out the book");
        }
        return new CheckedOutState(book, branchId, patronId);
    }
    
    @Override
    public BookState cancelHold() {
        return new AvailableState(book, holdPlacedAt);
    }
    
    @Override
    public BookState expireHold() {
        if (Instant.now().isAfter(holdTill)) {
            return new AvailableState(book, holdPlacedAt);
        }
        return this;
    }
    
    @Override
    public boolean canBeCheckedOut(PatronId patronId) {
        return patronId.equals(byPatron);
    }
}
```

#### CheckedOutState

```java
public class CheckedOutState implements BookState {
    private final Book book;
    private final LibraryBranchId checkedOutAt;
    private final PatronId byPatron;
    
    @Override
    public BookState returnBook(LibraryBranchId branchId) {
        return new AvailableState(book, branchId);
    }
    
    @Override
    public boolean canBeReturned() {
        return true;
    }
    
    @Override
    public boolean canBeCheckedOut(PatronId patronId) {
        return false;
    }
    
    @Override
    public boolean canBePutOnHold(PatronId patronId) {
        return false;
    }
}
```

### Book Aggregate

The `Book` aggregate manages state transitions through the State pattern:

```java
public class Book {
    private final BookId bookId;
    private final BookType bookType;
    private BookState state;
    private Version version;
    
    public Book(BookId bookId, BookType bookType, LibraryBranchId branch, Version version) {
        this.bookId = bookId;
        this.bookType = bookType;
        this.version = version;
        this.state = new AvailableState(this, branch);
    }
    
    public void placeOnHold(PatronId patronId, LibraryBranchId branchId, Instant holdTill) {
        if (state.canBePutOnHold(patronId)) {
            state = state.placeOnHold(patronId, branchId, holdTill);
            version = version.next();
        }
    }
    
    public void checkout(PatronId patronId, LibraryBranchId branchId) {
        if (state.canBeCheckedOut(patronId)) {
            state = state.checkout(patronId, branchId);
            version = version.next();
        }
    }
    
    public void returnBook(LibraryBranchId branchId) {
        if (state.canBeReturned()) {
            state = state.returnBook(branchId);
            version = version.next();
        }
    }
}
```

## Business Rules and Validation

Based on the domain analysis documented in the design-level EventStorming, the following business rules apply to book state transitions:

### Hold Placement Rules

- Book must be available (not on hold or checked out)
- For regular patrons:
  - Cannot hold restricted books
  - Cannot have more than 4 existing holds
  - Cannot have more than 1 overdue checkout
  - Cannot place open-ended holds
- For researcher patrons:
  - Can hold restricted books
  - Can place open-ended holds
  - Cannot have more than 2 overdue checkouts

### Checkout Rules

- Book must be on hold by the requesting patron, OR
- Book must be available for direct checkout
- Patron cannot have excessive overdue checkouts

### Return Rules

- Only checked out books can be returned
- Returns trigger overdue checkout unregistration if applicable
- Returns may trigger fee application processes

### Hold Expiration Rules

- Close-ended holds expire automatically based on daily batch processing
- Open-ended holds (researcher patrons) do not expire
- Expired holds return books to available state

## Persistence Layer

The `BookDatabaseRepository` handles persistence for the original state model using optimistic locking:

```java
public class BookDatabaseRepository implements BookRepository {
    private int updateOptimistically(Book book) {
        int result = Match(book).of(
            Case($(instanceOf(AvailableBook.class)), this::update),
            Case($(instanceOf(BookOnHold.class)), this::update),
            Case($(instanceOf(CheckedOutBook.class)), this::update)
        );
        if (result == 0) {
            throw new AggregateRootIsStale("Someone has updated book in the meantime, book: " + book);
        }
        return result;
    }
}
```

The repository uses pattern matching to handle different state types and maintains version control for concurrent access protection.

### Database Schema

The `book_database_entity` table stores book state information:

- `book_state`: Enum value (Available, OnHold, CheckedOut)
- `available_at_branch`: Branch ID when available
- `on_hold_at_branch`: Branch ID where hold was placed
- `on_hold_by_patron`: Patron ID who placed the hold
- `on_hold_till`: Hold expiration timestamp
- `checked_out_at_branch`: Branch ID where checkout occurred
- `checked_out_by_patron`: Patron ID who checked out the book
- `version`: Optimistic locking version

## Architectural Comparison

### Original State Model

**Advantages:**
- Event-driven architecture integration
- Immutable state objects
- Clear separation of concerns
- Production-ready with existing infrastructure

**Use Cases:**
- Event sourcing architectures
- Systems requiring audit trails
- Complex domain event workflows
- Existing systems with established patterns

### New State Pattern

**Advantages:**
- Classic GoF State pattern implementation
- Centralized state management in aggregate
- Clear state transition validation
- Easier to extend with new states

**Use Cases:**
- Clean architecture implementations
- Systems with complex state validation
- New greenfield projects
- Teaching/demonstration purposes

## Integration with Domain Events

The system publishes domain events for state transitions:

- `BookPlacedOnHold`: When a book is placed on hold
- `BookCheckedOut`: When a book is checked out
- `BookReturned`: When a book is returned
- `BookHoldCanceled`: When a hold is canceled
- `BookHoldExpired`: When a hold expires
- `BookDuplicateHoldFound`: When duplicate holds are detected

These events are handled by the `PatronEventsHandler` to maintain consistency between patron and book aggregates.

## Daily Batch Processing

The system includes daily batch processes for:

- **Hold Expiration**: Checking `HoldsToExpireSheet` for expired holds
- **Overdue Detection**: Checking `CheckoutsToOverdueSheet` for overdue books

These processes ensure that business rules around time-based state transitions are enforced automatically.

## Configuration

The book state management is configured through Spring configuration:

```java
@Configuration
public class BookConfiguration {
    @Bean
    PatronEventsHandler bookEventsHandler(BookRepository bookRepository, DomainEvents domainEvents) {
        return new PatronEventsHandler(bookRepository, domainEvents);
    }
    
    @Bean
    BookDatabaseRepository bookDatabaseRepository(JdbcTemplate jdbcTemplate) {
        return new BookDatabaseRepository(jdbcTemplate);
    }
}
```

## Conclusion

The library system demonstrates two effective approaches to state management, each serving different architectural needs. The original model provides robust event-driven capabilities suitable for production systems, while the new state pattern offers cleaner state management for modern domain-driven designs. Understanding both approaches helps developers choose the appropriate pattern based on their specific requirements and architectural constraints.

The choice between approaches should consider factors such as:
- Existing system architecture
- Event sourcing requirements
- Team familiarity with patterns
- Performance characteristics
- Maintenance and extensibility needs

Both implementations maintain the same business rules and state transition logic, ensuring consistency regardless of the chosen architectural approach.
