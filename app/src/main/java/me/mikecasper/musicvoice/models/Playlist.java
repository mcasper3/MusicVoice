package me.mikecasper.musicvoice.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Playlist implements Parcelable {
    private String uri;
    private String name;
    private String id;
    private List<Image> images;
    private Track tracks;

    public Playlist(String uri, String name, String id, List<Image> images, Track tracks) {
        this.uri = uri;
        this.name = name;
        this.id = id;
        this.images = images;
        this.tracks = tracks;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Image> getImages() {
        return images;
    }

    public Track getTracks() {
        return tracks;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uri);
        dest.writeString(name);
        dest.writeString(id);
        dest.writeTypedList(images);
        tracks.writeToParcel(dest, tracks.describeContents());
    }

    public static final Parcelable.Creator<Playlist> CREATOR = new Parcelable.Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel source) {
            return new Playlist(source);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    private Playlist(Parcel in) {
        this.uri = in.readString();
        this.name = in.readString();
        this.id = in.readString();
        this.images = in.createTypedArrayList(Image.CREATOR);
        this.tracks = Track.CREATOR.createFromParcel(in);
    }
}
