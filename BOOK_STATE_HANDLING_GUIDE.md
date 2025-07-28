# Book State Handling Guide - Library Demo Application

## Overview

The Library Demo application implements a sophisticated book state management system using the **State Machine Pattern**. This guide provides a comprehensive overview of how book states are handled, including state transitions, validation rules, and integration with the event-driven architecture.

## Architecture Overview

The book state handling system is built around two main implementations:

1. **New Model** (`/new_model/`) - State Machine Pattern implementation
2. **Legacy Model** (`/model/`) - Event-driven implementation

This guide focuses primarily on the **New Model** which represents the current best practice implementation.

## Core Components

### 1. BookState Interface

The `BookState` interface defines the contract for all book states:

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
}
```

**Key Features:**
- **State Transitions**: Methods that return new state instances
- **Validation**: Methods to check if operations are allowed
- **State Information**: Methods to query current state details
- **Error Handling**: Default `invalidTransition()` method for illegal operations

### 2. Book Aggregate

The `Book` class serves as the aggregate root that coordinates state transitions:

```java
public class Book {
    private final BookId bookId;
    private final BookType bookType;
    private BookState state;
    private Version version;
    
    // State transition methods with validation
    public void placeOnHold(PatronId patronId, LibraryBranchId branchId, Instant holdTill) {
        if (state.canBePutOnHold(patronId)) {
            state = state.placeOnHold(patronId, branchId, holdTill);
            version = version.next();
        }
    }
}
```

**Key Responsibilities:**
- **Version Control**: Increments version on each state change
- **Validation**: Checks if transitions are allowed before executing
- **State Coordination**: Delegates operations to current state
- **Immutability**: Creates new state instances rather than modifying existing ones

## State Implementations

### 1. AvailableState

**Purpose**: Represents a book that is available for checkout or hold placement.

**Allowed Operations:**
- ✅ Place on hold → transitions to `OnHoldState`
- ✅ Direct checkout → transitions to `CheckedOutState`
- ❌ Return book (invalid - book is not checked out)
- ❌ Cancel hold (invalid - no hold exists)
- ❌ Expire hold (invalid - no hold exists)

**Business Rules:**
- Any patron can place a hold on an available book
- Any patron can directly check out an available book
- Book remains at its current library branch

**Code Example:**
```java
@Override
public BookState placeOnHold(PatronId patronId, LibraryBranchId branchId, Instant holdTill) {
    return new OnHoldState(book, branchId, patronId, holdTill);
}

@Override
public boolean canBePutOnHold(PatronId patronId) {
    return true; // Available books can always be put on hold
}
```

### 2. OnHoldState

**Purpose**: Represents a book that has been placed on hold by a specific patron.

**Allowed Operations:**
- ❌ Place on hold (invalid - already on hold)
- ✅ Checkout by holding patron → transitions to `CheckedOutState`
- ❌ Checkout by different patron (invalid - restricted access)
- ❌ Return book (invalid - book is not checked out)
- ✅ Cancel hold → transitions to `AvailableState`
- ✅ Expire hold → transitions to `AvailableState` (if past expiry time)

**Business Rules:**
- Only the patron who placed the hold can check out the book
- Hold can be canceled, returning book to available state
- Hold automatically expires after the specified time
- Book location is tracked at the branch where hold was placed

**Code Example:**
```java
@Override
public BookState checkout(PatronId patronId, LibraryBranchId branchId) {
    if (!patronId.equals(byPatron)) {
        return invalidTransition("Only the patron who placed the hold can check out the book");
    }
    return new CheckedOutState(book, branchId, patronId);
}

@Override
public boolean canBeCheckedOut(PatronId patronId) {
    return patronId.equals(byPatron); // Only holding patron can check out
}
```

### 3. CheckedOutState

**Purpose**: Represents a book that has been checked out by a patron.

**Allowed Operations:**
- ❌ Place on hold (invalid - book is checked out)
- ❌ Checkout (invalid - already checked out)
- ✅ Return book → transitions to `AvailableState`
- ❌ Cancel hold (invalid - no hold exists)
- ❌ Expire hold (invalid - no hold exists)

**Business Rules:**
- Book cannot be placed on hold while checked out
- Book cannot be checked out again until returned
- Only return operation is allowed
- Book location is tracked at the branch where it was checked out

**Code Example:**
```java
@Override
public BookState returnBook(LibraryBranchId branchId) {
    return new AvailableState(book, branchId);
}

@Override
public boolean canBeReturned() {
    return true; // Checked out books can always be returned
}
```

## State Transition Diagram

```
    ┌─────────────┐
    │  AVAILABLE  │
    └─────┬───────┘
          │
          ├─── placeOnHold() ────┐
          │                     │
          │                     ▼
          │              ┌─────────────┐
          │              │   ON_HOLD   │
          │              └─────┬───────┘
          │                    │
          │                    ├─── checkout() ──┐
          │                    │                 │
          │                    ├─── cancelHold() │
          │                    │                 │
          │                    ├─── expireHold() │
          │                    │                 │
          │                    │                 ▼
          │                    │         ┌─────────────┐
          │                    │         │ CHECKED_OUT │
          │                    │         └─────┬───────┘
          │                    │               │
          │                    │               │
          └─── checkout() ──────┼───────────────┘
                               │               │
                               │               │
                               └───────────────┴─── returnBook() ───┐
                                                                   │
                                                                   ▼
                                                            ┌─────────────┐
                                                            │  AVAILABLE  │
                                                            └─────────────┘
```

## Validation and Business Rules

### Validation Strategy

The system uses a **two-layer validation approach**:

1. **State-level validation**: Each state implements validation methods (`canBeCheckedOut`, `canBePutOnHold`, `canBeReturned`)
2. **Aggregate-level validation**: The `Book` aggregate checks validation before executing transitions

### Key Business Rules

1. **Hold Restrictions**:
   - Only one patron can hold a book at a time
   - Only the holding patron can check out a held book
   - Holds can expire automatically based on time

2. **Checkout Restrictions**:
   - Books can be checked out directly if available
   - Books can be checked out by holding patron if on hold
   - Only one patron can check out a book at a time

3. **Return Rules**:
   - Only checked out books can be returned
   - Returning a book makes it available again

4. **Version Control**:
   - Every state transition increments the book's version
   - Supports optimistic locking for concurrent access

## Error Handling

### Invalid State Transitions

The system handles invalid operations gracefully:

```java
default BookState invalidTransition(String message) {
    throw new IllegalStateException("Invalid state transition: " + message);
}
```

**Common Invalid Transitions:**
- Trying to return an available book
- Trying to place a hold on a checked out book
- Trying to check out a book held by another patron

### Exception Types

- `IllegalStateException`: Thrown for invalid state transitions
- `NullPointerException`: Thrown for null required parameters (via `Objects.requireNonNull`)

## Integration with Event-Driven Architecture

### Event Publishing

The system integrates with the broader event-driven architecture through:

1. **Domain Events**: State transitions can publish domain events
2. **Event Handlers**: External systems can react to book state changes
3. **Read Models**: State changes update read models for queries

### Event Examples

```java
// Events published during state transitions
- BookPlacedOnHold
- BookCheckedOut  
- BookReturned
- BookHoldCanceled
- BookHoldExpired
```

## Testing Strategy

### Unit Testing

The system includes comprehensive unit tests for:

1. **State Transitions**: Testing valid and invalid transitions
2. **Validation Logic**: Testing business rule enforcement
3. **Edge Cases**: Testing boundary conditions and error scenarios

### Test Examples

```groovy
def 'should place on hold book which is marked as available in the system'() {
    given:
        BookDSL availableBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
    when:
        BookOnHold onHold = the availableBook reactsTo bookPlacedOnHoldEvent
    then:
        onHold.bookId == availableBook.bookId
        onHold.byPatron == aPatron
        onHold.holdTill == oneHourLater
}
```

## Performance Considerations

### Memory Efficiency

- **Immutable States**: New state instances are created for each transition
- **Lightweight Objects**: State objects contain minimal data
- **No State History**: Only current state is maintained

### Concurrency

- **Version Control**: Optimistic locking prevents concurrent modification issues
- **Immutability**: Thread-safe by design
- **Atomic Operations**: State transitions are atomic

## Best Practices

### When to Use This Pattern

✅ **Good for**:
- Complex state machines with multiple states
- Business domains with clear state transitions
- Systems requiring audit trails and version control
- Applications with concurrent access requirements

❌ **Avoid when**:
- Simple boolean flags are sufficient
- State transitions are trivial
- Performance is critical and object creation is expensive

### Implementation Guidelines

1. **Keep States Simple**: Each state should have a single responsibility
2. **Validate Early**: Check preconditions before state transitions
3. **Use Immutability**: Create new instances rather than modifying existing ones
4. **Handle Errors Gracefully**: Provide clear error messages for invalid operations
5. **Test Thoroughly**: Cover all state transitions and edge cases

## Comparison with Legacy Model

### New Model (State Machine)

**Advantages**:
- Clear state transitions
- Compile-time safety
- Easy to understand and maintain
- Built-in validation

**Disadvantages**:
- More objects created
- Slightly more complex setup

### Legacy Model (Event-Driven)

**Advantages**:
- Event sourcing capabilities
- Historical state tracking
- Integration with event stores

**Disadvantages**:
- More complex to understand
- Runtime validation only
- Harder to reason about current state

## Conclusion

The book state handling system in the Library Demo application demonstrates a well-designed implementation of the State Machine Pattern. It provides:

- **Clear Separation of Concerns**: Each state handles its own behavior
- **Type Safety**: Compile-time guarantees about valid operations
- **Business Rule Enforcement**: Built-in validation prevents invalid operations
- **Extensibility**: Easy to add new states or modify existing behavior
- **Testability**: Clear interfaces make unit testing straightforward

This pattern is particularly well-suited for domain-driven design applications where business rules are complex and state transitions need to be carefully controlled and validated.
