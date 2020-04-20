package com.sn.time.strategy.impl;

import com.sn.time.entity.Chapters;
import com.sn.time.service.ChaptersService;
import com.sn.time.service.impl.ChaptersServiceImpl;
import com.sn.time.strategy.SourceService;
import com.sn.time.util.BeanUtil;
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
 * @date: 2020/4/5 15:08
 */
@Slf4j
public class BiqugeSourceServiceImpl implements SourceService {

    @Override
    public void updateChapter(Map<String, Object> map) {
        String sourceUrl = String.valueOf(map.get("sourceUrl"));
        String novelsId = String.valueOf(map.get("novelsId"));
        Document categoryDoc = HttpUtil.getHtmlFromUrl(sourceUrl, true);
        Element infoElement = categoryDoc.getElementById("info");
        String latestChapter = infoElement.getElementsByTag("p").get(3).getElementsByTag("a").get(0).html();
        ChaptersService chaptersService = BeanUtil.getBean(ChaptersServiceImpl.class);
        List<Map<String, Object>> isExistList = chaptersService.findByNovelsId(novelsId);
        String lastChapter = String.valueOf(isExistList.get(isExistList.size() - 1).get("chapter"));
        if (!latestChapter.equals(lastChapter)) {
            Element dlElement = categoryDoc.getElementById("list").getElementsByTag("dl").get(0);
            Elements ddList = dlElement.getElementsByTag("dd");
            String oldUpdateTime = isExistList.get(isExistList.size() - 1).get("updateTime").toString();
            String strUpdateTime = infoElement.getElementsByTag("p").get(2).html().split("：")[1];
            Date newUpdateTime = DateUtil.strToDate(strUpdateTime, "yyyy-MM-dd HH:mm:ss");
            List<String> timeList = DateUtil.stepTime(DateUtil.strToDate(oldUpdateTime, "yyyy-MM-dd HH:mm:ss"), newUpdateTime, ddList.size() - isExistList.size());
            for (int i = isExistList.size(), iLen = ddList.size(); i < iLen; i++) {
                Element a = ddList.get(i).getElementsByTag("a").get(0);
                String chapter = a.html();
                boolean isExist = false;
                for (Map<String, Object> item : isExistList) {
                    if (chapter.equals(String.valueOf(item.get("chapter")))) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    String chapterUrl = "http://www.xbiquge.la/" + a.attr("href");
                    Document contentDoc = HttpUtil.getHtmlFromUrl(chapterUrl, true);
                    String content = contentDoc.getElementById("content").html();
                    Chapters chapters = Chapters.builder().chapter(chapter).content(content).novelsId(novelsId).updateTime(timeList.get(i - isExistList.size())).build();
                    chaptersService.save(chapters);
                    log.info("当前小说novelsId: {}; 更新章节chapter: {}", novelsId, chapter);
                }
            }
        }
    }
}
