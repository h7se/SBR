package com.qou.h7se.sbr;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by k0de9x on 10/25/2015.
 */
public class PathUtils {
    public static String addLeadingSlash(String path) {
        if(!(path.startsWith("/"))) {
            return "/".concat(path);
        }
        return path;
    }

    public static String removeLeadingSlash(String path) {
        if(path.startsWith("/")) {
            return path.replaceFirst("^[/]", "");
        }
        return path;
    }

    public static String combine(String basePath, String... names) {
        String path = basePath;
        for(String name : names) {
            path = FilenameUtils.concat(path, PathUtils.removeLeadingSlash(name));
        }
        return path;
    }

    public static String getCanonicalPath(File file, boolean removeLeadingSlash) {
        String t = null;
        try {
            t = file.getCanonicalPath();
            if(removeLeadingSlash && t.startsWith("/")) {
                t = t.substring(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }

    public static String getCanonicalPath(String file, boolean removeLeadingSlash) {
        return getCanonicalPath(new File(file), removeLeadingSlash);
    }
}
