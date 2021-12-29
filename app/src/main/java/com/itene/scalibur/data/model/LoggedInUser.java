package com.itene.scalibur.data.model;

import android.os.Parcel;
import android.os.Parcelable;


public class LoggedInUser implements Parcelable {

    private Integer id;
    private String name;
    private String email;

    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }

    public LoggedInUser(Integer user_id, String name, String email) {
        this.id = user_id;
        this.name = name;
        this.email = email;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.email);
        dest.writeString(this.name);
    }

    protected LoggedInUser(Parcel in) {
        this.id = in.readInt();
        this.email = in.readString();
        this.name = in.readString();
    }


    public static final Parcelable.Creator<LoggedInUser> CREATOR = new Parcelable.Creator<LoggedInUser>() {
        @Override
        public LoggedInUser createFromParcel(Parcel source) {
            return new LoggedInUser(source);
        }

        @Override
        public LoggedInUser[] newArray(int size) {
            return new LoggedInUser[size];
        }
    };
}