package com.nightjar.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Bundle {

    private static ResourceBundle rsb = ResourceBundle.getBundle("default");

    /**
     * Returns a string from the resource bundle and binds it
     * with the given arguments. If the key is not found,
     * return the key.
     */
    public static String getResourceString(String key, Object[] args) {
        try {
            return MessageFormat.format(getResourceString(key), args);
        } catch (MissingResourceException e) {
            return key;
        } catch (NullPointerException e) {
            return "!" + key + "!";
        }
    }

    /**
     * Returns a string from the resource bundle.
     * We don't want to crash because of a missing String.
     * Returns the key if not found.
     */
    public static String getResourceString(String key) {
        try {
            return rsb.getString(key);
        } catch (MissingResourceException e) {
            return key;
        } catch (NullPointerException e) {
            return "!" + key + "!";
        }
    }

}
