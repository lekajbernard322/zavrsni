package hr.zavrsni.zavrsnitest2.utils.tcp;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TcpClient<T> extends TcpBase<T> {
    SocketChannel channel = null;

    @Override
    public void open(InetSocketAddress address, MessageListener<T> listener) throws Exception {
        this.listener = listener;
        selector = Selector.open();
        channel = SocketChannel.open();

        channel.configureBlocking(false);
        channel.connect(address);
        channel.register(selector, channel.validOps());
    }

    @Override
    public void close() throws Exception {
        super.close();
        channel.close();
    }

    @Override
    public int onRead(SelectionKey key) throws Exception {
        int read = super.onRead(key);
        Attachment attachment = (Attachment) key.attachment();
        attachment.inputBuffer.clear();

        return read;
    }

    @Override
    public void onWrite(SelectionKey key) throws Exception {
        super.onWrite(key);
    }
}
