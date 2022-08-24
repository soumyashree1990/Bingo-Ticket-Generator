package com.lindar.bingoticketgenerator.service;

import com.lindar.bingoticketgenerator.model.Ticket;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TicketService {


    private static final int ROW = 3;
    private static final int COLUMN = 9;
    //map to get the range of numbers to be set per each column
    Map<Integer, List<Integer>> rangeMap = new HashMap<>();

    /**
     * method to create strip which is a list with 6 ticket objects
     * @return
     */
    public List<Ticket> createStrip() {

        List<Ticket> strip = IntStream.range(0, 6)
                .mapToObj(i -> new Ticket())
                .collect(Collectors.toCollection(() -> new ArrayList<>(6)));

        this.setRangePerColumn();
        this.setRandomColumnValuesOnRow(0, strip);
        this.setValuesInFreeColumns(1,strip);
        this.setRandomColumnValuesOnRow(2, strip);

        if (!rangeMap.isEmpty()) {
            this.setRemainingNumbers(strip);
        }
        this.getFixForInvalidColumns(strip);
        this.sortColumns(strip);
        return strip;
    }

    /**
     * maps the range of numbers to column and stores in rangeMap
     */
    public void setRangePerColumn() {
        List<Integer> firstRange = IntStream.range(1, 10).boxed().collect(Collectors.toList());
        Collections.shuffle(firstRange);
        rangeMap.put(1, firstRange);
        IntStream.range(2,9).forEach(rangeNumber -> {
            List<Integer> range = IntStream.range((rangeNumber-1)*10, (rangeNumber-1)*10 + 10).boxed().collect(Collectors.toList());
            Collections.shuffle(range);

            rangeMap.put(rangeNumber, range);
        });
        List<Integer> lastRange = IntStream.range(80, 91).boxed().collect(Collectors.toList());
        Collections.shuffle(lastRange);
        rangeMap.put(9, lastRange);

    }

    /**
     * method to set random numbers from range to add on the columns of specific rows
     * @param row
     * @param strip
     */
    private void setRandomColumnValuesOnRow(int row, List<Ticket> strip) {
        for (Ticket ticket: strip) {
            List<Integer> columns = this.getRandomRangeNumbers(5, new ArrayList<>());
            columns.forEach(column -> ticket.getBingoTicket()[row][column-1] = this.getRandomNumberByRange(column));
        }
    }

    public int getRandomNumberByRange(int rangeNumber) {
        return rangeMap.get(rangeNumber).remove(0);
    }

    /**
     * method to find the free columns and set value in them
     * @param strip
     */
    private void setValuesInFreeColumns(int row,List<Ticket> strip) {
        for (Ticket ticket: strip) {
            List<Integer> freeColumns = this.getFreeColumnsUntilRow(ticket.getBingoTicket(), row);
            List<Integer> columns = this.getRandomRangeNumbers(5 - freeColumns.size(), freeColumns);
            columns.addAll(freeColumns);
            columns.forEach(column -> ticket.getBingoTicket()[1][column-1] = this.getRandomNumberByRange(column));
        }
    }

    /**
     * to generate random number from range
     * @param count
     * @param exclude
     * @return
     */
    public List<Integer> getRandomRangeNumbers(int count, List<Integer> exclude) {
        List<Integer> range = IntStream.range(1, 10)
                .filter(rangeNumber -> !exclude.contains(rangeNumber) && this.rangeMap.get(rangeNumber).size() > 0).boxed().collect(Collectors.toList());
        Collections.shuffle(range);
        return range.stream().limit(count).collect(Collectors.toList());
    }

    /**
     * method to get columns without any random number which is remaining from the range
     * 
     * @param ticket
     * @param row
     * @return
     */
    public List<Integer> getFreeColumnsUntilRow(int[][] ticket, int row) {
        List<Integer> freeColumns = new ArrayList<>();
        boolean isFree;
        for (int i=0; i<COLUMN; i++) {
            isFree = true;
            for (int j=0; j<row; j++) {
                if (ticket[j][i] != 0) {
                    isFree = false;
                }
            }
            if (isFree && this.rangeMap.get(i+1).size() >0) {
                freeColumns.add(i+1);
            }
        }
        return freeColumns;
    }


    /**
     * fix the entries in incorrectColumns
     * @param strip
     */
    public void getFixForInvalidColumns(List<Ticket> strip) {

        for (Ticket invalidTicketCandidate:strip) {
            int invalidColumn = this.getInvalidColumn(invalidTicketCandidate);
            if (invalidColumn != -1) {
                for (Ticket ticket: strip) {
                    if (this.fixInvalidColumnEntry(invalidColumn, ticket, invalidTicketCandidate)) {
                        break;
                    }
                }
            }
        }

    }

    /**
     * place the remaining numbers in empty fields
     * @param strip
     */
    public void setRemainingNumbers(List<Ticket> strip) {
        for (int i=1; i<10; i++) {
            if (!rangeMap.get(i).isEmpty()) {
                for (Integer value: rangeMap.get(i)) {
                    this.placeRemainingNumber(value, strip);
                }
            }
        }
    }

    /**
     * Method to add the remaining numbers
     * @param value
     * @param strip
     * @return
     */
    public boolean placeRemainingNumber(int value, List<Ticket> strip) {
        int column = value/10 != 9 ? value/10 : 8;
        Ticket incompleteTicket = this.getFirstIncompleteTicket(strip);
        int incompleteRow = this.getIncompleteRow(incompleteTicket);

        for (Ticket ticket:strip) {
            for (int i=0; i<ROW; i++) {
                int switchableColumn = this.getSwitchableColumn(i, ticket, incompleteTicket);
                if (ticket.getBingoTicket()[i][column] == 0 && switchableColumn > -1) {
                    ticket.getBingoTicket()[i][column] = value;
                    incompleteTicket.getBingoTicket()[incompleteRow][switchableColumn] = ticket.getBingoTicket()[i][switchableColumn];
                    ticket.getBingoTicket()[i][switchableColumn] = 0;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * sort columns
     * @param strip
     */
    public void sortColumns(List<Ticket> strip) {
        List<Integer> column;
        for (Ticket ticket:strip) {
            for (int i=0; i<COLUMN; i++) {
                column = this.getColumnList(i, ticket);
                if (Collections.frequency(column,0) < 2) {
                    int indexOfZero = column.indexOf(0);
                    if (indexOfZero!= -1) {
                        column.remove(indexOfZero);
                    }
                    Collections.sort(column);
                    if (indexOfZero!= -1) {
                        column.add(indexOfZero, Integer.valueOf(0));
                    }
                    for (int j=0; j<ROW; j++) {
                        ticket.getBingoTicket()[j][i] = column.get(j);
                    }
                }

            }
        }
    }

    /**
     * gets the invalid column from the ticket
     * @param ticket
     * @return
     */
    public int getInvalidColumn(Ticket ticket) {
        List<Integer> column;
        for (int i=0; i<COLUMN; i++) {
            column = this.getColumnList(i, ticket);

            if ( Collections.frequency(column, 0) == ROW) {
                return i;
            }
        }
        return -1;
    }

    /**
     * gets the column lists
     *
     * @param column
     * @param ticket
     * @return
     */
    public List<Integer> getColumnList(int column, Ticket ticket) {
        List<Integer> columnList = new ArrayList<>();
        for (int i=0; i<ROW; i++) {
            columnList.add(ticket.getBingoTicket()[i][column]);
        }
        return columnList;
    }

    /**
     * fixes the invalid column entries
     * @param invalidColumn
     * @param ticket
     * @param invalidTicket
     * @return
     */
    public boolean fixInvalidColumnEntry(int invalidColumn, Ticket ticket, Ticket invalidTicket) {
        List<Integer> columnList = this.getColumnList(invalidColumn, ticket);

        for (int i=0; i<ROW; i++) {
            if (ticket.getBingoTicket()[i][invalidColumn] != 0 && Collections.frequency(columnList, 0) <2) {
                for (int j=0; j<COLUMN; j++) {
                    if (ticket.getBingoTicket()[i][j] == 0 && invalidTicket.getBingoTicket()[i][j] != 0
                            && Collections.frequency(this.getColumnList(j, invalidTicket), 0) < 2 ){

                        invalidTicket.getBingoTicket()[i][invalidColumn] = ticket.getBingoTicket()[i][invalidColumn];
                        ticket.getBingoTicket()[i][invalidColumn] = 0;

                        ticket.getBingoTicket()[i][j] = invalidTicket.getBingoTicket()[i][j];
                        invalidTicket.getBingoTicket()[i][j] = 0;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * finds the column which can be switched
     * @param row
     * @param ticket
     * @param incompleteTicket
     * @return
     */
    public int getSwitchableColumn(int row, Ticket ticket, Ticket incompleteTicket) {
        int incompleteRow = this.getIncompleteRow(incompleteTicket);
        List<Integer> column;
        for (int i=0; i<COLUMN; i++) {
            column = new ArrayList<>();
            for (int j=0; j<ROW; j++) {
                column.add(ticket.getBingoTicket()[j][i]);
            }
            if (ticket.getBingoTicket()[row][i] != 0 && Collections.frequency(column, 0) <2 &&
                    incompleteTicket.getBingoTicket()[incompleteRow][i] == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * checks for any ticket which is incomplete
     * @param strip
     * @return
     */
    public Ticket getFirstIncompleteTicket(List<Ticket> strip) {
        for (Ticket ticket:strip) {
            if (this.getIncompleteRow(ticket) > -1) {
                return ticket;
            }
        }
        return null;
    }

    /**
     * gets the incomplete row
     * @param ticket
     * @return
     */
    public int getIncompleteRow(Ticket ticket) {
        int rowNumbers;
        for (int i=0; i<ROW; i++) {
            rowNumbers = 0;
            for (int j=0; j<COLUMN; j++) {
                if (ticket.getBingoTicket()[i][j] != 0) {
                    rowNumbers++;
                }
            }
            if (rowNumbers < 5) {
                return i;
            }
        }
        return -1;
    }


    /**
     * to print the strip of bingo tickets
     * @param strip
     */
    public void print(List<Ticket> strip) {
        for (Ticket ticket: strip) {
            System.out.println("---------------------------------------------------");
            for (int i=0; i<ROW; i++) {
                for (int j=0; j<COLUMN; j++) {
                    System.out.printf("%-5s ", ticket.getBingoTicket()[i][j]);
                }
                System.out.println();
            }
        }
        System.out.println("---------------------------------------------------");
    }

}
