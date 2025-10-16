package com.cs102.attendance.entity;

// import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

// @MappedSuperclass
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Entity {
    
    // @Id
    // @GeneratedValue(strategy = GenerationType.AUTO)
 
    private UUID id;
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(id, entity.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 