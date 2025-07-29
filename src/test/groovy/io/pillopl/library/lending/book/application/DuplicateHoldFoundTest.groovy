package io.pillopl.library.lending.book.application

import io.pillopl.library.commons.events.DomainEvent
import io.pillopl.library.commons.events.DomainEvents
import io.pillopl.library.lending.book.new_model.BookDuplicateHoldFound
import io.pillopl.library.catalogue.BookId
import io.pillopl.library.catalogue.BookType
import io.pillopl.library.commons.aggregates.Version
import java.util.UUID
import io.pillopl.library.lending.book.new_model.Book
import io.pillopl.library.lending.book.new_model.BookRepository
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.vavr.control.Option
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId

class DuplicateHoldFoundTest extends Specification {

    Book bookOnHold = createBookOnHold()
    BookRepository bookRepository = Stub()
    DomainEvents domainEvents = Mock()
    PatronEventsHandler patronEventsHandler = new PatronEventsHandler(bookRepository, domainEvents)

    PatronId patronId = anyPatronId()
    LibraryBranchId libraryBranchId = anyBranch()

    Book createBookOnHold() {
        Book book = new Book(new BookId(UUID.randomUUID()), BookType.Circulating, anyBranch(), new Version(0))
        book.placeOnHold(anyPatronId(), anyBranch(), Instant.now().plusSeconds(3600))
        return book
    }


    def 'should raise duplicate hold found event when someone placed on hold book already on hold'() {
        given:
            bookIsAlreadyOnHold()
        when:
            patronEventsHandler.handle(placedOnHoldBy(patronId))
        then:
            1 * domainEvents.publish({
                it.firstPatronId == bookOnHold.getCurrentPatron().patronId &&
                it.secondPatronId == patronId.patronId
            } as BookDuplicateHoldFound)
    }


    def 'should not raise anything if book is on hold by the same patron'() {
        given:
            bookIsAlreadyOnHold()
        when:
            patronEventsHandler.handle(placedOnHoldBy(bookOnHold.getCurrentPatron()))
        then:
            0 * domainEvents.publish(_ as DomainEvent)
    }

    PatronEvent.BookPlacedOnHold placedOnHoldBy(PatronId patronId) {
        return new PatronEvent.BookPlacedOnHold(Instant.now(), patronId.patronId, bookOnHold.getBookId().bookId, bookOnHold.getBookType(), libraryBranchId.libraryBranchId, Instant.now(), Instant.now())
    }

    void bookIsAlreadyOnHold() {
        bookRepository.findBy(bookOnHold.getBookId()) >> Option.of(bookOnHold)
    }
}
