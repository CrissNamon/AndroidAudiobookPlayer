package ru.hiddenproject.audioworld;

public class History {
    public int bookID;
    public int trackID;
    public int position;

    public History(int bookID, int trackID, int position) {
        this.bookID = bookID;
        this.trackID = trackID;
        this.position = position;
    }

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public int getTrackID() {
        return trackID;
    }

    public void setTrackID(int trackID) {
        this.trackID = trackID;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
