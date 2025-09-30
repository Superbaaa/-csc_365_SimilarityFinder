import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FileScraper {

    private List<FrequencyTable> allContentOfFrequencyTable;
    private List<String> extractedUrls;
    public List<String> pageTitles;

    public FileScraper(String filePath) throws IOException {
        this.allContentOfFrequencyTable = new ArrayList<>();
        this.extractedUrls = Files.readAllLines(Paths.get(filePath));
        this.pageTitles = new ArrayList<>();
    }

    public void processPage() {
        for (String url : extractedUrls) {
            try {
                Document document = Jsoup.connect(url).get();
                pageTitles.add(document.title());

                Elements paragraphs = document.select("#mw-content-text p");
                StringBuilder content = new StringBuilder();
                for (Element p : paragraphs) {
                    content.append(p.text()).append("\n");
                }
                String lowerCaseContent = content.toString().toLowerCase();
                String[] tokenizedContent = lowerCaseContent.split("\\W+");
                Set<String> common_words = Set.of("the", "and", "of", "to", "a", "in", "is", "it", "that", "on", "for", "as", "with", "by", "was",
                        "were", "this", "are", "an", "be", "which", "or", "from", "at", "you", "his", "her", "he", "she", "we", "they",
                        "their", "its", "my", "your", "our", "us", "me", "him", "them");

                FrequencyTable frequencyTable = new FrequencyTable();
                for (String word : tokenizedContent) {
                    if (!common_words.contains(word) && word.length() > 2 && !word.matches("\\d+")) {
                        frequencyTable.add(word);
                    }
                }
                allContentOfFrequencyTable.add(frequencyTable);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private List<TitleSimilarity> analyzeSimilarity(int index) {
        TFIDFCalculator tfidfCalculator = new TFIDFCalculator(allContentOfFrequencyTable);
        List<FrequencyTable> tfIdfvectors = tfidfCalculator.vectorizeAll();
        ArrayList<TitleSimilarity> similarities = new ArrayList<>();

        for (int i = 0; i < tfIdfvectors.size(); i++) {
            if (i != index) {
                double similarity = tfidfCalculator.cosineSimilarity(tfIdfvectors.get(index), tfIdfvectors.get(i));
                similarities.add(new TitleSimilarity(pageTitles.get(i), similarity));
//                System.out.println(pageTitles.get(i) + " " + similarity);
            }
        }
        return similarities;
    }

    public String displayTopTwoSimilarDocuments(int index) {
        List<TitleSimilarity> titleSimilarities = analyzeSimilarity(index);
        titleSimilarities.sort(Comparator.comparing(TitleSimilarity::getSimilarity).reversed());//Descending order

        String selectedTitle = pageTitles.get(index);
        StringBuilder sb = new StringBuilder();
        sb.append("Showing Similarities for ").append(selectedTitle);
        sb.append("\n===========================================\n");
        sb.append("Top 2 Most Similar Documents:\n");
        for (int i = 0; i < Math.min(2, titleSimilarities.size()); i++) {
            TitleSimilarity ts = titleSimilarities.get(i);
            sb.append(String.format("%d. %s (Similarity: %.4f)%n", i + 1, ts.title, ts.similarity));
        }
//        System.out.println(sb);
        return sb.toString();
    }
    //helper class for arraylist
    private static class TitleSimilarity {
        String title;
        double similarity;

        TitleSimilarity(String title, double similarity) {
            this.title = title;
            this.similarity = similarity;
        }

        public double getSimilarity() {
            return similarity;
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            String FilePath = "./src/wikipedia";

            FileScraper fileScraper = new FileScraper(FilePath);
            fileScraper.processPage();
            //Debug
//            fileScraper.displayTopTwoSimilarDocuments(0); // Animal
//            fileScraper.displayTopTwoSimilarDocuments(1); // Dog
//            fileScraper.displayTopTwoSimilarDocuments(2); // Mammal
//            fileScraper.displayTopTwoSimilarDocuments(3); // Carnivore

        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

    }
}
