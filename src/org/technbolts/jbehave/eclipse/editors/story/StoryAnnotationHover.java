package org.technbolts.jbehave.eclipse.editors.story;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.text.source.projection.AnnotationBag;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.technbolts.util.New;

public class StoryAnnotationHover implements IAnnotationHover, IAnnotationHoverExtension {

    private static Logger log = LoggerFactory.getLogger(StoryAnnotationHover.class);


    /**
     * Creates a new default annotation hover.
     *
     */
    public StoryAnnotationHover() {
    }

    /*
     * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, int)
     */
    public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
        List<Annotation> javaAnnotations= getAnnotationsForLine(sourceViewer, lineNumber);
        if (javaAnnotations != null) {

            if (javaAnnotations.size() == 1) {

                // optimization
                Annotation annotation = javaAnnotations.get(0);
                String message = getMessageFrom(annotation);
                if (message != null && message.trim().length() > 0)
                    return formatSingleMessage(message);

            } else {

                List<String> messages= new ArrayList<String>();

                for(Annotation annotation : javaAnnotations) {
                    String message = getMessageFrom(annotation);
                    if (message != null && message.trim().length() > 0)
                        messages.add(message.trim());
                }

                if (messages.size() == 1)
                    return formatSingleMessage((String)messages.get(0));

                if (messages.size() > 1)
                    return formatMultipleMessages(messages);
            }
        }

        return null;
    }
    
    @Override
    public IInformationControlCreator getHoverControlCreator() {
        return new IInformationControlCreator() {
            @SuppressWarnings("restriction")
            public IInformationControl createInformationControl(
                    final Shell parent) {
                if (BrowserInformationControl.isAvailable(parent)) {
                    String font= PreferenceConstants.APPEARANCE_JAVADOC_FONT;
                    BrowserInformationControl iControl= new BrowserInformationControl(parent, font, "Press 'F2' for focus");
                    iControl.setStatusText("Press 'F2' for focus");
                    iControl.addLocationListener(new LocationListener() {
                        @Override
                        public void changing(LocationEvent event) {
                            //System.out.println("StoryAnnotationHover.getHoverControlCreator().new IInformationControlCreator() {...}.createInformationControl(...).new LocationListener() {...}.changing(" + event + ")");
                        }
                        
                        @Override
                        public void changed(LocationEvent event) {
                            //System.out.println("StoryAnnotationHover.getHoverControlCreator().new IInformationControlCreator() {...}.createInformationControl(...).new LocationListener() {...}.changed(" + event + ")");
                        }
                    });
                    return iControl;
                } else {
                    return new DefaultInformationControl(parent, true);
                }
            }
        };
    }
    
    @Override
    public boolean canHandleMouseCursor() {
        return false;
    }
    
    @Override
    public Object getHoverInfo(ISourceViewer sourceViewer,
            ILineRange lineRange,
            int visibleNumberOfLines) {
        return getHoverInfo(sourceViewer, lineRange.getStartLine());
    }
    
    @Override
    public ILineRange getHoverLineRange(ISourceViewer viewer,
            int lineNumber) {
        return new LineRange(lineNumber, 1);
    }

    private String getMessageFrom(Annotation annotation) {
        if(annotation instanceof MarkerAnnotation) {
            String annotationMessage = getMessageFrom((MarkerAnnotation)annotation);
            if(annotationMessage!=null)
                return annotationMessage;
        }
        //return annotation.getText();
        return null;
    }

    private String getMessageFrom(MarkerAnnotation annotation) {
        IMarker marker = annotation.getMarker();
        try {
            if(MarkingStoryValidator.MARKER_ID.equals(marker.getType())) {
                int intCode = marker.getAttribute(Marks.ERROR_CODE, -1);
                Marks.Code errorCode = Marks.Code.lookup(intCode, null);
                if(errorCode==null)
                    return null;
                switch(errorCode) {
                    case MultipleMatchingSteps:
                    case MultipleMatchingSteps_PrioritySelection: {
                        String html = (String)marker.getAttribute(Marks.STEPS_HTML);
                        String message = (String) marker.getAttribute(Marks.MESSAGE);
                        if (message == null) {
                            message = "<b>Multiple steps matching:</b>";
                        }
                        message = StringEscapeUtils.escapeHtml(message);
                        return String.format("%s<br><br>%s", message, html);
                    }
                }
            }
        } catch (CoreException e) {
            log.warn("Unable to retrieve information from marker (" + annotation.getText() + ")", e);
        }
        return null;
    }
    
    /**
     * Tells whether the annotation should be included in
     * the computation.
     *
     * @param annotation the annotation to test
     * @return <code>true</code> if the annotation is included in the computation
     */
    protected boolean isIncluded(Annotation annotation) {
        return true;
    }

    /**
     * Hook method to format the given single message.
     * <p>
     * Subclasses can change this to create a different
     * format like HTML.
     * </p>
     *
     * @param message the message to format
     * @return the formatted message
     */
    protected String formatSingleMessage(String message) {
        return message;
    }

    /**
     * Hook method to formats the given messages.
     * <p>
     * Subclasses can change this to create a different
     * format like HTML.
     * </p>
     *
     * @param messages the messages to format
     * @return the formatted message
     */
    protected String formatMultipleMessages(List<String> messages) {
        StringBuffer buffer= new StringBuffer();
        Iterator<String> e= messages.iterator();
        while (e.hasNext()) {
            buffer.append('\n');
            String listItemText= (String) e.next();
            buffer.append(listItemText); //$NON-NLS-1$
        }
        return buffer.toString();
    }

    private boolean isRulerLine(Position position, IDocument document, int line) {
        if (position.getOffset() > -1 && position.getLength() > -1) {
            try {
                return line == document.getLineOfOffset(position.getOffset());
            } catch (BadLocationException x) {
            }
        }
        return false;
    }

    private IAnnotationModel getAnnotationModel(ISourceViewer viewer) {
        if (viewer instanceof ISourceViewerExtension2) {
            ISourceViewerExtension2 extension= (ISourceViewerExtension2) viewer;
            return extension.getVisualAnnotationModel();
        }
        return viewer.getAnnotationModel();
    }

    private boolean includeAnnotation(Annotation annotation, Position position) {
        if (!isIncluded(annotation))
            return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    private List<Annotation> getAnnotationsForLine(ISourceViewer viewer, int line) {
        IAnnotationModel model= getAnnotationModel(viewer);
        if (model == null)
            return null;

        IDocument document= viewer.getDocument();
        List<Annotation> javaAnnotations= New.arrayList();
        Iterator<Annotation> iterator = model.getAnnotationIterator();

        while (iterator.hasNext()) {
            Annotation annotation= (Annotation) iterator.next();

            Position position= model.getPosition(annotation);
            if (position == null)
                continue;

            if (!isRulerLine(position, document, line))
                continue;

            if (annotation instanceof AnnotationBag) {
                AnnotationBag bag= (AnnotationBag) annotation;
                Iterator<Annotation> e= bag.iterator();
                while (e.hasNext()) {
                    annotation= (Annotation) e.next();
                    position= model.getPosition(annotation);
                    if (position != null && includeAnnotation(annotation, position))
                        javaAnnotations.add(annotation);
                }
                continue;
            }

            if (includeAnnotation(annotation, position))
                javaAnnotations.add(annotation);
        }

        return javaAnnotations;
    }
    
}
