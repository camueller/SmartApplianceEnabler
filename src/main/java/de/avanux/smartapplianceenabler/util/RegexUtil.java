/*
 * Copyright (C) 2019 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package de.avanux.smartapplianceenabler.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    private static Map<String, Pattern> patterns = new HashMap<>();

    public static boolean isMatch(String text, String regex) {
        if(regex == null) {
            return false;
        }
        Pattern pattern = getPattern(regex);
        Matcher regexMatcher = pattern.matcher(text);
        return regexMatcher.find();
    }

    /**
     * Returns the matching group 1 of a text using a regular expression.
     * The regular expression has to contain a capture group containing the value.
     * @param text the string containing the value
     * @param regex the regular expression to be used to extract the value
     * @return the content of matchin group 1 or the text if the regular expression is null or could not be matched
     */
    public static String getMatchingGroup1(String text, String regex)  {
        if(regex == null) {
            return text;
        }
        Pattern pattern = getPattern(regex);
        Matcher regexMatcher = pattern.matcher(text);
        if (regexMatcher.find()) {
            return regexMatcher.group(1);
        }
        return text;
    }

    private static Pattern getPattern(String regex) {
        Pattern pattern = patterns.get(regex);
        if(pattern == null) {
            pattern = Pattern.compile(regex, Pattern.DOTALL);
            patterns.put(regex, pattern);
        }
        return pattern;
    }
}
