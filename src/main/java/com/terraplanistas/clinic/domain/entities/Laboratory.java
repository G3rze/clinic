package com.terraplanistas.clinic.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "laboratories", schema = "clinic")
@Getter
@Setter
public class Laboratory extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;
}