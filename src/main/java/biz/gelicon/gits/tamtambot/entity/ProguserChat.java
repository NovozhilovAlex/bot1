package biz.gelicon.gits.tamtambot.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "PROGUSERCHAT")
@Data
public class ProguserChat {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "newrec")
    @SequenceGenerator(name = "newrec", sequenceName = "PROGUSERCHAT_ID_GEN", allocationSize = 1)
    @Column(name = "PROGUSERCHAT_ID")
    private int proguserChatId;
    @Column(name = "PROGUSER_ID")
    private int proguserId;
    @Column(name = "TYPE_IM_ID")
    private int typeImId;
    @Column(name = "CHATID")
    private String chatId;
}
