package vttp.paf.day26workshop.repository;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import vttp.paf.day26workshop.model.Game;

@Repository
public class GameRepository {

    @Autowired
    private MongoTemplate template;

    public Long countGames() {
        return template.count(new Query(), "game");
    }

    public List<Game> search(Integer offset, Integer limit) {

        // final PageRequest pageable = PageRequest.of(offset, limit);
        // Query query = new Query().with(pageable);

        Query query = new Query()
                .skip(offset)
                .limit(limit)
                .with(Sort.by(Direction.ASC, "gid"));

        return template.find(query, Document.class, "game")
                .stream()
                .map(g -> {
                    JsonReader reader = Json.createReader(new StringReader(g.toJson()));
                    return Game.fromJson(reader.readObject());
                })
                .toList();
    }

    public List<Game> getGamesByRank(Integer offset, Integer limit) {

        Query query = new Query()
                .skip(offset)
                .limit(limit)
                .with(Sort.by(Sort.Direction.ASC, "ranking"));

        return template.find(query, Document.class, "game")
                .stream()
                .map(g -> {
                JsonReader reader = Json.createReader(new StringReader(g.toJson()));
                return Game.fromJson(reader.readObject());
                })
                .toList();
    }
    
    public Optional<Game> getGameDetails(Integer gid) {

        Query query = new Query();
        query.addCriteria(Criteria.where("gid").is(gid));
        return Optional.ofNullable(template.findOne(query, Game.class, "game"));
    }

    public List<Document> getGameReviews(Integer gid) {

        LookupOperation lookupOps = Aggregation.lookup("comment", "gid", "gid", "reviews");
        MatchOperation matchOps = Aggregation.match(Criteria.where("gid").is(gid));

        Aggregation pipeline = Aggregation.newAggregation(lookupOps, matchOps);
        return template.aggregate(pipeline, "game", Document.class).getMappedResults();
    }
}
