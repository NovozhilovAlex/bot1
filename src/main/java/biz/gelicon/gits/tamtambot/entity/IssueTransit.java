package biz.gelicon.gits.tamtambot.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ERRORTRANSIT")
@Data
public class IssueTransit {
    @Id
    @Column(name = "ERRORTRANSIT_ID")
    private int issueTransitId;
    @Column(name = "ERRORTRANSIT_DATE")
    private LocalDateTime issueTransitDate;
    @Column(name = "ERRORTRANSIT_DATENEED")
    private LocalDateTime issueTransitDateNeed;
    @Column(name = "ERRORTRANSIT_TEXT")
    private byte[] issueTransitText;
    @ManyToOne
    @JoinColumn(name = "WORKER_ID")
    private Worker worker;
    @ManyToOne
    @JoinColumn(name = "FROMWORKER_ID")
    private Worker fromWorker;
    @ManyToOne
    @JoinColumn(name = "ERRORTRANSITTYPE_ID")
    private IssueTransitType issueTransitType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ERROR_ID", referencedColumnName = "ERROR_ID", insertable = false, updatable = false)
    private Issue issue;
//    @OneToOne(mappedBy = "issueTransit")
//    private IssueStatus status;
}
