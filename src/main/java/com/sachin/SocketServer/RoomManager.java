package com.sachin.SocketServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.socket.WebSocketSession;

public class RoomManager {

  private final Map<String, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();
  private final Map<String, String> offers = new ConcurrentHashMap<>(); // Map to hold offers
  private final Map<String, String> candidates =
      new ConcurrentHashMap<>(); // Map to hold candidates

  // Create a new room with the first peer
  public boolean createRoom(String roomId, WebSocketSession session) {
    if (rooms.containsKey(roomId)) {
      return false; // Room already exists
    }

    rooms.put(roomId, new ArrayList<>());
    rooms.get(roomId).add(session); // Add the first peer
    return true;
  }

  // A second peer joins the room
  public void joinRoom(String roomId, WebSocketSession session) {
    if (!rooms.containsKey(roomId)) {
      return; // Room does not exist
    }

    rooms.get(roomId).add(session); // Add the second peer
  }

  // Check if the room exists
  public boolean roomExists(String roomId) {
    return rooms.containsKey(roomId);
  }

  public void removeRoom(String roomId) {
    if (!rooms.containsKey(roomId)) {
      return;
    }
    rooms.remove(roomId);
  }

  // Retrieve the other peer in the room (for peer-to-peer communication)
  public WebSocketSession getPeer(String roomId, WebSocketSession session) {
    List<WebSocketSession> peers = rooms.getOrDefault(roomId, new ArrayList<>());
    for (WebSocketSession peer : peers) {
      if (!peer.equals(session)) {
        return peer; // Return the other peer
      }
    }
    return null; // No other peer found (e.g., only one peer in the room)
  }

  // Remove a peer from the room
  public boolean removePeer(String roomId, WebSocketSession session) {
    if (!rooms.containsKey(roomId)) {
      return false;
    }

    List<WebSocketSession> peers = rooms.get(roomId);
    peers.remove(session);

    // If the room is empty after removing the peer, delete the room
    if (peers.isEmpty()) {
      rooms.remove(roomId);
      offers.remove(roomId); // Remove the offer if room is deleted
      candidates.remove(roomId); // Remove the candidates if room is deleted
    }

    return true;
  }

  // Set the offer for a room
  public void setOffer(String roomId, String offer) {
    offers.put(roomId, offer); // Store the offer for the room
  }

  // Get the offer for a room
  public String getOffer(String roomId) {
    return offers.get(roomId); // Retrieve the offer for the room
  }

  // Set ICE candidates for a room
  public void setCandidates(String roomId, String candidate) {
    candidates.put(roomId, candidate); // Store the candidates for the room
  }

  // Get ICE candidates for a room
  public String getCandidates(String roomId) {
    return candidates.get(roomId); // Retrieve the candidates for the room
  }
}
