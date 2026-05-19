package com.terraplanistas.clinic.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "medicines", schema = "clinic")
@Getter
@Setter
public class Medicine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratory_id", nullable = false)
    private Laboratory laboratory;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "generic_name")
    private String genericName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "composition", nullable = false)
    private Map<String, Object> composition;

    @Column(name = "use_from", nullable = false)
    private String useFrom;

    @Column(name = "atc_code", nullable = false)
    private String atcCode;
}