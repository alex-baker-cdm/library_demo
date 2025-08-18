package io.pillopl.library.lending.book.model

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.catalogue.BookType
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatron

class BookEdgeCasesTest extends Specification {

    def "AvailableBook should handle null parameters in constructor gracefully"() {
        when:
            new AvailableBook(null, anyBranch(), 1)
        then:
            thrown(NullPointerException)
    }

    def "BookOnHold should handle null parameters in constructor gracefully"() {
        when:
            new BookOnHold(null, anyBranch(), anyPatron(), Instant.now(), 1)
        then:
            thrown(NullPointerException)
    }

    def "CheckedOutBook should handle null parameters in constructor gracefully"() {
        when:
            new CheckedOutBook(null, anyBranch(), anyPatron(), 1)
        then:
            thrown(NullPointerException)
    }

    def "AvailableBook should handle edge case with same reference equality"() {
        given:
            BookInformation bookInfo = new BookInformation(anyBookId(), BookType.Circulating)
            AvailableBook book = new AvailableBook(bookInfo, anyBranch(), 1)
        expect:
            book.equals(book)
    }

    def "BookOnHold should handle edge case with same reference equality"() {
        given:
            BookInformation bookInfo = new BookInformation(anyBookId(), BookType.Circulating)
            BookOnHold book = new BookOnHold(bookInfo, anyBranch(), anyPatron(), Instant.now(), 1)
        expect:
            book.equals(book)
    }

    def "CheckedOutBook should handle edge case with same reference equality"() {
        given:
            BookInformation bookInfo = new BookInformation(anyBookId(), BookType.Circulating)
            CheckedOutBook book = new CheckedOutBook(bookInfo, anyBranch(), anyPatron(), 1)
        expect:
            book.equals(book)
    }

    def "BookOnHold by method should handle null patron gracefully"() {
        given:
            BookInformation bookInfo = new BookInformation(anyBookId(), BookType.Circulating)
            BookOnHold book = new BookOnHold(bookInfo, anyBranch(), anyPatron(), Instant.now(), 1)
        when:
            book.by(null)
        then:
            thrown(NullPointerException)
    }

    def "AvailableBook isRestricted should work with both book types"() {
        given:
            BookInformation circulatingInfo = new BookInformation(anyBookId(), BookType.Circulating)
            BookInformation restrictedInfo = new BookInformation(anyBookId(), BookType.Restricted)
            AvailableBook circulatingBook = new AvailableBook(circulatingInfo, anyBranch(), 1)
            AvailableBook restrictedBook = new AvailableBook(restrictedInfo, anyBranch(), 1)
        expect:
            !circulatingBook.isRestricted()
            restrictedBook.isRestricted()
    }
}
