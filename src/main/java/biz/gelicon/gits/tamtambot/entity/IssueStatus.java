package biz.gelicon.gits.tamtambot.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ERRORSTATUS")
@Data
public class IssueStatus {
    @Id
    @Column(name = "ERROR_ID")
    private int issueId;
    @Column(name = "ERRORTRANSIT_ID")
    private int issueTransitId;
}
