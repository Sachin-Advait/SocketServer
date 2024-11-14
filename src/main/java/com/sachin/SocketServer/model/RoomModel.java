package com.sachin.SocketServer.model;

public class RoomModel {
  private String roomId;
  private String offer;
  private String candidate;
  private String userName;
  private String image;
  private Boolean audio;
  private Boolean video;
  private String remotePeerId;

  // Constructor
  public RoomModel(
      String roomId,
      String offer,
      String candidate,
      String userName,
      String image,
      Boolean audio,
      Boolean video,
      String remotePeerId) {
    this.roomId = roomId;
    this.offer = offer;
    this.candidate = candidate;
    this.image = image;
    this.userName = userName;
    this.audio = audio != null ? audio : false;
    this.video = video != null ? video : false;
    this.remotePeerId = remotePeerId;
  }

  // Getters and Setters
  public String getRoomId() {
    return roomId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public String getOffer() {
    return offer;
  }

  public void setOffer(String offer) {
    this.offer = offer;
  }

  public String getCandidate() {
    return candidate;
  }

  public void setCandidate(String candidate) {
    this.candidate = candidate;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public Boolean getAudio() {
    return audio;
  }

  public void setAudio(Boolean audio) {
    this.audio = audio;
  }

  public Boolean getVideo() {
    return video;
  }

  public void setVideo(Boolean video) {
    this.video = video;
  }

  public String getRemotePeerId() {
    return remotePeerId;
  }

  public void setRemotePeerId(String remotePeerId) {
    this.remotePeerId = remotePeerId;
  }
}
