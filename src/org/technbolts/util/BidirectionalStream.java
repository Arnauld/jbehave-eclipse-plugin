package org.technbolts.util;


public interface BidirectionalStream {
    
    public int read();

    public void unread();
}