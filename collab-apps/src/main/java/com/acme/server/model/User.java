package com.acme.server.model;

import com.acme.server.util.ActionLinks;
import org.apache.log4j.Logger;

import java.util.UUID;
import java.util.List;


public class User {

    private String userID;
    private String firstName;
    private String lastName;
    private String nickname;
    private List<ActionLinks> nextActionLinks;

    private static Logger log = Logger.getLogger(User.class);

    //Used from UserService when building a container resource for gson parsing
    public User(){}

    //Used from a POST operation when creating a new User
    public User(String firstName, String lastName, String nickname) {
        this.userID = "USER:" + UUID.randomUUID().toString();
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
    }

    //Used when Redis is retrieving existing data
    public User(String firstName, String lastName, String nickname, String userID) {
        log.info("Entering User: " + firstName + lastName + nickname + userID);
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
    }

    public String getUserID() {
        return userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNickname() {
        return nickname;
    }

    public List<ActionLinks> getNextActionLinks() {
        return nextActionLinks;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setNextActionLinks(List<ActionLinks> nextActionLinks) {
        this.nextActionLinks = nextActionLinks;
    }

}