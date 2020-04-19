package com.sn.time.schedule;

import com.sn.time.elasticsearch.dao.ElasticSearchDao;
import com.sn.time.elasticsearch.entity.ElasticSearch;
import com.sn.time.strategy.SourceContent;
import io.searchbox.core.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author: songning
 * @date: 2020/4/6 15:38
 */
@Component
@EnableScheduling
@Slf4j
public class Schedule {

    @Autowired
    private ElasticSearchDao elasticSearchDao;
    @Resource(name = "SourceExecutor")
    private Executor sourceExecutor;

    /**
     * 每天凌晨执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateLatest() {
        try {
            List<String> sourceList = Arrays.asList("笔趣阁", "147小说", "天天书吧", "飞库小说", "趣书吧");
            ElasticSearch elasticSearch = ElasticSearch.builder().index("novels_index").type("novels").sort("createTime").order("desc").build();
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
                            map.put("sourceName", ((Map) src.get(i)).get("sourceName"));
                            map.put("sourceUrl", ((Map) src.get(i)).get("sourceUrl"));
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
