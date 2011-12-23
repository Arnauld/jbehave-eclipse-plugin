package org.technbolts.jbehave.eclipse.editors.story.outline;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jbehave.core.steps.StepType;
import org.technbolts.jbehave.eclipse.PotentialStep;
import org.technbolts.jbehave.eclipse.editors.story.StoryEditor;
import org.technbolts.util.New;

public class QuickOutlinePopupDialogMain {
    public static void main(String[] args) {
        List<PotentialStep> steps = New.arrayList();
        steps.add(new PotentialStep(null, null, StepType.GIVEN, "a user named $username"));
        steps.add(new PotentialStep(null, null, StepType.WHEN, "the user's firstname is changed to $firstname"));
        steps.add(new PotentialStep(null, null, StepType.WHEN, "the user's lastname is changed to $lastname"));
        steps.add(new PotentialStep(null, null, StepType.WHEN, "the user's login is changed to $login"));
        steps.add(new PotentialStep(null, null, StepType.WHEN, "the user clicks on $button button"));
        steps.add(new PotentialStep(null, null, StepType.THEN, "the '$password' field becomes '$color'"));
        steps.add(new PotentialStep(null, null, StepType.THEN, "the page title is '$title'"));
        steps.add(new PotentialStep(null, null, StepType.THEN, "the user is logged"));
        steps.add(new PotentialStep(null, null, StepType.GIVEN, "the account balance is $amount"));
        steps.add(new PotentialStep(null, null, StepType.GIVEN, "the card is valid"));
        steps.add(new PotentialStep(null, null, StepType.GIVEN, "the machine contains enough money"));
        steps.add(new PotentialStep(null, null, StepType.WHEN, "the Account Holder requests $amount"));
        steps.add(new PotentialStep(null, null, StepType.THEN, "the ATM should not dispense any money"));
        steps.add(new PotentialStep(null, null, StepType.THEN, "the ATM should say there are insufficient funds"));
        steps.add(new PotentialStep(null, null, StepType.THEN, "the account balance should be $amount"));
        steps.add(new PotentialStep(null, null, StepType.THEN, "the card should be returned"));

        final StoryEditor editor = mock(StoryEditor.class);
        when(editor.getPotentialSteps()).thenReturn(steps);
        
        final Display display = new Display();
        final Shell shell = new Shell(display, SWT.DIALOG_TRIM);
        
        final ImageRegistry imageRegistry = new ImageRegistry(display);

        Button btn = new Button(shell, SWT.PUSH);
        btn.setText("Press to see the InfoPopup");
        btn.setBounds(90, 10, 200, 30);

        btn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se) {
                // You can set the sixe of the Rectangle
                new QuickOutlinePopupDialog(new Shell(), SWT.NONE, editor, imageRegistry).open();
            }
        });
        shell.setSize(400, 200);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
    
}
