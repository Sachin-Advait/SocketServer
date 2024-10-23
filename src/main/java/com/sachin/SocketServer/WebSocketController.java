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
    String offer = "";
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

    if (jsonMessage.has("sdp")) {
      offer = jsonMessage.getString("sdp");
    }

    switch (messageType) {
      case "createRoom":
        handleCreateRoom(session, roomId, offer);
        break;
      case "joinRoom":
        handleJoinRoom(session, roomId);
        break;
      case "checkRoom":
        handleCheckRoom(session, roomId);
        break;
      case "offer":
      case "answer":
      case "candidate":
        handleSignalingMessage(jsonMessage, session);
        break;
      case "muteVideo":
      case "unmuteVideo":
      case "muteAudio":
      case "unmuteAudio":
        handleToggleMedia(session, jsonMessage);
        break;
      default:
        sendError(session, "Unknown message type: " + messageType);
    }
  }

  private void handleCreateRoom(WebSocketSession session, String roomId, String offer) {

    if (roomManager.createRoom(roomId, session)) {
      roomManager.setOffer(roomId, offer);
      sendRoomCreated(session, roomId);

    } else {
      sendError(session, "Room already exists");
    }
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

  private void handleCheckRoom(@NotNull WebSocketSession session, String roomId) throws Exception {
    JSONObject response = new JSONObject();
    response.put("type", "roomExists");
    response.put("exists", roomManager.roomExists(roomId));

    session.sendMessage(new TextMessage(response.toString()));
  }

  private void handleSignalingMessage(@NotNull JSONObject jsonMessage, WebSocketSession session)
      throws Exception {

    String roomId = jsonMessage.getString("roomId");

    if (!roomManager.roomExists(roomId)) {
      sendError(session, "Room not found");
      return;
    }

    // Forward the signaling message (answer or candidate) to all participants
    for (WebSocketSession participant : roomManager.getParticipants(roomId)) {
      if (participant != session && participant.isOpen()) {
        // Here we can add a check to see if the message is an answer or a candidate
        if (jsonMessage.has("sdp") || jsonMessage.has("candidate")) {
          participant.sendMessage(new TextMessage(jsonMessage.toString()));
        }
      }
    }

    // Optionally, you could handle the answer or candidate specifically here
    if (jsonMessage.getString("type").equals("answer")) {
      // Handle answer specifics if necessary
    } else if (jsonMessage.getString("type").equals("candidate")) {
      // Handle candidate specifics if necessary
    }
  }

  private void handleToggleMedia(WebSocketSession session, @NotNull JSONObject jsonMessage)
      throws Exception {
    String roomId = jsonMessage.getString("roomId");
    String mediaType = jsonMessage.getString("mediaType");
    String action = jsonMessage.getString("action");

    if (!roomManager.roomExists(roomId)) {
      sendError(session, "Room not found");
      return;
    }

    for (WebSocketSession participant : roomManager.getParticipants(roomId)) {
      if (participant != session && participant.isOpen()) {
        JSONObject mediaUpdate = new JSONObject();
        mediaUpdate.put("type", "mediaToggle");
        mediaUpdate.put("mediaType", mediaType);
        mediaUpdate.put("action", action);
        participant.sendMessage(new TextMessage(mediaUpdate.toString()));
      }
    }
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
