package com.sachin.SocketServer.service;

import com.sachin.SocketServer.dao.RoomDao;
import com.sachin.SocketServer.model.RoomModel;

public class DatabaseService {
  private final RoomDao RoomDao;

  public DatabaseService() {
    this.RoomDao = new RoomDao();
  }

  public void setOffer(String roomId, String offer) {
    RoomModel roomData = RoomDao.getRoomData(roomId);
    if (roomData == null) {
      roomData = new RoomModel(roomId, offer, null, null, null, null, null);
    } else {
      roomData.setOffer(offer);
    }
    RoomDao.saveOrUpdateRoomData(roomData);
  }

  public String getOffer(String roomId) {
    RoomModel roomData = RoomDao.getRoomData(roomId);
    return roomData != null ? roomData.getOffer() : null;
  }

  public void setCandidates(String roomId, String candidate) {
    RoomModel roomData = RoomDao.getRoomData(roomId);
    if (roomData == null) {
      roomData = new RoomModel(roomId, null, candidate, null, null, null, null);
    } else {
      roomData.setCandidate(candidate);
    }
    RoomDao.saveOrUpdateRoomData(roomData);
  }

  public String getCandidates(String roomId) {
    RoomModel roomData = RoomDao.getRoomData(roomId);
    return roomData != null ? roomData.getCandidate() : null;
  }

  public void setUserName(String roomId, String userName) {
    RoomModel roomData = RoomDao.getRoomData(roomId);
    if (roomData == null) {
      roomData = new RoomModel(roomId, null, null, userName, null, null, null);
    } else {
      roomData.setUserName(userName);
    }
    RoomDao.saveOrUpdateRoomData(roomData);
  }

  public String getUserName(String roomId) {
    RoomModel roomData = RoomDao.getRoomData(roomId);
    return roomData != null ? roomData.getUserName() : null;
  }

  public void setImage(String roomId, String image) {
    RoomModel roomData = RoomDao.getRoomData(roomId);
    if (roomData == null) {
      roomData = new RoomModel(roomId, null, null, null, image, null, null);
    } else {
      roomData.setImage(image);
    }
    RoomDao.saveOrUpdateRoomData(roomData);
  }

  public String getImage(String roomId) {
    RoomModel roomData = RoomDao.getRoomData(roomId);
    return roomData != null ? roomData.getImage() : null;
  }

  public void setAudio(String roomId, Boolean audio) {
    RoomModel roomData = RoomDao.getRoomData(roomId);
    if (roomData == null) {
      roomData = new RoomModel(roomId, null, null, null, null, audio, null);
    } else {
      roomData.setAudio(audio);
    }
    RoomDao.saveOrUpdateRoomData(roomData);
  }

  public Boolean getAudio(String roomId) {
    RoomModel roomData = RoomDao.getRoomData(roomId);
    return roomData != null ? roomData.getAudio() : null;
  }

  public void setVideo(String roomId, Boolean video) {
    RoomModel roomData = RoomDao.getRoomData(roomId);
    if (roomData == null) {
      roomData = new RoomModel(roomId, null, null, null, null, null, video);
    } else {
      roomData.setVideo(video);
    }
    RoomDao.saveOrUpdateRoomData(roomData);
  }

  public Boolean getVideo(String roomId) {
    RoomModel roomData = RoomDao.getRoomData(roomId);
    return roomData != null ? roomData.getVideo() : null;
  }

  public void deleteRoom(String roomId) {
    RoomDao.deleteRoomData(roomId);
  }
}
