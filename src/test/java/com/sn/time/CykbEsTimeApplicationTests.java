package com.sn.time;

import com.sn.time.elasticsearch.dao.ElasticSearchDao;
import com.sn.time.elasticsearch.entity.ElasticSearch;
import com.sn.time.strategy.SourceContent;
import io.searchbox.core.SearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class CykbEsTimeApplicationTests {

    @Autowired
    private ElasticSearchDao elasticSearchDao;

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void test3() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
