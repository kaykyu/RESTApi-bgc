package vttp.paf.day26workshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vttp.paf.day26workshop.service.SearchService;

@Controller
@RequestMapping(path = "/search")
public class SearchController {

    @Autowired
    SearchService svc;

    @GetMapping
    public ResponseEntity<String> getSearch(@RequestParam("title") String search) {
        return ResponseEntity.ok(svc.getGameByName(search).toString());
    }
}
