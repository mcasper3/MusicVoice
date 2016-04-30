package me.mikecasper.musicvoice.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Track implements Parcelable {
    private int duration_ms;
    private String uri;
    private String name;
    private boolean is_playable;
    private Album album;
    private List<Artist> artists;
    private int total;

    public Track(int duration_ms, String uri, String name, boolean is_playable, Album album, List<Artist> artists, int total) {
        this.duration_ms = duration_ms;
        this.uri = uri;
        this.name = name;
        this.is_playable = is_playable;
        this.album = album;
        this.artists = artists;
        this.total = total;
    }

    public int getDuration() {
        return duration_ms;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public boolean isPlayable() {
        return is_playable;
    }

    public Album getAlbum() {
        return album;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public int getTotal() {
        return total;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(duration_ms);
        dest.writeString(uri);
        dest.writeString(name);
        dest.writeInt(is_playable ? 1 : 0);
        album.writeToParcel(dest, album.describeContents());
        dest.writeTypedList(artists);
        dest.writeInt(total);
    }

    public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel source) {
            return new Track(source);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    private Track(Parcel in) {
        this.duration_ms = in.readInt();
        this.uri = in.readString();
        this.name = in.readString();
        this.is_playable = in.readInt() == 1;
        this.album = Album.CREATOR.createFromParcel(in);
        this.artists = in.createTypedArrayList(Artist.CREATOR);
        this.total = in.readInt();
    }
}