package ru.hiddenproject.audioworld;

import java.util.List;

public class Book {
    public String title;
    public String author;
    public String img;
    public String reader;
    public String info;
    public int id;
    public List<String> tracks;
    public List<String> genres;
    public String status = "success";

    public Book(String title, String author, String img, String reader, String info, int id, List<String> tracks, List<String> genres, String status) {
        this.title = title;
        this.author = author;
        this.img = img;
        this.reader = reader;
        this.info = info;
        this.id = id;
        this.tracks = tracks;
        this.genres = genres;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getReader() {
        return reader;
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getTracks() {
        return tracks;
    }

    public void setTracks(List<String> tracks) {
        this.tracks = tracks;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
