package indexing;

import dawg.DAWG;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A program for indexing a text in a DAWG-like structure.
 *
 * @author Tim Sockel
 */
public class IndexDAWG {

    /**
     * Starting point of DAWG-based text indexing program.
     * @param args
     */
    public static void main(String[] args) {

        DAWG indexedDAWG;

        Scanner input = new Scanner(System.in);

        String menu1 = "///// DAWG Implementation /////\n";
        String menu2 = "///// Tim Sockel (B.Sc.), 12396122, University of Munich /////\n";
        String menu3 = "///// Course: Algorithmische und formale Aspekte II, Summer Semester 2022 /////\n";
        String menu4 = "///// following the paper: Blumer, A. et al. (1985). The smallest automation recognizing the subwords of a text. " +
                "Theoretical Computer Science, 40, 31–55. DOI: 10.1016/0304-3975(85)90157-4 /////\n";
        String menu = menu1 + menu2 + menu3 + menu4 + "\n";

        String menu5 = "You now have the following options: \n" +
                "(0) Exit the program.\n" +
                "(1) Print all of the DAWG's primary and secondary edges.\n" +
                "(2) Save all of the DAWG's primary and secondary edges to a file.\n" +
                "(3) Print the number of the DAWG's states.\n" +
                "(4) Search for a specific string within the DAWG.";

        String prompt1 = "Please enter the path of the file you want to index! >>> ";
        String prompt2 = "This seems to be a large file. How many lines do you want to analyze? >>> ";
        String prompt3 = "Do you prefer a verbose output? (Y/N) >>> ";
        String prompt4 = "Type in the number of your preferred action! >>> ";
        String prompt5 = "Enter a string to check whether or not it is contained in the input file. >>> ";

        boolean nextPrompt = false;
        String pathString = "";
        List<String> textList = new ArrayList<>();
        boolean verbose = false;

        System.out.println(menu);

        // File input:
        do {
            System.out.print(prompt1);
            pathString = input.next();
            try {
                Path textPath = Path.of(pathString);
                textList = Files.readAllLines(textPath);
                nextPrompt = true;
            } catch (Exception e) {
                System.out.println("Unfortunately, this didn't work.");
            }
        } while (!nextPrompt);

        // First n lines:
        do {
            if (textList.size() > 5000) {
                System.out.print(prompt2);
                try {
                    int numberOfLines = input.nextInt();
                    textList = textList.subList(0, numberOfLines);
                    nextPrompt = true;
                } catch (Exception e) {
                    System.out.println("Not a number.");
                    nextPrompt = false;
                }
            } else {
                nextPrompt = true;
            }
        } while (!nextPrompt);

        // Verbose output:
        do {
            System.out.print(prompt3);
            String verboseString = input.next();
            switch (verboseString.toLowerCase()) {
                case "y" -> {
                    nextPrompt = true;
                    verbose = true;
                }
                case "n" -> {
                    nextPrompt = true;
                    verbose = false;
                }
                default -> {
                    nextPrompt = false;
                    System.out.println("Unfortunately, this didn't work.");
                }
            }
        } while (!nextPrompt);

        // Create the DAWG:
        indexedDAWG = new DAWG(textList, verbose);

        // Ask for further actions:
        int choice = 0;
        do {
            System.out.println(menu5);
            System.out.print(prompt4);
            choice = input.nextInt();
            switch (choice) {
                case 1 -> indexedDAWG.printEdges();
                case 2 -> indexedDAWG.saveEdges(pathString);
                case 3 -> indexedDAWG.printNumberOfStates();
                case 4 -> {
                    input.nextLine();
                    System.out.print(prompt5);
                    String query = input.nextLine();
                    boolean contained = indexedDAWG.search(query);
                    System.out.print("==============\nThe string \"" + query + "\" is ");
                    if (!contained) {
                        System.out.print("not ");
                    }
                    System.out.println("an infix of the input text.\n==============");
                }
                default -> System.out.println("Done.");
            }
        } while (choice >= 1 && choice <= 4);

        input.close();

    }

}
