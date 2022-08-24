package com.lindar.bingoticketgenerator;

import com.lindar.bingoticketgenerator.service.TicketService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.stream.IntStream;

@Component
@AllArgsConstructor
public class TicketRunner implements CommandLineRunner {

    private TicketService ticketService;

    /**
     * Method to check and display the time spent to create 10K strips and print a strip
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {

        long start = System.currentTimeMillis();
        IntStream.range(0, 10000).forEach(i -> ticketService.createStrip());
        System.out.println("\nTime taken to generate 10K strips in milliseconds::" + (System.currentTimeMillis() - start) );

        System.out.println("\nBelow is an example of a strip generated.\n");
        ticketService.print(ticketService.createStrip());
    }
}
