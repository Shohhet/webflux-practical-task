package com.shoggoth.webfluxfileserver.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
public class UserEntity implements Persistable<Long> {
    @Id
    private Long id;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("email")
    private String email;

    @Column("password")
    private String password;

    @Column("role")
    private Role role;

    @Column("created")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column("status")
    private Status status;

    @Transient
    @ToString.Exclude
    private List<EventEntity> events;
    @Override
    public boolean isNew() {
        return Objects.isNull(id);
    }
}
