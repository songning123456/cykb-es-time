package com.sn.time;

import com.sn.time.elasticsearch.dao.ElasticSearchDao;
import com.sn.time.elasticsearch.entity.ElasticSearch;
import com.sn.time.strategy.SourceContent;
import io.searchbox.core.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Executor;

@SpringBootTest
@Slf4j
class CykbEsTimeApplicationTests {

    @Autowired
    private ElasticSearchDao elasticSearchDao;
    @Resource(name = "SourceExecutor")
    private Executor sourceExecutor;

    @Test
    void test1() {
        try {
            String sourceName = "天天书吧";
            ElasticSearch elasticSearch = ElasticSearch.builder().index("novels_index").type("novels").sort("createTime").order("asc").build();
            Map<String, Object> termParams = new HashMap<String, Object>(2) {{
                put("sourceName", sourceName);
            }};
            List<SearchResult.Hit<Object, Void>> src = elasticSearchDao.mustTermRangeQuery(elasticSearch, termParams, null);
            // 排除最后一个正在新增的小说
            for (int i = 0, length = src.size(); i < length; i++) {
                Map<String, Object> map = new HashMap<>(2);
                map.put("novelsId", src.get(i).id);
                map.put("sourceName", ((Map) src.get(i).source).get("sourceName"));
                map.put("sourceUrl", ((Map) src.get(i).source).get("sourceUrl"));
                System.out.println(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void test2() {
        try {
            String novelsId = "hRlcg3EBj8NokppAwbyT";
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void test3() {
        try {
            List<String> sourceList = Arrays.asList("笔趣阁", "147小说", "天天书吧", "飞库小说", "趣书吧");
            ElasticSearch elasticSearch = ElasticSearch.builder().index("novels_index").type("novels").sort("createTime").build();
            for (String sourceName : sourceList) {
                sourceExecutor.execute(() -> {
                    try {
                        Map<String, Object> termParams = new HashMap<String, Object>(2) {{
                            put("sourceName", sourceName);
                        }};
                        List<SearchResult.Hit<Object, Void>> src = elasticSearchDao.mustTermRangeQuery(elasticSearch, termParams, null);
                        // 排除最后一个正在新增的小说
                        for (int i = 0, length = src.size(); i < length - 1; i++) {
                            Map<String, Object> map = new HashMap<>(2);
                            map.put("novelsId", src.get(i).id);
                            map.put("sourceName", ((Map) src.get(i).source).get("sourceName"));
                            map.put("sourceUrl", ((Map) src.get(i).source).get("sourceUrl"));
                            SourceContent.doSource(map);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("更新文章失败: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("定时更新失败: {}", e.getMessage());
        }
    }

}
