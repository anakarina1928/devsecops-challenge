package com.mibanco.challenge.service;

import com.mibanco.challenge.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
    }

    @Test
    void shouldCreateTaskWithGeneratedId() {
        Task task = new Task(null, "Escribir README", false);

        Task created = taskService.create(task);

        assertNotNull(created.getId());
        assertEquals("Escribir README", created.getTitle());
        assertFalse(created.isCompleted());
    }

    @Test
    void shouldFindTaskById() {
        Task created = taskService.create(new Task(null, "Configurar AKS", false));

        Optional<Task> found = taskService.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals("Configurar AKS", found.get().getTitle());
    }

    @Test
    void shouldReturnEmptyWhenTaskDoesNotExist() {
        Optional<Task> found = taskService.findById(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldUpdateExistingTask() {
        Task created = taskService.create(new Task(null, "Crear Helm chart", false));

        Optional<Task> updated = taskService.update(created.getId(), new Task(null, "Crear Helm chart", true));

        assertTrue(updated.isPresent());
        assertTrue(updated.get().isCompleted());
    }

    @Test
    void shouldNotUpdateNonExistingTask() {
        Optional<Task> updated = taskService.update(999L, new Task(null, "No existe", false));

        assertTrue(updated.isEmpty());
    }

    @Test
    void shouldDeleteExistingTask() {
        Task created = taskService.create(new Task(null, "Eliminar esta tarea", false));

        boolean deleted = taskService.delete(created.getId());

        assertTrue(deleted);
        assertTrue(taskService.findById(created.getId()).isEmpty());
    }

    @Test
    void shouldReturnAllTasks() {
        taskService.create(new Task(null, "Tarea 1", false));
        taskService.create(new Task(null, "Tarea 2", false));

        assertEquals(2, taskService.findAll().size());
    }
}
