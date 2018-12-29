package com.example.wnsvy.kakaocalorie.Model;

public class RankModel {

    private String userEmail;
    private String userPhoto;
    private String footstep;
    private String distance;

    public RankModel(String title,String userPhoto,String footstep,String distance) {
        userEmail = title;
        this.userPhoto = userPhoto;
        this.footstep = footstep;
        this.distance = distance;
    }

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
}
