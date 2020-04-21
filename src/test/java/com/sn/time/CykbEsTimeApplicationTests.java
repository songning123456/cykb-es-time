package com.sn.time;

import com.sn.time.service.ChaptersService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@Slf4j
class CykbEsTimeApplicationTests {

    @Autowired
    private ChaptersService chaptersService;

    @Test
    public void test() {
        try {
            chaptersService.updateLatestChapter();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("updateLatestChapter fail: {}", e.getMessage());
        }
    }

}
