package Team3rd.DaeCar.DaeCar.domain.chat.controller;

import Team3rd.DaeCar.DaeCar.domain.chat.dto.ChatMessageRequest;
import Team3rd.DaeCar.DaeCar.domain.chat.dto.ChatMessageResponse;
import Team3rd.DaeCar.DaeCar.domain.chat.enums.MessageType;
import Team3rd.DaeCar.DaeCar.domain.chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessageRequest message) {
        ChatMessageResponse savedMessage = chatService.saveMessage(message);
        messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), savedMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(ChatMessageRequest message) {
        message.setMessageType(MessageType.JOIN);
        message.setContent(message.getSenderName() + " joined the room");
        ChatMessageResponse savedMessage = chatService.saveMessage(message);
        messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), savedMessage);
    }

    @GetMapping("/room/{roomId}/messages")
    public List<ChatMessageResponse> getRoomMessages(@PathVariable Long roomId) {
        return chatService.getRoomMessages(roomId);
    }
}