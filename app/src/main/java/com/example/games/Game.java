package com.example.games;

import android.util.EventLogTags;

import java.util.ArrayList;
//42 Tento objekt predstavuje informácie o jednej hre
public class Game {
    private boolean favorite;
    private String id;
    private String name;
    private String background_image;
    private String rating;
    private ArrayList<String> platforms;
    private ArrayList<String> short_screenshots;
    private String released;
    //dalsie
    private String description;

    public Game(){
        this.id = "error";
        this.name = "error";
        this.background_image = "error";
        this.rating = "error";
        this.platforms = new ArrayList<>();
        platforms.add("error");
        this.short_screenshots = new ArrayList<>();
        short_screenshots.add("error");
        this.released = "error";
        this.favorite = false;
    }

    /**
     *
     * @param id id
     * @param name name
     * @param background_image background_image
     * @param rating rating
     * @param platforms platforms
     * @param released released
     */
    public Game(String id, String name, String background_image, String rating, ArrayList<String> platforms, ArrayList<String> short_screenshots, String released) {
        this.id = id;
        this.name = name;
        this.background_image = background_image;
        this.rating = rating;
        this.platforms = platforms;
        this.short_screenshots = short_screenshots;
        this.released = released;
        this.favorite =false;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBackground_image() {
        return background_image;
    }

    public void setBackground_image(String background_image) {
        this.background_image = background_image;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public ArrayList<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(ArrayList<String> platforms) {
        this.platforms = platforms;
    }

    public ArrayList<String> getShort_screenshots() {
        return short_screenshots;
    }

    public void setShort_screenshots(ArrayList<String> short_screenshots) {
        this.short_screenshots = short_screenshots;
    }

    public String getReleased() {
        return released;
    }

    public void setReleased(String released) {
        this.released = released;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
