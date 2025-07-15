package io.pillopl.library.catalogue;

class BookFixture {

    static final String DDD_ISBN_STR = "9780321125217";

    static final ISBN DDD_ISBN_10 = new ISBN(DDD_ISBN_STR);

    static final ISBN NON_PRESENT_ISBN = new ISBN("9780321125218");

    static final Book DDD = new Book(new ISBN(DDD_ISBN_STR), new Title("DDD"), new Author("Eric Evans"));
}
