package vttp.paf.day26workshop.controller;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.paf.day26workshop.model.Comment;
import vttp.paf.day26workshop.service.SearchService;

@RestController
public class ApiController {

    @Autowired
    SearchService svc;

    @GetMapping(path = "/games")
    public ResponseEntity<String> getGames(@RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "25") Integer limit) {

        return ResponseEntity.status(HttpStatusCode.valueOf(201)).body(svc.getGames(offset, limit).toString());
    }

    @GetMapping(path = "/games/rank")
    public ResponseEntity<String> getRankedGames(@RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "25") Integer limit) {

        return ResponseEntity.status(HttpStatusCode.valueOf(201)).body(svc.getRankedGames(offset, limit).toString());
    }

    @GetMapping(path = "/game/{id}")
    public ResponseEntity<String> findGame(@PathVariable("id") Integer gid) {

        JsonObject result = svc.findGames(gid);
        if (result.isEmpty())
            return ResponseEntity.status(HttpStatusCode.valueOf(404))
                    .body("Game with ID %d does not exist.".formatted(gid));
        return ResponseEntity.status(HttpStatusCode.valueOf(201)).body(result.toString());
    }

    @GetMapping(path = "/comment")
    public ResponseEntity<String> searchComments(@RequestParam MultiValueMap<String, String> query,
            @RequestParam(defaultValue = "0") Integer offset, @RequestParam(defaultValue = "25") Integer limit) {

        JsonArray result = svc.findComments(query.getFirst("q"), query.getFirst("without"), offset, limit);
        return ResponseEntity.status(HttpStatusCode.valueOf(201)).body(result.toString());
    }

    @PostMapping(path = "/review")
    public ResponseEntity<String> postReview(@RequestBody MultiValueMap<String, String> payload) {

        Comment comment = new Comment(
                payload.getFirst("user"),
                Integer.valueOf(payload.getFirst("rating")),
                payload.getFirst("comment"),
                Integer.valueOf(payload.getFirst("gid")),
                null,
                DateTime.now());

        JsonObject game = svc.findGames(comment.getGid());
        if (game.isEmpty())
            return ResponseEntity.status(HttpStatusCode.valueOf(404)).body("Game ID is not valid.");

        comment.setGname(game.getString("name"));
        return ResponseEntity.status(HttpStatusCode.valueOf(201)).body(svc.saveReview(comment).toString());
    }

    @PutMapping(path = "/review")
    public ResponseEntity<String> putReview(@ModelAttribute Comment comment) {

        comment.setPosted(DateTime.now());

        if (svc.updateReview(comment))
            return ResponseEntity.ok("Success");
        return ResponseEntity.status(HttpStatusCode.valueOf(404)).body("Review does not exist.");
    }

    @GetMapping(path = "/review/{id}")
    public ResponseEntity<String> getReview(@PathVariable("id") String id) {

        JsonObject result = svc.getReview(id, false);
        if (result.isEmpty())
            return ResponseEntity.status(HttpStatusCode.valueOf(404)).body("Review does not exist.");
        return ResponseEntity.ok(result.toString());
    }
    
    @GetMapping(path = "review/{id}/history")
    public ResponseEntity<String> getReviewHistory(@PathVariable("id") String id) {

        JsonObject result = svc.getReview(id, true);
        if (result.isEmpty())
            return ResponseEntity.status(HttpStatusCode.valueOf(404)).body("Review does not exist.");
        return ResponseEntity.ok(result.toString());
    }

    @GetMapping(path = "/game/{id}/reviews")
    public ResponseEntity<String> getGameReviews(@PathVariable("id") Integer id) {

        JsonObject result = svc.getGameReviews(id);
        if (result.isEmpty())
            return ResponseEntity.status(HttpStatusCode.valueOf(404)).body("Game does not exist.");
        return ResponseEntity.ok(result.toString());
    }
    
    @GetMapping(path = "/games/highest")
    public ResponseEntity<String> getHighest() {
       return ResponseEntity.ok(svc.getRanked(true).toString());
    }

    @GetMapping(path = "/games/lowest")
    public ResponseEntity<String> getLowest() {
        return ResponseEntity.ok(svc.getRanked(false).toString());
    }
}
