package vttp.paf.day26workshop.model;

import org.joda.time.DateTime;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class Comment {

    private String user;
    private Integer rating;
    private String comment;
    private Integer gid;
    private String gname;
    private DateTime posted;

    public Comment(String user, Integer rating, String comment, Integer gid, String gname, DateTime posted) {
        this.user = user;
        this.rating = rating;
        this.comment = comment;
        this.gid = gid;
        this.gname = gname;
        this.posted = posted;
    }

    public Comment() {
    }

    @Override
    public String toString() {
        return "Comment [user=%s, rating=%d, text=%s, gid=%d, gname=%s, posted=%s]".formatted(user, rating, comment, gid, gname, posted.toString());
    }

    public JsonObject toJson() {

        return Json.createObjectBuilder()
                .add("user", getUser())
                .add("rating", getRating())
                .add("c_text", getComment())
                .add("gid", getGid())
                .add("g_name", getGname())
                .add("posted", getPosted().toString())
                .build();
    }

    public static Comment fromJson(JsonObject comment) {

        return new Comment(
                comment.getString("user"),
                comment.getInt("rating"),
                comment.getString("c_text"),
                comment.getInt("gid"),
                comment.getString("g_name"),
                DateTime.parse(comment.getString("posted")));
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public String getGname() {
        return gname;
    }

    public void setGname(String gname) {
        this.gname = gname;
    }

    public DateTime getPosted() {
        return posted;
    }

    public void setPosted(DateTime posted) {
        this.posted = posted;
    }
}
