package perguntas;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class jsonLoad {

    public static List<Question> loadQuestions(String path) {
        Gson gson = new Gson();
        try {
            JsonReader reader = new JsonReader(new FileReader(path));
            QuizList quizList = gson.fromJson(reader, QuizList.class);

            return quizList.getQuizzes().get(0).getQuestions();
        } catch (FileNotFoundException e) {
            System.err.println("Ficheiro JSON não encontrado: " + path);
            return List.of();
        }
    }
}
