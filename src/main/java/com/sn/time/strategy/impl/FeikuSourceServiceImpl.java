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
 * @date: 2020/4/5 15:10
 */
@Slf4j
public class FeikuSourceServiceImpl implements SourceService {
    @Override
    public void updateChapter(Map<String, Object> map) {
        String sourceUrl = String.valueOf(map.get("sourceUrl"));
        String novelsId = String.valueOf(map.get("novelsId"));
        Document baseDoc = HttpUtil.getHtmlFromUrl(sourceUrl, true);
        String latestChapter = baseDoc.getElementsByClass("book-intro").get(0).getElementsByTag("a").html();
        ChaptersService chaptersService = BeanUtil.getBean(ChaptersServiceImpl.class);
        List<Map<String, Object>> isExistList = chaptersService.findByNovelsId(novelsId);
        String lastChapter = String.valueOf(isExistList.get(isExistList.size() - 1).get("chapter"));
        if (!latestChapter.equals(lastChapter)) {
            String listUrl = baseDoc.getElementsByClass("catalogbtn").get(0).attr("href");
            Document listDoc = HttpUtil.getHtmlFromUrl(listUrl, true);
            Elements chapterList = listDoc.getElementsByClass("chapter-list").get(0).getElementsByTag("a");
            String oldUpdateTime = isExistList.get(isExistList.size() - 1).get("updateTime").toString();
            Element bookInfo = baseDoc.getElementsByClass("book-intro").get(0);
            String paramTime = bookInfo.getElementsByTag("b").get(1).html();
            int left = paramTime.indexOf("(");
            int right = paramTime.indexOf(")");
            String strUpdateTime = paramTime.substring(left + 1, right) + " 00:00:00";
            Date newUpdateTime = DateUtil.strToDate(strUpdateTime, "yyyy-MM-dd HH:mm:ss");
            List<String> timeList = DateUtil.stepTime(DateUtil.strToDate(oldUpdateTime, "yyyy-MM-dd HH:mm:ss"), newUpdateTime, chapterList.size() - isExistList.size());
            for (int i = isExistList.size(), iLen = chapterList.size(); i < iLen; i++) {
                String chapter = chapterList.get(i).html();
                boolean isExist = false;
                for (Map<String, Object> item : isExistList) {
                    if (chapter.equals(String.valueOf(item.get("chapter")))) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    String contentUrl = "http://www.feiku.org" + chapterList.get(i).attr("href");
                    Document contentDoc = HttpUtil.getHtmlFromUrl(contentUrl, true);
                    String content = contentDoc.getElementsByClass("article-con").get(0).html();
                    Chapters chapters = Chapters.builder().chapter(chapter).content(content).novelsId(novelsId).updateTime(timeList.get(i - isExistList.size())).build();
                     chaptersService.save(chapters);
                    log.info("当前小说novelsId: {}; 更新章节chapter: {}", novelsId, chapter);
                }
            }
        }
    }
}
