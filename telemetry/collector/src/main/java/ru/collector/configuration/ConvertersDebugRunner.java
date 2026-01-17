//package ru.collector.configuration;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
//
//@Slf4j
//@Configuration
//@RequiredArgsConstructor
//public class ConvertersDebugRunner {
//    private final RequestMappingHandlerAdapter handlerAdapter;
//
//    @Bean
//    public ApplicationRunner logMessageConverters() {
//        return args -> {
//            log.info("=== HttpMessageConverters registered ===");
//            handlerAdapter.getMessageConverters()
//                    .forEach(c -> log.info("Converter: {}", c.getClass().getName()));
//            log.info("=== end ===");
//        };
//    }
//}
