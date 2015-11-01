/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.boot.context.embedded.netty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * A {@link org.springframework.boot.SpringApplication} harness for integration tests.
 * </p>
 * Implemented in Java to avoid currency issues caused by repeated annotation scanning of Groovy controllers.
 */
@Controller
@EnableAutoConfiguration(exclude = WebMvcAutoConfiguration.class)
@ComponentScan
@EnableWebMvc
class TestApplication {
    private static final String MESSAGE = "Hello, World!";
    private static final Callable<String> MESSAGE_CALLABLE = new Callable<String>() {
        @Override
        public String call() throws Exception {
            return MESSAGE;
        }
    };

    @RequestMapping(value = "/plaintext", produces = "text/plain")
    @ResponseBody
    public String plaintext() {
        return MESSAGE;
    }

    @RequestMapping(value = "/async", produces = "text/plain")
    @ResponseBody
    public Callable<String> async() {
        return MESSAGE_CALLABLE;
    }

    @RequestMapping(value = "/json", produces = "application/json")
    @ResponseBody
    public Message json() {
        return new Message(MESSAGE);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String upload(HttpServletRequest request) throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        int total = 0;
        while (true) {
            byte[] bytes = new byte[8192];
            int read = inputStream.read(bytes);
            if (read == -1) {
                break;
            }
            total += read;
        }
        return "Total bytes received: " + total;
    }

    @RequestMapping("/sleepy")
    @ResponseBody
    public String sleepy() throws InterruptedException {
        int millis = 500;
        Thread.sleep(millis);
        return "Yawn! I slept for " + millis + "ms";
    }

    @Bean
    public ServletRegistrationBean nullServletRegistration() {
        return new ServletRegistrationBean(new NullHttpServlet(), "/null");
    }

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    private static class Message {
        private final String message;

        public Message(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private class NullHttpServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        }
    }
}
