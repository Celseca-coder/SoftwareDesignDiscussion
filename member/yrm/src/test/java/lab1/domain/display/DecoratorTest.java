package lab1.domain.display;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 目标：单元测试 Decorator 模式
 * 验证：LineNumberDecorator 是否能正确地为 BasicContentDisplayer 添加行号
 */
class DecoratorTest {

    private final List<String> TEST_LINES = List.of("Line 1", "Line 2");

    @Test
    void testBasicDisplayer_NoLineNumbers() {
        // 准备：基础组件
        IContentDisplayer basic = new BasicContentDisplayer();

        // 执行
        String output = basic.display(TEST_LINES, 1, 2);

        // 断言：输出不包含行号
        assertFalse(output.contains("1:"));
        assertTrue(output.contains("Line 1"));
        assertTrue(output.contains("Line 2"));
    }

    @Test
    void testLineNumberDecorator_AddsLineNumbers() {
        // 准备：用装饰器包装基础组件
        IContentDisplayer basic = new BasicContentDisplayer();
        IContentDisplayer decorated = new LineNumberDecorator(basic);

        // 执行
        String output = decorated.display(TEST_LINES, 1, 2);

        // 断言：输出必须包含行号
        assertTrue(output.contains("1: Line 1"));
        assertTrue(output.contains("2: Line 2"));
    }
}
