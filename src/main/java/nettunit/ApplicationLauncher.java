package nettunit;

import nettunit.persistence.PendingMessageRepository;
import nettunit.persistence.PendingMessage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class ApplicationLauncher {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationLauncher.class, args);
    }

    //@Bean
    /*ONLY FOR TESTING REPOSITORY*/
    public CommandLineRunner demo(PendingMessageRepository repository) {
        return (args) -> {
            // save a few customers
            repository.save(new PendingMessage("Jack", "Bauer"));
            repository.save(new PendingMessage("Chloe", "O'Brian"));
            repository.save(new PendingMessage("Kim", "Bauer2"));
            repository.save(new PendingMessage("David", "Palmer"));
            repository.save(new PendingMessage("Michelle", "Dessler"));

            // fetch all customers

            System.out.println("-------------------------------");
            for (PendingMessage customer : repository.findAll()) {
                System.out.println(customer.toString());
            }
            System.out.println("");

            // fetch an individual customer by ID
            PendingMessage customer = repository.findByTaskID("Jack");
            System.out.println("Customer found with findById(\"Jack\"):");
            System.out.println("--------------------------------");
            System.out.println(customer.toString());
            System.out.println("");

            customer = repository.findByCommType("Bauer");
            System.out.println("Customer found with findByCommType(\"Bauer\"):");
            System.out.println("--------------------------------");
            System.out.println(customer.toString());
            System.out.println("");

            // fetch customers by last name
            System.out.println("Customer found with findByLastName('Bauer'):");
            System.out.println("--------------------------------------------");
        };
    }
}