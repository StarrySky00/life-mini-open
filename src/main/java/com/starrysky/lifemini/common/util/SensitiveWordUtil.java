package com.starrysky.lifemini.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SensitiveWordUtil {

    /* ========== 前缀树节点 ========== */
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd;
    }

    private static final TrieNode ROOT = new TrieNode();

    /* ========== 初始化敏感词（启动时调用一次） ========== */
    public static void loadWords(Collection<String> words) {
        for (String word : words) {
            addWord(word);
        }
    }

    private static void addWord(String word) {
        if (word == null || word.isBlank()) {
            return;
        }
        TrieNode node = ROOT;
        for (char c : word.trim().toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEnd = true;
    }

    /* ========== 本地敏感词检测 ========== */

    /**
     *
     * @param text
     * @return  true 违规
     */
    public static boolean containsSensitive(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            TrieNode node = ROOT;
            int j = i;

            while (j < chars.length) {
                char c = chars[j];

                // 跳过符号，防止“傻-逼”绕过
                if (isSkipChar(c)) {
                    j++;
                    continue;
                }

                node = node.children.get(c);
                if (node == null) {
                    break;
                }

                if (node.isEnd) {
                    return true; // 命中
                }
                j++;
            }
        }
        return false;
    }
    /* ========== 工具方法 ========== */

    /**
     *
     * @param c
     * @return true 不是字母，数字  并且不是（常用中文）汉字
     */
    private static boolean isSkipChar(char c) {
        return !Character.isLetterOrDigit(c) && !isChinese(c);
    }

    /**
     * 判断是否是汉字
     * @param c
     * @return
     */
    private static boolean isChinese(char c) {
        return c >= '\u4e00' && c <= '\u9fa5';
    }
}
