package io.bugsbunny.dataScience.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class Scientist implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private String email;

    public Scientist() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
