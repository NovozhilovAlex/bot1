package biz.gelicon.gits.tamtambot.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "PROGUSERWORKER")
@Data
public class ProguserWorker {
    @Id
    @Column(name = "PROGUSER_ID")
    private int proguserId;
    @Column(name = "WORKER_ID")
    private int workerId;
}
