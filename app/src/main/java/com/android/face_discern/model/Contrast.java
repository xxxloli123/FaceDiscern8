package com.android.face_discern.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class Contrast {

    @Id
    private Long id;
    // RedRepair

    private String name;

    private String age;

    private Long time;

    private String captureImg;

    private String registerImg;

    private String contrastPercentage;

    @Generated(hash = 1233162053)
    public Contrast(Long id, String name, String age, Long time, String captureImg,
            String registerImg, String contrastPercentage) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.time = time;
        this.captureImg = captureImg;
        this.registerImg = registerImg;
        this.contrastPercentage = contrastPercentage;
    }

    @Generated(hash = 1866461808)
    public Contrast() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return this.age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public Long getTime() {
        return this.time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getCaptureImg() {
        return this.captureImg;
    }

    public void setCaptureImg(String captureImg) {
        this.captureImg = captureImg;
    }

    public String getRegisterImg() {
        return this.registerImg;
    }

    public void setRegisterImg(String registerImg) {
        this.registerImg = registerImg;
    }

    public String getContrastPercentage() {
        return this.contrastPercentage;
    }

    public void setContrastPercentage(String contrastPercentage) {
        this.contrastPercentage = contrastPercentage;
    }

}
