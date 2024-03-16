package com.shoggoth.webfluxfileserver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("event")
public class EventEntity implements Persistable<Long> {
    @Id
    private Long id;

    @Transient
    private UserEntity user;

    @Transient
    private FileEntity file;

    @Column("user_id")
    private Long userId;

    @Column("file_id")
    private Long fileId;

    @Column("created")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column("status")
    private Status status;

    @Override
    public boolean isNew() {
        return Objects.isNull(id);
    }
}
