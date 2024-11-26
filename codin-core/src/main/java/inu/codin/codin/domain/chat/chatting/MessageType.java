package inu.codin.codin.domain.chat.chatting;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MessageType {
    TEXT("텍스트"),
    IMAGE("이미지");

    private final String description;
}
