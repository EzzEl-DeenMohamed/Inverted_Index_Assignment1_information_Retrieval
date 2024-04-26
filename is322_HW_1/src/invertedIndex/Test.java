/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * 
 * @author ehab
 */

public class Test {

    public static void main(String args[]) throws IOException {
        Index5 index = new Index5();
        //|**  change it to your collection directory 
        //|**  in windows "C:\\tmp11\\rl\\collection\\"
        String files = "D:\\Collage\\ThirdYear\\Semster 2\\Information Retrival\\Inverted_Index_Assignment1_information_Retrieval\\tmp11\\rl\\collection\\";

        File file = new File(files);
        //|** String[] 	list()
        //|**  Returns an array of strings naming the files and directories in the directory denoted by this abstract pathname.
        String[] fileList = file.list();

        fileList = index.sort(fileList);
        index.N = fileList.length;

        for (int i = 0; i < fileList.length; i++) {
            fileList[i] = files + fileList[i];
        }
        index.buildIndex(fileList);
        index.store("index");
        index.printDictionary();


        String phrase = "";

        do {
            System.out.println("Print search phrase: ");
//            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            Scanner scanner = new Scanner(System.in);
            phrase = scanner.nextLine();

            if(phrase.charAt(0) == '\"' && phrase.charAt(phrase.length()-1) == '\"') {
                phrase = phrase.replace("\"", "");
                phrase = phrase.replace(" ", "_");
            }

            if (!phrase.isEmpty()) {
                // Perform the search and display the results
                System.out.println("Search results for \"" + phrase + "\":\n" + index.find_24_01(phrase));
            }

        } while (!phrase.isEmpty());

    }
}
