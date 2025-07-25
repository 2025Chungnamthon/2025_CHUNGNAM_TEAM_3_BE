package Team3rd.DaeCar.DaeCar.domain.chat.controller;

import Team3rd.DaeCar.DaeCar.domain.chat.dto.ChatMessageRequest;
import Team3rd.DaeCar.DaeCar.domain.chat.dto.ChatMessageResponse;
import Team3rd.DaeCar.DaeCar.domain.chat.entity.ChatMessage;
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
    @SendTo("/topic/room/{roomId}")
    public ChatMessageResponse sendMessage(ChatMessageRequest message) {
        ChatMessageResponse savedMessage = chatService.saveMessage(message);
        messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), savedMessage);
        return savedMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/room/{roomId}")
    public ChatMessageResponse addUser(ChatMessageRequest message) {
        message.setMessageType(ChatMessage.MessageType.JOIN);
        message.setContent(message.getSenderName() + " joined the room");
        ChatMessageResponse savedMessage = chatService.saveMessage(message);
        messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), savedMessage);
        return savedMessage;
    }

    @GetMapping("/room/{roomId}/messages")
    public List<ChatMessageResponse> getRoomMessages(@PathVariable Long roomId) {
        return chatService.getRoomMessages(roomId);
    }
}