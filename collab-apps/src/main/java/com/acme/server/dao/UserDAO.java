package com.acme.server.dao;

import com.acme.server.model.User;
import org.apache.log4j.Logger;
import java.util.Date;
import java.util.UUID;

public class UserDAO {

    private String userID;
    private String firstName;
    private String lastName;
    private String nickname;
    private long lastModified;
    private static Logger log = Logger.getLogger(UserDAO.class);

    public UserDAO(){log.info("UserDAO()");}

    public UserDAO(String firstName, String lastName, String nickname) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.userID = "USER:" + UUID.randomUUID().toString();
        this.lastModified = new Date().getTime();
    }

    public UserDAO(User user) {
        log.info("Entering UserDAO(User)");
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.nickname = user.getNickname();
        this.userID = "USER:" + UUID.randomUUID().toString();
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
}