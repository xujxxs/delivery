package com.uia.delivery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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
            SimpMessagingTemplate messagingTemplate,
            DeliveryOrderRepository deliveryOrderRepository,
            ScheduleService scheduleService
    ) {
        return system.actorOf(
            DispatcherManager.props(
                courierRepository, 
                messagingTemplate, 
                deliveryOrderRepository, 
                scheduleService
            ), "dispatcherManager"
        );
    }
}
