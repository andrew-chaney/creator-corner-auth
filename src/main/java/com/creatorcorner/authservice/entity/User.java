package com.creatorcorner.authservice.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Builder
@Data
@Table("users")
public class User implements Persistable<UUID> {

    @Id
    private UUID userId;

    private String firstName;

    private String lastName;

    private String email;

    private String hashedPassword;

    private LocalDateTime createdTsEpoch;

    private LocalDateTime updatedTsEpoch;

    @Override
    public UUID getId() {
        return this.getUserId();
    }

    @Override
    public boolean isNew() {
        boolean result = Objects.isNull(this.userId);
        if (result) {
            this.userId = UUID.randomUUID();
        }
        return result;
    }
}
