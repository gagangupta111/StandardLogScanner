package com.loganalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Size;
import java.io.File;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;

@SpringBootApplication
public class LogAnalyzerMain {

    public static void main(String[] args) throws Exception {

        if (args != null && args.length >= 1 && !args[0].isEmpty()) {

            File file = new File(args[0]);
            if (!file.exists()) {
                throw new Exception("Path provided in argument does not exist");
            }

        }

        SpringApplication.run(LogAnalyzerMain.class, args);

    }

    @Bean
    MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleValidationFailure(ConstraintViolationException ex) {

        StringBuilder messages = new StringBuilder();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            messages.append(violation.getMessage() + "\n");
        }

        return messages.toString();
    }


    @RestController
    @Validated
    @RequestMapping("/api")
    class HelloResource {

        @RequestMapping("/greet/{name}")
        public Map<String, String> greet(@PathVariable("name") @Size(min = 3, max = 10, message = "You messed up!") String name) {
            return Collections.singletonMap("greeting", format("Hello %s", name));
        }
    }

}
