package biz.gelicon.gits.tamtambot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "PROGUSERCHAT")
@Data
public class ProguserChat {
    @Id
    @Column(name = "PROGUSERCHAT_ID")
    private int proguserChatId;
    @Column(name = "PROGUSER_ID")
    private int proguserId;
    @Column(name = "TYPE_IM_ID")
    private int typeImId;
    @Column(name = "CHATID")
    private String chatId;
}
