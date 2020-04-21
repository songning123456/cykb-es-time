package com.sn.time.service.impl;

import com.sn.time.elasticsearch.dao.ElasticSearchDao;
import com.sn.time.elasticsearch.entity.ElasticSearch;
import com.sn.time.entity.Chapters;
import com.sn.time.service.ChaptersService;
import com.sn.time.util.DateUtil;
import com.sn.time.util.HttpUtil;
import io.searchbox.core.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public void updateLatestChapter() {
        ElasticSearch novelsEsSearch = ElasticSearch.builder().index("novels_index").type("novels").size(10000).sort("createTime").build();
        List<SearchResult.Hit<Object, Void>> src;
        try {
            Map<String, Object> termParams = new HashMap<String, Object>(2) {{
                put("status", "连载中");
            }};
            src = elasticSearchDao.mustTermRangeQuery(novelsEsSearch, termParams, null);
            if (src != null && !src.isEmpty()) {
                for (SearchResult.Hit<Object, Void> item : src) {
                    String sourceUrl = String.valueOf(((Map) item.source).get("sourceUrl"));
                    String novelsId = String.valueOf(((Map) item.source).get("novelsId"));
                    Document listDoc = HttpUtil.getHtmlFromUrl(sourceUrl, true);
                    String latestChapter = listDoc.getElementById("info").getElementsByTag("p").get(3).getElementsByTag("a").get(0).html();
                    List<Map<String, Object>> isExistList = this.findByNovelsId(novelsId);
                    String lastChapter = String.valueOf(isExistList.get(isExistList.size() - 1).get("chapter"));
                    if (!latestChapter.equals(lastChapter)) {
                        Elements ddElements = listDoc.getElementById("list").getElementsByTag("dd");
                        String oldUpdateTime = isExistList.get(isExistList.size() - 1).get("updateTime").toString();
                        String strUpdateTime = listDoc.getElementById("info").getElementsByTag("p").get(2).html().split("：")[1];
                        Date newUpdateTime = DateUtil.strToDate(strUpdateTime, "yyyy-MM-dd HH:mm:ss");
                        List<String> timeList = DateUtil.stepTime(DateUtil.strToDate(oldUpdateTime, "yyyy-MM-dd HH:mm:ss"), newUpdateTime, ddElements.size() - isExistList.size());
                        for (int i = isExistList.size(), iLen = ddElements.size(); i < iLen; i++) {
                            Element chapterElement = ddElements.get(i).getElementsByTag("a").get(0);
                            String chapter = chapterElement.html();
                            boolean isExist = false;
                            for (Map<String, Object> val : isExistList) {
                                if (chapter.equals(String.valueOf(val.get("chapter")))) {
                                    isExist = true;
                                    break;
                                }
                            }
                            if (!isExist) {
                                String contentUrl = "http://www.147xiaoshuo.com/" + chapterElement.attr("href");
                                Document contentDoc = HttpUtil.getHtmlFromUrl(contentUrl, true);
                                String content = contentDoc.getElementById("content").html();
                                Chapters chapters = Chapters.builder().chapter(chapter).content(content).contentUrl(contentUrl).novelsId(novelsId).updateTime(timeList.get(i - isExistList.size())).build();
                                ElasticSearch chaptersEsSearch = ElasticSearch.builder().index("chapters_index").type("chapters").build();
                                elasticSearchDao.save(chaptersEsSearch, chapters);
                                log.info("当前小说novelsId: {}; 更新章节chapter: {}", novelsId, chapter);
                            }
                        }
                    }
                }
            } else {
                log.info("~~~库中不存在连载小说!~~~");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateContent() {

    }

    private List<Map<String, Object>> findByNovelsId(String novelsId) {
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
}
