package lab1.domain.editor;

public class EditorFactory {
    public static IEditor createEditor(String filePath, String content) {
        if (filePath.endsWith(".txt")) {
            return new TextEditor(filePath, content);
        }
        // 根据实验1，只支持txt。Lab2可以在这里扩展
        throw new UnsupportedOperationException("不支持的文件类型: " + filePath);
    }

    public static IEditor createEditor(String filePath) {
        return createEditor(filePath, "");
    }
}
