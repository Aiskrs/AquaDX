package icu.samnyan.aqua.sega.chusan.dao.userdata;

import icu.samnyan.aqua.net.games.GenericUserDataRepo;
import icu.samnyan.aqua.sega.chusan.model.userdata.UserData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Repository("ChusanUserDataRepository")
public interface UserDataRepository extends GenericUserDataRepo<UserData, Long> {

    Optional<UserData> findByCard_ExtId(Long extId);
}
