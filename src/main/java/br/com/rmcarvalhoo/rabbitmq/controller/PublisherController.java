package br.com.rmcarvalhoo.rabbitmq.controller;

import br.com.rmcarvalhoo.rabbitmq.dto.EmailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;

@Slf4j
@RestController
@RequestMapping(value = "/publisher")
@RequiredArgsConstructor
public class PublisherController {

    private final AmqpTemplate amqpTemplate;

    @RequestMapping(path = "/{text}", method = RequestMethod.GET)
    public ResponseEntity<String> download(@PathVariable String text) throws IOException {
        return ResponseEntity.ok()
                .body(text.concat(" SUCESSO!!!"));
    }


    @PostMapping(value = "/mail")
    public String producer(@RequestBody EmailDTO dto) {
        log.info("Sending message to the RabbitMQ [{}]", dto);

        amqpTemplate.convertAndSend("retry-exchange", "mainQueue.retry", dto);
        return "Message sent to the RabbitMQ Successfully";
    }

}

