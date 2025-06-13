package com.bytebender.premnoybiye.DBConnection;

import java.util.List;
import java.util.ArrayList;

public class userInfo {
    private String name = "";
    private String email = "";
    private String dob = "";
    private String gender = "";
    private String religion = "";
    private String city = "";
    private String image = "";
    private String education = "";
    private String profession = "";
    private String income = "";
    private String bio = "";
    private String prefAge = "";
    private String prefLocation = "";
    private String prefProfession = "";
    private String userId = ""; // Firebase Authentication UID used as primary key
    private List<String> likedUsers = new ArrayList<>(); // List of liked user IDs
    private List<String> rejectedUsers = new ArrayList<>(); // List of rejected user IDs

    public userInfo(String name, String email, String dob, String gender, String religion,
            String city, String image, String education, String profession, String income, String bio, String prefAge,
            String prefLocation, String prefProfession) {
        this.name = name;
        this.email = email;
        this.dob = dob;
        this.gender = gender;
        this.religion = religion;
        this.city = city;
        this.image = image;
        this.education = education;
        this.profession = profession;
        this.income = income;
        this.bio = bio;
        this.prefAge = prefAge;
        this.prefLocation = prefLocation;
        this.prefProfession = prefProfession;
        this.userId = ""; // Will be set to Firebase Authentication UID during login or registration
    }

    // Constructor with userId
    public userInfo(String name, String email, String dob, String gender, String religion,
            String city, String image, String education, String profession, String income, String bio, String prefAge,
            String prefLocation, String prefProfession, String userId) {
        this.name = name;
        this.email = email;
        this.dob = dob;
        this.gender = gender;
        this.religion = religion;
        this.city = city;
        this.image = image;
        this.education = education;
        this.profession = profession;
        this.income = income;
        this.bio = bio;
        this.prefAge = prefAge;
        this.prefLocation = prefLocation;
        this.prefProfession = prefProfession;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public String getReligion() {
        return religion;
    }

    public String getCity() {
        return city;
    }

    public String getImage() {
        return image;
    }

    public String getEducation() {
        return education;
    }

    public String getProfession() {
        return profession;
    }

    public String getIncome() {
        return income;
    }

    public String getBio() {
        return bio;
    }

    public String getPrefAge() {
        return prefAge;
    }

    public String getPrefLocation() {
        return prefLocation;
    }

    public String getPrefProfession() {
        return prefProfession;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public void setIncome(String income) {
        this.income = income;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setPrefAge(String prefAge) {
        this.prefAge = prefAge;
    }

    public void setPrefLocation(String prefLocation) {
        this.prefLocation = prefLocation;
    }

    public void setPrefProfession(String prefProfession) {
        this.prefProfession = prefProfession;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getLikedUsers() {
        return likedUsers;
    }

    public void setLikedUsers(List<String> likedUsers) {
        this.likedUsers = likedUsers;
    }

    public List<String> getRejectedUsers() {
        return rejectedUsers;
    }

    public void setRejectedUsers(List<String> rejectedUsers) {
        this.rejectedUsers = rejectedUsers;
    }

    // Helper methods to add individual users to liked/rejected lists
    public void addLikedUser(String userId) {
        if (!this.likedUsers.contains(userId)) {
            this.likedUsers.add(userId);
        }
    }

    public void addRejectedUser(String userId) {
        if (!this.rejectedUsers.contains(userId)) {
            this.rejectedUsers.add(userId);
        }
    }

    // Helper method to check if user has been seen (liked or rejected)
    public boolean hasSeenUser(String userId) {
        return this.likedUsers.contains(userId) || this.rejectedUsers.contains(userId);
    }
}