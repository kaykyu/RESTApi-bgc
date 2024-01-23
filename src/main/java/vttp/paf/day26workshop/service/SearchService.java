package vttp.paf.day26workshop.service;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import vttp.paf.day26workshop.model.Comment;
import vttp.paf.day26workshop.model.Game;
import vttp.paf.day26workshop.model.Games;
import vttp.paf.day26workshop.repository.CommentRepository;
import vttp.paf.day26workshop.repository.GameRepository;

@Service
public class SearchService {

    @Autowired
    GameRepository gameRepo;

    @Autowired
    CommentRepository comRepo;

    public String checkNullString(String check) {

        if (check == null)
            return "";
        return check;
    }

    public JsonObject getGames(Integer offset, Integer limit) {

        Games result = new Games(gameRepo.search(offset, limit), offset, limit, gameRepo.countGames().intValue(), DateTime.now());
        return result.toJson();
    }

    public JsonObject getRankedGames(Integer offset, Integer limit) {

        Games result = new Games(gameRepo.getGamesByRank(offset, limit), offset, limit,
                gameRepo.countGames().intValue(), DateTime.now());
        return result.toJson();
    }
    
    public JsonObject findGames(Integer gid) {

        Optional<Game> game = gameRepo.getGameDetails(gid);
        if (game.isEmpty())
            return JsonObject.EMPTY_JSON_OBJECT;
        return game.get().toJson();
    }
    
    public JsonArray findComments(String request, String without, Integer offset, Integer limit) {

        Object include = new Object();
        Object exclude = new Object();
        if (request.contains("+"))
            include = request.split("+");
        else
            include = new String[] { request };

        if (without.contains("+"))
            exclude = without.split("+");
        else
            exclude = new String[] { without };

        JsonArrayBuilder builder = Json.createArrayBuilder();
        List<Comment> list = comRepo.search((String[]) include, (String[]) exclude, offset, limit);
        for (Comment c : list) {
            builder.add(c.toJson());
        }

        return builder.build();
    }
    
    public JsonObject saveReview(Comment comment) {
        return comRepo.saveReview(comment);
    }

    public Boolean updateReview(Comment comment) {

        Document doc = comRepo.findReview(comment);
        if (doc == null)
            return false;

        JsonObject update = Json.createObjectBuilder()
                .add("comment", comment.getComment())
                .add("rating", comment.getRating())
                .add("posted", comment.getPosted().toString())
                .build();

        return comRepo.updateReview(doc, update);
    }
    
    public JsonObject getReview(String id, Boolean history) {

        Document doc = comRepo.findReview(id);

        if (doc == null) {
            return JsonObject.EMPTY_JSON_OBJECT;
        }

        Game game = gameRepo.getGameDetails(doc.getInteger("gid")).get();
        Integer rating = doc.getInteger("rating");
        String comment = doc.getString("c_text");
        String posted = doc.getString("posted");
        Boolean edited = false;

        if (doc.get("edited") != null) {

            List<Document> edits = doc.getList("edited", Document.class);
            Document edit = edits.get(edits.size() - 1);

            JsonObject latest = Json.createReader(new StringReader(edit.toJson())).readObject();
            rating = latest.getInt("rating");
            comment = latest.getString("comment");
            posted = latest.getString("posted");
            edited = true;
        }

        if (!history || !edited)
            return Json.createObjectBuilder()
                    .add("user", doc.getString("user"))
                    .add("rating", rating)
                    .add("comment", checkNullString(comment))
                    .add("ID", doc.getInteger("gid"))
                    .add("posted", checkNullString(posted))
                    .add("name", game.getName())
                    .add("edited", edited)
                    .add("timestamp", DateTime.now().toString())
                    .build();

        List<Document> edits = doc.getList("edited", Document.class);
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (Document d : edits) {
            builder.add(Json.createReader(new StringReader(d.toJson())).readObject());
        }

        return Json.createObjectBuilder()
                .add("user", doc.getString("user"))
                .add("rating", rating)
                .add("comment", comment)
                .add("ID", doc.getInteger("gid"))
                .add("posted", posted)
                .add("name", game.getName())
                .add("edited", builder.build())
                .add("timestamp", DateTime.now().toString())
                .build();
    }
    
    public JsonObject getGameReviews(Integer id) {

        List<Document> docs = gameRepo.getGameReviews(id);

        if (docs.size() <= 0)
            return JsonObject.EMPTY_JSON_OBJECT;

        Document doc = docs.get(0);
        List<Document> reviews = doc.getList("reviews", Document.class);
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (Document review : reviews) {
            builder.add("/review/%s".formatted(review.getObjectId("_id").toString()));
        }

        return Json.createObjectBuilder()
                .add("game_id", id)
                .add("name", doc.getString("name"))
                .add("year", doc.getInteger("year"))
                .add("rank", doc.getInteger("ranking"))
                .add("users_rated", doc.getInteger("users_rated"))
                .add("url", doc.getString("url"))
                .add("thumbnail", doc.getString("image"))
                .add("reviews", builder.build())
                .add("timestamp", DateTime.now().toString())
                .build();
    }
    
    public JsonObject getRanked(Boolean highest) {

        String var = "";
        List<Document> docs = comRepo.findRanked(highest);
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (Document doc : docs) {

            Integer id = doc.getInteger("_id");
            Game game = gameRepo.getGameDetails(id).get();

            builder.add(Json.createObjectBuilder()
                    .add("_id", id)
                    .add("name", game.getName())
                    .add("rating", doc.getInteger("rating"))
                    .add("user", doc.getString("user"))
                    .add("comment", doc.getString("comment"))
                    .add("review_id", doc.getObjectId("cid").toHexString())
                    .build());
        }

        if (highest)
            var = "highest";
        else
            var = "lowest";

        return Json.createObjectBuilder()
                .add("rating", var)
                .add("games", builder.build())
                .add("timestamp", DateTime.now().toString())
                .build();
    }
    
    public JsonArray getGameByName(String name) {

        List<Document> docs = gameRepo.getGameByName(name);
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (Document game : docs) {
            List<Document> reviews = game.getList("comments", Document.class);
            JsonArrayBuilder builder = Json.createArrayBuilder();
            for (Document review : reviews) {
                builder.add(Json.createObjectBuilder()
                        .add("user", review.getString("user"))
                        .add("rating", review.getInteger("rating"))
                        .add("c_text", review.getString("c_text"))
                        .build());
            }

            result.add(Json.createObjectBuilder()
                    .add("_id", game.getInteger("_id"))
                    .add("name", game.getString("name"))
                    .add("ranking", game.getInteger("ranking"))
                    .add("comments", builder.build())
                    .build());
        }

        return result.build();
    }
}
