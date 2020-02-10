package com.example.socialconnection.Model;

import java.util.ArrayList;

public class Images {
    String str_folder;

    public Images(){}

    public Images(String str_folder, ArrayList<String> al_imagepath) {
        this.str_folder = str_folder;
        this.al_imagepath = al_imagepath;
    }

    public String getStr_folder() {
        return str_folder;
    }

    public void setStr_folder(String str_folder) {
        this.str_folder = str_folder;
    }

    public ArrayList<String> getAl_imagepath() {
        return al_imagepath;
    }

    public void setAl_imagepath(ArrayList<String> al_imagepath) {
        this.al_imagepath = al_imagepath;
    }

    ArrayList<String> al_imagepath;

}
