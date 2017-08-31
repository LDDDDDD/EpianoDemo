package com.epiano.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/18.
 */

@SuppressWarnings("serial")
public class Lesson implements Serializable {


    /**
     * UserName : gengtian
     * EstablishTime : 2017-08-16 09:00:00
     * LastActTime : 2017-08-16 10:00:00
     */

    private long id;				//课程id
    private String UserName;
    private String EstablishTime;  //课程开始时间
    private String LastActTime;  //课程结束时间

    private int num;				//第几课
    private String lessonDate;		//上课日期
    private int status;				//0 未上 1已上2 系统确认


    public String getUserName() {
        return UserName;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public String getEstablishTime() {
        return EstablishTime;
    }

    public void setEstablishTime(String EstablishTime) {
        this.EstablishTime = EstablishTime;
    }

    public String getLastActTime() {
        return LastActTime;
    }

    public void setLastActTime(String LastActTime) {
        this.LastActTime = LastActTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getLessonDate() {
        return lessonDate;
    }

    public void setLessonDate(String lessonDate) {
        this.lessonDate = lessonDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLessonDateSTime(){
        return new StringBuffer().append(lessonDate).append(" ").append(this.EstablishTime).toString();
    }
}
