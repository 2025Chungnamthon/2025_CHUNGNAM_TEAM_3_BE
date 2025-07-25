package Team3rd.DaeCar.DaeCar.domain.chat.dto;

import Team3rd.DaeCar.DaeCar.domain.chat.entity.ChatMessage;

public class ChatMessageRequest {
    private Long roomId;
    private String senderId;
    private String senderName;
    private String content;
    private ChatMessage.MessageType messageType;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ChatMessage.MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(ChatMessage.MessageType messageType) {
        this.messageType = messageType;
    }
}