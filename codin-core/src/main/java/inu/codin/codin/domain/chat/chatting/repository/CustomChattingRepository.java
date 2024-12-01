package inu.codin.codin.domain.chat.chatting.repository;

import inu.codin.codin.domain.chat.chatting.entity.Chatting;

public interface CustomChattingRepository {

    Chatting findRecentMessageByChatroomId(String id);

}
