package me.mikecasper.musicvoice.models;

public class SpotifyUser {

    public static final String NAME = "spotifyUserName";
    public static final String PROFILE_IMAGE = "spotifyUserProfileImage";

    private String id;
    private String uri;
    private Image[] images;
    private String display_name;

    public SpotifyUser(String id, String uri, Image[] images, String display_name) {
        this.id = id;
        this.uri = uri;
        this.images = images;
        this.display_name = display_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Image[] getImages() {
        return images;
    }

    public void setImages(Image[] images) {
        this.images = images;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }
}
