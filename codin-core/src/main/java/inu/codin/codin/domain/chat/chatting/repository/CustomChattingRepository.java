package inu.codin.codin.domain.chat.chatting.repository;

import inu.codin.codin.domain.chat.chatting.entity.Chatting;
import org.bson.types.ObjectId;

public interface CustomChattingRepository {

    Chatting findRecentMessageByChatRoomId(ObjectId id);

}
