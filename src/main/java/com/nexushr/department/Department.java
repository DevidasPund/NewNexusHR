package com.nexushr.department;

import com.nexushr.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    /** Employee id of the department head (nullable). */
    private Long headId;

    /** UI colour key for the icon tile: violet | blue | amber | green | pink. */
    private String colorKey = "violet";

    public Department() {
    }

    public Department(String name, String code, String colorKey) {
        this.name = name;
        this.code = code;
        this.colorKey = colorKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getHeadId() {
        return headId;
    }

    public void setHeadId(Long headId) {
        this.headId = headId;
    }

    public String getColorKey() {
        return colorKey;
    }

    public void setColorKey(String colorKey) {
        this.colorKey = colorKey;
    }
}
