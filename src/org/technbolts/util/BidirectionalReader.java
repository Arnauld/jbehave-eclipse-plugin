package org.technbolts.util;

import static org.technbolts.util.IO.CR;
import static org.technbolts.util.IO.LF;

import org.technbolts.util.IO.LineSeparator;


public class BidirectionalReader implements BidirectionalStream {
    private BidirectionalStream stream;
    private int pos;
    private boolean eof;
    private LineSeparator lineSeparator;

    public BidirectionalReader(BidirectionalStream stream) {
        super();
        this.stream = stream;
        this.pos = 0;
    }
    
    public boolean eof() {
        return eof;
    }
    
    public LineSeparator getLineSeparator() {
        return lineSeparator;
    }

    public StringBuilder readLine() {
        StringBuilder builder = new StringBuilder (80);
        
        int read;
        while((read=read()) != IO.EOF) {
            builder.append((char)read);
            if(read==LF) {
                lineSeparator = LineSeparator.LineFeed;
                return builder;
            }
            else if(read==CR) {
                read = read();
                if(read==LF) {
                    lineSeparator = LineSeparator.CRLF;
                    builder.append((char)read);
                }
                else{
                    lineSeparator = LineSeparator.CarriageReturn;
                    unread();
                }
                return builder;
            }
        }
        return builder;
    }
    
    public int getPosition() {
        return pos;
    }

    public void backToPosition(int newPos) {
        while(pos>newPos)
            unread();
    }
    
    public int peek () {
        int read = read();
        unread();
        return read;
    }

    public int read() {
        int read = stream.read();
        if(read==IO.EOF)
            eof = true;
        else
            pos++;
        return read;
    }

    public void unread() {
        pos--;
        eof = false;
        stream.unread();
    }
}
