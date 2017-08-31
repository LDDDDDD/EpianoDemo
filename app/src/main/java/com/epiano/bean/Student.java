package com.epiano.bean;

/**
 * Created by Administrator on 2017/8/10.
 */

public class Student {


    /**
     * StudentUserId : 3
     * UserName : junjun
     * PortraitId : 3
     * PhoneNum : 13530921311
     */

    private String StudentUserId;
    private String UserName;
    private String PortraitId;
    private String PhoneNum;

    public String getStudentUserId() {
        return StudentUserId;
    }

    public void setStudentUserId(String StudentUserId) {
        this.StudentUserId = StudentUserId;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

    public String getPortraitId() {
        return PortraitId;
    }

    public void setPortraitId(String PortraitId) {
        this.PortraitId = PortraitId;
    }

    public String getPhoneNum() {
        return PhoneNum;
    }

    public void setPhoneNum(String PhoneNum) {
        this.PhoneNum = PhoneNum;
    }
}
