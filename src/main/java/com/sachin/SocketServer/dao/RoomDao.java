package com.sachin.SocketServer.dao;

import com.sachin.SocketServer.model.RoomModel;
import java.sql.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RoomDao {

  private static final String DB_URL = "jdbc:sqlite:rooms.db";
  private static final Logger log = LogManager.getLogger(RoomDao.class);

  public RoomDao() {
    createTable();
  }

  // Method to create the RoomData table if it does not exist
  private void createTable() {
    String sql =
        """
            CREATE TABLE IF NOT EXISTS RoomData (
              roomId TEXT PRIMARY KEY,
              offer TEXT,
              candidate TEXT,
              userName TEXT,
              image TEXT,
              audio BOOLEAN NOT NULL DEFAULT 0,
              video BOOLEAN NOT NULL DEFAULT 0,
              remotePeerId TEXT
            );
            """;

    try (Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement()) {
      stmt.execute(sql);
    } catch (SQLException e) {
      log.error("Error creating table: ", e);
    }
  }

  // Insert or update RoomData
  public void saveOrUpdateRoomData(RoomModel roomData) {
    String sql =
        "INSERT INTO RoomData (roomId, offer, candidate, userName, image, audio, video, remotePeerId) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT(roomId) DO UPDATE SET offer = ?, candidate = ?, userName = ?, image = ?, audio = ?, video = ?, remotePeerId = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, roomData.getRoomId());
      stmt.setString(2, roomData.getOffer());
      stmt.setString(3, roomData.getCandidate());
      stmt.setString(4, roomData.getUserName());
      stmt.setString(5, roomData.getImage());
      stmt.setBoolean(6, roomData.getAudio());
      stmt.setBoolean(7, roomData.getVideo());
      stmt.setString(8, roomData.getRemotePeerId());
      stmt.setString(9, roomData.getOffer());
      stmt.setString(10, roomData.getCandidate());
      stmt.setString(11, roomData.getUserName());
      stmt.setString(12, roomData.getImage());
      stmt.setBoolean(13, roomData.getAudio());
      stmt.setBoolean(14, roomData.getVideo());
      stmt.setString(15, roomData.getRemotePeerId());
      stmt.executeUpdate();
    } catch (SQLException e) {
      log.error("Error saving or updating RoomData: ", e);
    }
  }

  // Get RoomData by roomId
  public RoomModel getRoomData(String roomId) {
    String sql = "SELECT * FROM RoomData WHERE roomId = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, roomId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return new RoomModel(
            rs.getString("roomId"),
            rs.getString("offer"),
            rs.getString("candidate"),
            rs.getString("userName"),
            rs.getString("image"),
            rs.getBoolean("audio"),
            rs.getBoolean("video"),
            rs.getString("remotePeerId"));
      }
    } catch (SQLException e) {
      log.error("Error fetching RoomData: ", e);
    }
    return null;
  }

  // Delete RoomData by roomId
  public void deleteRoomData(String roomId) {
    String sql = "DELETE FROM RoomData WHERE roomId = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, roomId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      log.error("Error deleting RoomData: ", e);
    }
  }
}
