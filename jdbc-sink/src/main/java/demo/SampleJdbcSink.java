package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.messaging.support.GenericMessage;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Soby Chacko
 */
@EnableBinding(Sink.class)
@SpringBootApplication
public class SampleJdbcSink {

	public static void main(String... args){
		SpringApplication.run(SampleJdbcSink.class, args);
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private DataSource dataSource;

	@StreamListener("input")
	public void input(Foo foo) {
		jdbcTemplate.update("INSERT INTO test(id, name, tag) VALUES (?,?,?)", foo.id, foo.name, foo.tag);
	}

	@PostConstruct
	public void initializeData(){
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(resourceLoader.getResource("classpath:sample-schema.sql"));
		populator.setContinueOnError(true);
		DatabasePopulatorUtils.execute(populator, dataSource);
	}

	static class Foo {

		int id;
		String name;
		String tag;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getTag() {
			return tag;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}
	}

	//Following source is used as test producer.
	@EnableBinding(Source.class)
	static class TestSource {

		private AtomicBoolean semaphore = new AtomicBoolean(true);

		@Bean
		@InboundChannelAdapter(channel = Source.OUTPUT, poller = @Poller(fixedDelay = "1000"))
		public MessageSource<Foo> sendTestData() {
			Foo foo1 = new Foo();
			foo1.setId(100);
			foo1.setName("Foobar");
			foo1.setTag("1");

			Foo foo2 = new Foo();
			foo2.setId(200);
			foo2.setName("BarFoo");
			foo2.setTag("2");

			return () ->
					new GenericMessage<>(this.semaphore.getAndSet(!this.semaphore.get()) ? foo1 : foo2);

		}
	}

}
