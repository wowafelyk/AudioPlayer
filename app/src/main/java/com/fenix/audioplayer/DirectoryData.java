package com.fenix.audioplayer;

/**
 * Created by fenix on 15.08.2015.
 */
public class DirectoryData {
    private String foderName;
    private Integer count;

    public DirectoryData(String foderName, Integer count) {
        this.foderName = foderName;
        this.count = count;
    }

    public String getFoderName() {
        return foderName;
    }

    public void setFoderName(String foderName) {
        this.foderName = foderName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
