package com.example;

import java.util.Collection;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

@EnableBinding(Sink.class)
@IntegrationComponentScan
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationServiceApplication {
    
    @Bean
    CommandLineRunner commandLineRunner(ReservationRepository reservationRepository){
        return srtings ->{
            Stream.of("Sean","Bill","THing","OtherThing","What am i doing","ok one more")
            .forEach(n-> reservationRepository.save(new Reservation(n)));
        };
    }

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}


@MessageEndpoint
class ReservationProcessor {
    private final ReservationRepository reservationRepository;
    
    @ServiceActivator(inputChannel = "input")
    public void onMessage(Message<String> msg) {
        this.reservationRepository.save(new Reservation(msg.getPayload()));
        
    }
    @Autowired
    public ReservationProcessor(ReservationRepository reservationRepository){
        this.reservationRepository = reservationRepository;
    }
}

@RefreshScope
@RestController
class MessageRestConrtoller {
    @Value("${message}")
    private String msg;
    
    @RequestMapping("/message")
    String message(){
        return this.msg;
    }
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long>{
    @RestResource(path="by-name")
    Collection<Reservation> findByReservationName(@Param("rn") String rn);   
    
}

@Data
@Entity
class Reservation{
    @Id
    @GeneratedValue
    private Long id;
    private String reservationName;

    public Reservation() {
        super();
    }

    public Reservation(String reservationName) {
        super();
        this.reservationName = reservationName;
    }
}


