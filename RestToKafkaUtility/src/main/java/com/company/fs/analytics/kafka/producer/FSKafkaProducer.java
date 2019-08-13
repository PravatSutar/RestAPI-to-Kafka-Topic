package com.company.fs.analytics.kafka.producer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.company.fs.analytics.kafka.consumer.FSKafkaConsumer;
import com.company.fs.analytics.kafka.dbfactory.DBConnector;

/**
 * This class is the Kafka Producer which allows applications to send streams of
 * data to the Kafka cluster.
 */

@SpringBootApplication
public class FSKafkaProducer implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(FSKafkaProducer.class);

	@Value("${kafka.topic.name}")
	private String topicName;

	@Value("${kafka.bootstrap.servers}")
	private String kafkaBootstrapServers;

	@Value("${zookeeper.groupId}")
	private String zookeeperGroupId;

	@Value("${zookeeper.host}")
	String zookeeperHost;

	@Value("${rest.api.base.url}")
	String restURL;

	int counter = 0;
	private DBConnector dbConnector = new DBConnector();
	private KafkaProducer<String, String> producer;

	public static void main(String[] args) {
		SpringApplication.run(FSKafkaProducer.class, args);
	}

	@Override
	public void run(String... args) throws Exception, Exception {

		/* Kafka Consumer Properties */

		Properties consumerProperties = new Properties();
		consumerProperties.put("bootstrap.servers", kafkaBootstrapServers);
		consumerProperties.put("group.id", zookeeperGroupId);
		consumerProperties.put("zookeeper.session.timeout.ms", "6000");
		consumerProperties.put("zookeeper.sync.time.ms", "2000");
		consumerProperties.put("auto.commit.enable", "false");
		consumerProperties.put("auto.commit.interval.ms", "1000");
		consumerProperties.put("consumer.timeout.ms", "-1");
		consumerProperties.put("max.poll.records", "1");
		consumerProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consumerProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		/* Creating a thread to listen to the kafka topic */

		Thread kafkaConsumerThread = new Thread(() -> {
			logger.info("Starting Kafka consumer thread.");

			FSKafkaConsumer simpleKafkaConsumer = new FSKafkaConsumer(topicName, consumerProperties);

			simpleKafkaConsumer.runSingleWorker();
		});
		// Starting the first thread.

		kafkaConsumerThread.start();

		/* Kafka Producer Properties */
		Properties producerProperties = new Properties();
		producerProperties.put("bootstrap.servers", kafkaBootstrapServers);
		producerProperties.put("acks", "all");
		producerProperties.put("retries", 0);
		producerProperties.put("batch.size", 16384);
		producerProperties.put("linger.ms", 1);
		producerProperties.put("buffer.memory", 33554432);
		producerProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producerProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producer = new KafkaProducer<>(producerProperties);

		invokeProducerThread(producer);
	}

	private void invokeProducerThread(KafkaProducer<String, String> producer) {
		logger.info("Starting Kafka producer thread.");
		sendMessagesToKafka(producer);
	}

	/**
	 * Function to send Rest API JSON string message to Kafka
	 *
	 * @param producer The Kafka producer we created in the run() method earlier.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws Exception
	 * @throws MalformedURLException
	 */
	private void sendMessagesToKafka(KafkaProducer<String, String> producer) {

		try {
			String encodedTS = getTimeStamp();
			System.out.println("encodedTS: " + encodedTS);
			if (encodedTS != null) {
				String payloadStr = getPayload(restURL + encodedTS);
				logger.info("STR Value: " + payloadStr);
				logger.info("Topic name is " + topicName);
				postMessageToKafkaTopic(payloadStr, producer, topicName);

				updateControlTable(payloadStr);
			}
		} catch (Exception e) {
			logger.error("Error in sendTestMessagesToKafka " + e);
		}
	}

	private String getTimeStamp() {

		String encodedTS = null;
		try {
			String last_ts = dbConnector.getLastTimeStamp();
			encodedTS = URLEncoder.encode(last_ts, StandardCharsets.UTF_8.toString());
		} catch (Exception e) {
			System.err.println("Error in sendTestMessagesToKafka " + e);
		}
		return encodedTS;
	}

	private void updateControlTable(String payloadStr) {
		String latest_ts = null;
		int num_record = 0;
		if (payloadStr.contains("{") && payloadStr.contains("}")) {
			latest_ts = payloadStr.substring((payloadStr.length() - 29), payloadStr.length() - 3);
			String a[] = payloadStr.split("tran_id");
			num_record = a.length;
		}

		logger.info("The latest timestamp received from the database is " + latest_ts);
		if (latest_ts != null && num_record != 0) {
			try {
				dbConnector.updateControlTable(latest_ts, num_record - 1, "FSQ879A");
			} catch (Exception e) {
				System.err.println("Error in sendTestMessagesToKafka " + e);
				e.printStackTrace();
			}
		}
	}

	private String getPayload(String myURL) {
		System.out.println("Requeted URL:" + myURL);
		StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			URL url = new URL(myURL);
			urlConn = url.openConnection();
			if (urlConn != null)
				urlConn.setReadTimeout(60 * 1000);
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				if (bufferedReader != null) {
					int cp;
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}
			}
			in.close();
		} catch (Exception e) {
			throw new RuntimeException("Exception while calling URL:" + myURL, e);
		}

		return sb.toString();
	}

	/**
	 * Function to send a message to Kafka. The thread is running on every 7 seconds
	 * to pull the latest data from the database through the rest api end-point.
	 * 
	 * @param payload  The String message that is send to the Kafka topic
	 * @param producer The KafkaProducer object
	 * @param topic    The topic to send the message
	 */
	private void postMessageToKafkaTopic(String payload, KafkaProducer<String, String> producer, String topic) {
		logger.info("Sending Kafka message: " + payload);
		producer.send(new ProducerRecord<>(topic, payload), new Callback() {

			@Override
			public void onCompletion(RecordMetadata metadata, Exception exception) {
				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				invokeProducerThread(producer);
			}
		});
	}
}
