package vttp.paf.day26workshop.model;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class Game {

    private Integer gid;
    private String name;
    private Integer year;
    private Integer ranking;
    private Integer users_rated;
    private String url;
    private String image;

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public Integer getUsersRated() {
        return users_rated;
    }

    public void setUsersRated(Integer usersRated) {
        this.users_rated = usersRated;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Game() {
    }

    public Game(Integer gid, String name, Integer year, Integer ranking, Integer usersRated, String url, String image) {
        this.gid = gid;
        this.name = name;
        this.year = year;
        this.ranking = ranking;
        this.users_rated = usersRated;
        this.url = url;
        this.image = image;
    }

    @Override
    public String toString() {
        return "Game [gid=%d, name=%s, year=%d, ranking=%d, usersRated=%d, url=%s, image=%s]".formatted(gid, name, year, ranking, users_rated, url, image);
    }

    public JsonObject toJson() {
        
        return Json.createObjectBuilder()
                .add("gid", getGid())
                .add("name", getName())
                .add("year", getYear())
                .add("ranking", getRanking())
                .add("users_rated", getUsersRated() != null ? getUsersRated() : 0)
                .add("url", getUrl())
                .add("image", getImage())
                .build();
    }

    public static Game fromJson(JsonObject game) {

        return new Game(
            game.getInt("gid"),
            game.getString("name"),
            game.getInt("year"),
            game.getInt("ranking"),
            game.getInt("users_rated"),
            game.getString("url"),
            game.getString("image")
        );
    }
}
