package xatal.petlove.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import xatal.petlove.util.Util;

import java.util.Date;

@Entity
public class Operation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "origin", nullable = false)
	private String origin = "";

	@Column(name = "action", nullable = false)
	private String action = "";

	@Column(name = "target", nullable = false)
	private String target = "";

	@Column(name = "date", nullable = false)
	private String date = Util.dateToString(new Date());

	public Operation() {
	}

	public Operation(String origin, String action, String target) {
		this.origin = origin;
		this.action = action;
		this.target = target;
		this.date = Util.dateToString(new Date());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
