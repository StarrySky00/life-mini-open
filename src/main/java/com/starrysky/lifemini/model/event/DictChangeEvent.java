package com.starrysky.lifemini.model.event;

/**
 * @author StarrySky
 * @date 2026/4/24 21:21 星期五
 */

//告诉监听者是哪个字典变了，比如 "category" 或 "keyword"
public record DictChangeEvent(String dictType) {

}
