package com.sachin.SocketServer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.socket.WebSocketSession;

public class RoomService {

  private final Map<String, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();
  private final DatabaseService databaseService = new DatabaseService();

  // Create a new room with the first peer
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
    databaseService.deleteRoom(roomId); // Delete room data from the database
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

  public void setOffer(String roomId, String offer) {
    databaseService.setOffer(roomId, offer);
  }

  public String getOffer(String roomId) {
    return databaseService.getOffer(roomId);
  }

  // Set and retrieve candidates
  public void setCandidates(String roomId, String candidate) {
    databaseService.setCandidates(roomId, candidate);
  }

  public String getCandidates(String roomId) {
    return databaseService.getCandidates(roomId);
  }

  // Set and retrieve audio state
  public void setUserName(String roomId, String userName) {
    databaseService.setUserName(roomId, userName);
  }

  public String getUserName(String roomId) {
    return databaseService.getUserName(roomId);
  }

  // Set and retrieve audio state
  public void setImage(String roomId, String image) {
    databaseService.setImage(roomId, image);
  }

  public String getImage(String roomId) {
    return databaseService.getImage(roomId);
  }

  // Set and retrieve audio state
  public void setAudio(String roomId, Boolean audio) {
    databaseService.setAudio(roomId, audio);
  }

  public Boolean getAudio(String roomId) {
    return databaseService.getAudio(roomId);
  }

  // Set and retrieve video state
  public void setVideo(String roomId, Boolean video) {
    databaseService.setVideo(roomId, video);
  }

  public Boolean getVideo(String roomId) {
    return databaseService.getVideo(roomId);
  }
}
