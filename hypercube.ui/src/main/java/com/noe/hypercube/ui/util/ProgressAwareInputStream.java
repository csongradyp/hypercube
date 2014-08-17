package com.noe.hypercube.ui.util;

import java.io.IOException;
import java.io.InputStream;

public class ProgressAwareInputStream extends InputStream {
    private InputStream wrappedInputStream;
    private Long size;
    private Object tag;
    private Long counter;
    private Double lastPercent;
    private OnProgressListener listener;

    public ProgressAwareInputStream(InputStream in, long size, Object tag) {
        wrappedInputStream = in;
        this.size = size;
        this.tag = tag;
        counter = 0L;
        lastPercent = (double) 0;
    }

    public void setOnProgressListener(OnProgressListener listener) {
        this.listener = listener;
    }

    public Object getTag() {
        return tag;
    }

    @Override
    public int read() throws IOException {
        counter += 1;
        check();
        return wrappedInputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int retVal = wrappedInputStream.read(b);
        counter += retVal;
        check();
        return retVal;
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        int retVal = wrappedInputStream.read(b, offset, length);
        counter += retVal;
        check();
        return retVal;
    }

    private void check() {
        Double percent = counter / size.doubleValue();
        if (percent - lastPercent >= 0.001d) {
            lastPercent = percent;
            if (listener != null)
                listener.onProgress(percent, tag);
        }
    }

    @Override
    public void close() throws IOException {
        wrappedInputStream.close();
    }

    @Override
    public int available() throws IOException {
        return wrappedInputStream.available();
    }

    @Override
    public void mark(int readlimit) {
        wrappedInputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        wrappedInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return wrappedInputStream.markSupported();
    }

    @Override
    public long skip(long n) throws IOException {
        return wrappedInputStream.skip(n);
    }

    /**
     * Interface for classes that want to monitor this input stream
     */
    public interface OnProgressListener {
        void onProgress(Double percentage, Object tag);
    }
}


