package model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_id_gen")
    @SequenceGenerator(name = "event_id_gen", sequenceName = "event_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "eventAbstract")
    private String eventAbstract;

    @Column(name = "location")
    private String location;

    @Column(name = "speakerbio")
    private String speakerBio;

    @Column(name = "speakername")
    private String speakerName;

    @Column(name = "title")
    private String title;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "times")
    private String times;

    @Column(name = "endTime")
    private String endTime;

    @Column(name = "startTime")
    private String startTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventAbstract() {
        return eventAbstract;
    }

    public void setEventAbstract(String eventAbstract) {
        this.eventAbstract = eventAbstract;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSpeakerBio() {
        return speakerBio;
    }

    public void setSpeakerBio(String speakerBio) {
        this.speakerBio = speakerBio;
    }

    public String getSpeakerName() {
        return speakerName;
    }

    public void setSpeakerName(String speakerName) {
        this.speakerName = speakerName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @Transient
    public LocalDateTime getStartDateTime() {
        String[] hhmm = getStartTime().split(":");
        if (hhmm.length != 2) {
            System.out.printf("Warning: Event %s getStartDateTime failed parse");
            hhmm = new String[] { "12", "00"};
        }
        return LocalDateTime.of(getDate(),
                LocalTime.of(
                        Integer.parseInt(hhmm[0]),
                        Integer.parseInt(hhmm[1])));
    }

    @Transient
    public LocalDateTime getEndDateTime() {
        String[] hhmm = getEndTime().split(":");
        if (hhmm.length != 2) {
            System.out.printf("Warning: Event %s getEndDateTime failed parse");
            hhmm = new String[] { "12", "00"};
        }
        return LocalDateTime.of(getDate(),
                LocalTime.of(
                        Integer.parseInt(hhmm[0]),
                        Integer.parseInt(hhmm[1])));
    }
}
