package biz.gelicon.gits.tamtambot.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PROGUSER")
@Data
public class Proguser {
    @Id
    @Column(name = "PROGUSER_ID")
    private int proguserId;
    @Column(name = "PROGUSER_NAME")
    private String proguserName;
}
