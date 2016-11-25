package com.sourcecanyon.whatsClone.helpers;

/**
 * Created by Abderrahim El imame on 6/20/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class UtilsString {

    /**
     * method to remove the last string
     *
     * @param str this is parameter for removelastString  method
     * @return return value
     */
    public static String removelastString(String str) {
        if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == ',') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }


    /**
     * method to escape string
     *
     * @param string this is parameter for escapeJava  method
     * @return return value
     */
    public static String escapeJava(String string) {
        StringBuilder builder = new StringBuilder();
        try {
            for (int i = 0; i < string.length(); i++) {
                char c = string.charAt(i);
                if (!Character.isLetterOrDigit(c) && !Character.isSpaceChar(c) && !Character.isWhitespace(c)) {
                    String unicode = String.valueOf(c);
                    int code = (int) c;
                    if (!(code >= 0 && code <= 255)) {
                        unicode = "\\\\u" + Integer.toHexString(c);
                    }
                    builder.append(unicode);
                } else {
                    builder.append(c);
                }
            }
            AppHelper.LogCat("Unicode Block " + builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }


    /**
     * method to unescape string
     *
     * @param escaped this is parameter for unescapeJava  method
     * @return return value
     */
    public static String unescapeJava(String escaped) {

        if (escaped.indexOf("\\u") == -1)
            return escaped;

        String processed = "";

        int position = escaped.indexOf("\\u");
        while (position != -1) {
            if (position != 0)
                processed += escaped.substring(0, position);
            String token = escaped.substring(position + 2, position + 6);
            escaped = escaped.substring(position + 6);
            processed += (char) Integer.parseInt(token, 16);
            position = escaped.indexOf("\\u");
        }
        processed += escaped;

        return processed;
    }


    /**
     * method to unescape string
     *
     * @param escaped this is parameter for unescapeJavaString  method
     * @return return value
     */
    public static String unescapeJavaString(String escaped) {

        StringBuilder sb = new StringBuilder(escaped.length());

        for (int i = 0; i < escaped.length(); i++) {
            char ch = escaped.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == escaped.length() - 1) ? '\\' : escaped
                        .charAt(i + 1);
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < escaped.length() - 1) && escaped.charAt(i + 1) >= '0'
                            && escaped.charAt(i + 1) <= '7') {
                        code += escaped.charAt(i + 1);
                        i++;
                        if ((i < escaped.length() - 1) && escaped.charAt(i + 1) >= '0'
                                && escaped.charAt(i + 1) <= '7') {
                            code += escaped.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    case 'u':
                        if (i >= escaped.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + escaped.charAt(i + 2) + escaped.charAt(i + 3)
                                        + escaped.charAt(i + 4) + escaped.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * method to escape string
     *
     * @param string this is parameter for escapeJavaString  method
     * @return return value
     */
    public static String escapeJavaString(String string) {

        StringBuilder sb = new StringBuilder(string.length());

        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == string.length() - 1) ? '\\' : string.charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < string.length() - 1) && string.charAt(i + 1) >= '0'
                            && string.charAt(i + 1) <= '7') {
                        code += string.charAt(i + 1);
                        i++;
                        if ((i < string.length() - 1) && string.charAt(i + 1) >= '0'
                                && string.charAt(i + 1) <= '7') {
                            code += string.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    case 'u':
                        if (i >= string.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + string.charAt(i + 2) + string.charAt(i + 3)
                                        + string.charAt(i + 4) + string.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}
