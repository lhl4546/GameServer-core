package core.fire.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

// 关键字过滤
public class SensitiveFilter
{
    // 关键字文件名
    private String wordFile;
    @SuppressWarnings("rawtypes")
    private Map pool = new HashMap<>();

    private SensitiveFilter(String wordFile) {
        this.wordFile = wordFile;
    }

    /**
     * 以从类路径下加载关键字文件的方式创建一个新过滤器
     * 
     * @param filterFile 关键字文件类路径，UTF-8无BOM格式，一个关键字占一行
     * @return
     */
    public static SensitiveFilter createFromClassPath(String filterFile) {
        SensitiveFilter filter = new SensitiveFilter(filterFile);
        filter.load();
        return filter;
    }

    private void load() {
        try {
            Path path = Paths.get(SensitiveFilter.class.getClassLoader().getResource(wordFile).toURI());
            try (Stream<String> lines = Files.lines(path)) {
                @SuppressWarnings("rawtypes")
                Map tmpPool = new HashMap();
                lines.forEach(line -> processLine(line, tmpPool));
                this.pool = tmpPool;
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 重新加载敏感字配置
     */
    public void reload() {
        load();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void processLine(String line, Map pool) {
        Map nowMap = pool;
        char[] chars = line.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char ch = Character.toUpperCase(chars[i]);
            Object wordMap = nowMap.get(ch);
            if (wordMap != null) {
                nowMap = (Map) wordMap;
            } else {
                Map newWordMap = new HashMap();
                newWordMap.put("isEnd", "0");
                nowMap.put(ch, newWordMap);
                nowMap = newWordMap;
            }

            if (i == chars.length - 1) {
                nowMap.put("isEnd", "1");
            }
        }
    }

    /**
     * 过滤关键字，返回处理后的字符串，每个关键字均以一个*代替，大小写不敏感
     * 
     * @param source
     * @return
     */
    @SuppressWarnings("rawtypes")
    public String replace(String source) {
        StringBuilder sb = new StringBuilder(source);
        char[] chars = source.toCharArray();

        int start = -1, end = -1;
        Map nowMap = pool;
        for (int i = 0; i < chars.length; i++) {
            char ch = Character.toUpperCase(chars[i]);
            nowMap = (Map) nowMap.get(ch);

            if (nowMap != null) {
                if (start == -1) {
                    start = i;
                }
                if ("1".equals(nowMap.get("isEnd"))) {
                    end = i;
                    for (int j = start; j <= end; j++) {
                        sb.setCharAt(j, '*');
                    }
                    start = -1;
                    end = -1;
                }
            } else {
                start = -1;
                nowMap = pool;
                nowMap = (Map) nowMap.get(ch);
                if (nowMap != null) {
                    if (start == -1) {
                        start = i;
                    }
                    if ("1".equals(nowMap.get("isEnd"))) {
                        end = i;
                        for (int j = start; j <= end; j++) {
                            sb.setCharAt(j, '*');
                        }
                        start = -1;
                        end = -1;
                    }
                } else {
                    nowMap = pool;
                }
            }
        }

        return sb.toString();
    }

    /**
     * 判断是否包含关键字
     * 
     * @param source
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean hasSensitiveWord(String source) {
        char[] chars = source.toCharArray();

        Map nowMap = pool;
        for (int i = 0; i < chars.length; i++) {
            char ch = Character.toUpperCase(chars[i]);
            nowMap = (Map) nowMap.get(ch);

            if (nowMap != null) {
                if ("1".equals(nowMap.get("isEnd"))) {
                    return true;
                }
            } else {
                nowMap = pool;
                nowMap = (Map) nowMap.get(ch);
                if (nowMap != null) {
                    if ("1".equals(nowMap.get("isEnd"))) {
                        return true;
                    }
                } else {
                    nowMap = pool;
                }
            }
        }

        return false;
    }
}
