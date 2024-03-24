package searchengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import searchengine.controllers.ApiController;


@SpringBootApplication
public class Application {
    public static final Logger LOG= LoggerFactory.getLogger(ApiController.class);
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        LOG.info("Приложение запустилось!");
    }
}
