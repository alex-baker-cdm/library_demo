package io.pillopl.library.lending.book.model

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.catalogue.BookType
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.version0
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatron

class BookEdgeCasesSpec extends Specification {

    def "AvailableBook should handle null parameters in constructor gracefully"() {
        when:
            new AvailableBook(null, anyBranch(), version0())
        then:
            thrown(NullPointerException)
    }

    def "BookOnHold should handle null parameters in constructor gracefully"() {
        when:
            new BookOnHold(null, anyBranch(), anyPatron(), Instant.now(), version0())
        then:
            thrown(NullPointerException)
    }

    def "CheckedOutBook should handle null parameters in constructor gracefully"() {
        when:
            new CheckedOutBook(null, anyBranch(), anyPatron(), version0())
        then:
            thrown(NullPointerException)
    }

    def "AvailableBook should handle edge case with same reference equality"() {
        given:
            BookInformation bookInfo = new BookInformation(anyBookId(), BookType.Circulating)
            AvailableBook book = new AvailableBook(bookInfo, anyBranch(), version0())
        expect:
            book.equals(book)
    }

    def "BookOnHold should handle edge case with same reference equality"() {
        given:
            BookInformation bookInfo = new BookInformation(anyBookId(), BookType.Circulating)
            BookOnHold book = new BookOnHold(bookInfo, anyBranch(), anyPatron(), Instant.now(), version0())
        expect:
            book.equals(book)
    }

    def "CheckedOutBook should handle edge case with same reference equality"() {
        given:
            BookInformation bookInfo = new BookInformation(anyBookId(), BookType.Circulating)
            CheckedOutBook book = new CheckedOutBook(bookInfo, anyBranch(), anyPatron(), version0())
        expect:
            book.equals(book)
    }

    def "BookOnHold by method should handle null patron gracefully"() {
        given:
            BookInformation bookInfo = new BookInformation(anyBookId(), BookType.Circulating)
            BookOnHold book = new BookOnHold(bookInfo, anyBranch(), anyPatron(), Instant.now(), version0())
        expect:
            !book.by(null)
    }

    def "AvailableBook isRestricted should work with both book types"() {
        given:
            BookInformation circulatingInfo = new BookInformation(anyBookId(), BookType.Circulating)
            BookInformation restrictedInfo = new BookInformation(anyBookId(), BookType.Restricted)
            AvailableBook circulatingBook = new AvailableBook(circulatingInfo, anyBranch(), version0())
            AvailableBook restrictedBook = new AvailableBook(restrictedInfo, anyBranch(), version0())
        expect:
            !circulatingBook.isRestricted()
            restrictedBook.isRestricted()
    }
}
