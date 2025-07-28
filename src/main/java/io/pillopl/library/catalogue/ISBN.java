package io.pillopl.library.catalogue;


import java.util.Objects;

class ISBN {

    private static final String ISBN_13_PATTERN = "^97[89]\\d{9}\\d$";

    private final String isbn;

    ISBN(String isbn) {
        String trimmedIsbn = isbn.trim();
        if (!trimmedIsbn.matches(ISBN_13_PATTERN)) {
            throw new IllegalArgumentException("Wrong ISBN! ISBN must be 13 digits starting with 978 or 979.");
        }
        if (!isValidISBN13Checksum(trimmedIsbn)) {
            throw new IllegalArgumentException("Invalid ISBN-13 checksum!");
        }
        this.isbn = trimmedIsbn;
    }

    private boolean isValidISBN13Checksum(String isbn) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(isbn.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit == Character.getNumericValue(isbn.charAt(12));
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
