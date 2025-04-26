package model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_id_gen")
    @SequenceGenerator(name = "event_id_gen", sequenceName = "event_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "eventabstract")
    private String eventabstract;

    @Column(name = "location")
    private String location;

    @Column(name = "speakerbio")
    private String speakerbio;

    @Column(name = "speakername")
    private String speakername;

    @Column(name = "title")
    private String title;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "times")
    private String times;

    @Column(name = "endtime")
    private String endtime;

    @Column(name = "starttime")
    private String starttime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventabstract() {
        return eventabstract;
    }

    public void setEventabstract(String eventabstract) {
        this.eventabstract = eventabstract;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSpeakerbio() {
        return speakerbio;
    }

    public void setSpeakerbio(String speakerbio) {
        this.speakerbio = speakerbio;
    }

    public String getSpeakername() {
        return speakername;
    }

    public void setSpeakername(String speakername) {
        this.speakername = speakername;
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

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

}