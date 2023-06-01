package biz.gelicon.gits.tamtambot.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
