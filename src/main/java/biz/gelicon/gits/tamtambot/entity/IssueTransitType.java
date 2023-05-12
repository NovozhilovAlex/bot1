package biz.gelicon.gits.tamtambot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
