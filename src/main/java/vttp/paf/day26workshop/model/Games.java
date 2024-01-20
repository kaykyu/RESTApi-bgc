package vttp.paf.day26workshop.model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

public class Games {

    private List<Game> games;
    private Integer offset;
    private Integer limit;
    private Integer total;
    private DateTime timestamp;

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Games() {
    }

    public Games(List<Game> games, Integer offset, Integer limit, Integer total, DateTime timestamp) {
        this.games = games;
        this.offset = offset;
        this.limit = limit;
        this.total = total;
        this.timestamp = timestamp;
    }

    public JsonObject toJson() {

        List<Game> games = getGames();
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (Game game : games) {
            builder.add(game.toJson());
        }

        return Json.createObjectBuilder()
                .add("games", builder.build())
                .add("offset", getOffset())
                .add("limit", getLimit())
                .add("total", getTotal())
                .add("timestamp", getTimestamp().toString())
                .build();
    }

    public static Games fromJson(JsonObject games) {

        JsonArray array = games.getJsonArray("games");
        List<Game> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(Game.fromJson((JsonObject) array.get(i)));
        }
        
        return new Games(
            list,
            games.getInt("offset"),
            games.getInt("limit"),
            games.getInt("total"),
            //DateTime(games.getString("timestamp")
            null
            );
    }
}
