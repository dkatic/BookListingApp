package com.example.david.booklistingapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by david on 27.7.2017..
 */

public class Book implements Parcelable {

    private String title;
    private ArrayList<String> authors;
    private String description;

    public Book() {}

    public Book(Parcel in) {
        title = in.readString();
        authors = in.readArrayList(null);
        description = in.readString();
    }

    //Get and Set methods
    public String getTitle() {
        return this.title;
    }

    public ArrayList<String> getAuthors() {
        return this.authors;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthors(ArrayList<String> authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeList(authors);
        dest.writeString(description);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {

        @Override
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

}
