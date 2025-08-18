package io.pillopl.library.lending.book.model

import io.pillopl.library.catalogue.BookType
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import static BookDSL.aCirculatingBook
import static BookDSL.aRestrictedBook
import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatron

class BookMethodsTest extends Specification {

    def "AvailableBook should return true for isRestricted when book type is Restricted"() {
        given:
            BookDSL restrictedBook = aRestrictedBook() with anyBookId() locatedIn anyBranch() stillAvailable()
        when:
            AvailableBook availableBook = restrictedBook.book()
        then:
            availableBook.isRestricted()
    }

    def "AvailableBook should return false for isRestricted when book type is Circulating"() {
        given:
            BookDSL circulatingBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
        when:
            AvailableBook availableBook = circulatingBook.book()
        then:
            !availableBook.isRestricted()
    }

    def "BookOnHold should return true for by method when patron matches"() {
        given:
            PatronId patron = anyPatron()
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy patron
        when:
            BookOnHold bookOnHold = book.book()
        then:
            bookOnHold.by(patron)
    }

    def "BookOnHold should return false for by method when patron does not match"() {
        given:
            PatronId patron1 = anyPatron()
            PatronId patron2 = anyPatron()
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy patron1
        when:
            BookOnHold bookOnHold = book.book()
        then:
            !bookOnHold.by(patron2)
    }

    def "AvailableBook should implement Book interface bookId method"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
        when:
            AvailableBook availableBook = book.book()
        then:
            availableBook.bookId() == book.bookId
    }

    def "AvailableBook should implement Book interface type method"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
        when:
            AvailableBook availableBook = book.book()
        then:
            availableBook.type() == BookType.Circulating
    }

    def "BookOnHold should implement Book interface bookId method"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()
        when:
            BookOnHold bookOnHold = book.book()
        then:
            bookOnHold.bookId() == book.bookId
    }

    def "BookOnHold should implement Book interface type method"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()
        when:
            BookOnHold bookOnHold = book.book()
        then:
            bookOnHold.type() == BookType.Circulating
    }

    def "CheckedOutBook should implement Book interface bookId method"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() checkedOutBy anyPatron()
        when:
            CheckedOutBook checkedOutBook = book.book()
        then:
            checkedOutBook.bookId() == book.bookId
    }

    def "CheckedOutBook should implement Book interface type method"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() checkedOutBy anyPatron()
        when:
            CheckedOutBook checkedOutBook = book.book()
        then:
            checkedOutBook.type() == BookType.Circulating
    }
}
