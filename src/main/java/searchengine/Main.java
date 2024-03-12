package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            LuceneMorphology luceneMorph =
                    new RussianLuceneMorphology();
//            List<String> wordBaseForms =
//                    luceneMorph.getNormalForms("лисы");
//            wordBaseForms.forEach(System.out::println);
            String name = "у";
            List<String> worldInfo = luceneMorph.getMorphInfo(name);
            worldInfo.forEach(System.out::println);
            for (String w : worldInfo) {
                if (w.contains("МЕЖД")) {
                    System.out.println(true);
                } else System.out.println(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
