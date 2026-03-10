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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAgentParser {
    private static final String UNKNOWN = "Unknown";

    private UserAgentParser() {}

    public static UserAgentInfo parse(String userAgent) {
        if (StringUtil.isEmpty(userAgent)) {
            return new UserAgentInfo(UNKNOWN, "", UNKNOWN);
        }
        return new UserAgentInfo(parseBrowser(userAgent), parseVersion(userAgent), parseOs(userAgent));
    }

    private static String parseBrowser(String userAgent) {
        if (userAgent.contains("Edg/")) {
            return "Edge";
        }
        if (userAgent.contains("OPR/") || userAgent.contains("Opera")) {
            return "Opera";
        }
        if (userAgent.contains("Chrome/")) {
            return "Chrome";
        }
        if (userAgent.contains("Firefox/")) {
            return "Firefox";
        }
        if (userAgent.contains("Safari/") && userAgent.contains("Version/")) {
            return "Safari";
        }
        if (userAgent.contains("MSIE") || userAgent.contains("Trident/")) {
            return "Internet Explorer";
        }
        return UNKNOWN;
    }

    private static String parseVersion(String userAgent) {
        String[] tokens = {"Edg/", "OPR/", "Opera/", "Chrome/", "Firefox/", "Version/", "MSIE "};
        for (String token : tokens) {
            String version = matchVersion(userAgent, token);
            if (StringUtil.isNotEmpty(version)) {
                return version;
            }
        }
        if (userAgent.contains("Trident/")) {
            return matchVersion(userAgent, "rv:");
        }
        return "";
    }

    private static String matchVersion(String userAgent, String token) {
        Pattern pattern = Pattern.compile(Pattern.quote(token) + "([\\d.]+)");
        Matcher matcher = pattern.matcher(userAgent);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String parseOs(String userAgent) {
        if (userAgent.contains("Windows")) {
            return "Windows";
        }
        if (userAgent.contains("Mac OS X") || userAgent.contains("Macintosh")) {
            return "macOS";
        }
        if (userAgent.contains("Android")) {
            return "Android";
        }
        if (userAgent.contains("iPhone") || userAgent.contains("iPad") || userAgent.contains("iPod")) {
            return "iOS";
        }
        if (userAgent.contains("Linux")) {
            return "Linux";
        }
        return UNKNOWN;
    }
}
