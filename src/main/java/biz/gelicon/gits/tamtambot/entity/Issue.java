package biz.gelicon.gits.tamtambot.entity;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ERROR")
@Data
public class Issue {
    @Id
    @Column(name = "ERROR_ID")
    private int issueId;
    @Column(name = "ERROR_TEXT")
    private String issueText;
    @Column(name = "WORKER_ID")
    private int workerId;
    @Column(name = "ERROR_STATUS")
    private int issueStatus;
    @Column(name = "ERROR_DATE")
    private LocalDateTime issueDate;
    @Column(name = "ERROR_DATENEED")
    private LocalDateTime issueDateNeed;
    @Column(name = "ERROR_PRIORITY")
    private String issuePriority;
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany()
    @JoinColumn(name = "ERROR_ID")
    private List<IssueTransit> issueTransits;
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany()
    @JoinColumn(name = "ERROR_ID")
    private List<IssueAppendix> issueAppendices;

}
