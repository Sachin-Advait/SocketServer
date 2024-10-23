package com.sachin.SocketServer;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
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
    } catch (Exception e) {
      sendError(session, "Invalid JSON format");
      return;
    }

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
      default:
        sendError(session, "Unknown message type: " + messageType);
    }
  }

  @SneakyThrows
  private void setCallerCandidates(JSONObject jsonMessage) {
    String roomId = jsonMessage.getString("roomId");

    if (jsonMessage.has("candidate")) {
      roomManager.setCandidates(roomId, jsonMessage.toString());
    }
  }

  @SneakyThrows
  private void handleCreateRoom(WebSocketSession session, JSONObject jsonMessage) {

    String roomId = jsonMessage.getString("roomId");

    if (roomManager.createRoom(roomId, session)) {
      roomManager.setOffer(roomId, jsonMessage.getString("sdp"));
      sendRoomCreated(session, roomId);

    } else {
      sendError(session, "Room already exists");
    }
  }

  @SneakyThrows
  private void handleCheckRoom(@NotNull WebSocketSession session, String roomId) {
    JSONObject response = new JSONObject();
    response.put("type", "roomExists");
    response.put("exists", roomManager.roomExists(roomId));
    response.put("callerCandidate", roomManager.getCandidates(roomId));

    session.sendMessage(new TextMessage(response.toString()));
  }

  @SneakyThrows
  private void handleJoinRoom(WebSocketSession session, String roomId) {
    if (roomManager.roomExists(roomId)) {
      roomManager.joinRoom(roomId, session);
      // sendJoinedRoom(session, roomId);

      // If there's an existing offer in the room, send it to the new participant
      String offer = roomManager.getOffer(roomId);
      sendOfferAfterRoomJoined(session, offer);
    } else {
      sendError(session, "Room not found");
    }
  }

  @SneakyThrows
  private void handleSignalingMessage(JSONObject jsonMessage, WebSocketSession session) {

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
  }

  @SneakyThrows
  private void handleToggleMedia(WebSocketSession session, @NotNull JSONObject jsonMessage) {
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
  }

  @SneakyThrows
  private void sendRoomCreated(@NotNull WebSocketSession session, String roomId) {
    JSONObject response = new JSONObject();
    response.put("type", "roomCreated");
    response.put("roomId", roomId);

    session.sendMessage(new TextMessage(response.toString()));
  }

  @SneakyThrows
  private void sendOfferAfterRoomJoined(WebSocketSession session, String offer) {
    if (offer != null) {
      JSONObject offerMessage = new JSONObject();
      offerMessage.put("type", "offer");
      offerMessage.put("sdp", offer);
      session.sendMessage(new TextMessage(offerMessage.toString()));
    } else {
      sendError(session, "'offer' offer is not available");
    }
  }

  @SneakyThrows
  private void sendError(@NotNull WebSocketSession session, String error) {
    JSONObject response = new JSONObject();
    response.put("type", "error");
    response.put("message", error);
    session.sendMessage(new TextMessage(response.toString()));
  }
}
