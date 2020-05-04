package com.github.gkang.kafka;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

// Kafka documentation: https://kafka.apache.org/documentation/
public class Producer {

  private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092";

  public static void main(String[] args) {

    // 1. Create Producer properties: https://kafka.apache.org/documentation/#producerconfigs
    Properties properties = new Properties();
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);

    // key.serializer and value.serializer helps producer know what type of value we are sending to kafka and how to serialize it to bytes.
    // Kafka client converts everything to bytes (0s and 1s) when sending to Kafka.
    properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // 2. Create the producer: We create producer with string key and value.
    KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties);

    // 3. Create a producer record.
    ProducerRecord<String, String> record = new ProducerRecord<>("firstTopic", "Hi There!!");

    // 4. Send data --> It is asynchronous (happening in the background)
    kafkaProducer.send(record);

    // Since 4. is asynchronous, the program exits and data is never sent.
    // To wait for data to be produced, we can flush data.
    kafkaProducer.flush();
    // flush and close producer
    kafkaProducer.close();
  }
}
