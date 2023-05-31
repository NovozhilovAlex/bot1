package biz.gelicon.gits.tamtambot.entity;

import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ERRORAPPENDIX")
@Data
public class IssueAppendix {
    @Id
    @Column(name = "ERRORAPPENDIX_ID")
    private int issueAppendixId;
    @Column(name = "ERRORAPPENDIX_NAME")
    private String issueAppendixName;
    @Column(name = "ERRORAPPENDIX_CONTENT")
    private byte[] issueAppendixContent;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ERROR_ID", insertable = false, updatable = false)
    private Issue issue;
}
