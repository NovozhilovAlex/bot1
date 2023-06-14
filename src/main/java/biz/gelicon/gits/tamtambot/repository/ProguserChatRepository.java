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
    Optional<ProguserChat> findByUserId(String userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO PROGUSERCHAT (PROGUSERCHAT_ID, PROGUSER_ID, TYPE_IM_ID, USERID) " +
            "VALUES (next value FOR PROGUSERCHAT_ID_GEN, ?1,  ?2, ?3)",
            nativeQuery = true)
    void insert(int proguserId, int typeImId, String userId);

    @Modifying
    @Transactional
    void deleteByUserId(String userId);
}
