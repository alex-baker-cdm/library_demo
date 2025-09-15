package io.pillopl.library.lending.book.new_model.infrastructure;

import io.pillopl.library.catalogue.BookType;
import java.time.Instant;
import java.util.UUID;

public class BookDatabaseEntity {
    public enum BookState {
        Available, OnHold, CheckedOut
    }

    public UUID book_id;
    public BookType book_type;
    public BookState book_state;
    public UUID available_at_branch;
    public UUID on_hold_at_branch;
    public UUID on_hold_by_patron;
    public Instant on_hold_till;
    public UUID checked_out_at_branch;
    public UUID checked_out_by_patron;
    public int version;
}
