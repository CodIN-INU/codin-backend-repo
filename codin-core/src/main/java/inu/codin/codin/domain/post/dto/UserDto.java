package inu.codin.codin.domain.post.dto;

import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.user.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
    private final String nickname;
    private final String imageUrl;
    private final boolean deleted;

    private UserDto(String nickname, String imageUrl, boolean deleted) {
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.deleted = deleted;
    }

        /**
         * 게시물용 사용자 DTO 생성
         *
         * @param post 게시물 엔티티
         * @param user 사용자 엔티티
         * @param defaultProfileImageUrl 기본 프로필 이미지 URL
         * @return 게시물용 사용자 DTO
         */
        public static UserDto ofPost(PostEntity post, UserEntity user, String defaultProfileImageUrl) {
            if (user.getDeletedAt() != null) {
                return ofDeletedUser(user);
            }

            if (post.isAnonymous()) {
                return UserDto.builder()
                        .nickname("익명")
                        .imageUrl(defaultProfileImageUrl)
                        .deleted(false)
                        .build();
            }

            return ofNormalUser(user);
        }

        /**
         * 댓글용 사용자 DTO 생성
         *
         * @param comment 댓글 엔티티
         * @param user 사용자 엔티티
         * @param anonNum 익명 번호
         * @param defaultProfileImageUrl 기본 프로필 이미지 URL
         * @return 댓글용 사용자 DTO
         */
        public static UserDto ofComment(CommentEntity comment, UserEntity user, int anonNum, String defaultProfileImageUrl) {
            if (user.getDeletedAt() != null) {
                return ofDeletedUser(user);
            }

            if (comment.isAnonymous()) {
                String nickname = anonNum == 0 ? "글쓴이" : "익명" + anonNum;
                return UserDto.builder()
                        .nickname(nickname)
                        .imageUrl(defaultProfileImageUrl)
                        .deleted(false)
                        .build();
            }

            return ofNormalUser(user);
        }

    /**
     * 댓글용 사용자 DTO 생성
     *
     * @param reply 대댓글 엔티티
     * @param user 사용자 엔티티
     * @param anonNum 익명 번호
     * @param defaultProfileImageUrl 기본 프로필 이미지 URL
     * @return 댓글용 사용자 DTO
     */
        public static UserDto ofReply(ReplyCommentEntity reply, UserEntity user, int anonNum, String defaultProfileImageUrl) {
            if (user.getDeletedAt() != null) {
                return ofDeletedUser(user);
            }

            if (reply.isAnonymous()) {
                String nickname = (anonNum == 0) ? "글쓴이" : "익명" + anonNum;
                return UserDto.builder()
                        .nickname(nickname)
                        .imageUrl(defaultProfileImageUrl)
                        .deleted(false)
                        .build();
            }

            return ofNormalUser(user);
        }

        /**
         * 삭제된 사용자 DTO 생성
         */
        public static UserDto ofDeletedUser(UserEntity user) {
            return UserDto.builder()
                    .nickname(user.getNickname())
                    .imageUrl(user.getProfileImageUrl())
                    .deleted(true)
                    .build();
        }

        /**
         * 일반 사용자 DTO 생성
         */
        public static UserDto ofNormalUser(UserEntity user) {
            return UserDto.builder()
                    .nickname(user.getNickname())
                    .imageUrl(user.getProfileImageUrl())
                    .deleted(false)
                    .build();
        }
    }