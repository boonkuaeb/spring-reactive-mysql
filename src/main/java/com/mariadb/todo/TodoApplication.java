package com.mariadb.todo;

import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class TodoApplication {

	private static final Logger logger = LoggerFactory.getLogger(TodoApplication.class);



	@Bean
	public CommandLineRunner load(TasksRepository repository)
	{
		return args -> {

			List<Task> tasks = new ArrayList();

			logger.info("----------");
			logger.info(">> DELETE ALL");
			repository.deleteALLFromTask();

			for (int i = 0; i < 100000; i++) {
				tasks.add(new Task("Task No. " + i, true));
			}
			logger.info("----------");
			logger.info(">> INSERT 1000,000 record");
			logger.info(">> >> Insert in-progress");
			repository.saveAll(tasks).blockLast(Duration.ofSeconds(600));
			logger.info(">> Insert 100,000 done");
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(TodoApplication.class, args);
	}

}


@RestController
@RequestMapping("/api/tasks")
class TasksController{
	@Autowired
	private TasksRepository repository;

	@GetMapping("/list")
	public Flux<Task> get()
	{
		return this.repository.findAll();
	}
}

interface TasksRepository extends ReactiveCrudRepository<Task,Integer> {

	@Query("DELETE from task WHERE id > 0")
	void deleteALLFromTask();
}

@Data
@NoArgsConstructor
@Table("task")
class Task{
	@Id

	private Integer id;
	@NonNull
	private String description;
	private Boolean completed;

	public Task(@NonNull String description, Boolean completed) {
		this.description = description;
		this.completed = completed;
	}
}
