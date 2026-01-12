package com.example.CivicConnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.CivicConnect.entity.system.Notification;

public interface NotificationRepository 
extends JpaRepository<Notification, Long>
{

}
