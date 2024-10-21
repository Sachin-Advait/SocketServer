package com.sachin.SocketServer;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.socket.WebSocketSession;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;


public class RoomManager {

    private final Map<String, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> offers = new ConcurrentHashMap<>(); // Map to hold offers

    public boolean createRoom(String roomId, WebSocketSession session) {
        if (rooms.containsKey(roomId)) {
            return false; // Room already exists
        }

        rooms.put(roomId, new ArrayList<>());
        rooms.get(roomId).add(session);
        return true;
    }

    public void joinRoom(String roomId, WebSocketSession session) {
        if (!rooms.containsKey(roomId)) {
            return; // Room does not exist
        }

        rooms.get(roomId).add(session);
    }

    public boolean roomExists(String roomId) {

        return rooms.containsKey(roomId);
    }

    public List<WebSocketSession> getParticipants(String roomId) {
        return rooms.getOrDefault(roomId, new ArrayList<>());
    }

    public boolean removeParticipant(String roomId, WebSocketSession session) {
        if (!rooms.containsKey(roomId)) {
            return false;
        }

        List<WebSocketSession> participants = rooms.get(roomId);
        participants.remove(session);

        // If the room is empty, remove it
        if (participants.isEmpty()) {
            rooms.remove(roomId);
            offers.remove(roomId); // Remove the offer if room is deleted
        }

        return true;
    }

    public void setOffer(String roomId, String offer) {
        offers.put(roomId, offer); // Store the offer for the room
    }

    public String getOffer(String roomId) {
        return offers.get(roomId); // Retrieve the offer for the room
    }
}
