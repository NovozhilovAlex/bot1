package biz.gelicon.gits.tamtambot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
