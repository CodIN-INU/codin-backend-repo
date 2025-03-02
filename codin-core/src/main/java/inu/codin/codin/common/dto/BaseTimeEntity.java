package inu.codin.codin.common.dto;

import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
public abstract class BaseTimeEntity {

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    @CreatedBy
    private String createdUser;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    @LastModifiedBy
    private String updatedUser;

    @Field("deleted_at")
    private LocalDateTime deletedAt;

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    public void recreatedAt(){
        this.createdAt = LocalDateTime.now();
    }

    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

}