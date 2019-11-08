package com.login.tt;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class currentJoinUser {


    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("nikName")
    @Expose
    public String nikName;

//    currentJoinUser(int id,String nikName){
//        id=id;
//        nikName=nikName;
//    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNikName() {
        return nikName;
    }

    public void setNikName(String nikName) {
        this.nikName = nikName;
    }
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(this.nikName);
        return sb.toString();
    }
}
