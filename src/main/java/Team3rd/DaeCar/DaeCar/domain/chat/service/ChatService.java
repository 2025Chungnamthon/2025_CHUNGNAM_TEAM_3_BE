package Team3rd.DaeCar.DaeCar.domain.chat.service;

import Team3rd.DaeCar.DaeCar.domain.chat.dto.ChatMessageRequest;
import Team3rd.DaeCar.DaeCar.domain.chat.dto.ChatMessageResponse;
import Team3rd.DaeCar.DaeCar.domain.chat.entity.ChatMessage;
import Team3rd.DaeCar.DaeCar.domain.chat.repository.ChatMessageRepository;
import Team3rd.DaeCar.DaeCar.domain.room.entity.RoomParticipant;
import Team3rd.DaeCar.DaeCar.domain.room.repository.RoomParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private RoomParticipantRepository roomParticipantRepository;

    public ChatMessageResponse saveMessage(ChatMessageRequest request) {
        boolean isParticipant = roomParticipantRepository
                .findByRoomIdAndUserIdAndIsActiveTrue(request.getRoomId(), request.getSenderId())
                .isPresent();

        if (!isParticipant) {
            throw new RuntimeException("User is not a participant of this room");
        }

        ChatMessage message = new ChatMessage();
        message.setRoomId(request.getRoomId());
        message.setSenderId(request.getSenderId());
        message.setSenderName(request.getSenderName());
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());

        ChatMessage savedMessage = chatMessageRepository.save(message);
        return new ChatMessageResponse(savedMessage);
    }

    public List<ChatMessageResponse> getRoomMessages(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository.findTop50ByRoomIdOrderByCreatedAtDesc(roomId);
        return messages.stream()
                .map(ChatMessageResponse::new)
                .collect(Collectors.toList());
    }
}