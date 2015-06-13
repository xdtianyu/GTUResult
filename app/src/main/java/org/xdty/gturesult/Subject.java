package org.xdty.gturesult;

/**
 * Created by ty on 15-6-12.
 */
public class Subject {
    String code;
    String name;
    Grade theory = new Grade();
    Grade practical = new Grade();
    String grade;

    public String toString() {
        return code + " - " + name +
                "\n\n" + "Theory Grade: \n ESE: " + theory.ESE + ", PA: " + theory.PA + ", TOTAL: " + theory.TOTAL +
                "\nPractical Grade: \n ESE: " + practical.ESE + ", PA: " + practical.PA + ", TOTAL: " + practical.TOTAL +
                "\nSubject Grade: " + grade;
    }

    class Grade {
        String ESE;
        String PA;
        String TOTAL;
    }
}
