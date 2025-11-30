package lab1.domain.display;


import java.util.List;

public interface IContentDisplayer {
    String display(List<String> lines, int startLine, int endLine);
}