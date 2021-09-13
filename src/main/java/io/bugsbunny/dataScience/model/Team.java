package io.bugsbunny.dataScience.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public class Team implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private List<Scientist> scientists;

    public Team() {
    }

    public List<Scientist> getScientists() {
        return scientists;
    }

    public void setScientists(List<Scientist> scientists) {
        this.scientists = scientists;
    }
}
