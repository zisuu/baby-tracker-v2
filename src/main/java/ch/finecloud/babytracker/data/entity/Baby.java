package ch.finecloud.babytracker.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@Entity
public class Baby extends AbstractEntity {
    @NotBlank
    private String name;
    @NotNull
    private LocalDate birthday;

    @OneToMany(mappedBy = "baby")
    @Nullable
    private List<Event> events = new LinkedList<>();
    @ManyToOne
    @JoinColumn(name = "user_account_id")
    @NotNull
    @JsonIgnoreProperties({"babies"})
    private UserAccount userAccount;

    @Formula("(select count(c.id) from Event c where c.baby_id = id)")
    private int eventCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public int getEventCount() {
        return eventCount;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }
}
