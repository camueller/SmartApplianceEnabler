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

public class ValueExtractor {

    private Map<String, Pattern> patterns = new HashMap<>();

    /**
     * Extract the value from the a string using a regular expression.
     * The regular expression has contain a capture group containing the value.
     * @param response the string containing the value
     * @param regex the regular expression to be used to extract the value
     * @return the value extracted or the full string if the regular expression is null or could not be matched
     */
    public String extractValue(String response, String regex)  {
        if(regex == null) {
            return response;
        }
        Pattern pattern = this.patterns.get(regex);
        if(pattern == null) {
            pattern = Pattern.compile(regex, Pattern.DOTALL);
            this.patterns.put(regex, pattern);
        }
        Matcher regexMatcher = pattern.matcher(response);
        if (regexMatcher.find()) {
            return regexMatcher.group(1);
        }
        return response;
    }
}
