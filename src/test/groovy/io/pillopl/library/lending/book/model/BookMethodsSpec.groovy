package io.pillopl.library.lending.book.model

import io.pillopl.library.catalogue.BookType
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import static BookDSL.aCirculatingBook
import static BookDSL.aRestrictedBook
import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatron

class BookMethodsSpec extends Specification {

    def "AvailableBook should return true for isRestricted when book type is Restricted"() {
        given:
            BookInformation restrictedBookInfo = new BookInformation(anyBookId(), BookType.Restricted)
            AvailableBook availableBook = new AvailableBook(restrictedBookInfo, anyBranch(), version0())
        expect:
            availableBook.isRestricted()
    }

    def "AvailableBook should return false for isRestricted when book type is Circulating"() {
        given:
            BookDSL circulatingBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
        when:
            AvailableBook availableBook = circulatingBook.bookProvider()
        then:
            !availableBook.isRestricted()
    }

    def "BookOnHold should return true for by method when patron matches"() {
        given:
            PatronId patron = anyPatron()
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy patron
        when:
            BookOnHold bookOnHold = book.bookProvider()
        then:
            bookOnHold.by(patron)
    }

    def "BookOnHold should return false for by method when patron does not match"() {
        given:
            PatronId patron1 = anyPatron()
            PatronId patron2 = anyPatron()
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy patron1
        when:
            BookOnHold bookOnHold = book.bookProvider()
        then:
            !bookOnHold.by(patron2)
    }

    def "AvailableBook should implement Book interface bookId method"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
        when:
            AvailableBook availableBook = book.bookProvider()
        then:
            availableBook.getBookId() == book.bookId
    }

    def "AvailableBook should implement Book interface type method"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
        when:
            AvailableBook availableBook = book.bookProvider()
        then:
            availableBook.getBookInformation().getBookType() == BookType.Circulating
    }

    def "BookOnHold should implement Book interface bookId method"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()
        when:
            BookOnHold bookOnHold = book.bookProvider()
        then:
            bookOnHold.getBookId() == book.bookId
    }

    def "BookOnHold should implement Book interface type method"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()
        when:
            BookOnHold bookOnHold = book.bookProvider()
        then:
            bookOnHold.getBookInformation().getBookType() == BookType.Circulating
    }

    def "CheckedOutBook should implement Book interface bookId method"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() checkedOutBy anyPatron()
        when:
            CheckedOutBook checkedOutBook = book.bookProvider()
        then:
            checkedOutBook.getBookId() == book.bookId
    }

    def "CheckedOutBook should implement Book interface type method"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() checkedOutBy anyPatron()
        when:
            CheckedOutBook checkedOutBook = book.bookProvider()
        then:
            checkedOutBook.getBookInformation().getBookType() == BookType.Circulating
    }
}
