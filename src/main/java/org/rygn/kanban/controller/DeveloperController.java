package org.rygn.kanban.controller;

import java.util.List;

import org.rygn.kanban.domain.Developer;
import org.rygn.kanban.service.DeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeveloperController {

	@Autowired
	private DeveloperService developerService;

	@GetMapping("/developers")
	List<Developer> findAllDevelopers() {
		return this.developerService.findAllDevelopers();
	}
}
