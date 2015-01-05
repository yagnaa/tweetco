package com.tweetco.dao;

import android.os.Parcel;
import android.os.Parcelable;

public class Tweet implements Parcelable {
    public String Id;
    public String tweetcontent;
    public String tweetowner;
    public String secondarytweetid;
    public String sourceuser;
    public String sourceiterator;
    public String retweeters;
    public String imageurl;
    public int iterator;
    public String upvoters;
    public String hashtags;
    public String contenttags;
    public String bookmarkers;
    public String hiders;
    public String __createdAt;
    public String __updatedAt;
    

  protected Tweet(Parcel in) {
      Id = in.readString();
      tweetcontent = in.readString();
      tweetowner = in.readString();
      secondarytweetid = in.readString();
      sourceuser = in.readString();
      sourceiterator = in.readString();
      retweeters = in.readString();
      imageurl = in.readString();
      iterator = in.readInt();
      upvoters = in.readString();
      hashtags = in.readString();
      contenttags = in.readString();
      bookmarkers = in.readString();
      hiders = in.readString();
      __createdAt = in.readString();
      __updatedAt = in.readString();
  }

  @Override
  public int describeContents() {
      return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(Id);
      dest.writeString(tweetcontent);
      dest.writeString(tweetowner);
      dest.writeString(secondarytweetid);
      dest.writeString(sourceuser);
      dest.writeString(sourceiterator);
      dest.writeString(retweeters);
      dest.writeString(imageurl);
      dest.writeInt(iterator);
      dest.writeString(upvoters);
      dest.writeString(hashtags);
      dest.writeString(contenttags);
      dest.writeString(bookmarkers);
      dest.writeString(hiders);
      dest.writeString(__createdAt);
      dest.writeString(__updatedAt);
  }

  public static final Parcelable.Creator<Tweet> CREATOR = new Parcelable.Creator<Tweet>() {
      @Override
      public Tweet createFromParcel(Parcel in) {
          return new Tweet(in);
      }

      @Override
      public Tweet[] newArray(int size) {
          return new Tweet[size];
      }
  };
}

