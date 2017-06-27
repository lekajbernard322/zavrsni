package hr.zavrsni.zavrsnitest2.utils.tcp;

import java.nio.ByteBuffer;

public class Attachment {
    ByteBuffer inputBuffer, outputBuffer;

    public Attachment() {
        inputBuffer = ByteBuffer.allocate(1024);
        outputBuffer = ByteBuffer.allocate(1024);
    }
}
