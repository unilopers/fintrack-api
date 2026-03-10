package com.guimarobo.Fintrack.repository;

import com.guimarobo.Fintrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
