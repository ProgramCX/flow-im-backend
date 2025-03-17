package cn.programcx.im;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ImApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory(".").load();
        SpringApplication.run(ImApplication.class, args);
    }

}
