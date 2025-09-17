package com.paklog.warehouse.infrastructure.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.kafka")
    public KafkaProperties kafkaProperties() {
        return new KafkaProperties();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        KafkaProperties properties = kafkaProperties();
        
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                properties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
                StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
                JsonSerializer.class);
        
        // Producer optimization settings
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        KafkaProperties properties = kafkaProperties();
        
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                properties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, 
                properties.getConsumer().getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
                JsonDeserializer.class);
        
        // Consumer optimization settings
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.paklog.warehouse.domain.*");
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Enable manual acknowledgment for better control
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Set concurrency level
        factory.setConcurrency(3);
        
        return factory;
    }

    // Configuration properties class
    public static class KafkaProperties {
        private String bootstrapServers = "localhost:9092";
        private Consumer consumer = new Consumer();
        private Producer producer = new Producer();

        public String getBootstrapServers() { return bootstrapServers; }
        public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }

        public Consumer getConsumer() { return consumer; }
        public void setConsumer(Consumer consumer) { this.consumer = consumer; }

        public Producer getProducer() { return producer; }
        public void setProducer(Producer producer) { this.producer = producer; }

        public static class Consumer {
            private String groupId = "warehouse-operations";
            private String autoOffsetReset = "earliest";

            public String getGroupId() { return groupId; }
            public void setGroupId(String groupId) { this.groupId = groupId; }

            public String getAutoOffsetReset() { return autoOffsetReset; }
            public void setAutoOffsetReset(String autoOffsetReset) { this.autoOffsetReset = autoOffsetReset; }
        }

        public static class Producer {
            private String acks = "all";
            private int retries = 3;

            public String getAcks() { return acks; }
            public void setAcks(String acks) { this.acks = acks; }

            public int getRetries() { return retries; }
            public void setRetries(int retries) { this.retries = retries; }
        }
    }
}