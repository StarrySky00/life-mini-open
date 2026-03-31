package com.starrysky.lifemini.config;

import com.starrysky.lifemini.common.util.SensitiveWordUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class SensitiveWordInitializer {

    @PostConstruct
    public void init() {
        List<String> words = loadFromFile("sensitive-words.txt");//在templates目录下创建文件加入敏感词，每行一个
        SensitiveWordUtil.loadWords(words);
    }

    private List<String> loadFromFile(String fileName) {
        List<String> words = new ArrayList<>();
        try (
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream(fileName);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    words.add(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("加载敏感词失败", e);
        }
        return words;
    }
}
