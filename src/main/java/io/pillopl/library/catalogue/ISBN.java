package io.pillopl.library.catalogue;


import java.util.Objects;

class ISBN {

    private static final String VERY_SIMPLE_ISBN_CHECK = "^(\\d{9}[\\d|X]|97[89]\\d{10})$";

    private final String isbn;

    ISBN(String isbn) {
        if (!isbn.trim().matches(VERY_SIMPLE_ISBN_CHECK)) {
            throw new IllegalArgumentException("Wrong ISBN!");
        }
        this.isbn = isbn.trim();
    }

    public String getIsbn() {
        return isbn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ISBN isbn1 = (ISBN) o;
        return Objects.equals(isbn, isbn1.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return "ISBN{" +
                "isbn='" + isbn + '\'' +
                '}';
    }
}
