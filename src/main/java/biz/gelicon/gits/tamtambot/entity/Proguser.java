package biz.gelicon.gits.tamtambot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

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
