package vttp.paf.day26workshop.repository;

import java.io.StringReader;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoExpression;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.paf.day26workshop.model.Comment;

@Repository
public class CommentRepository {

    @Autowired
    private MongoTemplate template;

    public List<Comment> search(String[] includes, String[] excludes, Integer offset, Integer limit) {

        // TextIndexDefinition textIndex = new TextIndexDefinitionBuilder()
        // .onField("c_text")
        // .build();

        // template.indexOps(Comment.class).ensureIndex(textIndex);

        TextCriteria criteria = TextCriteria.forDefaultLanguage()
                .matchingAny(includes)
                .notMatchingAny(excludes);

        TextQuery query = (TextQuery) TextQuery.queryText(criteria)
                .sortByScore()
                .includeScore("score")
                .skip(offset)
                .limit(limit);

        return template.find(query, Document.class, "comment")
                .stream()
                .map(c -> {
                    JsonReader reader = Json.createReader(new StringReader(c.toJson()));
                    return Comment.fromJson(reader.readObject());
                })
                .toList();
    }

    public JsonObject saveReview(Comment comment) {

        Document doc = template.insert(Document.parse(comment.toJson().toString()), "comment");
        doc.remove("_id");
        JsonReader reader = Json.createReader(new StringReader(doc.toJson()));
        return reader.readObject();
    }

    public Document findReview(Comment comment) {

        Query query = new Query(Criteria.where("user").is(comment.getUser())
                .and("gid").is(comment.getGid()));

        return template.findOne(query, Document.class, "comment");
    }

    public Document findReview(String id) {

        Query query = new Query(Criteria.where("_id").is(new ObjectId(id)));
        return template.findOne(query, Document.class, "comment");
    }

    public Boolean updateReview(Document doc, JsonObject update) {

        Query query = new Query(Criteria.where("_id").is(doc.getObjectId("_id")));
        Update updOps = new Update()
                .push("edited", Document.parse(update.toString()));

        return template.updateFirst(query, updOps, Document.class, "comment").getModifiedCount() > 0;
    }

    public List<Document> findRanked(Boolean highest) {

        AggregationOptions options = AggregationOptions.builder().allowDiskUse(true).build();

        String var = "";

        if (highest) 
            var = "highest_rating";
        else 
            var = "lowest_rating";

        GroupOperation groupOpsHigh = Aggregation.group("gid")
                .max("$rating").as(var)
                .push(new BasicDBObject("user", "$user")
                        .append("comment", "$c_text")
                        .append("rating", "$rating")
                        .append("cid", "$_id"))
                .as("reviews");
                        
        GroupOperation groupOpsLow = Aggregation.group("gid")
                .min("$rating").as(var)
                .push(new BasicDBObject("user", "$user")
                        .append("comment", "$c_text")
                        .append("rating", "$rating")
                        .append("cid", "$_id"))
                .as("reviews");

        UnwindOperation unwindOps1 = Aggregation.unwind("$reviews");

        ProjectionOperation projectOps1 = Aggregation.project(var)
                .and("$reviews.rating").as("rating")
                .and("$reviews.user").as("user")
                .and("$reviews.comment").as("comment")
                .and("$reviews.cid").as("cid");

        MatchOperation matchOpsHigh = Aggregation.match(
                AggregationExpression.from(MongoExpression.create("""
                        $expr: {$eq: ["$highest_rating", "$rating"]}                
                        """)));

        MatchOperation matchOpsLow = Aggregation.match(
                AggregationExpression.from(MongoExpression.create("""
                        $expr: {$eq: ["$lowest_rating", "$rating"]}
                        """)));

        // LookupOperation lookupOps = Aggregation.lookup("game", "_id", "gid", "game");

        // ProjectionOperation projectOps2 = Aggregation.project("rating", "user", "comment", "cid")
        //         .and("$game.name").as("name");

        // UnwindOperation unwindOps2 = Aggregation.unwind("$game");

        if (highest) {
            Aggregation pipeline = Aggregation.newAggregation(groupOpsHigh, unwindOps1, projectOps1, matchOpsHigh)
                    .withOptions(options);
            return template.aggregate(pipeline, "comment", Document.class).getMappedResults();

        } else {
            Aggregation pipeline = Aggregation.newAggregation(groupOpsLow, unwindOps1, projectOps1, matchOpsLow)
                    .withOptions(options);
            return template.aggregate(pipeline, "comment", Document.class).getMappedResults();
        }
    }
}
