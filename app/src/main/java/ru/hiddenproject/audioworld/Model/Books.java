package ru.hiddenproject.audioworld;

import java.util.List;

public class Books {
    public List<Book> books;
    public String v;

    public Books(List<Book> books, String v) {
        this.books = books;
        this.v = v;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }
}
