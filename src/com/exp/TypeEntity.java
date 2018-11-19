package com.exp;


import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "type", schema = "test")
public class TypeEntity {
    private int id;
    private String name;
    private String remark;

    private Set<MusicEntity> musics=new HashSet<MusicEntity>();

    @Basic
    @Column(name = "musics")
    public Set<MusicEntity> getMusics() {
        return musics;
    }

    public void setMusics(Set<MusicEntity> musics) {
        this.musics = musics;
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
        TypeEntity that = (TypeEntity) o;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(remark, that.remark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, remark);
    }
}
