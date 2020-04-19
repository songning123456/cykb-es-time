package com.sn.time.strategy;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author: songning
 * @date: 2020/4/5 15:20
 */
@Slf4j
public class SourceContent {
    public static void doSource(Map<String, Object> map) {
        String sourceName = String.valueOf(map.get("sourceName"));
        String novelsId = String.valueOf(map.get("novelsId"));
        Class sourceClazz = SourceEnum.currentSource(sourceName);
        try {
            assert sourceClazz != null;
            SourceService sourceService = (SourceService) sourceClazz.newInstance();
            sourceService.updateChapter(map);
        } catch (Exception e) {
            log.error("更新失败=>小说novelsId: {}, sourceName: {}", novelsId, sourceName);
        }
    }
}
