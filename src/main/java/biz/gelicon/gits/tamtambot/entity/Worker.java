package biz.gelicon.gits.tamtambot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
