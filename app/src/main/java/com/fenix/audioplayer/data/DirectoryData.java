package com.fenix.audioplayer.data;

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

    public Integer getCount() {
        return count;
    }
}
