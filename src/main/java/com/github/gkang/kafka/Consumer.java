package com.github.gkang.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Consumer {

  private static final Logger logger = LoggerFactory.getLogger(Consumer.class);
  private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092";
  private static final String GROUP_ID = "MyKafkaAppGroup";
  private static final String TOPIC = "firstTopic";

  public static void main(String[] args) {

    // 1. Create consumer properties: https://kafka.apache.org/documentation/#consumerconfigs
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
    // When producer serializes the value to bytes, consumer has to deserialize bytes to get back the original string value value.
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
    // AUTO_OFFSET_RESET_CONFIG can be earliest (from beginning), latest (only new message onwards, none (throws error if no offset is saved)
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    // 2. Create consumer
    KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);

    // 3. Subscribe consumer to topic: subscribe() API can take a pattern or collection of topics.
    kafkaConsumer.subscribe(Arrays.asList(TOPIC));

    // 4. Poll for new data: Get data by asking for data
    while(true) {
      ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
      for(ConsumerRecord<String, String> record : records) {
        logger.info("Key: "+ record.key() + ", Value: "+ record.value());
        logger.info("Partition: "+ record.partition() + ", Offset: "+ record.offset());
      }
    }

    // To see the re-balancing of consumer group, run this class twice in parallel and you will see re-balancing:
    // Attempt to heartbeat failed since group is re-balancing
    // One group will have: Adding newly assigned partitions: firstTopic-1, firstTopic-0
    // Other consumer will have: Adding newly assigned partitions: firstTopic-2
    // When you run producer, fist consumer group only reads from partition 0 and 1. second one only reads from partition 2.
  }
}
