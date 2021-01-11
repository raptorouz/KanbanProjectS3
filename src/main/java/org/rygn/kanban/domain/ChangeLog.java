package org.rygn.kanban.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
public class ChangeLog {

	private @Id @GeneratedValue Long id;
	
	
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime occured;
	
	@ManyToOne
	@ToString.Exclude
	@JsonIgnoreProperties({"changeLogs","developers"})
	private Task task;
	
	@ManyToOne
	private TaskStatus targetStatus;
	
	@ManyToOne
	private TaskStatus sourceStatus;
	
	public ChangeLog() {
	}
}
