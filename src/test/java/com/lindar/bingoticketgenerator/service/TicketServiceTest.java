package com.lindar.bingoticketgenerator.service;


import com.lindar.bingoticketgenerator.model.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class TicketServiceTest {

    @Autowired
    private TicketService ticketService;

    private static final int ROW = 3;
    private static final int COLUMN = 9;

    @Test
    void generate10kStrips() {

        IntStream.range(0, 10000).mapToObj(i -> ticketService.createStrip()).forEach(strip -> {
            strip.forEach(this::validateTicket);
            this.validateStripAllNumbers(strip);
        });
    }

    private boolean isColumnSorted(List<Integer> column) {
        if (Collections.frequency(column, 0) < 2 ) {
            int indexOfZero = column.indexOf(0);
            if (indexOfZero!= -1) {
                column.remove(indexOfZero);
            }
            if (column.size() > 2) {
                return column.get(0) < column.get(1) && column.get(1) < column.get(2);
            }
            if (column.size() == 2) {
                return column.get(0) < column.get(1);
            }
        }
        return true;
    }

    private void validateTicket(Ticket ticket) {
        assertTrue(ticketService.getIncompleteRow(ticket) == -1);

        for (int i=0; i<COLUMN; i++) {
            List<Integer> column = ticketService.getColumnList(i, ticket);
            assertTrue(Collections.frequency(column, 0) < 3);
            for (Integer colNum: column) {
                if (colNum != 0) {
                    if (i == 0) {
                        assertTrue(colNum > 0  && colNum < 10);
                    }
                    if (i >0 && i < 8) {
                        assertTrue(colNum >= 10*i && colNum < 10*i + 10);
                    }
                    if (i == 9) {
                        assertTrue(colNum >= 10*i && colNum <= 10*i + 10);
                    }
                }
            }
            assertTrue(this.isColumnSorted(column));
        }
    }

    private void validateStripAllNumbers(List<Ticket> strip) {
        List<Integer> allStripNumbers = new ArrayList<>();
        for (Ticket ticket: strip) {
            for (int i=0; i<ROW; i++) {
                for (int j=0; j<COLUMN; j++) {
                    if (ticket.getBingoTicket()[i][j] !=0 ) {
                        allStripNumbers.add(ticket.getBingoTicket()[i][j]);
                    }
                }
            }
        }
        List<Integer> allBingoNumbers = IntStream.range(1, 91).boxed().collect(Collectors.toList());
        assertTrue(allBingoNumbers.containsAll(allStripNumbers) && allStripNumbers.containsAll(allBingoNumbers));
    }
}
