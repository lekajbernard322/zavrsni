package hr.zavrsni.zavrsnitest2;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import hr.zavrsni.zavrsnitest2.utils.tcp.Message;
import hr.zavrsni.zavrsnitest2.utils.tcp.TcpBase;
import hr.zavrsni.zavrsnitest2.utils.tcp.TcpClient;
import hr.zavrsni.zavrsnitest2.utils.tcp.TcpServer;

public class ChatFragment extends Fragment implements TcpBase.MessageListener<Message> {
    View contentView = null;
    List<Message> messages = new ArrayList<>();
    RecyclerView recyclerView;
    ChatAdapter adapter;
    EditText input;
    Button sendButton;
    TcpThread tcpThread;
    String groupOwnerMac;
    MediaPlayer mp;

    public static Fragment newInstance(boolean isGroupOwner, String groupOwnerAddress, String groupOwnerMac) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isGroupOwner", isGroupOwner);
        bundle.putString("groupOwnerAddress", groupOwnerAddress);
        bundle.putString("groupOwnerMac", groupOwnerMac);
        ChatFragment cf = new ChatFragment();
        cf.setArguments(bundle);

        return cf;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            Bundle bundle = getArguments();
            boolean isGroupOwner = bundle.getBoolean("isGroupOwner");
            String groupOwnerAddress = bundle.getString("groupOwnerAddress");
            groupOwnerMac = bundle.getString("groupOwnerMac");

            tcpThread = new TcpThread(this, isGroupOwner, groupOwnerAddress);
            tcpThread.start();

            mp = MediaPlayer.create(getActivity(), R.raw.message_sound_effect);
        } catch (Exception e) {
            Log.d(MainActivity.TAG, "Failed to create tcp connection...");
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.chat_fragment, null);

        recyclerView = (RecyclerView)
                contentView.findViewById(R.id.chat_fragment_recycler_view);
        adapter = new ChatAdapter(messages, groupOwnerMac);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        input = (EditText) contentView.findViewById(R.id.chat_fragment_edit_text);
        sendButton = (Button) contentView.findViewById(R.id.chat_fragment_send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.getText().length() > 0) {
                    Message message = new Message(groupOwnerMac, input.getText().toString());
                    input.setText("");

                    if (tcpThread != null && tcpThread.tcpObject != null)
                        tcpThread.tcpObject.addToBuffers2(message);
                    if (tcpThread.isServer)
                        onMessageReceived(message);
                }
            }
        });

        return contentView;
    }

    @Override
    public void onMessageReceived(Message message) {
        messages.add(message);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(messages.size() - 1);
                recyclerView.scrollToPosition(messages.size() - 1);
                mp.start();
            }
        });
    }

    private static class TcpThread extends Thread {
        TcpBase<Message> tcpObject = null;
        InetSocketAddress address = null;
        ChatFragment chatFragment = null;
        boolean isServer = false;

        String groupOwnerAddress = "";

        TcpThread(ChatFragment chatFragment, boolean isServer, String groupOwnerAddress) throws Exception {
            this.chatFragment = chatFragment;
            this.isServer = isServer;
            this.groupOwnerAddress = groupOwnerAddress;
        }

        private void initialize() throws Exception {

            if (isServer) {
                address = new InetSocketAddress(45565);
                tcpObject = new TcpServer<>();
            } else {
                address = new InetSocketAddress(groupOwnerAddress, 45565);
                tcpObject = new TcpClient<>();
            }

            Log.d(MainActivity.TAG, "Opening Tcp" + (isServer ? "Server" : "Client") + "...");
            tcpObject.open(address, chatFragment);
        }

        @Override
        public void run() {
            try {
                initialize();

                while (true) {
                    if (tcpObject.getSelector().select() > 0) {
                        if (tcpObject.process(tcpObject.getSelector().selectedKeys()))
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

        private List<Message> messageList;
        String groupOwnerMac;

        ChatAdapter(List<Message> messageList, String groupOwnerMac) {
            this.messageList = messageList;
            this.groupOwnerMac = groupOwnerMac;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_row, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Message message = messageList.get(position);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.helperView.getLayoutParams();
            params.weight = message.getMac().equals(groupOwnerMac) ? 1 : 0;
            holder.helperView.setLayoutParams(params);

            String msg = message.getMac() + ":\n" + message.getMessage();
            holder.messageText.setText(msg);

        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View helperView;
            TextView messageText;
            CardView messageCard;

            ViewHolder(View view) {
                super(view);
                this.helperView = view.findViewById(R.id.message_helper_view);
                this.messageText = (TextView)view.findViewById(R.id.message_text);
                this.messageCard = (CardView) view.findViewById(R.id.message_card_view);
            }
        }

    }
}
