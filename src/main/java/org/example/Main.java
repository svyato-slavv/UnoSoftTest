package org.example;



import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        File file = new File(args[0]);
        List<Set<String>> groups = readFileAndFindGroups(file);
        writeListToFile(groups);
        long finish = System.currentTimeMillis();
        long elapsed = finish - start;
        System.out.println("Прошло времени, мс: " + elapsed);
    }

    private static class NewWord {
        public String value;
        public int position;

        public NewWord(String value, int position) {
            this.value = value;
            this.position = position;
        }
    }


    private static List<Set<String>> readFileAndFindGroups(File file) {
        List<Map<String, Integer>> wordsToGroupsNumbers = new ArrayList<>();
        List<Set<String>> linesGroups = new ArrayList<>();
        Map<Integer, Integer> mergedGroupNumberToFinalGroupNumber = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] numbers = line.split(";");
                for (int i = 0; i < numbers.length; i++) {
                    if (numbers[i].startsWith("\"") && numbers[i].endsWith("\"")) {
                        numbers[i] = numbers[i].substring(1, numbers[i].length() - 1);
                    }
                }
                if (!Arrays.stream(numbers).allMatch(str -> NumberUtils.isParsable(str) || str.isEmpty()))
                    continue;

                TreeSet<Integer> foundInGroups = new TreeSet<>();
                List<NewWord> newWords = new ArrayList<>();
                for (int i = 0; i < numbers.length; i++) {
                    String number = numbers[i];
                    if (wordsToGroupsNumbers.size() == i) {
                        wordsToGroupsNumbers.add(new HashMap<>());
                    }
                    if (number.isEmpty())
                        continue;


                    Integer wordGroupNumber = wordsToGroupsNumbers.get(i).get(number);
                    if (wordGroupNumber != null) {
                        while (mergedGroupNumberToFinalGroupNumber.containsKey(wordGroupNumber))
                            wordGroupNumber = mergedGroupNumberToFinalGroupNumber.get(wordGroupNumber);
                        foundInGroups.add(wordGroupNumber);
                    } else {
                        newWords.add(new NewWord(number, i));
                    }
                }
                int groupNumber;
                if (foundInGroups.isEmpty()) {
                    groupNumber = linesGroups.size();
                    linesGroups.add(new HashSet<>());
                } else {
                    groupNumber = foundInGroups.first();
                }
                for (NewWord newWord : newWords) {
                    wordsToGroupsNumbers.get(newWord.position).put(newWord.value, groupNumber);
                }
                for (int mergeGroupNumber : foundInGroups) {
                    if (mergeGroupNumber != groupNumber) {
                        mergedGroupNumberToFinalGroupNumber.put(mergeGroupNumber, groupNumber);
                        linesGroups.get(groupNumber).addAll(linesGroups.get(mergeGroupNumber));
                        linesGroups.set(mergeGroupNumber, null);
                    }
                }
                linesGroups.get(groupNumber).add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem with file reading!");
        }
        linesGroups.removeAll(Collections.singleton(null));
        return linesGroups;
    }

    private static void writeListToFile(List<Set<String>> groups) {
        long countOfGroups = groups.stream()
                .filter(x -> x.size() > 1)
                .count();
        groups.sort((o1, o2) -> o2.size() - o1.size());
        File file = new File("output.txt");
        int groupNumber = 0;
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write("Число групп с более чем одним элементом: " + countOfGroups + "\n");
            for (Set<String> group : groups) {
                groupNumber++;
                writer.write("Группа " + groupNumber + "\n");
                for (String line : group) {
                    writer.write(line + "\n");
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}


