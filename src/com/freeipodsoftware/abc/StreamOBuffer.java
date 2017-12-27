package com.freeipodsoftware.abc;

import javazoom.jl.decoder.Obuffer;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class StreamOBuffer extends Obuffer {
    private OutputStream output;
    private int channels;
    private short[][] buffers;
    private int[] bufferPointerNext;
    private int[] bufferPointerFirst;
    private int pointer;

    public StreamOBuffer(OutputStream output, int channels) {
        this.output = output;
        this.channels = channels;
        this.buffers = new short[channels]['\uffff'];
        this.bufferPointerNext = new int[channels];
        this.bufferPointerFirst = new int[channels];
        this.pointer = 0;
    }

    public void append(int arg0, short arg1) {
        this.buffers[arg0][this.bufferPointerNext[arg0]] = arg1;
        ++this.bufferPointerNext[arg0];

        while (this.bufferPointerNext[this.pointer] > this.bufferPointerFirst[this.pointer]) {
            short value = this.buffers[this.pointer][this.bufferPointerFirst[this.pointer]];
            ++this.bufferPointerFirst[this.pointer];

            try {
                this.output.write((byte) (value >> 8 & 255));
                this.output.write((byte) (value & 255));
            } catch (Exception var6) {
                StringWriter stackTrace = new StringWriter();
                var6.printStackTrace(new PrintWriter(stackTrace));
                throw new RuntimeException(stackTrace.getBuffer().toString());
            }

            ++this.pointer;
            if (this.pointer >= this.channels) {
                this.pointer = 0;
            }
        }

    }

    public void clear_buffer() {
        this.bufferPointerNext = new int[this.channels];
        this.bufferPointerFirst = new int[this.channels];
        this.pointer = 0;
    }

    public void close() {
    }

    public void set_stop_flag() {
    }

    public void write_buffer(int arg0) {
    }
}
