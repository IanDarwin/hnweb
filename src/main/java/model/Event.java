package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class Event implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	long id;
	LocalDate date;
	String startTime, endTime;
	String title;
	String location;
	String eventAbstract;
	String speakerName;
	String speakerBio;

	public long getId() {
		return id;
	}
	// And no setId() please.

	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		if (startTime.length()!=5) {
			throw new IllegalArgumentException("Please enter the time in 24h clock, like 15:30");
		}
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		if (endTime.length()!=5) {
			throw new IllegalArgumentException("Please enter the time in 24h clock, like 15:30");
		}
		this.endTime = endTime;
	}

	@Transient
	public String getTimesString() {
		return String.format("%s - %s", LocalTime.parse(startTime), LocalTime.parse(endTime));
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getEventAbstract() {
		return eventAbstract;
	}

	public void setEventAbstract(String eventAbstract) {
		this.eventAbstract = eventAbstract;
	}

	public String getSpeakerName() {
		return speakerName;
	}

	public void setSpeakerName(String speakerName) {
		this.speakerName = speakerName;
	}

	public String getSpeakerBio() {
		return speakerBio;
	}

	public void setSpeakerBio(String speakerBio) {
		this.speakerBio = speakerBio;
	}
}
