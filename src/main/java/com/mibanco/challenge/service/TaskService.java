package com.mibanco.challenge.service;

import com.mibanco.challenge.model.Task;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public List<Task> findAll() {
        return List.copyOf(tasks.values());
    }

    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public Task create(Task task) {
        long id = idGenerator.incrementAndGet();
        task.setId(id);
        tasks.put(id, task);
        return task;
    }

    public Optional<Task> update(Long id, Task updatedTask) {
        if (!tasks.containsKey(id)) {
            return Optional.empty();
        }
        updatedTask.setId(id);
        tasks.put(id, updatedTask);
        return Optional.of(updatedTask);
    }

    public boolean delete(Long id) {
        return tasks.remove(id) != null;
    }
}
