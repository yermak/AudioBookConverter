//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.freeipodsoftware.abc;

import java.io.IOException;
import java.io.OutputStream;

public class ResamplingOutputStream extends OutputStream {
    private static final int MAX_BUFFER_SIZE = 16384;
    private int channels;
    private OutputStream outputStream;
    private byte[][][] buffer;
    private int bufferSize;
    private int currentChannel;
    private int currentOctet;
    private double factor;
    private int newChannels;

    public ResamplingOutputStream(OutputStream outputStream, int channels, int newChannels, int sampleFrequency, int newSampleFrequency) {
        if(outputStream == null) {
            throw new NullPointerException("OutputStream must be set.");
        } else {
            this.assertGreaterThanNull(channels, "Channels");
            this.assertGreaterThanNull(sampleFrequency, "FrequencyBefore");
            this.assertGreaterThanNull(newSampleFrequency, "FrequencyAfter");
            this.outputStream = outputStream;
            this.channels = channels;
            this.newChannels = newChannels;
            this.factor = (double)newSampleFrequency / (double)sampleFrequency;
            this.buffer = new byte[channels][16384][2];
            this.bufferSize = 0;
            this.currentChannel = 0;
            this.currentOctet = 0;
        }
    }

    public void write(int b) throws IOException {
        this.buffer[this.currentChannel][this.bufferSize][this.currentOctet] = (byte)(b & 255);
        ++this.currentOctet;
        if(this.currentOctet == 2) {
            this.currentOctet = 0;
            ++this.currentChannel;
            if(this.currentChannel == this.channels) {
                this.currentChannel = 0;
                ++this.bufferSize;
                if(this.bufferSize == 16384) {
                    this.writeOut();
                }
            }
        }

    }

    private void writeOut() throws IOException {
        for(int i = 0; (double)i < (double)this.bufferSize * this.factor; ++i) {
            int channelIndex = 0;

            for(int y = 0; y < this.newChannels; ++y) {
                ++channelIndex;
                if(channelIndex == this.channels) {
                    channelIndex = 0;
                }

                this.outputStream.write(this.buffer[channelIndex][(int)((double)i / this.factor)][0]);
                this.outputStream.write(this.buffer[channelIndex][(int)((double)i / this.factor)][1]);
            }
        }

        this.bufferSize = 0;
        this.currentChannel = 0;
    }

    public void flush() throws IOException {
        super.flush();
        this.writeOut();
    }

    private void assertGreaterThanNull(int value, String name) {
        if(value <= 0) {
            throw new RuntimeException(name + " must be set to a vaule > 0.");
        }
    }

    public void close() throws IOException {
        this.flush();
        super.close();
    }
}
