package io.pillopl.library.lending.book.model

import io.pillopl.library.catalogue.BookType
import spock.lang.Specification

import static BookDSL.aCirculatingBook
import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatron

class BookEqualsHashCodeToStringTest extends Specification {

    def "AvailableBook should have equal instances with same book information"() {
        given:
            BookDSL book1 = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
            BookDSL book2 = aCirculatingBook() with book1.bookId locatedIn anyBranch() stillAvailable()
        when:
            AvailableBook availableBook1 = book1.book()
            AvailableBook availableBook2 = book2.book()
        then:
            availableBook1.equals(availableBook2)
            availableBook1.hashCode() == availableBook2.hashCode()
    }

    def "AvailableBook should not be equal to different book"() {
        given:
            BookDSL book1 = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
            BookDSL book2 = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
        when:
            AvailableBook availableBook1 = book1.book()
            AvailableBook availableBook2 = book2.book()
        then:
            !availableBook1.equals(availableBook2)
    }

    def "AvailableBook should not be equal to null"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
        when:
            AvailableBook availableBook = book.book()
        then:
            !availableBook.equals(null)
    }

    def "AvailableBook should not be equal to different class"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
        when:
            AvailableBook availableBook = book.book()
        then:
            !availableBook.equals("not a book")
    }

    def "AvailableBook should have consistent toString format"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
        when:
            AvailableBook availableBook = book.book()
            String result = availableBook.toString()
        then:
            result.contains("AvailableBook{")
            result.contains("bookInformation=")
            result.contains("libraryBranch=")
    }

    def "BookOnHold should have equal instances with same book information"() {
        given:
            BookDSL book1 = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()
            BookDSL book2 = aCirculatingBook() with book1.bookId locatedIn anyBranch() placedOnHoldBy anyPatron()
        when:
            BookOnHold bookOnHold1 = book1.book()
            BookOnHold bookOnHold2 = book2.book()
        then:
            bookOnHold1.equals(bookOnHold2)
            bookOnHold1.hashCode() == bookOnHold2.hashCode()
    }

    def "BookOnHold should not be equal to different book"() {
        given:
            BookDSL book1 = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()
            BookDSL book2 = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()
        when:
            BookOnHold bookOnHold1 = book1.book()
            BookOnHold bookOnHold2 = book2.book()
        then:
            !bookOnHold1.equals(bookOnHold2)
    }

    def "BookOnHold should have consistent toString format"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()
        when:
            BookOnHold bookOnHold = book.book()
            String result = bookOnHold.toString()
        then:
            result.contains("BookOnHold{")
            result.contains("bookInformation=")
            result.contains("holdPlacedAt=")
    }

    def "CheckedOutBook should have equal instances with same book information"() {
        given:
            BookDSL book1 = aCirculatingBook() with anyBookId() locatedIn anyBranch() checkedOutBy anyPatron()
            BookDSL book2 = aCirculatingBook() with book1.bookId locatedIn anyBranch() checkedOutBy anyPatron()
        when:
            CheckedOutBook checkedOutBook1 = book1.book()
            CheckedOutBook checkedOutBook2 = book2.book()
        then:
            checkedOutBook1.equals(checkedOutBook2)
            checkedOutBook1.hashCode() == checkedOutBook2.hashCode()
    }

    def "CheckedOutBook should not be equal to different book"() {
        given:
            BookDSL book1 = aCirculatingBook() with anyBookId() locatedIn anyBranch() checkedOutBy anyPatron()
            BookDSL book2 = aCirculatingBook() with anyBookId() locatedIn anyBranch() checkedOutBy anyPatron()
        when:
            CheckedOutBook checkedOutBook1 = book1.book()
            CheckedOutBook checkedOutBook2 = book2.book()
        then:
            !checkedOutBook1.equals(checkedOutBook2)
    }

    def "CheckedOutBook should have consistent toString format"() {
        given:
            BookDSL book = aCirculatingBook() with anyBookId() locatedIn anyBranch() checkedOutBy anyPatron()
        when:
            CheckedOutBook checkedOutBook = book.book()
            String result = checkedOutBook.toString()
        then:
            result.contains("CheckedOutBook{")
            result.contains("bookInformation=")
            result.contains("checkedOutAt=")
    }
}
