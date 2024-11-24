package br.com.rmcarvalhoo.rabbitmq.listener;

import static br.com.rmcarvalhoo.rabbitmq.config.RabbitConfiguration.QUEUE;
import static br.com.rmcarvalhoo.rabbitmq.config.RabbitConfiguration.UNDELIVERED_QUEUE;

import br.com.rmcarvalhoo.rabbitmq.dto.EmailDTO;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.retry.count}")
    private Integer retryCount;

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = QUEUE)
    public void primary(EmailDTO email, @Header(required = false, name = "x-death") Map<String, ?> xDeath) throws Exception {
        log.info("Message read from Queue: [{}]", email);
        if (checkRetryCount(xDeath)) {
            sendToUndelivered(email);
            return;
        }

        throw new Exception("Random error");
    }


    private boolean checkRetryCount(Map<String, ?> xDeath) {
        if (xDeath != null && !xDeath.isEmpty()) {
            Long count = (Long) xDeath.get("count");
            return count >= retryCount;
        }
        return false;
    }

    private void sendToUndelivered(EmailDTO email) {
        log.info("Maximum retry reached, send message to the undelivered queue, msg: [{}]", email);
        this.rabbitTemplate.convertAndSend(UNDELIVERED_QUEUE, email);
    }
}
