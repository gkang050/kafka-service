package com.github.gkang.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsumerWithThread {

  public static void main(String[] args) {
    new ConsumerWithThread().run();
  }

  private ConsumerWithThread(){
  }

  private void run() {
    Logger logger = LoggerFactory.getLogger(ConsumerWithThread.class);
    final String bootStrapServer = "127.0.0.1:9092";
    final String groupId = "MyNewKafkaAppGroup";
    final String topic = "firstTopic";

    // latch to deal with multiple threads.
    final CountDownLatch latch = new CountDownLatch(1);

    // create consumer runnable.
    logger.info("Creating consumer thread.");
    Runnable myConsumerRunnable = new ConsumerRunnable(bootStrapServer, groupId, topic, latch);

    // start the thread.
    Thread thread = new Thread(myConsumerRunnable);
    thread.start();

    // add shutdown hook to properly shutdown the application.
    Runtime.getRuntime().addShutdownHook(new Thread( ()  -> {
      logger.info("Caught shutdown hook.");
      // to shutdown our consumer.
      ((ConsumerRunnable) myConsumerRunnable).shutdown();
      try {
        latch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      logger.info("Application has exited");
    }

    ));

    try {
      // this makes us wait all the way until the application is over.
      latch.await();
    } catch (InterruptedException e) {
      logger.info("Application got interrupted.", e);
    } finally {
      logger.info("Application is closing.");
    }
  }

  // Consumer thread is where our consumer consumes.
  // We give latch to this thread and the latch will be able to shutdown our consumer correctly.
  public class ConsumerRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class);
    // CountDownLatch in java to deal with concurrency
    private final CountDownLatch latch;
    private final KafkaConsumer<String, String> kafkaConsumer;

    public ConsumerRunnable(String bootstrapServer, String groupId, String topic,CountDownLatch latch){
      this.latch = latch;

      Properties properties = new Properties();
      properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
      properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
      properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

      kafkaConsumer = new KafkaConsumer<>(properties);
      kafkaConsumer.subscribe(Arrays.asList(topic));
    }

    @Override
    public void run() {
      try {
        while (true) {
          // We give poll to complete in 100ms.
          ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
          for (ConsumerRecord<String, String> record : records) {
            logger.info("Key: " + record.key() + ", Value: " + record.value());
            logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());
          }
        }
      }catch(WakeupException exception) {
        // This is an expected exception which happens when we receive shutdown signal. So, no need to log exception.
        logger.info("Received shutdown signal.");
    } finally {
        kafkaConsumer.close();
        // to allow our main code to understand that we are done with consumer.
        latch.countDown();
      }
    }

    // To shutdown consumer thread.
    public void shutdown(){
      // wakeup() is a special method to interrupt consumer.poll()
      // it throws exception called WakeupException.
      kafkaConsumer.wakeup();
    }
  }
}
