/*
 LionChat
 Copyright (C) 2014 Enrico Fasoli ( fazius2009 at gmail dot com )

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package parsing;

import java.util.ArrayList;

/**
 * This class handles a settings file's entry
 *
 * @author fazo
 */
public class Setting {

    private String name, value;

    public Setting(String name, String value) {
        this.name = name;
        this.value = value.trim();
    }

    public static ArrayList<Setting> parse(String filepath) {
        String fileContent = utilz.Filez.getFileContent(filepath);
        if (fileContent == null) {
            return null;
        }
        ArrayList<String> fileLines = utilz.Utils.toList(fileContent, "\n");
        if (fileLines == null || fileLines.isEmpty()) {
            return null;
        }
        ArrayList<Setting> settings = new ArrayList<Setting>();
        int count=0;
        for (String s : fileLines) {
            if (s == null || s == "") {
                continue;
            }
            s = s.trim();
            if (s.startsWith("#")) {
                continue;
            }
            if (!s.contains("=")) {
                continue;
            }
            String[] sss = s.split("=");
            settings.add(new Setting(sss[0], sss[1]));
            count++;
        }
        return settings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value.trim();
    }

    public boolean isBoolean() {
        if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return true;
        }
        return false;
    }

    public boolean getBooleanValue() {
        if (!isBoolean()) {
            throw new UnsupportedOperationException("Propriety is not boolean");
        }
        if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")) {
            return false;
        }
        throw new UnsupportedOperationException("Propriety is not boolean");
    }
}
