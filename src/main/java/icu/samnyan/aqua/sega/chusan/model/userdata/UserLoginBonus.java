package icu.samnyan.aqua.sega.chusan.model.userdata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "ChusanUserLoginBonus")
@Table(name = "chusan_user_login_bonus")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginBonus implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private int id;
    private int version;
    private int user;
    private int presetId;
    private int bonusCount;
    private LocalDateTime lastUpdateDate;
    private boolean isWatched;
    private boolean isFinished;

    public UserLoginBonus(int version, int user, int presetId) {
        this.version = version;
        this.user = user;
        this.presetId = presetId;
        this.bonusCount = 0;
        this.lastUpdateDate = LocalDateTime.parse("2018-01-01T00:00:00");
        this.isWatched = false;
        this.isFinished = false;
    }
}
