package hr.zavrsni.zavrsnitest2.utils.tcp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import hr.zavrsni.zavrsnitest2.utils.BufferUtils;
import hr.zavrsni.zavrsnitest2.utils.NetworkUtils;

public abstract class TcpBase<T> {
    Selector selector = null;
    private List<Attachment> attachments = new ArrayList<>();
    MessageListener listener;

    public interface MessageListener<T> {
        void onMessageReceived(T message);
    }

    public abstract void open(InetSocketAddress address, MessageListener<T> listener) throws Exception;

    public void close() throws Exception {
        selector.close();
    }

    public Selector getSelector() {
        return selector;
    }

    public void addToBuffers2(T object) {
        addToBuffers(ByteBuffer.wrap(NetworkUtils.serialize(object)));
    }

    public void addToBuffers(ByteBuffer buffer) {
        synchronized (this) {
            for (Attachment a : attachments) {
                if (a.outputBuffer.position() > 0) {
                    List<Object> objects = NetworkUtils.deserializeMultiple(
                            BufferUtils.byteBufferToArray(buffer));
                    objects.addAll(NetworkUtils.deserializeMultiple(
                            BufferUtils.byteBufferToArray(a.outputBuffer)));
                    byte[] bytes = NetworkUtils.serializeMultiple(objects);
                    a.outputBuffer.clear();
                    a.outputBuffer.put(bytes);
                }
                else
                    BufferUtils.cloneByteBuffer2(buffer, a.outputBuffer);
            }
        }
    }

    public int onRead(SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();

        int read = channel.read(attachment.inputBuffer);
        if (read > 0)
            System.out.println("Read " + read + " bytes...");

        List<Object> lo = NetworkUtils.deserializeMultiple(
                BufferUtils.byteBufferToArray(attachment.inputBuffer));

        for (Object o : lo) {
            T m = (T) o;
            listener.onMessageReceived(m);
        }

        return read;
    }

    public void onWrite(SelectionKey key) throws Exception {
        synchronized (this) {
            SocketChannel channel = (SocketChannel) key.channel();
            Attachment attachment = (Attachment) key.attachment();
            ByteBuffer readOnly = attachment.outputBuffer.asReadOnlyBuffer();
            readOnly.flip();
            int written = channel.write(readOnly);
            if (written > 0)
                System.out.println("Written " + written + " bytes...");

            attachment.outputBuffer.clear();
        }
    }

    public boolean process(Set<SelectionKey> keys) throws Exception {
        Iterator<SelectionKey> iter = keys.iterator();

        while (iter.hasNext()) {
            SelectionKey key = iter.next();
            iter.remove();

            if (key.isAcceptable()) {
                onAccept(key);
            }
            if (key.isConnectable()) {
                boolean connected = onConnect(key);
                if (!connected)
                    return true;
            }
            if (key.isReadable()) {
                onRead(key);
            }
            if (key.isWritable()) {
                onWrite(key);
            }
        }

        return false;
    }

    private void onAccept(SelectionKey key) throws Exception {
        ServerSocketChannel server =
                (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();

        if (client != null) {
            client.configureBlocking(false);
            SelectionKey clientKey = client.register(key.selector(), client.validOps());
            Attachment attachment = new Attachment();
            clientKey.attach(attachment);
            attachments.add(attachment);
        }
    }

    private boolean onConnect(SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();

        while (channel.isConnectionPending())
            channel.finishConnect();

        Attachment attachment = new Attachment();
        key.attach(attachment);
        attachments.add(attachment);

        return true;
    }

}
