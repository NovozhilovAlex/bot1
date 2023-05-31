package biz.gelicon.gits.tamtambot.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

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
