package biz.gelicon.gits.tamtambot.repository;

import biz.gelicon.gits.tamtambot.entity.ProguserChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface ProguserChatRepository extends JpaRepository<ProguserChat, Integer> {
    Optional<ProguserChat> findByChatId(String chatId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO PROGUSERCHAT (PROGUSERCHAT_ID, PROGUSER_ID, TYPE_IM_ID, CHATID) " +
            "VALUES (next value FOR PROGUSERCHAT_ID_GEN, ?1,  ?2, ?3)",
            nativeQuery = true)
    void insert(int proguserId, int typeImId, String chatId);
}
