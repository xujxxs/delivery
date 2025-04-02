package com.uia.delivery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.uia.delivery.akka.DispatcherManager;
import com.uia.delivery.repository.CourierRepository;
import com.uia.delivery.repository.DeliveryOrderRepository;
import com.uia.delivery.service.ScheduleService;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Configuration
public class AkkaConfig 
{
    @Bean
    public ActorSystem actorSystem() {
        return ActorSystem.create("CourierSystem");
    }

    @Bean
    public ActorRef dispatcherManager(
            ActorSystem system,
            CourierRepository courierRepository,
            DeliveryOrderRepository deliveryOrderRepository,
            ScheduleService scheduleService
    ) {
        return system.actorOf(DispatcherManager.props(courierRepository, deliveryOrderRepository, scheduleService), "dispatcherManager");
    }
}
