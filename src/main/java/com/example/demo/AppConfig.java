package com.example.demo;

import java.sql.Timestamp;
import java.time.Instant;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageChannel;

@Configuration
@ConfigurationProperties("kafka")
public class AppConfig {
	@Bean
	public IntegrationFlow fromKafkaFlow(ConsumerFactory<String, String> consumerFactory) {
		return IntegrationFlows
				.from(Kafka.messageDrivenChannelAdapter(consumerFactory, "t"))
				//.<String, String>transform(String::toUpperCase)
				//.<String, String>transform((p) -> p + "YYY")
				//.<String>handle((p, h) -> p + "VAS")
				.<String>handle((p, h) -> p + Timestamp.from(Instant.now()))
				//sending output to the direct channel myChannel
				.channel("myChannel")
				.get();
	}
	
	//creating new topic for outbound flow
	@Bean
	public NewTopic topic() {
		return new NewTopic("t-t", 1, (short) 1);
	}
	
	// channel from integration flow
	@Bean
	public MessageChannel myChannel() {
		DirectChannel directChannel = new DirectChannel();
		return directChannel;
	}

	@Bean
	public IntegrationFlow outFlow(KafkaTemplate<String, String> kafkaTemplate) {
		return IntegrationFlows.from("myChannel")
				.handle(Kafka.outboundChannelAdapter(kafkaTemplate).topic("t-t")).
				get();
	}

}
