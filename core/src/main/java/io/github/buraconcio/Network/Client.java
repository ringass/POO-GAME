package io.github.buraconcio.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import com.badlogic.gdx.utils.Json;

import io.github.buraconcio.Objects.Player;
import io.github.buraconcio.Utils.Constants;
import io.github.buraconcio.Utils.PlayerManager;

public class Client {

    private Socket socket;
    private ServerListener listener;
    private boolean svScreen = true;

    public void startTCPClient() {
        Thread thread = new Thread(() -> connect());
        thread.setDaemon(true);
        thread.start();
    }

    public void connect() {
        try {

            socket = new Socket();
            socket.connect(new InetSocketAddress(Constants.IP, Constants.PORT), 5000);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            String serverMsg = (String) in.readObject();
            System.out.println("receive: " + serverMsg);
            out.flush();

            sendLocalPlayer(out);

            receivePlayerList(in);

            // receber estagios e outras atualizacoes por tcp

        } catch (IOException | ClassNotFoundException e) {

            if (listener != null) {
                listener.ServerDisconnected();
            }

            if ("Socket closed".equals(e.getMessage())) {

                System.err.println("connection successfully closed");

            } else {

                // e.printStackTrace();

                System.out.println("connection erro" + e.getMessage());


            }
        }
    }

    public void sendLocalPlayer(ObjectOutputStream out) throws IOException {
        Player p = PlayerManager.getInstance().getLocalPlayer();
        out.writeObject(p);
        out.flush();
    }

    public void receivePlayerList(ObjectInputStream in) throws IOException, ClassNotFoundException {

        while (svScreen) {
            Object obj = in.readObject();

            if (obj instanceof Boolean) {

                Boolean msg = (Boolean) obj;

                if (!msg) {
                    svScreen = false;
                }

            } else if (obj instanceof List<?>) {

                @SuppressWarnings("unchecked")
                List<Player> players = (List<Player>) obj;
                PlayerManager.getInstance().setPlayers(players);

                if (listener != null) {
                    listener.PlayerCon();
                }
            } else if (obj instanceof String) {
                String msg = (String) obj;

                if (msg.equals("get out")) {

                    disconnect();

                    if (listener != null) {
                        listener.ServerDisconnected();
                    }

                    svScreen = false;

                }else if(msg.equals("tocancel")){

                    if(listener !=null){
                        listener.ServerStartMatch();
                    }

                }else if(msg.equals("tostart")){
                    if(listener !=null){
                        listener.ServerCancelMatch();
                    }
                }
            }
        }
    }

    public interface ServerListener { // puxar o refresh
        void PlayerCon();

        void ServerDisconnected();

        void ServerStartMatch();

        void ServerCancelMatch();
    }

    public void setServerListener(ServerListener listener) {
        this.listener = listener;
    }

    public void disconnect() throws IOException {

        if (socket != null && socket.isConnected()) {
            socket.close();
        }

        System.out.println("client disconnect");
    }

}
