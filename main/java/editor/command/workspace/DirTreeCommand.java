// DirTreeCommand.java
package editor.command.workspace;

import editor.command.Command;
import java.io.File;

public class DirTreeCommand implements Command {
    private String path;

    public DirTreeCommand(String path) {
        this.path = path;
    }

    @Override
    public void execute() {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Invalid directory: " + path);
            return;
        }

        System.out.println(dir.getName());
        printTree(dir, "", true);
    }

    private void printTree(File dir, String prefix, boolean isRoot) {
        File[] files = dir.listFiles();
        if (files == null) return;

        java.util.Arrays.sort(files);

        for (int i = 0; i < files.length; i++) {
            boolean isLast = (i == files.length - 1);
            String connector = isLast ? "└── " : "├── ";
            String childPrefix = isLast ? "    " : "│   ";

            System.out.println(prefix + connector + files[i].getName());

            if (files[i].isDirectory()) {
                printTree(files[i], prefix + childPrefix, false);
            }
        }
    }
}