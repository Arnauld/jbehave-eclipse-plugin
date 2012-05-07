package org.technbolts.jbehave.eclipse.console;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

public class ScriptStyleRange extends StyleRange {

    public static final int UNKNOWN = -1;
    public static final int STDIN = 0;
    public static final int PROMPT = 1;
    public static final int STDOUT = 2;
    public static final int STDERR = 3;

    public int scriptType;

    public ScriptStyleRange(final int start, final int len, final Color fore,
            final Color back, final int scriptType) {
        super(start, len, fore, back);
        Assert.isTrue(len >= 0);
        this.scriptType = scriptType;
    }

}
