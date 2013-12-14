package au.com.suncoastpc.auth.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A static utility class for escaping/cleaning HTML markup tags from user input and/or unchecked 
 * URL parameters
 * 
 * @author aroth
 */
public class EscapeUtility {
    /**
     * Regexp for matching HTML script elements ("<script>...</script>")
     */
    private static final String SCRIPT_REGEXP = "<[ \\r\\t\\n]*script([ \\r\\t\\n][^>]*>|>)." +
            "*?<[ \\r\\t\\n]*/[ \\r\\t\\n]*script([ \\r\\t\\n][^>]*>|>)";
    
    private static final String TAG_REGEXP = "<[ \\r\\t\\n]*TAG([ \\r\\t\\n][^>]*>|>)";
    
    private static final String TAG_CLOSING_REGEXP = "<[ \\r\\t\\n]*/TAG([ \\r\\t\\n][^>]*>|>)";
    
    /**
     * Remove any script tags found to be embedded in the input string, this is useful for preventing javascript injection attacks.  
     * Any script tags present in the input will be replaced with a blank space (" ").
     * 
     * @param input the string to remove scripting elements from
     * 
     * @return the same string, with any script elements ("<script>...</script>") present removed.
     */
    public static String removeScriptTags(String input) {
        return replaceScriptTags(input, " ");
    }
    
    /**
     * Remove any script tags found to be embedded in the input string, this is useful for preventing javascript injection attacks.  
     * Any script tags present in the input will be replaced with the specified replacement string.
     * 
     * @param input the string to remove scripting elements from
     * @param replacement the string to replace scripting element with
     * 
     * @return the same string, with any script elements ("<script>...</script>") present removed.
     */
    public static String replaceScriptTags(String input, String replacement) {
        if (input == null) {
            return input;
        }
        if (replacement == null) {
            replacement = "";
        }
        Pattern regexp = Pattern.compile(SCRIPT_REGEXP, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher = regexp.matcher(input);
        if (matcher.find()) {
            return matcher.replaceAll(replacement);
        }
        return input;
    }
    
    /**
     * Strip the input text of any document level elements, like <html>, <head>, and <body> tags.  The 
     * contents of these tags are allowed to persist, but the tags themselves are removed from the 
     * markup.
     * 
     * @param input the string to remove these tags from
     * @return a cleaned string that does not include any document-level tag elements
     */
    public static String removeDocumentLevelElements(String input) {
        if (input == null) {
            return input;
        }
        //first get rid of that silly document header thing
        input = replaceRegexp(input, TAG_REGEXP.replace("TAG", "!DOCTYPE"), "");
        
        //now repeat for <hmtl>, <body>, <head>, and <meta> tags
        input = replaceRegexp(input, TAG_REGEXP.replace("TAG", "html"), "");
        input = replaceRegexp(input, TAG_REGEXP.replace("TAG", "body"), "");
        input = replaceRegexp(input, TAG_REGEXP.replace("TAG", "head"), "");
        input = replaceRegexp(input, TAG_REGEXP.replace("TAG", "meta"), "");
        
        //now do the closing tags
        input = replaceRegexp(input, TAG_CLOSING_REGEXP.replace("TAG", "html"), "");
        input = replaceRegexp(input, TAG_CLOSING_REGEXP.replace("TAG", "body"), "");
        input = replaceRegexp(input, TAG_CLOSING_REGEXP.replace("TAG", "head"), "");
        input = replaceRegexp(input, TAG_CLOSING_REGEXP.replace("TAG", "meta"), "");
        
        return input;
    }
    
    private static String replaceRegexp(String input, String pattern, String replacement) {
        Pattern regexp = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher = regexp.matcher(input);
        if (matcher.find()) {
            return matcher.replaceAll(replacement);
        }
        return input;
    }
    
    /**
     * Escape any HTML formatting characters ('<' and '>') such that when rendered by a browser these characters 
     * are interpreted as literal text instead of as markup code.  Literal single and double quote characters are 
     * also escaped so that the returned string can also be safely used inside of tag elements.
     * 
     * @param input the input string to escape
     * 
     * @return the input string with all '<', '>', "'", and '"' characters escaped.
     */
    public static String escapeMarkupChars(String input) {
        if (input == null) {
            return input;
        }
        input = input.replaceAll("<", "&lt;");
        input = input.replaceAll(">", "&gt;");
        input = input.replaceAll("\\\"", "&quot;");
        input = input.replaceAll("\\'", "&apos;");
        
        return input;
    }
    
    public static String partialUnescape(String input) {
    	if (input == null) {
    		return input;
    	}
    	input = input.replaceAll("\\&quot;", "\"");
        input = input.replaceAll("\\&apos;", "'");
        
        return input;
    }
}
