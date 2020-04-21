package com.sn.time.strategy.impl;

import com.sn.time.entity.Chapters;
import com.sn.time.service.impl.ChaptersServiceImpl;
import com.sn.time.util.DateUtil;
import com.sn.time.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: songning
 * @date: 2020/4/5 15:09
 */
@Slf4j
public class YisiqiSourceServiceImpl {
    @Override
    public void updateChapter(Map<String, Object> map) {
        String sourceUrl = String.valueOf(map.get("sourceUrl"));
        String novelsId = String.valueOf(map.get("novelsId"));
        Document baseDoc = HttpUtil.getHtmlFromUrl(sourceUrl, true);
        String latestChapter = baseDoc.getElementById("info").getElementsByTag("p").get(3).getElementsByTag("a").get(0).html();
        ChaptersService chaptersService = BeanUtil.getBean(ChaptersServiceImpl.class);
        List<Map<String, Object>> isExistList = chaptersService.findByNovelsId(novelsId);
        String lastChapter = String.valueOf(isExistList.get(isExistList.size() - 1).get("chapter"));
        if (!latestChapter.equals(lastChapter)) {
            Elements ddElements = baseDoc.getElementById("list").getElementsByTag("dd");
            String oldUpdateTime = isExistList.get(isExistList.size() - 1).get("updateTime").toString();
            String strUpdateTime = baseDoc.getElementById("info").getElementsByTag("p").get(2).html().split("：")[1];
            Date newUpdateTime = DateUtil.strToDate(strUpdateTime, "yyyy-MM-dd HH:mm:ss");
            List<String> timeList = DateUtil.stepTime(DateUtil.strToDate(oldUpdateTime, "yyyy-MM-dd HH:mm:ss"), newUpdateTime, ddElements.size() - isExistList.size());
            for (int i = isExistList.size(), iLen = ddElements.size(); i < iLen; i++) {
                Element chapterElement = ddElements.get(i).getElementsByTag("a").get(0);
                String chapter = chapterElement.html();
                boolean isExist = false;
                for (Map<String, Object> item : isExistList) {
                    if (chapter.equals(String.valueOf(item.get("chapter")))) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    String chapterUrl = "http://www.147xiaoshuo.com/" + chapterElement.attr("href");
                    Document chapterDoc = HttpUtil.getHtmlFromUrl(chapterUrl, true);
                    String content = chapterDoc.getElementById("content").html();
                    Chapters chapters = Chapters.builder().chapter(chapter).content(content).novelsId(novelsId).updateTime(timeList.get(i - isExistList.size())).build();
                    chaptersService.save(chapters);
                    log.info("当前小说novelsId: {}; 更新章节chapter: {}", novelsId, chapter);
                }
            }
        }
    }
}
