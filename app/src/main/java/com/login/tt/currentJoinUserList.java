package com.login.tt;



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class currentJoinUserList {


        @SerializedName("list")
        @Expose
        private ArrayList<currentJoinUser> list = null;

        public ArrayList<currentJoinUser> getList() {
            return list;
        }

        public void setList(ArrayList<currentJoinUser> list) {
            this.list = list;
        }
}
