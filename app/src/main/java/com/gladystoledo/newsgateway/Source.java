package com.gladystoledo.newsgateway;

import java.io.Serializable;

public class Source implements Serializable, Comparable {
    private final String id;
    private final String name;
    private final String category;

    public Source(String id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        Source s = (Source) o;
        return this.getName().equals(s.getName());
    }

    //@Override
    public int compareTo(Object o) {
        Source s = (Source) o;
        return this.getName().compareTo(s.getName());
    }

}
