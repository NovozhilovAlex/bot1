package biz.gelicon.gits.tamtambot.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ERRORTRANSITTYPE")
@Data
public class IssueTransitType {
    @Id
    @Column(name = "ERRORTRANSITTYPE_ID")
    private int issueTransitTypeId;
    @Column(name = "ERRORTRANSITTYPE_NAME")
    private String issueTransitTypeName;
}
