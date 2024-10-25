package com.sachin.SocketServer.controller;

import com.sachin.SocketServer.RoomManager;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketController extends TextWebSocketHandler {

  private final RoomManager roomManager = new RoomManager(); // Instantiate RoomManager

  @Override
  public void afterConnectionEstablished(@NotNull WebSocketSession session) {
    System.out.println("New connection established: " + session.getId());
  }

  @Override
  public void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message)
      throws Exception {
    System.out.println(
        "Received message: " + message.getPayload() + " from session: " + session.getId());

    JSONObject jsonMessage;
    String roomId = "";

    try {
      jsonMessage = new JSONObject(message.getPayload());
    } catch (JSONException e) {
      sendError(session, "Invalid JSON format");
      return;
    }

    try {
      if (!jsonMessage.has("type")) {
        sendError(session, "'type' field is missing in the message");
        return;
      }

      String messageType = jsonMessage.getString("type");

      if (jsonMessage.has("roomId")) {
        roomId = jsonMessage.getString("roomId");
      }

      switch (messageType) {
        case "createRoom":
          handleCreateRoom(session, jsonMessage);
          break;
        case "callerCandidate":
          setCallerCandidates(jsonMessage);
          break;
        case "joinRoom":
          handleJoinRoom(session, roomId);
          break;
        case "checkRoom":
          handleCheckRoom(session, roomId);
          break;
        case "answer":
        case "calleeCandidate":
          handleSignalingMessage(jsonMessage, session);
          break;
        case "mediaToggle":
          handleToggleMedia(session, jsonMessage);
          break;
        case "removeRoom":
          removeRoom(session, roomId);
          break;
        default:
          sendError(session, "Unknown message type: " + messageType);
      }
    } catch (JSONException e) {
      sendError(session, "Error processing JSON message: " + e.getMessage());
    }
  }

  private void removeRoom(@NotNull WebSocketSession session, String roomId)
      throws IOException, JSONException {
    roomManager.removeRoom(roomId);
    JSONObject response = new JSONObject();
    response.put("type", "roomRemoved");
    response.put("roomId", roomId);

    session.sendMessage(new TextMessage(response.toString()));
  }

  private void setCallerCandidates(JSONObject jsonMessage) {
    try {
      String roomId = jsonMessage.getString("roomId");

      if (jsonMessage.has("candidate")) {
        roomManager.setCandidates(roomId, jsonMessage.toString());
      }
    } catch (JSONException e) {
      throw new RuntimeException("Error retrieving candidates: " + e.getMessage());
    }
  }

  private void handleCreateRoom(WebSocketSession session, JSONObject jsonMessage)
      throws IOException {
    try {
      String roomId = jsonMessage.getString("roomId");

      if (roomManager.createRoom(roomId, session)) {
        roomManager.setOffer(roomId, jsonMessage.getString("sdp"));
        sendRoomCreated(session, roomId);
      } else {
        sendError(session, "Room already exists");
      }
    } catch (JSONException | IOException e) {
      sendError(session, "Error retrieving room data: " + e.getMessage());
    }
  }

  private void handleCheckRoom(@NotNull WebSocketSession session, String roomId)
      throws IOException {
    try {
      JSONObject response = new JSONObject();
      response.put("type", "roomExists");
      response.put("exists", roomManager.roomExists(roomId));
      response.put("callerCandidate", roomManager.getCandidates(roomId));

      session.sendMessage(new TextMessage(response.toString()));
    } catch (JSONException | IOException e) {
      sendError(session, "Error checking room: " + e.getMessage());
    }
  }

  private void handleJoinRoom(WebSocketSession session, String roomId)
      throws JSONException, IOException {
    if (roomManager.roomExists(roomId)) {
      roomManager.joinRoom(roomId, session);
      // If there's an existing offer in the room, send it to the new participant
      String offer = roomManager.getOffer(roomId);
      sendOfferAfterRoomJoined(session, offer);
    } else {
      sendError(session, "Room not found");
    }
  }

  private void handleSignalingMessage(JSONObject jsonMessage, WebSocketSession session)
      throws IOException {
    try {
      String roomId = jsonMessage.getString("roomId");

      if (!roomManager.roomExists(roomId)) {
        sendError(session, "Room not found");
        return;
      }

      // Get the peer (the other participant) in the room
      WebSocketSession peer = roomManager.getPeer(roomId, session);

      // If there's no peer or the peer's connection is not open, handle the error
      if (peer == null || !peer.isOpen()) {
        sendError(session, "Peer not connected or unavailable");
        return;
      }

      // Forward the signaling message (SDP answer or ICE candidate) to the peer
      if (jsonMessage.has("sdp") || jsonMessage.has("candidate")) {
        peer.sendMessage(new TextMessage(jsonMessage.toString()));
      }
    } catch (JSONException | IOException e) {
      sendError(session, "Error processing signaling message: " + e.getMessage());
    }
  }

  private void handleToggleMedia(WebSocketSession session, @NotNull JSONObject jsonMessage)
      throws IOException {
    try {
      String roomId = jsonMessage.getString("roomId");
      String mediaType = jsonMessage.getString("mediaType");
      String action = jsonMessage.getString("action");

      // Check if the room exists
      if (!roomManager.roomExists(roomId)) {
        sendError(session, "Room not found");
        return;
      }

      // Get the peer (the other participant in the room)
      WebSocketSession peer = roomManager.getPeer(roomId, session);

      // If no peer is found or peer's connection is closed, return an error
      if (peer == null || !peer.isOpen()) {
        sendError(session, "Peer not connected or unavailable");
        return;
      }

      // Prepare the media toggle update message
      JSONObject mediaUpdate = new JSONObject();
      mediaUpdate.put("type", "mediaToggle");
      mediaUpdate.put("mediaType", mediaType);
      mediaUpdate.put("action", action);

      // Send the media toggle message to the peer
      peer.sendMessage(new TextMessage(mediaUpdate.toString()));
    } catch (JSONException | IOException e) {
      sendError(session, "Error processing media toggle: " + e.getMessage());
    }
  }

  private void sendRoomCreated(@NotNull WebSocketSession session, String roomId)
      throws JSONException, IOException {
    JSONObject response = new JSONObject();
    try {
      response.put("type", "roomCreated");
      response.put("roomId", roomId);

      session.sendMessage(new TextMessage(response.toString()));
    } catch (JSONException | IOException e) {
      sendError(session, "Error could create room: " + e.getMessage());
    }
  }

  private void sendOfferAfterRoomJoined(WebSocketSession session, String offer) throws IOException {
    try {
      if (offer != null) {
        JSONObject offerMessage = new JSONObject();
        offerMessage.put("type", "offer");
        offerMessage.put("sdp", offer);
        session.sendMessage(new TextMessage(offerMessage.toString()));
      } else {
        sendError(session, "'offer' is not available");
      }
    } catch (JSONException | IOException e) {
      sendError(session, "Error could create room: " + e.getMessage());
    }
  }

  private void sendError(@NotNull WebSocketSession session, String error) throws IOException {
    JSONObject response = new JSONObject();
    try {
      response.put("type", "error");
      response.put("message", error);
      session.sendMessage(new TextMessage(response.toString()));
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }
}
