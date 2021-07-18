package com.example.hitchikersguide;

public class SpacePic {
    String imgDate;
    String imgTitle;
    String imgDescription;
    String imgURL;
    String imgHDURL;
    long imgID;

    public SpacePic(String[] args, long msgID){
        // send in parameters from JSON and save them.
        this.imgDate = args[0];
        this.imgURL = args[1];
        this.imgHDURL = args[2];
        if (args.length>3){
            this.imgDescription = args[3];
        }
        this.imgID = imgID;
    }

    // save title for the image
}