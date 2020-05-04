package com.github.gkang.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Assign and seek are used to replay data or fetch a specific message. So, we do not need a group id.
public class ConsumerWithAssignAndSeek {

  private static final Logger logger = LoggerFactory.getLogger(ConsumerWithAssignAndSeek.class);
  private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092";
  private static final String TOPIC = "firstTopic";

  public static void main(String[] args) {

    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);

    TopicPartition partitionToReadFrom = new TopicPartition(TOPIC, 0);
    long offsetToReadFrom = 15L;
    // assign which partition of topic to read from.
    kafkaConsumer.assign(Arrays.asList(partitionToReadFrom));
    // see to go to a specific offset.
    kafkaConsumer.seek(partitionToReadFrom, offsetToReadFrom);

    int numberOfMessagesToRead = 5;
    boolean keepOnReading = true;
    int numberOfMessagesReadSoFar = 0;

    while(keepOnReading) {
      ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
      for(ConsumerRecord<String, String> record : records) {
        numberOfMessagesReadSoFar += 1;
        logger.info("Key: "+ record.key() + ", Value: "+ record.value());
        logger.info("Partition: "+ record.partition() + ", Offset: "+ record.offset());
        if(numberOfMessagesReadSoFar >= numberOfMessagesToRead) {
          keepOnReading = false;
          break;
        }
      }
    }
    logger.info("Exiting the application.");
  }
}
