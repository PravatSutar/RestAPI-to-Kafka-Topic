package com.company.fs.analytics.kafka.consumer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the Apache Kafka Consumer class that consumes records from a Kafka cluster. It will transparently handle 
 * the failure of servers in the Apache Kafka cluster, and transparently adapt as partitions of data it fetches migrate 
 * within the cluster. It interacts with the server to allow groups of consumers to load balance consumption using consumer groups.
 * 
 * The consumer maintains TCP connections to the necessary brokers to fetch data. 
 * Failure to close the consumer after use will leak these connections. 
 */
public class FSKafkaConsumer {

	private static final Logger logger = LoggerFactory.getLogger(FSKafkaConsumer.class);

	private KafkaConsumer<String, String> kafkaConsumer;

	public FSKafkaConsumer(String topicName, Properties consumerProperties) {

		kafkaConsumer = new KafkaConsumer<>(consumerProperties);
		kafkaConsumer.subscribe(Arrays.asList(topicName));
	}

	/**
	 * runSingleWorker() starts a single worker thread per topic. Once the consumer
	 * object is created, it is subscribed to a list of Kafka topics in the
	 * constructor.
	 */
	public void runSingleWorker() {

		/*
		 * This infinite while loop will keep on listening to new messages in each topic
		 * that is subscribed to.
		 */
		while (true) {

			ConsumerRecords<String, String> records = kafkaConsumer.poll(100);

			for (ConsumerRecord<String, String> record : records) {
				String message = record.value();
				logger.info("Received message: " + message);

				try {
					JSONObject receivedJsonObject = new JSONObject(message);
					logger.info("Index of deserialized JSON object: " + receivedJsonObject.getInt("index"));
				} catch (JSONException e) {
					logger.error(e.getMessage());
				}

				/*
				 * Once processing of a Kafka message is complete, commit the offset. By
				 * default, the consumer object takes care of this.
				 */
				{
					Map<TopicPartition, OffsetAndMetadata> commitMessage = new HashMap<>();

					commitMessage.put(new TopicPartition(record.topic(), record.partition()),
							new OffsetAndMetadata(record.offset() + 1));
					kafkaConsumer.commitSync(commitMessage);
					logger.info("Offset committed to Kafka.");
				}
			}
		}
	}
}
