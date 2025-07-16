package io.pillopl.library.catalogue;

import java.util.Objects;

class Book {

    private final ISBN bookIsbn;
    private final Title title;
    private final Author author;

    Book(ISBN bookIsbn, Title title, Author author) {
        this.bookIsbn = Objects.requireNonNull(bookIsbn);
        this.title = Objects.requireNonNull(title);
        this.author = Objects.requireNonNull(author);
    }

    Book(String isbn, String author, String title) {
        this(
            new ISBN(isbn), 
            new Title(title), 
            new Author(author)
        );
    }

    public ISBN getBookIsbn() {
        return bookIsbn;
    }

    public Title getTitle() {
        return title;
    }

    public Author getAuthor() {
        return author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(bookIsbn, book.bookIsbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookIsbn);
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookIsbn=" + bookIsbn +
                ", title=" + title +
                ", author=" + author +
                '}';
    }
}


class Title {

    private final String title;

    Title(String title) {
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        this.title = title.trim();
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Title title1 = (Title) o;
        return Objects.equals(title, title1.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }

    @Override
    public String toString() {
        return "Title{" +
                "title='" + title + '\'' +
                '}';
    }
}

class Author {

    private final String name;

    Author(String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Author cannot be empty");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(name, author.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Author{" +
                "name='" + name + '\'' +
                '}';
    }
}
