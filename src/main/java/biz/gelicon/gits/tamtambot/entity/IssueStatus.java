package biz.gelicon.gits.tamtambot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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
