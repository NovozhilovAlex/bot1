package biz.gelicon.gits.tamtambot.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
