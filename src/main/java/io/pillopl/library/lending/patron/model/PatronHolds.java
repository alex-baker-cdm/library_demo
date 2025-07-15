package io.pillopl.library.lending.patron.model;

import io.pillopl.library.lending.book.new_model.Book;
import io.pillopl.library.lending.book.new_model.Book;
import lombok.NonNull;
import lombok.Value;

import java.util.Set;

@Value
class PatronHolds {

    static int MAX_NUMBER_OF_HOLDS = 5;

    Set<Hold> resourcesOnHold;

    boolean a(@NonNull Book bookOnHold) {
        Hold hold = new Hold(bookOnHold.getBookId(), bookOnHold.getCurrentBranch());
        return resourcesOnHold.contains(hold);
    }

    int count() {
        return resourcesOnHold.size();
    }

    boolean maximumHoldsAfterHolding(Book book) {
        return count() + 1 == MAX_NUMBER_OF_HOLDS;
    }
}
