package org.example;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        File file = new File(args[0]);
        List<String> lines = readFileToListOfLines(file);
        List<List<String>> groups = findGroups(lines);
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

    private static List<List<String>> findGroups(List<String> lines) {
        List<Map<String, Integer>> wordsToGroupsNumbers = new ArrayList<>();
        List<List<String>> linesGroups = new ArrayList<>();
        Map<Integer, Integer> mergedGroupNumberToFinalGroupNumber = new HashMap<>();
        for (String line : lines) {
            String[] words = line.split(";");
            for (int i = 0; i < words.length; i++) {
                words[i] = words[i].substring(1, words[i].length() - 1);
            }
            if (Arrays.stream(words).anyMatch(str -> str.contains("\"")))
                continue;

            TreeSet<Integer> foundInGroups = new TreeSet<>();
            List<NewWord> newWords = new ArrayList<>();
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (wordsToGroupsNumbers.size() == i)
                    wordsToGroupsNumbers.add(new HashMap<>());

                if (word.isEmpty())
                    continue;

                Map<String, Integer> wordToGroupNumber = wordsToGroupsNumbers.get(i);
                Integer wordGroupNumber = wordToGroupNumber.get(word);
                if (wordGroupNumber != null) {
                    while (mergedGroupNumberToFinalGroupNumber.containsKey(wordGroupNumber))
                        wordGroupNumber = mergedGroupNumberToFinalGroupNumber.get(wordGroupNumber);
                    foundInGroups.add(wordGroupNumber);
                } else {
                    newWords.add(new NewWord(word, i));
                }
            }
            int groupNumber;
            if (foundInGroups.isEmpty()) {
                groupNumber = linesGroups.size();
                linesGroups.add(new ArrayList<>());
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
        linesGroups.removeAll(Collections.singleton(null));
        return linesGroups;
    }

    private static List<String> readFileToListOfLines(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem with file reading!");
        }
        return lines;
    }

    private static void writeListToFile(List<List<String>> groups) {
        long countOfGroups = groups.stream().filter(x -> x.size() > 1).count();
        groups.sort(new Comparator<List<String>>() {
            @Override
            public int compare(List<String> o1, List<String> o2) {
                return o2.size() - o1.size();
            }
        });
        File file = new File("output.txt");
        int groupNumber = 0;
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write("Число групп с более чем одним элементом: " + countOfGroups + "\n");
            for (List<String> group : groups) {
                Set<String> groupSet = new HashSet<String>(group);
                groupNumber++;
                writer.write("Группа " + groupNumber + "\n");
                for (String line : groupSet) {
                    writer.write(line + "\n");
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

}


