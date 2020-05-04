package com.github.gkang.kafka;

import java.util.Properties;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// To understand: Where the message was produced, if it wa produced correctly, partition number, its offset value etc.
public class ProducerWithCallback {

  private static final Logger logger = LoggerFactory.getLogger(ProducerWithCallback.class);
  private static final String BOOTSTRAP_SERVER = "127.0.0.1:9092";

  public static void main(String[] args) {

    Properties properties = new Properties();
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
    properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties);

    for(int i = 0; i < 10; i++) {
      String topic = "firstTopic";
      String key = "id_"+ i;
      String value = "Hi There!!"+i;

      ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
      logger.info("Key is:"+ key);

      kafkaProducer.send(record, new Callback() {
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
          // executes every time a record is successfully sent or there is an exception.
          if (e == null) {
            // record was successfully sent.
            logger.info("Received new metadata: \n" +
                "Topic: " + recordMetadata.topic() + "\n" +
                "Partition: " + recordMetadata.partition() + "\n" +
                "Offset: " + recordMetadata.offset() + "\n" +
                "Timestamp: " + recordMetadata.timestamp());
          } else {
            logger.error("Error while producing.", e);
          }
        }});
      //  }).get(); ----> get() blocks the send function and makes it synchronous. It is terrible for performance and should not be done in production.
      // You can check same key goes tos amd partition by using get(). Send once and send again. Same key will always go to same partition.
    }

    kafkaProducer.flush();
    kafkaProducer.close();
  }
}
