package io.pillopl.library.catalogue

import spock.lang.Specification

class BookTitleAuthorISBNTest extends Specification {

    def "title should be trimmed"() {
        given:
            Title title = new Title("   to trim  ")
        expect:
            title.title == "to trim"

    }

    def "author should be trimmed"() {
        given:
            Author author = new Author("   to trim  ")
        expect:
            author.name == "to trim"
    }

    def "title should not be empty"() {
        when:
            new Title("")
        then:
            thrown(IllegalArgumentException)
    }

    def "author should not be empty"() {
        when:
            new Author("")
        then:
            thrown(IllegalArgumentException)
    }

    def "title should not be null"() {
        when:
            new Title(null)
        then:
            thrown(NullPointerException)
    }

    def "author should not be null"() {
        when:
            new Author(null)
        then:
            thrown(NullPointerException)
    }

    def "isbn should be correct"() {
        when:
            ISBN isbn = new ISBN("123412341X")
        then:
            isbn.isbn == "123412341X"
    }

    def "isbn should be trimmed"() {
        when:
            ISBN isbn = new ISBN("  1234123414  ")
        then:
            isbn.isbn == "1234123414"
    }

    def "wrong isbn should not be accepted"() {
        when:
            new ISBN("not isbn")
        then:
            thrown(IllegalArgumentException)
    }

    def "should create Book with ISBN, Title and Author"() {
        given:
            ISBN isbn = new ISBN("123412341X")
            Title title = new Title("Domain Driven Design")
            Author author = new Author("Eric Evans")
        when:
            Book book = new Book(isbn, title, author)
        then:
            book.getBookIsbn() == isbn
            book.getTitle() == title
            book.getAuthor() == author
    }

    def "should have equal Books with same ISBN, title and author"() {
        given:
            ISBN isbn = new ISBN("123412341X")
            Title title = new Title("Domain Driven Design")
            Author author = new Author("Eric Evans")
            Book book1 = new Book(isbn, title, author)
            Book book2 = new Book(isbn, title, author)
        expect:
            book1.equals(book2)
            book1.hashCode() == book2.hashCode()
    }

    def "should have different Books with different ISBN"() {
        given:
            Title title = new Title("Domain Driven Design")
            Author author = new Author("Eric Evans")
            Book book1 = new Book(new ISBN("123412341X"), title, author)
            Book book2 = new Book(new ISBN("0987654321"), title, author)
        expect:
            !book1.equals(book2)
            book1.hashCode() != book2.hashCode()
    }

    def "should have different Books with different title"() {
        given:
            ISBN isbn = new ISBN("123412341X")
            Author author = new Author("Eric Evans")
            Book book1 = new Book(isbn, new Title("Domain Driven Design"), author)
            Book book2 = new Book(isbn, new Title("Clean Code"), author)
        expect:
            !book1.equals(book2)
    }

    def "should have different Books with different author"() {
        given:
            ISBN isbn = new ISBN("123412341X")
            Title title = new Title("Domain Driven Design")
            Book book1 = new Book(isbn, title, new Author("Eric Evans"))
            Book book2 = new Book(isbn, title, new Author("Robert Martin"))
        expect:
            !book1.equals(book2)
    }

    def "should not be equal to null"() {
        given:
            Book book = new Book(new ISBN("123412341X"), new Title("Title"), new Author("Author"))
        expect:
            !book.equals(null)
    }

    def "should not be equal to different class"() {
        given:
            Book book = new Book(new ISBN("123412341X"), new Title("Title"), new Author("Author"))
        expect:
            !book.equals("not a book")
    }

    def "should have consistent toString format"() {
        given:
            Book book = new Book(new ISBN("123412341X"), new Title("Domain Driven Design"), new Author("Eric Evans"))
        when:
            String result = book.toString()
        then:
            result.contains("Book{")
            result.contains("isbn=")
            result.contains("title=")
            result.contains("author=")
    }
}


