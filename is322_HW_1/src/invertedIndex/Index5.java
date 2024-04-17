/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.log10;
import static java.lang.Math.sqrt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.io.PrintWriter;

/**
 * Represents an inverted index.
 *
 * @author ehab
 */
public class Index5 {

    //--------------------------------------------
    int N = 0; // Total number of documents
    public Map<Integer, SourceRecord> sources;  // Map to store document id and file name.

    public HashMap<String, DictEntry> index; // Inverted index data structure
    //--------------------------------------------

    /**
     * Constructor for Index5 class.
     */
    public Index5() {
        sources = new HashMap<Integer, SourceRecord>(); // Initialize sources map
        index = new HashMap<String, DictEntry>(); // Initialize index map
    }

    /**
     * Setter method for total number of documents.
     *
     * @param n Total number of documents
     */
    public void setN(int n) {
        N = n;
    }

    //---------------------------------------------

    // Method to print a posting list
    public void printPostingList(Posting p) {
        System.out.print("["); // Print the opening bracket of the posting list
        if (p != null) { // Check if the posting list is not empty
            System.out.print(p.docId); // Print the document ID of the first node
            p = p.next; // Move to the next node in the posting list
            while (p != null) { // Iterate through the remaining nodes in the posting list
                System.out.print(", " + p.docId); // Print the document ID of the current node
                p = p.next; // Move to the next node in the posting list
            }
        }
        System.out.println("]"); // Print the closing bracket of the posting list
    }

    //---------------------------------------------

    // Method to print the inverted index
    public void printDictionary() {
        Iterator it = index.entrySet().iterator(); // Create an iterator for the index entries
        while (it.hasNext()) { // Iterate through each entry in the index
            Map.Entry pair = (Map.Entry) it.next(); // Get the next entry in the index
            DictEntry dd = (DictEntry) pair.getValue(); // Get the DictEntry object associated with the current term
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> "); // Print term and document frequency
            printPostingList(dd.pList); // Print the posting list associated with the term
        }
        System.out.println("------------------------------------------------------"); // Print separator
        System.out.println("*** Number of terms = " + index.size()); // Print the total number of terms in the index
    }

    //-----------------------------------------------

    // Method to build the inverted index from files
    public void buildIndex(String[] files) {
        int fid = 0;
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                if (!sources.containsKey(fileName)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;
                int flen = 0;
                while ((ln = file.readLine()) != null) {
                    String[] words = ln.split("\\W+");
                    for (int i = 0; i < words.length; i++) {
                        String word = words[i].toLowerCase();
                        if (i < words.length - 1) {
                            String biword = word + "_" + words[i+1].toLowerCase();
                            if (!index.containsKey(biword)) {
                                index.put(biword, new DictEntry());
                            }
                            index.get(biword).term_freq += 1;
                            index.get(biword).addPosting(fid);
                        }
                        if (!index.containsKey(word)) {
                            index.put(word, new DictEntry());
                        }
                        index.get(word).term_freq += 1;
                        index.get(word).addPosting(fid);
                    }
                }
                sources.get(fid).length = flen;
            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
    }

    //----------------------------------------------------------------------------

    // Method to process a single line and update index
    public int indexOneLine(String ln, int fid) {
        int flen = 0;

        String[] words = ln.split("\\W+");
        // String[] words = ln.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", " ").toLowerCase().split("\\s+");
        flen += words.length;
        for (String word : words) {
            word = word.toLowerCase();
            if (stopWord(word)) {
                continue;
            }
            word = stemWord(word);
            // check to see if the word is not in the dictionary
            // if not add it
            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());
            }
            // add document id to the posting list
            if (!index.get(word).postingListContains(fid)) {
                index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term
                if (index.get(word).pList == null) {
                    index.get(word).pList = new Posting(fid);
                    index.get(word).last = index.get(word).pList;
                } else {
                    index.get(word).last.next = new Posting(fid);
                    index.get(word).last = index.get(word).last.next;
                }
            } else {
                index.get(word).last.dtf += 1;
            }
            //set the term_fteq in the collection
            index.get(word).term_freq += 1;
            if (word.equalsIgnoreCase("lattice")) {

                System.out.println("  <<" + index.get(word).getPosting(1) + ">> " + ln);
            }

        }
        return flen;
    }

    //----------------------------------------------------------------------------

    // Method to check if a word is a stop word
    boolean stopWord(String word) {
        // Check if the word is a stop word
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in") || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            // Return true if it's a stop word
            return true;
        }
        // Check if the word is too short to be indexed
        if (word.length() < 2) {
            // Return true if it's too short
            return true;
        }
        // Return false if it's not a stop word and long enough
        return false;
    }

    //----------------------------------------------------------------------------

    // Method to stem a word
    String stemWord(String word) {
        Stemmer s = new Stemmer();
        s.addString(word);
        s.stem();
        return s.toString();
    }

    //----------------------------------------------------------------------------

    // Method to intersect two posting lists
    Posting intersect(Posting pL1, Posting pL2) {

        Posting answer = null; // Head of the result list
        Posting last = null;   // Pointer to the last node in the result list

        // Traverse both lists until one of them reaches the end
        while (pL1 != null && pL2 != null) {
            // If both lists have the same docId, add it to the result list
            if (pL1.docId == pL2.docId) {
                // If answer is null, initialize it with the first common Posting
                if (answer == null) {
                    answer = new Posting(pL1.docId, pL1.dtf);
                    last = answer;
                } else {
                    // Append the common Posting to the result list
                    last.next = new Posting(pL1.docId, pL1.dtf);
                    last = last.next;
                }
                // Move both pointers to the next elements
                pL1 = pL1.next;
                pL2 = pL2.next;
            }
            // If the docId of pL1 is less than pL2, advance pL1
            else if (pL1.docId < pL2.docId) {
                pL1 = pL1.next;
            }
            // If the docId of pL2 is less than pL1, advance pL2
            else {
                pL2 = pL2.next;
            }
        }
        return answer;
    }

    //----------------------------------------------------------------------------

    // Method to perform a non-optimized search for a phrase
    public String find_24_01(String phrase) {
        String result = ""; // Initialize the result string
        String[] words = phrase.split("\\W+"); // Split the phrase into individual words
        int len = words.length; // Get the length of the word array

        // Retrieve the posting list for the first word
        Posting posting = index.get(words[0].toLowerCase()).pList;
        int i = 1;
        // Iterate through the remaining words
        while (i < len) {
            // Intersect the current posting list with the posting list of the next word
            posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
            i++;
        }
        // Iterate through the resulting posting list
        while (posting != null) {
            // Append document information to the result string
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
            // Move to the next posting
            posting = posting.next;
        }
        // Return the final result string
        return result;
    }

    //---------------------------------

    // Method to sort an array of words (bubble sort)
    String[] sort(String[] words) {
        // Initialize a flag to indicate whether the array is sorted
        boolean sorted = false;
        String sTmp;

        // Loop until the array is sorted
        while (!sorted) {
            sorted = true;
            // Iterate through the array
            for (int i = 0; i < words.length - 1; i++) {
                // Compare adjacent elements
                int compare = words[i].compareTo(words[i + 1]);
                // If the order is incorrect, swap them
                if (compare > 0) {
                    sTmp = words[i];
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    // Set sorted flag to false to indicate that swapping occurred
                    sorted = false;
                }
            }
        }
        // Return the sorted array
        return words;
    }

    //---------------------------------

    // Method to store the index in a file
    public void store(String storageName) {
        try {
            // Define the path to the storage file
            String pathToStorage = "D:\\Collage\\ThirdYear\\Semster 2\\Information Retrival\\Inverted_Index_Assignment1_information_Retrieval\\tmp11\\rl\\" + storageName;

            // Create a FileWriter object to write to the storage file
            Writer wr = new FileWriter(pathToStorage);

            // Iterate through the source records and write them to the storage file
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ","); //String formattedDouble = String.format("%.2f", fee );
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }

            // Write a section separator to the storage file
            wr.write("section2" + "\n");

            // Iterate through the index entries and write them to the storage file
            Iterator it = index.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();
                //  System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");
                Posting p = dd.pList;
                while (p != null) {
                    //    System.out.print( p.docId + "," + p.dtf + ":");
                    wr.write(p.docId + "," + p.dtf + ":");
                    p = p.next;
                }
                wr.write("\n");
            }

            // Write an end marker to the storage file
            wr.write("end" + "\n");

            // Close the FileWriter
            wr.close();

            // Print message indicating successful storage
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            // Print stack trace if an exception occurs
            e.printStackTrace();
        }
    }
    //=========================================

    // Method to check if the storage file exists
    public boolean storageFileExists(String storageName){
        // Create a File object with the specified storage file path
        java.io.File f = new java.io.File("/home/ehab/tmp11/rl/" + storageName);

        // Check if the file exists and is not a directory
        if (f.exists() && !f.isDirectory())
            return true;

        return false;
    }

    //----------------------------------------------------

    // Method to create a new storage file
    public void createStore(String storageName) {
        try {
            // Construct the path to the storage file
            String pathToStorage = "/home/ehab/tmp11/" + storageName;

            // Create a FileWriter to write to the storage file
            Writer wr = new FileWriter(pathToStorage);

            // Write "end" to the storage file
            wr.write("end" + "\n");

            // Close the FileWriter
            wr.close();

        } catch (Exception e) {
            // Print the stack trace if an exception occurs
            e.printStackTrace();
        }
    }

    //----------------------------------------------------

    // Method to load index from hard disk into memory
    public HashMap<String, DictEntry> load(String storageName) {
        try {
            // Construct the path to the storage file
            String pathToStorage = "/home/ehab/tmp11/rl/" + storageName;

            // Initialize sources and index HashMaps
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();

            // Create a BufferedReader to read from the storage file
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;

            // Read lines until "section2" is encountered
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }

                // Split the line by commas
                String[] ss = ln.split(",");

                // Parse the fields
                int fid = Integer.parseInt(ss[0]);
                try {
                    // Create a SourceRecord from the parsed fields
                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    sources.put(fid, sr);
                } catch (Exception e) {
                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Read lines until "end" is encountered
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }

                // Split the line by semicolons
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(",");
                String[] ss1b = ss1[1].split(":");

                // Create a new DictEntry and add it to the index
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));

                // Process the postings and add them to the index
                for (int i = 0; i < ss1b.length; i++) {
                    String[] ss1bx = ss1b[i].split(",");
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }

            // Print a message indicating that the loading process is finished
            System.out.println("============= END LOAD =============");

        } catch (Exception e) {
            // Print the stack trace if an exception occurs
            e.printStackTrace();
        }

        // Return the loaded index
        return index;
    }

}

//=====================================================================
