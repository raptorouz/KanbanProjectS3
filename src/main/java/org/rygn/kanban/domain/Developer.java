package org.rygn.kanban.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
public class Developer {

	private @Id @GeneratedValue Long id;
	  
	private String firstname;
	
	private String lastname;
	
	private String email;
	
	private String password;
	
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate startContract;
	 
	@ManyToMany(mappedBy="developers", fetch=FetchType.EAGER)
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@JsonIgnoreProperties("developers")
    private Set<Task> tasks;
	
	public Developer() {
		
		this.tasks = new HashSet<>();
	}
}
