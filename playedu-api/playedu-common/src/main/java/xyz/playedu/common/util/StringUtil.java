/*
 * Copyright (C) 2023 杭州白書科技有限公司
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.playedu.common.util;

import java.util.*;
import org.springframework.util.AntPathMatcher;

/** 字符串工具類 */
public class StringUtil extends org.apache.commons.lang3.StringUtils {
    /** 空字符串 */
    private static final String NULL_STR = "";

    /** 下劃線 */
    private static final char SEPARATOR = '_';

    /**
     * 獲取參數不爲空值
     *
     * @param value defaultValue 要判斷的value
     * @return value 返回值
     */
    public static <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * 判斷一個Collection是否爲空,包含List, Set, Queue
     *
     * @param coll 要判斷的Collection
     * @return true=爲空, false=非空
     */
    public static boolean isEmpty(Collection<?> coll) {
        return isNull(coll) || coll.isEmpty();
    }

    /**
     * 判斷一個Collection是否非空,包含List, Set, Queue
     *
     * @param coll 要判斷的Collection
     * @return true=非空, false=空
     */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * 判斷一個對象數組是否爲空
     *
     * @param objects 要判斷的對象數組
     * @return true=爲空, false=非空
     */
    public static boolean isEmpty(Object[] objects) {
        return isNull(objects) || (objects.length == 0);
    }

    /**
     * 判斷一個對象數組是否非空
     *
     * @param objects 要判斷的對象數組
     * @return true=非空, false=空
     */
    public static boolean isNotEmpty(Object[] objects) {
        return !isEmpty(objects);
    }

    /**
     * 判斷一個Map是否爲空
     *
     * @param map 要判斷的Map
     * @return true=爲空, false=非空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return isNull(map) || map.isEmpty();
    }

    /**
     * 判斷一個Map是否爲空
     *
     * @param map 要判斷的Map
     * @return true=非空, false=空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 判斷一個字符串是否爲空串
     *
     * @param str String
     * @return true=爲空, false=非空
     */
    public static boolean isEmpty(String str) {
        return isNull(str) || NULL_STR.equals(str.trim());
    }

    /**
     * 判斷一個字符串是否爲非空串
     *
     * @param str String
     * @return true=非空串, false=空串
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判斷一個對象是否爲空
     *
     * @param object Object
     * @return true=爲空, false=非空
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /**
     * 判斷一個對象是否非空
     *
     * @param object Object
     * @return true=非空, false=空
     */
    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    /**
     * 判斷一個對象是否是數組類型（Java基本型別的數組）
     *
     * @param object 對象
     * @return true=是數組, false=不是數組
     */
    public static boolean isArray(Object object) {
        return isNotNull(object) && object.getClass().isArray();
    }

    /** 去空格 */
    public static String trim(String str) {
        return (str == null ? "" : str.trim());
    }

    /**
     * 截取字符串
     *
     * @param str 字符串
     * @param start 開始
     * @return 結果
     */
    public static String substring(final String str, int start) {
        if (str == null) {
            return NULL_STR;
        }

        if (start < 0) {
            start = str.length() + start;
        }

        if (start < 0) {
            start = 0;
        }

        if (start > str.length()) {
            return NULL_STR;
        }

        return str.substring(start);
    }

    /**
     * 截取字符串
     *
     * @param str 字符串
     * @param start 開始
     * @param end 結束
     * @return 結果
     */
    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return NULL_STR;
        }

        if (end < 0) {
            end = str.length() + end;
        }

        if (start < 0) {
            start = str.length() + start;
        }

        if (end > str.length()) {
            end = str.length();
        }

        if (start > end) {
            return NULL_STR;
        }

        if (start < 0) {
            start = 0;
        }

        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * 字符串轉set
     *
     * @param str 字符串
     * @param sep 分隔符
     * @return set集合
     */
    public static Set<String> str2Set(String str, String sep) {
        return new HashSet<String>(str2List(str, sep, true, false));
    }

    /**
     * 字符串轉list
     *
     * @param str 字符串
     * @param sep 分隔符
     * @param filterBlank 過濾純空白
     * @param trim 去掉首尾空白
     * @return list集合
     */
    public static List<String> str2List(String str, String sep, boolean filterBlank, boolean trim) {
        List<String> list = new ArrayList<String>();
        if (StringUtil.isEmpty(str)) {
            return list;
        }

        // 過濾空白字符串
        if (filterBlank && StringUtil.isBlank(str)) {
            return list;
        }

        String[] split = str.split(sep);
        for (String string : split) {
            if (filterBlank && StringUtil.isBlank(string)) {
                continue;
            }
            if (trim) {
                string = string.trim();
            }
            list.add(string);
        }

        return list;
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一個字符串同時串忽略大小寫
     *
     * @param cs 指定字符串
     * @param searchCharSequences 需要檢查的字符串數組
     * @return 是否包含任意一個字符串
     */
    public static boolean containsAnyIgnoreCase(
            CharSequence cs, CharSequence... searchCharSequences) {
        if (isEmpty(cs) || isEmpty(searchCharSequences)) {
            return false;
        }
        for (CharSequence testStr : searchCharSequences) {
            if (containsIgnoreCase(cs, testStr)) {
                return true;
            }
        }
        return false;
    }

    /** 駝峯轉下劃線命名 */
    public static String toUnderScoreCase(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 前置字符是否大寫
        boolean preCharIsUpperCase = true;
        // 當前字符是否大寫
        boolean cureCharIsUpperCase = true;
        // 下一字符是否大寫
        boolean nextCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i > 0) {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            } else {
                preCharIsUpperCase = false;
            }

            cureCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1)) {
                nextCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && cureCharIsUpperCase && !nextCharIsUpperCase) {
                sb.append(SEPARATOR);
            } else if ((i != 0 && !preCharIsUpperCase) && cureCharIsUpperCase) {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * 是否包含字符串
     *
     * @param str 驗證字符串
     * @param strArr 字符串組
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strArr) {
        if (str != null && strArr != null) {
            for (String s : strArr) {
                if (str.equalsIgnoreCase(trim(s))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 將下劃線大寫方式命名的字符串轉換爲駝峯式。 如果轉換前的下劃線大寫方式命名的字符串爲空, 則返回空字符串。 例如：HELLO_WORLD->HelloWorld
     *
     * @param name 轉換前的下劃線大寫方式命名的字符串
     * @return 轉換後的駝峯式命名的字符串
     */
    public static String convertToCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        // 快速檢查
        if (name == null || name.isEmpty()) {
            // 沒必要轉換
            return "";
        } else if (!name.contains("_")) {
            // 不含下劃線，僅將首字母大寫
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        // 用下劃線將原始字符串分割
        String[] camels = name.split("_");
        for (String camel : camels) {
            // 跳過原始字符串中開頭、結尾的下換線或雙重下劃線
            if (camel.isEmpty()) {
                continue;
            }
            // 首字母大寫
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * 駝峯式命名法 例如：user_name->userName
     *
     * @param s 字符串
     * @return 駝峯字符串
     */
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 查找指定字符串是否匹配指定字符串列表中的任意一個字符串
     *
     * @param str 指定字符串
     * @param strArr 需要檢查的字符串數組
     * @return 是否匹配
     */
    public static boolean matches(String str, List<String> strArr) {
        if (isEmpty(str) || isEmpty(strArr)) {
            return false;
        }
        for (String pattern : strArr) {
            if (isMatch(pattern, str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判斷url是否與規則設定: ? 表示單個字符; * 表示一層路徑內的任意字符串，不可跨層級; ** 表示任意層路徑;
     *
     * @param pattern 匹配規則
     * @param url 需要匹配的url
     * @return boolean
     */
    public static boolean isMatch(String pattern, String url) {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    /**
     * 數字左邊補齊0,使之達到指定長度。 注意，如果數字轉換爲字符串後,長度大於size,則只保留 最後size個字符。
     *
     * @param num 數字對象
     * @param size 字符串指定長度
     * @return 返回數字的字符串格式，該字符串爲指定長度。
     */
    public static String padL(final Number num, final int size) {
        return padL(num.toString(), size, '0');
    }

    /**
     * 字符串左補齊 如果原始字符串s長度大於size,則只保留最後size個字符。
     *
     * @param s 原始字符串
     * @param size 字符串指定長度
     * @param c 用於補齊的字符
     * @return 返回指定長度的字符串，由原字符串左補齊或截取得到。
     */
    public static String padL(final String s, final int size, final char c) {
        final StringBuilder sb = new StringBuilder(size);
        if (s != null) {
            final int len = s.length();
            if (s.length() <= size) {
                for (int i = size - len; i > 0; i--) {
                    sb.append(c);
                }
                sb.append(s);
            } else {
                return s.substring(len - size, len);
            }
        } else {
            for (int i = size; i > 0; i--) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 格式化文本, {} 表示佔位符<br>
     * 此方法只是簡單將佔位符 {} 按照順序替換爲參數<br>
     * 如果想輸出 {} 使用 \\轉義 { 即可，如果想輸出 {} 之前的 \ 使用雙轉義符 \\\\ 即可<br>
     * 例：<br>
     * 通常使用：format("this is {} for {}", "a", "b") -> this is a for b<br>
     * 轉義{}： format("this is \\{} for {}", "a", "b") -> this is \{} for a<br>
     * 轉義\： format("this is \\\\{} for {}", "a", "b") -> this is \a for b<br>
     *
     * @param strPattern 文本模板，被替換的部分用 {} 表示
     * @param argArray 參數值
     * @return 格式化後的文本
     */
    public static String format(String strPattern, Object... argArray) {
        String EMPTY_JSON = "{}";
        char C_BACKSLASH = '\\';
        char C_DELIM_START = '{';

        if (isEmpty(argArray) || isEmpty(strPattern)) {
            return strPattern;
        }

        final int strPatternLength = strPattern.length();
        StringBuilder sbuf = new StringBuilder(strPatternLength + 50);
        int handledPosition = 0;
        int delimIndex;
        for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
            delimIndex = strPattern.indexOf(EMPTY_JSON, handledPosition);
            if (delimIndex == -1) {
                if (handledPosition == 0) {
                    return strPattern;
                } else {
                    sbuf.append(strPattern, handledPosition, strPatternLength);
                    return sbuf.toString();
                }
            } else {
                if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == C_BACKSLASH) {
                    if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == C_BACKSLASH) {
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(argArray[argIndex]);
                        handledPosition = delimIndex + 2;
                    } else {
                        // 佔位符被轉義
                        argIndex--;
                        sbuf.append(strPattern, handledPosition, delimIndex - 1);
                        sbuf.append(C_DELIM_START);
                        handledPosition = delimIndex + 1;
                    }
                } else {
                    // 正常佔位符
                    sbuf.append(strPattern, handledPosition, delimIndex);
                    sbuf.append(argArray[argIndex]);
                    handledPosition = delimIndex + 2;
                }
            }
        }

        sbuf.append(strPattern, handledPosition, strPattern.length());
        return sbuf.toString();
    }

    public static String arrayToString(Object[] array) {
        StringBuilder result = new StringBuilder();
        if (array != null && array.length > 0) {
            for (Object o : array) {
                if (StringUtil.isNotNull(o)) {
                    try {
                        Object jsonObj = JsonUtils.toJson(o);
                        result.append(jsonObj.toString());
                    } catch (Exception e) {
                    }
                }
            }
        }
        return result.toString();
    }
}
