package com.exp;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "music", schema = "test")
public class MusicEntity {
    private int id;
    private String name;
    private String remark;

    private TypeEntity typeEntity;

    @Basic
    @Column(name = "type")
    public TypeEntity getTypeEntity() {
        return typeEntity;
    }

    public void setTypeEntity(TypeEntity typeEntity) {
        this.typeEntity = typeEntity;
    }

    @Id
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "REMARK")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicEntity that = (MusicEntity) o;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(remark, that.remark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, remark);
    }
}
