KAFKA SETUP

1. Download latest kafka from: https://kafka.apache.org/quickstart
2. You will  see the tar file in Downloads and can move it somewhere else.
3. Uncompress/un-tar it:   tar -xzf kafka_2.12-2.5.0.tgz
4. You must have Java installed on the system. To check, see the java version: java -version
5. If not found, install it as below:
   brew tap caskroom/versions
   brew cask install java8
6. To avoid typing bin/{command}, add the bin directory to path. Go inside bin: cd bin
7. Get full path and copy it: pwd 
8. Paste this path inside bash_profile file.   
   open ~/.bash_profile
   Paste: 
   export PATH="$PATH:Paste_copied_path_here"
   (Make this change carefully, else you wont be able to open bash profile again. 
   You can also find bash_profile in finder home directory after pressing cmd+shift+. to unhide  hidden files)
9. Now, you can run kafka commands anywhere and without  giving full path. Just type kafka and tab and you will see all commands available.

---------------------------------------------------------------------------------------------------------------------------
Another way  of installing:

1. brew install kafka
It willa utomatically install binaries to home directory.  

---------------------------------------------------------------------------------------------------------------------------

Starting Zookeeper and Kafka locally: Go inside kafka directory.

1. Type: zookeeper-server-start config/zookeeper.properties
If everything goes well, you will see this: INFO binding to port 0.0.0.0/0.0.0.0:2181
Zookeeper started on port 2181.
Keep this running. If something is already binded to 2181, youw ill run into issues.

2. Look at  the config of zookeeper: cat config/zookeeper.properties
You will see this: dataDir=/tmp/zookeeper
This is not good. Change it to new one.
mkdir data/zookeeper
Get full path with pwd and paste it instead of  /tmp/zookeeper
- To change the path: nano config/zookeeper.properties
dataDir=/Users..../data/zookeeper

3. To stop zookeeper, ctrl+c and re-start it. Now, check and you  will see new files inside data/zookeeper.

4. Also make kafka directory: mkdir data/kafka

5.  Edit config/server.properties same as above with full path to data/kafka for kafka. Change log.dirs. Data in kafka will be stored in this directory and not lost.

6. Start kafka in new terminal: kafka-server-start config/server.properties

If everything goes well, you will see: INFO [KafkaServer id=0] started (kafka.server.KafkaServer)

7. Check data/kafka and you will see some files.

------------------------------------------------------------------------------------------------------

Create a  topic:

1. Point to zookeeper address 127.0.0.1:2181
kafka-topics --zookeeper 127.0.0.1:2181 --topic firstTopic --create --partitions 3 --replication-factor 2
You will get error: Error while executing topic command : Replication factor: 2 larger than available brokers: 1.
In Kafka, you can not create replication factor >  number  of  brokers. We started only one broker on our cluster.
kafka-topics --zookeeper 127.0.0.1:2181 --topic firstTopic --create --partitions 3 --replication-factor 1

2. To check if  topic was craeted:  kafka-topics --zookeeper 127.0.0.1:2181 --list

3. To learn more about this topic, its partitions and where they are assigned:
kafka-topics --zookeeper 127.0.0.1:2181 --topic  firstTopic --describe

Topic:firstTopic	PartitionCount:3	ReplicationFactor:1	Configs:
	Topic: firstTopic	Partition: 0	Leader: 0	Replicas: 0	Isr: 0
	Topic: firstTopic	Partition: 1	Leader: 0	Replicas: 0	Isr: 0
	Topic: firstTopic	Partition: 2	Leader: 0	Replicas: 0	Isr: 0

	It shows broker id = 0 is the leader. You can see this broker id in kafka window. Since, replication factor is 0, replica is 0 and ISR   (in-sync replica) is also 0.

4. Check deletion: create second topic: kafka-topics --zookeeper 127.0.0.1:2181 --topic secondTopic --create --partitions 3 --replication-factor 1
   kafka-topics --zookeeper 127.0.0.1:2181 --topic secondTopic --delete 
Topic secondTopic is marked for deletion.
Note: This will have no impact if delete.topic.enable is not set to true.
By default, this property is set to true. So, when you see the list, it will not be there  anymore.
------------------------------------------------------------------------------------------------------

Kafka Console Producer CLI

1. To lauch producer (check for REQUIRED  parameters in documentation):  
kafka-console-producer --broker-list 127.0.0.1:9092 --topic firstTopic
You get:
>

Now you can type messages:
> Hi!
> Lauched producer.
We produced 2 messages  without error.
Press control c.

2. You can set the acks property:
kafka-console-producer --broker-list 127.0.0.1:9092 --topic firstTopic --producer-property acks=all

3. If you try to send messages to topic that has not been created:
kafka-console-producer --broker-list 127.0.0.1:9092 --topic SecondTopic
> message
You get this warning: WARN [Producer clientId=console-producer] Error while fetching metadata with correlation id 3 : {SecondTopic=LEADER_NOT_AVAILABLE} (org.apache.kafka.clients.NetworkClient)

>ok
After next messsage, you wont get tha warning anymore.
So what happened here: The new topic was craeted if you check kafka tab. But, there  was no leader selected. So, we got WARN.
But producers can recover from errors. It tries and waited till leader was avaliable.
You will see new topic in the list.

Now, check its description, it will have 0 partition. So, always create topic before producing to it.
You can  set the  default in server.properties for num.partitions=3

------------------------------------------------------------------------------------------------------

Kafka Console Consumer CLI

1. To lauch consumer (check for REQUIRED  parameters in documentation):  
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic firstTopic
You wont see anything. Consumer only reads from the time you launch it.  It wont read messages produced before.

Open producer in separate tab and produce messages again: kafka-console-producer --broker-list 127.0.0.1:9092 --topic firstTopic
>Hi!

You will see those messages appearing on consumer tab.

2. How to see all messgaes in topic from beginning:
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic firstTopic --from-beginning
You will see all messages. Order of messages is not total. It is per partition. You will see order if you run topic with one partition.


------------------------------------------------------------------------------------------------------
Consumer group mode
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic firstTopic --group firstAppGroup
You wont see anything. Produce new message and you will see it.

Open new tab: Start exact  same consumer with same consumer group:
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic firstTopic --group firstAppGroup

Now produce messages. Messages will appear in both tabs. Consumer groups re-balace the load
If  one consumer is closed, you will see all messages going to other tabs. No message will be lost.

2. Read all messages in another consumer group from beginning (you will see all messages):
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic firstTopic --group NewAppGroup --from-beginning

3. Now stop, ctrl+c and run same command again: kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic firstTopic --group NewAppGroup --from-beginning

This time, you won't see any messages. 
Because, the group has already read messages and committed the offset till last message. Now, it will only read new messages.
--from-beginning is not taken into account anymore. Produce more messages and you will start seeing new ones on consumer side.

4. kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --list
You will see 4 consumer groups even though we craeted 2. When we do not give consumer group in console consumer, it generates random consumer group for you.

5. kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --describe --group firstAppGroup
Consumer group 'firstAppGroup' has no active members. (because we stopped it).
Start a consumer in other window: kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic firstTopic --group firstAppGroup
This error will disappear!!

If LAG is not 0, consumer group needs to catch up on some messages. (equal to lag)
GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
firstAppGroup   firstTopic      1          8               9               1               -               -               -
firstAppGroup   firstTopic      2          8               9               1               -               -               -
firstAppGroup   firstTopic      0          9               10              1               -               -               -
this needs to catch up on three messages.
 Now, catch up with lagging messages: kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic firstTopic --group firstAppGroup
 You will see 3 messgaes and now describe again (LAG is 0)

 GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
firstAppGroup   firstTopic      1          9               9               0               -               -               -
firstAppGroup   firstTopic      2          9               9               0               -               -               -
firstAppGroup   firstTopic      0          10              10              0               -               -               -

------------------------------------------------------------------------------------------------------
How to make consumer replay data (if it has already read messages)

1. You can reset offsets to tell where to read from.
kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --group firstAppGroup --reset-offsets --to-earliest --execute --topic firstTopic
GROUP                          TOPIC                          PARTITION  NEW-OFFSET
firstAppGroup                  firstTopic                     0          0
firstAppGroup                  firstTopic                     2          0
firstAppGroup                  firstTopic                     1          0
New offset is 0.

Restart consumer and you will see all messages.Describe it and lag will be 0.

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic firstTopic --group firstAppGroup
kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --describe --group firstAppGroup

2. You can shift forward or backward:
kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --group firstAppGroup --reset-offsets --shift-by 2 --execute --topic firstTopic
kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --group firstAppGroup --reset-offsets --shift-by -2 --execute --topic firstTopic

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic firstTopic --group firstAppGroup
Now you will see 6 messages (shift -2 shifts backward for each partition by 2).