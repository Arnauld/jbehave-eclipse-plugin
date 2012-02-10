package org.technbolts.eclipse.util;

import org.testng.annotations.Test;

public class HtmlStateMachineTest {

    @Test
    public void sample_1() throws Exception {
        String content = "<b>Ambiguous steps</b>" +
                "<ul>" +
                    "<li>when an agent $clicks on the button<br/>(<code>Agent#clicks</code>)</li>" +
                    "<li>when an agent $enters (<code>Agent#enters</code>)</li>" +
                    "</ul>";
        new HtmlStateMachine().parse("<root>"+content+"</root>");
    }
}
