package com.shoggoth.webfluxfileserver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("file")
public class FileEntity {
    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("path")
    private String path;

    @Column("created")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column("status")
    private Status status;
}
