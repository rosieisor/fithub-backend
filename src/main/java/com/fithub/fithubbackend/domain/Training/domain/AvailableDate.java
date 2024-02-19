package com.fithub.fithubbackend.domain.Training.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Where(clause = "deleted = false")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvailableDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Training training;

    @NotNull
    private LocalDate date;

    @NotNull
    @ColumnDefault("true")
    private boolean enabled;

    @OneToMany(mappedBy = "availableDate", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"availableDate"})
    private List<AvailableTime> availableTimes;

    private boolean deleted;

    @Builder
    public AvailableDate (LocalDate date, boolean enabled) {
        this.date = date;
        this.enabled = enabled;
    }

    public void updateAvailableTimes(List<AvailableTime> availableTimes) {
        this.availableTimes = availableTimes;
    }

    public void addTraining(Training training) {
        this.training = training;
        training.getAvailableDates().add(this);
    }

    public void closeCurrentTime(LocalTime now) {
        for (AvailableTime time : this.getAvailableTimes()) {
            if (time.isEnabled() && time.getTime().getHour() == now.getHour()) {
                time.closeTime();
                return;
            }
        }
    }

    public boolean isAllClosed() {
        for (AvailableTime time : this.getAvailableTimes()) {
            if (time.isEnabled()) return false;
        }
        return true;
    }

    public void closeDate() {
        this.enabled = false;
    }

    public void openDate() {
        this.enabled = true;
    }

    public void deleteDate() {
        this.deleted = true;
        this.availableTimes.forEach(AvailableTime::deleteTime);
    }
}
