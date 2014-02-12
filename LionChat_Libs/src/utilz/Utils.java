package utilz;

import java.util.ArrayList;

/**
 * This class offers various utilities shared by the LionChat client and server
 * applications. It has been created to avoid code duplication
 *
 * @author fazo
 */
public class Utils {

    /**
     * Converts nanoseconds given in seconds.
     *
     * @param n nanoseconds to convert to seconds
     * @return the time given in seconds.
     */
    public static int nanoToSec(Long n) {
        // Let's hope I counted the zeroes right
        return (int) (n / 1000000000);
    }

    /**
     * Checks if the string is ok.
     *
     * @param s the string to check
     * @return true if it's ok
     */
    public static boolean isValid(String s) {
        if (s == null) {
            return false;
        }
        if (s.length() == 0) {
            return false;
        }
        for (char c : s.toCharArray()) {
            if (c != ' ') {
                return true;
            }
        }
        return false;
    }

    /**
     * Splits the string into different parts and returns them in a dynamic
     * array
     *
     * @param s the string to transform
     * @param regex the character sequence to use as splitter
     * @return the resulting list of strings
     */
    public static ArrayList<String> toList(String s, String regex) { //passa da una stringa a una lista.
        if (regex == null) { //default splitter is a blank space
            regex = " ";
        }
        if (s == null) { // String is null? return null!
            return null;
        }
        String b[] = s.split(regex); // Split it
        if (b.length == 0) {
            return null;
        }
        ArrayList<String> content = new ArrayList<String>(b.length);
        for (String c : b) { // Get a dynamic array
            content.add(c.trim());
        }
        return content; //return it!
    }

    /**
     * Adds every item in B into the string A
     *
     * @param a the list to add items to
     * @param b the list from which items are picked
     */
    public static void mergeLists(ArrayList<String> a, ArrayList<String> b) {
        for (String c : b) {
            if (!a.contains(c)) {
                a.add(c.trim());
            }
        }
    }

    /**
     * Concatenates the string elements one after another, starting from the
     * first to the last, putting the divisor given between them. The divisor
     * can be null.
     *
     * @param ss the list to turn to string
     * @param divisor character sequence to put between each string part
     * @return the resulting string
     */
    public static String fromList(ArrayList<String> ss, String divisor) {
        if (ss == null) {
            return null;
        }
        if (divisor == null) {
            divisor = " ";
        }
        String a = ss.get(0);
        for (int i = 1; i < ss.size(); i++) {
            a += divisor + ss.get(i);
        }
        return a.trim();
    }
}
