package easyuser.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import easyuser.service.FeaturedCollectionRecommenderService;

@Controller
@RequestMapping("/featured")
public class FeaturedCollectionRecommenderController {

    @Autowired
    private FeaturedCollectionRecommenderService service;

    @GetMapping
    public String featuedCollection(ModelMap modelMap) {
        modelMap.addAttribute("value", service.similarity());

        return "featured";
    }
}
