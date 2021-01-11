package org.rygn.kanban.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
public class Task {

	private @Id @GeneratedValue Long id;
	
	@NotEmpty(message = "Error : Please insert a Title")
	@NotNull(message = "Error : Title is null !")
	private String title;
	
	@Min(value = 0, message = "Error : NbHoursForecast must be >= 0")
    @Max(value = 168, message = "NbHoursForecast muste be <= 168")
	@NotNull(message = "Error : NbHoursForecast is null ! ")
	private Integer nbHoursForecast;
	

	@Min(value = 0, message = "Error : NbHoursReal must be >= 0")
    @Max(value = 168, message = "NbHoursReal must be >= 168")
	@NotNull(message = "Error : NbHoursReal is null !")
	private Integer nbHoursReal;
	
	
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate created;
	
	@Valid
	@ManyToOne
	private TaskType type;
	
	@ManyToOne
	private TaskStatus status;	
	
	@ManyToMany(fetch=FetchType.EAGER)
	@NotEmpty(message = "Error : Developers is empty ! Please insert developers")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
    private Set<Developer> developers;
	
	@OneToMany(mappedBy="task", cascade={CascadeType.ALL}, orphanRemoval=true,fetch = FetchType.EAGER)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<ChangeLog> changeLogs;
	
	public Task() {
		
		this.developers = new HashSet<>();
		
		this.changeLogs = new HashSet<>();
	}
	
	public void addDeveloper(Developer developer) {
		
		developer.getTasks().add(this);
		
		this.developers.add(developer);
	}
	
	public void addChangeLog(ChangeLog changeLog) {
		
		changeLog.setTask(this);
		
		this.changeLogs.add(changeLog);
	}

	public void clearChangeLogs() {
		
		for (ChangeLog changeLog :  this.changeLogs) {
			
			changeLog.setTask(null);
		}
		
		this.changeLogs.clear();
	}
}
