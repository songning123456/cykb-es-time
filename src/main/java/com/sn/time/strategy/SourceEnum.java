package com.sn.time.strategy;

import com.sn.time.strategy.impl.*;

/**
 * @author: songning
 * @date: 2020/4/5 15:12
 */
public enum SourceEnum {

    /**
     * 来源分类
     */
    Biquge("笔趣阁", BiqugeSourceServiceImpl.class),
    Yisiqi("147小说", YisiqiSourceServiceImpl.class),
    Tiantianshuba("天天书吧", TiantianshubaSourceServiceImpl.class),
    Feikuxiaoshuo("飞库小说", FeikuSourceServiceImpl.class),
    Qushuba("趣书吧", QushubaSourceServiceImpl.class);

    private String key;
    private Class<? extends SourceService> value;

    public String getKey() {
        return this.key;
    }

    public Class getValue() {
        return this.value;
    }

    SourceEnum(String key, Class<? extends SourceService> value) {
        this.key = key;
        this.value = value;
    }

    public static Class currentSource(String key) {
        for (SourceEnum item : SourceEnum.values()) {
            if (key.equals(item.getKey())) {
                return item.getValue();
            }
        }
        return null;
    }
}
