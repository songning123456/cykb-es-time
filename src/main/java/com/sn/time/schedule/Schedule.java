package com.sn.time.schedule;

import com.sn.time.service.ChaptersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author: songning
 * @date: 2020/4/6 15:38
 */
@Component
@EnableScheduling
@Slf4j
public class Schedule {

    @Autowired
    private ChaptersService chaptersService;

    /**
     * 更新小说的最新章节
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateLatestChapters() {
        try {
            chaptersService.updateLatestChapter();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateLatestChapter fail: {}", e.getMessage());
        }
    }
}
