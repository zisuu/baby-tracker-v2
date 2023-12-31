package ch.finecloud.babytracker.data.service;

import ch.finecloud.babytracker.data.dto.BabySleepPerDay;
import ch.finecloud.babytracker.data.dto.EventTypeNumber;
import ch.finecloud.babytracker.data.entity.*;
import ch.finecloud.babytracker.data.repository.BabyRepository;
import ch.finecloud.babytracker.data.repository.EventRepository;
import ch.finecloud.babytracker.data.repository.UserAccountRepository;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.persistence.criteria.Path;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BabyTrackerService {

    private final EventRepository eventRepository;
    private final BabyRepository babyRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthenticationContext authenticationContext;

    @PreAuthorize("hasRole('USER')")
    public List<Event> findAllEventsByUserAccountEmail(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return eventRepository.findEventsByBaby_UserAccount_Email(getEmail());
        } else {
            return eventRepository.searchEventsByBaby_UserAccount_Email(stringFilter, getEmail());
        }
    }

    @PreAuthorize("hasRole('USER')")
    public Page<Event> list(Specification<Event> filter, Pageable pageable) {
        Specification<Event> finalFilter = filter.and((root, query, criteriaBuilder) -> {
            Path<String> userEmailPath = root.get("baby").get("userAccount").get("email");
            return criteriaBuilder.equal(userEmailPath, getEmail());
        });
        return eventRepository.findAll(finalFilter, pageable);
    }

    @PreAuthorize("hasRole('USER')")
    public List<Event> findAllEventsByUserAccountEmailAndEventType(String email, EventType eventType) {
        return eventRepository.findEventsByBaby_UserAccount_Email(email);
    }

    @PreAuthorize("hasRole('USER')")
    public long countEventsByBaby(String babyName) {
        return eventRepository.countEventsByBaby_UserAccount_EmailAndBabyName(getEmail(), babyName);
    }

    @PreAuthorize("hasRole('USER')")
    public void deleteEvent(Event event) {
        if (Objects.equals(event.getBaby().getUserAccount().getEmail(), getEmail())) {
            eventRepository.delete(event);
        } else {
            System.err.println("UserAccount " + getEmail() + "is not allowed to delete this event.");
        }
    }

    @PreAuthorize("hasRole('USER')")
    public void saveEvent(Event event) {
        if (event == null) {
            System.err.println("Event is null. Are you sure you have connected your form to the application?");
            return;
        }
        eventRepository.save(event);
    }

    @PreAuthorize("hasRole('USER')")
    public List<Baby> findBabyByUserAccount_Email(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return babyRepository.findBabyByUserAccount_Email(getEmail());
        } else {
            return babyRepository.searchBabiesByUserAccount_Email(stringFilter, getEmail());
        }
    }

    @PreAuthorize("hasRole('USER')")
    public void saveBaby(Baby baby) {
        if (baby == null) {
            System.err.println("Baby is null. Are you sure you have connected your form to the application?");
            return;
        }
        Optional<UserAccount> userAccount = userAccountRepository.findUserAccountByEmail(getEmail());
        if (userAccount.isPresent()) {
            baby.setUserAccount(userAccount.get());
        } else {
            System.err.println("UserAccount is unknown. Are you sure you have connected your form to the application?");
            return;
        }
        babyRepository.save(baby);
    }

    @PreAuthorize("hasRole('USER')")
    public void deleteBaby(Baby baby) {
        if (Objects.equals(baby.getUserAccount().getEmail(), getEmail())) {
            babyRepository.delete(baby);
        } else {
            System.err.println("UserAccount " + getEmail() + "is not allowed to delete this baby.");
        }
    }

    @PreAuthorize("hasRole('USER')")
    public List<EventTypeNumber> numberOfEventsPerEventType() {
        return eventRepository.numberOfEventsPerEventType(getEmail());
    }

    @PreAuthorize("hasRole('USER')")
    public List<BabySleepPerDay> findBabySleepPerDay() {
        return eventRepository.findBabySleepPerDay(getEmail(), LocalDateTime.now().minusDays(7));
    }

    public String addUserAccount(String email, String password) {
        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(email);
        userAccount.setPassword(password);
        userAccount.setRole(Role.USER);
        UserAccount savedUserAccount;
        try {
            savedUserAccount = userAccountRepository.save(userAccount);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        return savedUserAccount.getEmail();
    }

    public boolean checkIfUserExists(String email) {
        return userAccountRepository.findUserAccountByEmail(email).isPresent();
    }

    private String getEmail() {
        return authenticationContext.getPrincipalName().isPresent() ? authenticationContext.getPrincipalName().get() : "";
    }

    public Event findLatestEventByBaby(String babyName) {
        return eventRepository.findTopByBaby_NameOrderByStartDateDesc(babyName);
    }
}