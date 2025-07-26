package Team3rd.DaeCar.DaeCar.domain.chat.dto;

import Team3rd.DaeCar.DaeCar.domain.chat.entity.ChatMessage;
import Team3rd.DaeCar.DaeCar.domain.chat.enums.MessageType;
import java.time.LocalDateTime;

public class ChatMessageResponse {
    private Long id;
    private Long roomId;
    private String senderId;
    private String senderName;
    private String content;
    private MessageType messageType;
    private LocalDateTime createdAt;

    public ChatMessageResponse() {}

    public ChatMessageResponse(ChatMessage message) {
        this.id = message.getId();
        this.roomId = message.getRoomId();
        this.senderId = message.getSenderId();
        this.senderName = message.getSenderName();
        this.content = message.getContent();
        this.messageType = message.getMessageType();
        this.createdAt = message.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}