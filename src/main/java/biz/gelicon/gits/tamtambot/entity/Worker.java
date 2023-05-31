package biz.gelicon.gits.tamtambot.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "WORKER")
@Data
public class Worker {
    @Id
    @Column(name = "WORKER_ID")
    private int workerId;
    @Column(name = "WORKER_EMAIL")
    private String workerEmail;
    @Column(name = "WORKER_FAMILY")
    private String workerFamily;
}
