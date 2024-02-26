package icu.samnyan.aqua.sega.ongeki.dao.userdata

import icu.samnyan.aqua.net.utils.GenericUserDataRepo
import icu.samnyan.aqua.sega.general.model.Card
import icu.samnyan.aqua.sega.ongeki.model.userdata.UserData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Repository("OngekiUserDataRepository")
interface UserDataRepository : JpaRepository<UserData, Long>, GenericUserDataRepo {
    fun findByCard_ExtIdIn(userIds: Collection<Long>): List<UserData>

    override fun findByCard(card: Card): UserData?

    fun findByCard_ExtId(aimeId: Long): Optional<UserData>

    @Transactional
    fun deleteByCard(card: Card)

    @Query("select count(*) from OngekiUserData where playerRating > :rating")
    override fun getRanking(rating: Int): Long
}
