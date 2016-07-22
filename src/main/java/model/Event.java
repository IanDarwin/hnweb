package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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
	String times;
	String title;
	String location;
	String eventAbstract;
	String speakerName;
	String speakerBio;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}

	// KLUDGE because PrimeFaces doesn't support new date/time api yet
	@Transient
	public Date getLegacyDate() {
		return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	@SuppressWarnings("deprecation")
	public void setLegacyDate(Date legacyDate) {
		// final Instant instant = legacyDate.toInstant();
		// System.out.printf("Instant %s of type %s%n", instant, instant.getClass());
		// date = LocalDate.from(instant);
		date = LocalDate.of(
				legacyDate.getYear() + 1900,
				legacyDate.getMonth() + 1,
				legacyDate.getDate());
	}

	public String getTimes() {
		return times;
	}
	public void setTimes(String times) {
		this.times = times;
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
