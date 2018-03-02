package demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Soby Chacko
 */
@ConfigurationProperties("jdbc")
public class JdbcSourceProperties {

	/**
	 * The query to use to select data.
	 */
	private String query;

	/**
	 * An SQL update statement to execute for marking polled messages as 'seen'.
	 */
	private String update;

	/**
	 * trigger delay for polling
	 */
	private long triggerDelay = 1L;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public long getTriggerDelay() {
		return triggerDelay;
	}

	public void setTriggerDelay(long triggerDelay) {
		this.triggerDelay = triggerDelay;
	}

	public String getUpdate() {
		return update;
	}

	public void setUpdate(String update) {
		this.update = update;
	}

}