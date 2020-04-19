package com.sn.time.service;

import com.sn.time.entity.Chapters;

import java.util.List;
import java.util.Map;

/**
 * @author: songning
 * @date: 2020/4/6 11:08
 */
public interface ChaptersService {

    List<Map<String, Object>> findByNovelsId(String novelsId);

    void save(Chapters chapters);
}
