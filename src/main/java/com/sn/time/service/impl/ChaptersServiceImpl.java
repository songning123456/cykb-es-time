package com.sn.time.service.impl;

import com.sn.time.elasticsearch.dao.ElasticSearchDao;
import com.sn.time.elasticsearch.entity.ElasticSearch;
import com.sn.time.entity.Chapters;
import com.sn.time.service.ChaptersService;
import io.searchbox.core.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: songning
 * @date: 2020/4/6 11:09
 */
@Slf4j
@Service
public class ChaptersServiceImpl implements ChaptersService {

    @Autowired
    private ElasticSearchDao elasticSearchDao;

    @Override
    public List<Map<String, Object>> findByNovelsId(String novelsId) {
        ElasticSearch elasticSearch = ElasticSearch.builder().index("chapters_index").type("chapters").sort("updateTime").order("asc").build();
        Map<String, Object> termParams = new HashMap<String, Object>(2) {{
            put("novelsId", novelsId);
        }};
        List<SearchResult.Hit<Object, Void>> src = new ArrayList<>();
        try {
            src = elasticSearchDao.mustTermRangeQuery(elasticSearch, termParams, null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("findByNovelsId-fail: {}", e.getMessage());
        }
        List<Map<String, Object>> target = new ArrayList<>();
        for (SearchResult.Hit<Object, Void> objectVoidHit : src) {
            Map<String, Object> temp = new HashMap<>(2);
            temp.put("chapter", ((Map) objectVoidHit.source).get("chapter"));
            temp.put("updateTime", ((Map) objectVoidHit.source).get("updateTime"));
            target.add(temp);
        }
        return target;
    }

    @Override
    public void save(Chapters chapters) {
        ElasticSearch elasticSearch = ElasticSearch.builder().index("chapters_index").type("chapters").build();
        try {
            elasticSearchDao.save(elasticSearch, chapters);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("save Chapters fail: {}", e.getMessage());
        }
    }
}
