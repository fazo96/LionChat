/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

/**
 *
 * @author fazo
 */
public class Propriety {

    private String name, value;

    public Propriety(String name, String value) {
        this.name = name;
        this.value = value.trim();
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
