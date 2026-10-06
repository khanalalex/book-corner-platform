package com.bookcorner.auth.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    private Short id;

    // JdbcTypeCode(VARCHAR): keep the column a plain VARCHAR. Without it, Hibernate on MySQL expects a native
    // ENUM column and schema validation fails against our VARCHAR column.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "name", nullable = false, length = 30)
    private RoleName name;

    protected Role() {
        // required by JPA
    }

    public Short getId() {
        return id;
    }

    public RoleName getName() {
        return name;
    }
}
