package io.pillopl.library.lending.book.model

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.catalogue.BookType
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId

class BookInformationTest extends Specification {

    def "should create BookInformation with valid parameters"() {
        given:
            BookId bookId = anyBookId()
            BookType bookType = BookType.Circulating
        when:
            BookInformation bookInfo = new BookInformation(bookId, bookType)
        then:
            bookInfo.getBookId() == bookId
            bookInfo.getBookType() == bookType
    }

    def "should not create BookInformation with null bookId"() {
        when:
            new BookInformation(null, BookType.Circulating)
        then:
            thrown(NullPointerException)
    }

    def "should not create BookInformation with null bookType"() {
        when:
            new BookInformation(anyBookId(), null)
        then:
            thrown(NullPointerException)
    }

    def "should have equal BookInformation instances with same bookId and bookType"() {
        given:
            BookId bookId = anyBookId()
            BookType bookType = BookType.Circulating
            BookInformation bookInfo1 = new BookInformation(bookId, bookType)
            BookInformation bookInfo2 = new BookInformation(bookId, bookType)
        expect:
            bookInfo1.equals(bookInfo2)
            bookInfo1.hashCode() == bookInfo2.hashCode()
    }

    def "should not have equal BookInformation instances with different bookId"() {
        given:
            BookInformation bookInfo1 = new BookInformation(anyBookId(), BookType.Circulating)
            BookInformation bookInfo2 = new BookInformation(anyBookId(), BookType.Circulating)
        expect:
            !bookInfo1.equals(bookInfo2)
    }

    def "should not have equal BookInformation instances with different bookType"() {
        given:
            BookId bookId = anyBookId()
            BookInformation bookInfo1 = new BookInformation(bookId, BookType.Circulating)
            BookInformation bookInfo2 = new BookInformation(bookId, BookType.Restricted)
        expect:
            !bookInfo1.equals(bookInfo2)
    }

    def "should not be equal to null"() {
        given:
            BookInformation bookInfo = new BookInformation(anyBookId(), BookType.Circulating)
        expect:
            !bookInfo.equals(null)
    }

    def "should not be equal to different class"() {
        given:
            BookInformation bookInfo = new BookInformation(anyBookId(), BookType.Circulating)
        expect:
            !bookInfo.equals("not book information")
    }

    def "should have consistent toString format"() {
        given:
            BookInformation bookInfo = new BookInformation(anyBookId(), BookType.Circulating)
        when:
            String result = bookInfo.toString()
        then:
            result.contains("BookInformation{")
            result.contains("bookId=")
            result.contains("bookType=")
    }
}
