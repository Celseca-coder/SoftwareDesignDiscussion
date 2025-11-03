package lab1.domain.display;

import java.util.List;

public class BasicContentDisplayer implements IContentDisplayer {
    @Override
    public String display(List<String> lines, int start, int end) {
        StringBuilder sb = new StringBuilder();
        // 确保范围有效
        int actualStart = Math.max(1, start);
        int actualEnd = Math.min(lines.size(), end);

        for (int i = actualStart - 1; i < actualEnd; i++) {
            sb.append(lines.get(i)).append("\n");
        }
        // 移除最后一个换行符
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
