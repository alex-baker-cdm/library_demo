package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.new_model.AvailableState;
import io.pillopl.library.lending.book.new_model.CheckedOutState;
import io.pillopl.library.lending.book.new_model.OnHoldState;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;

import java.time.Instant;

public class BookAdapter {

    public static io.pillopl.library.lending.book.new_model.Book toNewModel(Book oldBook) {
        if (oldBook instanceof AvailableBook availableBook) {
            return new io.pillopl.library.lending.book.new_model.Book(
                    availableBook.getBookId(),
                    availableBook.getBookInformation().getBookType(),
                    availableBook.getLibraryBranch(),
                    availableBook.getVersion()
            );
        } else if (oldBook instanceof BookOnHold bookOnHold) {
            io.pillopl.library.lending.book.new_model.Book book = 
                new io.pillopl.library.lending.book.new_model.Book(
                    bookOnHold.getBookId(),
                    bookOnHold.getBookInformation().getBookType(),
                    bookOnHold.getHoldPlacedAt(),
                    bookOnHold.getVersion()
                );
            book.placeOnHold(bookOnHold.getByPatron(), bookOnHold.getHoldPlacedAt(), bookOnHold.getHoldTill());
            return book;
        } else if (oldBook instanceof CheckedOutBook checkedOutBook) {
            io.pillopl.library.lending.book.new_model.Book book = 
                new io.pillopl.library.lending.book.new_model.Book(
                    checkedOutBook.getBookId(),
                    checkedOutBook.getBookInformation().getBookType(),
                    checkedOutBook.getCheckedOutAt(),
                    checkedOutBook.getVersion()
                );
            book.checkout(checkedOutBook.getByPatron(), checkedOutBook.getCheckedOutAt());
            return book;
        }
        throw new IllegalArgumentException("Unknown book type: " + oldBook.getClass());
    }

    public static Book toOldModel(io.pillopl.library.lending.book.new_model.Book newBook) {
        String stateName = newBook.getCurrentState();
        switch (stateName) {
            case "AVAILABLE":
                return new AvailableBook(
                        newBook.getBookId(),
                        newBook.getBookType(),
                        newBook.getCurrentBranch(),
                        newBook.getVersion()
                );
            case "ON_HOLD":
                OnHoldState onHoldState = (OnHoldState) newBook.getState();
                return new BookOnHold(
                        newBook.getBookId(),
                        newBook.getBookType(),
                        onHoldState.getHoldPlacedAt(),
                        onHoldState.getByPatron(),
                        onHoldState.getHoldTill(),
                        newBook.getVersion()
                );
            case "CHECKED_OUT":
                CheckedOutState checkedOutState = (CheckedOutState) newBook.getState();
                return new CheckedOutBook(
                        newBook.getBookId(),
                        newBook.getBookType(),
                        checkedOutState.getCheckedOutAt(),
                        checkedOutState.getByPatron(),
                        newBook.getVersion()
                );
            default:
                throw new IllegalArgumentException("Unknown state: " + stateName);
        }
    }

    public static AvailableBook toAvailableBook(io.pillopl.library.lending.book.new_model.Book newBook) {
        if (!"AVAILABLE".equals(newBook.getCurrentState())) {
            throw new IllegalStateException("Book is not in available state");
        }
        return new AvailableBook(
                newBook.getBookId(),
                newBook.getBookType(),
                newBook.getCurrentBranch(),
                newBook.getVersion()
        );
    }

    public static BookOnHold toBookOnHold(io.pillopl.library.lending.book.new_model.Book newBook) {
        if (!"ON_HOLD".equals(newBook.getCurrentState())) {
            throw new IllegalStateException("Book is not on hold");
        }
        OnHoldState onHoldState = (OnHoldState) newBook.getState();
        return new BookOnHold(
                newBook.getBookId(),
                newBook.getBookType(),
                onHoldState.getHoldPlacedAt(),
                onHoldState.getByPatron(),
                onHoldState.getHoldTill(),
                newBook.getVersion()
        );
    }

    public static CheckedOutBook toCheckedOutBook(io.pillopl.library.lending.book.new_model.Book newBook) {
        if (!"CHECKED_OUT".equals(newBook.getCurrentState())) {
            throw new IllegalStateException("Book is not checked out");
        }
        CheckedOutState checkedOutState = (CheckedOutState) newBook.getState();
        return new CheckedOutBook(
                newBook.getBookId(),
                newBook.getBookType(),
                checkedOutState.getCheckedOutAt(),
                checkedOutState.getByPatron(),
                newBook.getVersion()
        );
    }
}
