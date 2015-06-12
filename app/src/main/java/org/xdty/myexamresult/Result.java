package org.xdty.myexamresult;

/**
 * Created by ty on 15-6-12.
 */
public class Result {
    String message;
    String currentBacklog = "";
    String totalBacklog = "";
    String SPI = "";
    String CPI = "";

    // search result
    String name;
    String enrollmentNumber;
    String examSeat;
    String declaredDate;
    String exam;
    String branch;

    public String toString() {
        if (currentBacklog.isEmpty()&&totalBacklog.isEmpty()&&SPI.isEmpty()&&CPI.isEmpty()) {
            return "";
        } else {
            return "Current Sem. Backlog: " + currentBacklog + "\nTotal Backlog: " + totalBacklog + "\nSPI: " + SPI + "\nCPI: " + CPI;
        }
    }
}
