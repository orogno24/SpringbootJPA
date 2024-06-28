package kopo.poly.config;

import java.util.*;

public class TfIdfCalculator {
    private Map<String, Double> idfScores = new HashMap<>();

    public void train(List<List<String>> documents) {
        Map<String, Integer> docFrequency = new HashMap<>();
        int totalDocs = documents.size();

        for (List<String> doc : documents) {
            Set<String> seenWords = new HashSet<>();
            for (String word : doc) {
                if (!seenWords.contains(word)) {
                    docFrequency.put(word, docFrequency.getOrDefault(word, 0) + 1);
                    seenWords.add(word);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : docFrequency.entrySet()) {
            double idf = Math.log((double) totalDocs / entry.getValue() + 1);
            idfScores.put(entry.getKey(), idf);
        }
    }

    public double tfIdf(List<String> words, String word) {
        double tf = (double) Collections.frequency(words, word) / words.size();
        double idf = idfScores.getOrDefault(word, 0.0);
        return tf * idf;
    }

    public double calculateDocumentScore(List<String> docWords, List<String> queryWords) {
        double score = 0.0;
        for (String word : queryWords) {
            score += tfIdf(docWords, word);
        }
        return score;
    }
}
