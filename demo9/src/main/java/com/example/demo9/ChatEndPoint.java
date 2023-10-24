package com.example.demo9;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint(
        value = "/chat/{username}",
        encoders = MessageEncoder.class,
        decoders = MessageDecoder.class)

public class ChatEndPoint {

    private Session session;

    private static Set<ChatEndPoint> chatEndPoints = new HashSet<>();
    private static HashMap<String, String> users = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username){
        this.session = session;
        chatEndPoints.add(this);
        users.put(session.getId(), username);

        Message message = new Message();
        message.setFrom(username);
        message.setContent("Connected!");

        broadcast(message);

    }


    @OnMessage
    public void OnMessage(Session session, Message message){
        message.setFrom(users.get(session.getId()));
        broadcast(message);
    }

    @OnClose
    public void OnClose(Session session){
        chatEndPoints.remove(this);
        Message message = new Message();
        message.setFrom(users.get(session.getId()));
        message.setContent("Disconnected!");
        broadcast(message);
    }

    public static void broadcast(Message message){
        chatEndPoints.forEach(chatEndPoint -> {
            synchronized (chatEndPoint){
                try {
                    chatEndPoint.session.getBasicRemote().sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
