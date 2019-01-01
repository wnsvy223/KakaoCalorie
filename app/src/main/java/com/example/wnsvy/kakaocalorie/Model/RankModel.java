package com.example.wnsvy.kakaocalorie.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class RankModel implements Parcelable {
    private String userID;
    private String userEmail;
    private String userPhoto;
    private String footstep;
    private String distance;

    public RankModel(String userID,String title,String userPhoto,String footstep,String distance) {
        this.userID = userID;
        userEmail = title;
        this.userPhoto = userPhoto;
        this.footstep = footstep;
        this.distance = distance;
    }

    protected RankModel(Parcel in) {
        userID = in.readString();
        userEmail = in.readString();
        userPhoto = in.readString();
        footstep = in.readString();
        distance = in.readString();
    }

    public static final Creator<RankModel> CREATOR = new Creator<RankModel>() {
        @Override
        public RankModel createFromParcel(Parcel in) {
            return new RankModel(in);
        }

        @Override
        public RankModel[] newArray(int size) {
            return new RankModel[size];
        }
    };

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getFootstep() {
        return footstep;
    }

    public void setFootstep(String footstep) {
        this.footstep = footstep;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeString(userEmail);
        dest.writeString(userPhoto);
        dest.writeString(footstep);
        dest.writeString(distance);
    }
}
