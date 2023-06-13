package biz.gelicon.gits.tamtambot.entity;

import lombok.Data;

import javax.persistence.*;

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
    @Column(name = "ERRORAPPENDIX_LINKPATH")
    private String issueAppendixLinkpath;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ERROR_ID", referencedColumnName = "ERROR_ID", insertable = false, updatable = false)
    private Issue issue;
}
