package biz.gelicon.gits.tamtambot.repository;

import biz.gelicon.gits.tamtambot.entity.ProguserChat;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProguserChatRepository extends JpaRepository<ProguserChat, Integer> {
    Optional<ProguserChat> findByChatId(String chatId);
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO PROGUSERCHAT (PROGUSERCHAT_ID, PROGUSER_ID, TYPE_IM_ID, CHATID)" +
            " VALUES (?1, ?2, ?3, ?4)",
            nativeQuery = true)
    void insert(int id, int proguserId, int typeImId, String chatId);
}
