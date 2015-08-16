package com.fenix.audioplayer;

/**
 * Created by fenix on 15.08.2015.
 */
public class DirectoryData {
    private String folderName;
    private Integer count;

    public DirectoryData(String folderName, Integer count) {
        this.folderName = folderName;
        this.count = count;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
