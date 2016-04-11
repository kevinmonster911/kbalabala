package com.kbalabala.tools;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by kevin on 15-4-20.
 */
public class JmbCsvUtilsTest {

    public static void main(String[] args){

        readFromCsv();
        writeToCsv();

    }

    /**
     * read csv file and print it to console
     */
    public static void readFromCsv(){
        try {

            JmbCsvUtils.CsvLineIterator lineIterator = JmbCsvUtils.fromCsv("/Users/kevin/workspace/projects/csv/test1.csv");

            while(lineIterator.hasNext()){

                String[] line = lineIterator.next();
                System.out.println(Arrays.deepToString(line));
            }

        } catch (IOException e) {
            // do you prefer
        }
    }

    /**
     * write the csv
     */
    public static void writeToCsv() {
        try(JmbCsvUtils.CsvLineWriter writer = JmbCsvUtils.toCsv("/Users/kevin/workspace/projects/csv/test2.csv")) {

            String[] line = null;

            for(int i = 0; i < 15; i++){
                line = new String[]{"cz" + i, "18" + i, "beijing"};
                writer.writeLine(line);
            }

        } catch (Exception e) {
            // do you prefer
        }

        System.out.println("the file has been written");
    }
}
