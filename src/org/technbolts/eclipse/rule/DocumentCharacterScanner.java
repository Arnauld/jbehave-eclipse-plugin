package org.technbolts.eclipse.rule;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;

public class DocumentCharacterScanner implements ICharacterScanner {

    private final IDocument document;
    private final int offset;
    private final int length;
    private final char[][] delimiters;
    //
    private int cursor;
    /** The cached column of the current scanner position */
    protected int column;
    /** Internal setting for the un-initialized column cache. */
    protected static final int UNDEFINED = -1;

    public DocumentCharacterScanner(IDocument document, int offset, int length) {
        super();
        this.document = document;
        this.offset = offset;
        this.length = length;
        this.cursor = offset;
        this.column = UNDEFINED;

        String[] delimiters = document.getLegalLineDelimiters();
        this.delimiters = new char[delimiters.length][];
        for (int i = 0; i < delimiters.length; i++)
            this.delimiters[i] = delimiters[i].toCharArray();
    }

    /*
     * @see ICharacterScanner#getColumn()
     */
    public int getColumn() {
        if (column == UNDEFINED) {
            try {
                int line = document.getLineOfOffset(offset);
                int start = document.getLineOffset(line);

                column = offset - start;

            } catch (BadLocationException ex) {
            }
        }
        return column;
    }

    /*
     * @see ICharacterScanner#getLegalLineDelimiters()
     */
    public char[][] getLegalLineDelimiters() {
        return delimiters;
    }

    /*
     * @see ICharacterScanner#read()
     */
    public int read() {

        try {

            if (cursor < (offset + length)) {
                try {
                    return document.getChar(cursor);
                } catch (BadLocationException e) {
                }
            }

            return EOF;

        } finally {
            ++cursor;
            column = UNDEFINED;
        }
    }

    /*
     * @see ICharacterScanner#unread()
     */
    public void unread() {
        --cursor;
        column = UNDEFINED;
    }

}
