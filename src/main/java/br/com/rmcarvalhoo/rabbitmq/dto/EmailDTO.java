package br.com.rmcarvalhoo.rabbitmq.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EmailDTO {
    private String subject;
    private String recipient;
    private String content;
}
