package com.nightjar.util;

public class StringUtils {

    public static String escapeMetaCharacters(String str) {
        final String[] chars = {"\\", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*", "+", "?", "|", "<", ">", "-", "&", "%"};

        for (int i = 0; i < chars.length; i++) {
            if (str.contains(chars[i])) {
                str = str.replace(chars[i], "\\" + chars[i]);
            }
        }

        return str;
    }

    public static boolean isEmpty(String str) {
        return str == null ? true : "".equals(str);
    }

}
