package com.bytebender.premnoybiye.DBConnection;

public class userInfo {
    private String name = "";
    private String email = "";
    private String password = "";
    private String dob = "";
    private String gender = "";
    private String religion = "";
    private String city = "";

    public userInfo(String name, String email, String password, String dob, String gender, String religion,
            String city) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.dob = dob;
        this.gender = gender;
        this.religion = religion;
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
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
}