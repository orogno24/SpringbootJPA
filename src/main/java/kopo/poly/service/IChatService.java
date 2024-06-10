package kopo.poly.service;

public interface IChatService {

    boolean findByRoomName(String roomName) throws Exception;

    void insertRoomName(String roomName, String userId) throws Exception;

    void deleteRoomName(String roomName) throws Exception;

    String getRoomOwner(String roomName) throws Exception;

}
