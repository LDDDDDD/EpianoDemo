package com.epiano.bean;

import java.io.Serializable;

public class Song implements Serializable {


    /**
     * idx : 1
     * songname : 梦中的婚礼（指法版，克莱德曼演奏版）
     * writer : 理查德.克莱德曼
     * pyname : MZDHL_ZFB_KLDMYZB_
     */

    private String idx;
    private String songname;
    private String writer;
    private String pyname;

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getSongname() {
        return songname;
    }

    public void setSongname(String songname) {
        this.songname = songname;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getPyname() {
        return pyname;
    }

    public void setPyname(String pyname) {
        this.pyname = pyname;
    }
}
