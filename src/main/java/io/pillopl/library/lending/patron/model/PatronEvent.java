package io.pillopl.library.lending.patron.model;

import io.pillopl.library.commons.events.DomainEvent;
import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.vavr.collection.List;
import io.vavr.control.Option;

import java.time.Instant;
import java.util.UUID;

public interface PatronEvent extends DomainEvent {

    default PatronId patronId() {
        return new PatronId(getPatronId());
    }

    UUID getPatronId();

    default UUID getAggregateId() {
       return getPatronId();
    }

    default List<DomainEvent> normalize() {
        return List.of(this);
    }

    class PatronCreated implements PatronEvent {
        private final UUID eventId = UUID.randomUUID();
        private final Instant when;
        private final UUID patronId;
        private final PatronType patronType;

        PatronCreated(Instant when, UUID patronId, PatronType patronType) {
            this.when = when;
            this.patronId = patronId;
            this.patronType = patronType;
        }

        public static PatronCreated now(PatronId patronId, PatronType type) {
            return new PatronCreated(Instant.now(), patronId.getPatronId(), type);
        }

        @Override
        public UUID getPatronId() {
            return patronId;
        }

        public UUID getEventId() {
            return eventId;
        }

        @Override
        public Instant getWhen() {
            return when;
        }

        public PatronType getPatronType() {
            return patronType;
        }
    }

    class BookPlacedOnHold implements PatronEvent {
        private final UUID eventId = UUID.randomUUID();
        private final Instant when;
        private final UUID patronId;
        private final UUID bookId;
        private final BookType bookType;
        private final UUID libraryBranchId;
        private final Instant holdFrom;
        private final Instant holdTill;

        BookPlacedOnHold(Instant when, UUID patronId, UUID bookId, BookType bookType, UUID libraryBranchId, Instant holdFrom, Instant holdTill) {
            this.when = when;
            this.patronId = patronId;
            this.bookId = bookId;
            this.bookType = bookType;
            this.libraryBranchId = libraryBranchId;
            this.holdFrom = holdFrom;
            this.holdTill = holdTill;
        }

        public static BookPlacedOnHold bookPlacedOnHoldNow(BookId bookId, BookType bookType, LibraryBranchId libraryBranchId, PatronId patronId, HoldDuration holdDuration) {
            return new BookPlacedOnHold(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    bookType,
                    libraryBranchId.getLibraryBranchId(),
                    holdDuration.getFrom(),
                    holdDuration.getTo().getOrNull());
        }

        @Override
        public UUID getPatronId() {
            return patronId;
        }

        public UUID getEventId() {
            return eventId;
        }

        @Override
        public Instant getWhen() {
            return when;
        }

        public UUID getBookId() {
            return bookId;
        }

        public BookType getBookType() {
            return bookType;
        }

        public UUID getLibraryBranchId() {
            return libraryBranchId;
        }

        public Instant getHoldFrom() {
            return holdFrom;
        }

        public Instant getHoldTill() {
            return holdTill;
        }
    }

    class BookPlacedOnHoldEvents implements PatronEvent {
        private final UUID eventId = UUID.randomUUID();
        private final UUID patronId;
        private final BookPlacedOnHold bookPlacedOnHold;
        private final Option<MaximumNumberOhHoldsReached> maximumNumberOhHoldsReached;

        BookPlacedOnHoldEvents(UUID patronId, BookPlacedOnHold bookPlacedOnHold, Option<MaximumNumberOhHoldsReached> maximumNumberOhHoldsReached) {
            this.patronId = patronId;
            this.bookPlacedOnHold = bookPlacedOnHold;
            this.maximumNumberOhHoldsReached = maximumNumberOhHoldsReached;
        }

        @Override
        public UUID getPatronId() {
            return patronId;
        }

        public UUID getEventId() {
            return eventId;
        }

        @Override
        public Instant getWhen() {
            return bookPlacedOnHold.getWhen();
        }

        public BookPlacedOnHold getBookPlacedOnHold() {
            return bookPlacedOnHold;
        }

        public Option<MaximumNumberOhHoldsReached> getMaximumNumberOhHoldsReached() {
            return maximumNumberOhHoldsReached;
        }

        public static BookPlacedOnHoldEvents events(BookPlacedOnHold bookPlacedOnHold) {
            return new BookPlacedOnHoldEvents(bookPlacedOnHold.getPatronId(), bookPlacedOnHold, Option.none());
        }

        public static BookPlacedOnHoldEvents events(BookPlacedOnHold bookPlacedOnHold, MaximumNumberOhHoldsReached maximumNumberOhHoldsReached) {
            return new BookPlacedOnHoldEvents(bookPlacedOnHold.getPatronId(), bookPlacedOnHold, Option.of(maximumNumberOhHoldsReached));
        }

        public List<DomainEvent> normalize() {
            return List.<DomainEvent>of(bookPlacedOnHold).appendAll(maximumNumberOhHoldsReached.toList());
        }
    }

    class MaximumNumberOhHoldsReached implements PatronEvent {
        private final UUID eventId = UUID.randomUUID();
        private final Instant when;
        private final UUID patronId;
        private final int numberOfHolds;

        MaximumNumberOhHoldsReached(Instant when, UUID patronId, int numberOfHolds) {
            this.when = when;
            this.patronId = patronId;
            this.numberOfHolds = numberOfHolds;
        }

        public static MaximumNumberOhHoldsReached now(PatronInformation patronInformation, int numberOfHolds) {
            return new MaximumNumberOhHoldsReached(
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    numberOfHolds);
        }

        @Override
        public UUID getPatronId() {
            return patronId;
        }

        public UUID getEventId() {
            return eventId;
        }

        @Override
        public Instant getWhen() {
            return when;
        }

        public int getNumberOfHolds() {
            return numberOfHolds;
        }
    }

    class BookCheckedOut implements PatronEvent {
        private final UUID eventId = UUID.randomUUID();
        private final Instant when;
        private final UUID patronId;
        private final UUID bookId;
        private final BookType bookType;
        private final UUID libraryBranchId;
        private final Instant till;

        BookCheckedOut(Instant when, UUID patronId, UUID bookId, BookType bookType, UUID libraryBranchId, Instant till) {
            this.when = when;
            this.patronId = patronId;
            this.bookId = bookId;
            this.bookType = bookType;
            this.libraryBranchId = libraryBranchId;
            this.till = till;
        }

        public static BookCheckedOut bookCheckedOutNow(BookId bookId, BookType bookType, LibraryBranchId libraryBranchId, PatronId patronId, CheckoutDuration checkoutDuration) {
            return new BookCheckedOut(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    bookType,
                    libraryBranchId.getLibraryBranchId(),
                    checkoutDuration.to());
        }

        @Override
        public UUID getPatronId() {
            return patronId;
        }

        public UUID getEventId() {
            return eventId;
        }

        @Override
        public Instant getWhen() {
            return when;
        }

        public UUID getBookId() {
            return bookId;
        }

        public BookType getBookType() {
            return bookType;
        }

        public UUID getLibraryBranchId() {
            return libraryBranchId;
        }

        public Instant getTill() {
            return till;
        }
    }

    class BookReturned implements PatronEvent {
        private final UUID eventId = UUID.randomUUID();
        private final Instant when;
        private final UUID patronId;
        private final UUID bookId;
        private final BookType bookType;
        private final UUID libraryBranchId;

        BookReturned(Instant when, UUID patronId, UUID bookId, BookType bookType, UUID libraryBranchId) {
            this.when = when;
            this.patronId = patronId;
            this.bookId = bookId;
            this.bookType = bookType;
            this.libraryBranchId = libraryBranchId;
        }

        @Override
        public UUID getPatronId() {
            return patronId;
        }

        public UUID getEventId() {
            return eventId;
        }

        @Override
        public Instant getWhen() {
            return when;
        }

        public UUID getBookId() {
            return bookId;
        }

        public BookType getBookType() {
            return bookType;
        }

        public UUID getLibraryBranchId() {
            return libraryBranchId;
        }
    }

    class BookHoldFailed implements PatronEvent {
        private final UUID eventId = UUID.randomUUID();
        private final String reason;
        private final Instant when;
        private final UUID patronId;
        private final UUID bookId;
        private final UUID libraryBranchId;

        BookHoldFailed(String reason, Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
            this.reason = reason;
            this.when = when;
            this.patronId = patronId;
            this.bookId = bookId;
            this.libraryBranchId = libraryBranchId;
        }

        static BookHoldFailed bookHoldFailedNow(Rejection rejection, BookId bookId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new BookHoldFailed(
                    rejection.getReason().getReason(),
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }

        @Override
        public UUID getPatronId() {
            return patronId;
        }

        public UUID getEventId() {
            return eventId;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public Instant getWhen() {
            return when;
        }

        public UUID getBookId() {
            return bookId;
        }

        public UUID getLibraryBranchId() {
            return libraryBranchId;
        }
    }

    class BookCheckingOutFailed implements PatronEvent {
        private final UUID eventId = UUID.randomUUID();
        private final String reason;
        private final Instant when;
        private final UUID patronId;
        private final UUID bookId;
        private final UUID libraryBranchId;

        BookCheckingOutFailed(String reason, Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
            this.reason = reason;
            this.when = when;
            this.patronId = patronId;
            this.bookId = bookId;
            this.libraryBranchId = libraryBranchId;
        }

        static BookCheckingOutFailed bookCheckingOutFailedNow(Rejection rejection, BookId bookId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new BookCheckingOutFailed(
                    rejection.getReason().getReason(),
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }

        @Override
        public UUID getPatronId() {
            return patronId;
        }

        public UUID getEventId() {
            return eventId;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public Instant getWhen() {
            return when;
        }

        public UUID getBookId() {
            return bookId;
        }

        public UUID getLibraryBranchId() {
            return libraryBranchId;
        }
    }

    class BookHoldCanceled implements PatronEvent {
        private final UUID eventId = UUID.randomUUID();
        private final Instant when;
        private final UUID patronId;
        private final UUID bookId;
        private final UUID libraryBranchId;

        BookHoldCanceled(Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
            this.when = when;
            this.patronId = patronId;
            this.bookId = bookId;
            this.libraryBranchId = libraryBranchId;
        }

        public static BookHoldCanceled holdCanceledNow(BookId bookId, LibraryBranchId libraryBranchId, PatronId patronId) {
            return new BookHoldCanceled(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }

        @Override
        public UUID getPatronId() {
            return patronId;
        }

        public UUID getEventId() {
            return eventId;
        }

        @Override
        public Instant getWhen() {
            return when;
        }

        public UUID getBookId() {
            return bookId;
        }

        public UUID getLibraryBranchId() {
            return libraryBranchId;
        }
    }

    class BookHoldCancelingFailed implements PatronEvent {
        private final UUID eventId = UUID.randomUUID();
        private final Instant when;
        private final UUID patronId;
        private final UUID bookId;
        private final UUID libraryBranchId;

        BookHoldCancelingFailed(Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
            this.when = when;
            this.patronId = patronId;
            this.bookId = bookId;
            this.libraryBranchId = libraryBranchId;
        }

        static BookHoldCancelingFailed holdCancelingFailedNow(BookId bookId, LibraryBranchId libraryBranchId, PatronId patronId) {
            return new BookHoldCancelingFailed(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }

        @Override
        public UUID getPatronId() {
            return patronId;
        }

        public UUID getEventId() {
            return eventId;
        }

        @Override
        public Instant getWhen() {
            return when;
        }

        public UUID getBookId() {
            return bookId;
        }

        public UUID getLibraryBranchId() {
            return libraryBranchId;
        }
    }

    class BookHoldExpired implements PatronEvent {
        private final UUID eventId = UUID.randomUUID();
        private final Instant when;
        private final UUID patronId;
        private final UUID bookId;
        private final UUID libraryBranchId;

        BookHoldExpired(Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
            this.when = when;
            this.patronId = patronId;
            this.bookId = bookId;
            this.libraryBranchId = libraryBranchId;
        }

        public static BookHoldExpired now(BookId bookId, PatronId patronId, LibraryBranchId libraryBranchId) {
            return new BookHoldExpired(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }

        @Override
        public UUID getPatronId() {
            return patronId;
        }

        public UUID getEventId() {
            return eventId;
        }

        @Override
        public Instant getWhen() {
            return when;
        }

        public UUID getBookId() {
            return bookId;
        }

        public UUID getLibraryBranchId() {
            return libraryBranchId;
        }
    }

    class OverdueCheckoutRegistered implements PatronEvent {
        private final UUID eventId = UUID.randomUUID();
        private final Instant when;
        private final UUID patronId;
        private final UUID bookId;
        private final UUID libraryBranchId;

        OverdueCheckoutRegistered(Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
            this.when = when;
            this.patronId = patronId;
            this.bookId = bookId;
            this.libraryBranchId = libraryBranchId;
        }

        public static OverdueCheckoutRegistered now(PatronId patronId, BookId bookId, LibraryBranchId libraryBranchId) {
            return new OverdueCheckoutRegistered(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }

        @Override
        public UUID getPatronId() {
            return patronId;
        }

        public UUID getEventId() {
            return eventId;
        }

        @Override
        public Instant getWhen() {
            return when;
        }

        public UUID getBookId() {
            return bookId;
        }

        public UUID getLibraryBranchId() {
            return libraryBranchId;
        }
    }

}



