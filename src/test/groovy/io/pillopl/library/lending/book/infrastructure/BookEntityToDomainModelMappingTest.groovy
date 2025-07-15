package io.pillopl.library.lending.book.infrastructure


import io.pillopl.library.lending.book.model.Book
import io.pillopl.library.catalogue.BookId
import io.pillopl.library.lending.book.new_model.Book as NewBook
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.catalogue.BookType.Circulating
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.*

class BookEntityToDomainModelMappingTest extends Specification {

    LibraryBranchId libraryBranchId = anyBranch()
    LibraryBranchId anotherBranchId = anyBranch()
    LibraryBranchId yetAnotherBranchId = anyBranch()
    PatronId patronId = anyPatronId()
    PatronId anotherPatronId = anyPatronId()
    BookId bookId = anyBookId()
    Instant holdTill = Instant.now()


    def 'should map to available book'() {
        given:
            BookDatabaseEntity entity = bookEntity(Available)
        when:
            Book book = entity.toDomainModel()
        and:
            NewBook newBook = book as NewBook
        then:
            newBook.bookId == bookId
            newBook.bookInformation.bookType == Circulating
            newBook.currentBranch == libraryBranchId
            newBook.currentState == "AVAILABLE"

    }

    def 'should map to on hold book'() {
        given:
            BookDatabaseEntity entity = bookEntity(OnHold)
        when:
            Book book = entity.toDomainModel()
        and:
            NewBook newBook = book as NewBook
        then:
            newBook.bookId == bookId
            newBook.bookInformation.bookType == Circulating
            newBook.currentBranch == anotherBranchId
            newBook.currentPatron == patronId
            newBook.currentState == "ON_HOLD"
    }

    def 'should map to checked out book'() {
        given:
            BookDatabaseEntity entity = bookEntity(CheckedOut)
        when:
            Book book = entity.toDomainModel()
        and:
            NewBook newBook = book as NewBook
        then:
            newBook.bookId == bookId
            newBook.bookInformation.bookType == Circulating
            newBook.currentBranch == yetAnotherBranchId
            newBook.currentPatron == anotherPatronId
            newBook.currentState == "CHECKED_OUT"
    }

    BookDatabaseEntity bookEntity(BookState state) {
        new BookDatabaseEntity(
                book_id: bookId.bookId,
                book_type: Circulating,
                book_state: state,
                available_at_branch: libraryBranchId.libraryBranchId,
                on_hold_at_branch: anotherBranchId.libraryBranchId,
                on_hold_by_patron: patronId.patronId,
                on_hold_till: holdTill,
                checked_out_at_branch: yetAnotherBranchId.libraryBranchId,
                checked_out_by_patron: anotherPatronId.patronId)
    }
}
