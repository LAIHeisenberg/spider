package yyt.com.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by YinYueTai-DEV on 2016/3/24.
 */
public class AlbumInfo {

    private String albumName;
    private String artist;
    private String language;
    //唱片公司
    private String company;
    //发行时间
    private String releaseTime;
    //专辑类别
    private String type;
    //专辑风格
    private String style;
    private String albumUrl;


    public AlbumInfo() {
    }

    public AlbumInfo(String albumName, String artist, String language, String company, String releaseTime, String type, String style) {
        this.albumName = albumName;
        this.artist = artist;
        this.language = language;
        this.company = company;
        this.releaseTime = releaseTime;
        this.type = type;
        this.style = style;
    }

    public AlbumInfo(String[] infos) {
        int length = infos.length;
        this.albumName = length > 0 ? infos[0] : null;
        this.artist = length > 1 ? infos[1] : null;
        this.language = length > 2 ? infos[2] : null;
        this.company = length > 3 ? infos[3] : null;
        this.releaseTime = length > 4 ? infos[4] : null;
        this.type = length > 5 ? infos[5] : null;
        this.style = length > 6 ? infos[6] : null;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(String releaseTime) {
        this.releaseTime = releaseTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }


    @Override
    public String toString() {
        return albumName+"}"+artist+"}"+company+"}"+language+"}"+releaseTime+"}"+type+"}"+style+"}"+albumUrl;
    }
}
