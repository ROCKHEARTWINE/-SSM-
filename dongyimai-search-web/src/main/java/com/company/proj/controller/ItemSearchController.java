package com.company.proj.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.company.proj.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemsearch")
public class ItemSearchController {
    @Reference
    private ItemSearchService itemSearchService;

    @RequestMapping("/search")
    public Map<String,Object> search(@RequestBody Map searchMap){
        Map<String,Object> searchResult = itemSearchService.search(searchMap);
        return searchResult;
    }
}
