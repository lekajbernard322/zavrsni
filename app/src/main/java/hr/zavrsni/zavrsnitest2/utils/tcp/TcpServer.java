package hr.zavrsni.zavrsnitest2.utils.tcp;

import android.util.Log;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import hr.zavrsni.zavrsnitest2.MainActivity;

public class TcpServer<T> extends TcpBase<T> {
    private ServerSocketChannel server = null;

    @Override
    public void open(InetSocketAddress address, MessageListener<T> listener) throws Exception {
        this.listener = listener;
        selector = Selector.open();
        server = ServerSocketChannel.open();

        server.configureBlocking(false);
        server.socket().bind(address);
        server.register(selector, server.validOps());
    }

    @Override
    public int onRead(SelectionKey key) throws Exception {
        int read = super.onRead(key);

        if (read > 0) {
            Attachment attachment = (Attachment) key.attachment();

            attachment.inputBuffer.flip();
            addToBuffers(attachment.inputBuffer);
            attachment.inputBuffer.clear();
        }

        return read;
    }

    @Override
    public void onWrite(SelectionKey key) throws Exception {
        super.onWrite(key);
    }
}
