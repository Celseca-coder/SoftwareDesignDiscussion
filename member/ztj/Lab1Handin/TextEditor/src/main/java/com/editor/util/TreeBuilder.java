package com.editor.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 目录树构建工具
 * 用于构建和显示目录树结构
 */
public class TreeBuilder {
    
    /**
     * 构建目录树
     */
    public static String buildTree(String rootPath) {
        return buildTree(rootPath, "", new StringBuilder()).toString();
    }
    
    private static StringBuilder buildTree(String path, String prefix, StringBuilder builder) {
        File file = new File(path);
        String name = file.getName();
        
        if (name.isEmpty()) {
            name = file.getPath();
        }
        
        builder.append(prefix);
        builder.append(name);
        builder.append("\n");
        
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null && children.length > 0) {
                List<File> dirs = new ArrayList<>();
                List<File> files = new ArrayList<>();
                
                for (File child : children) {
                    if (child.isDirectory()) {
                        dirs.add(child);
                    } else {
                        files.add(child);
                    }
                }
                
                List<File> all = new ArrayList<>(dirs);
                all.addAll(files);
                
                for (int i = 0; i < all.size(); i++) {
                    boolean isLast = (i == all.size() - 1);
                    String childPrefix = prefix + (isLast ? "└── " : "├── ");
                    buildTree(all.get(i).getAbsolutePath(), childPrefix, builder);
                    
                    if (!isLast && i < all.size() - 1) {
                        String nextPrefix = prefix + "│   ";
                        buildTree("", nextPrefix, new StringBuilder());
                    }
                }
            }
        }
        
        return builder;
    }
    
    /**
     * 递归构建目录树（简化版本）
     */
    public static String buildSimpleTree(String rootPath, int maxDepth) {
        return buildSimpleTree(rootPath, "", 0, maxDepth);
    }
    
    private static String buildSimpleTree(String path, String prefix, int depth, int maxDepth) {
        if (depth > maxDepth) {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        File file = new File(path);
        
        builder.append(prefix);
        builder.append(file.getName());
        builder.append("\n");
        
        if (file.isDirectory() && depth < maxDepth) {
            File[] children = file.listFiles();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    boolean isLast = (i == children.length - 1);
                    String childPrefix = prefix + (isLast ? "└── " : "├── ");
                    builder.append(buildSimpleTree(
                        children[i].getAbsolutePath(),
                        childPrefix,
                        depth + 1,
                        maxDepth
                    ));
                }
            }
        }
        
        return builder.toString();
    }
}

