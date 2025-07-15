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
            ISBN isbn = new ISBN("9780321125217")
        then:
            isbn.isbn == "9780321125217"
    }

    def "isbn should be trimmed"() {
        when:
            ISBN isbn = new ISBN("  9780321125218  ")
        then:
            isbn.isbn == "9780321125218"
    }

    def "wrong isbn should not be accepted"() {
        when:
            new ISBN("not isbn")
        then:
            thrown(IllegalArgumentException)
    }
}


