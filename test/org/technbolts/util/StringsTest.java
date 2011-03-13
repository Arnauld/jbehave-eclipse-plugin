package org.technbolts.util;

import static org.junit.Assert.assertEquals;
import static org.technbolts.util.Strings.removeTrailingNewlines;

import org.junit.Test;

public class StringsTest {
    @Test
    public void tet_removeTrailingNewline () {
        assertEquals("a", removeTrailingNewlines("a"));
        assertEquals("a", removeTrailingNewlines("a\r\n"));
        assertEquals("a", removeTrailingNewlines("a\n"));
        assertEquals("a", removeTrailingNewlines("a\n\n"));
        assertEquals("a", removeTrailingNewlines("a\r"));
        assertEquals("a", removeTrailingNewlines("a\r\r"));
        assertEquals("a\nb", removeTrailingNewlines("a\nb\n"));
    }
}
