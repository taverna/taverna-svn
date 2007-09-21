package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAnnotationContainer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractErrorHandlerLayer;

/**
 * Implements retry policy with delay between retries and exponential backoff
 * <p>
 * Default properties are as follows :
 * <ul>
 * <li>maxRetries = 0 (int)</li>
 * <li>initialDelay = 1000 (milliseconds)</li>
 * <li>maxDelay = 2000 (milliseconds)</li>
 * <li>backoffFactor = 1.0 (float)</li>
 * </ul>
 * 
 * @author Tom Oinn
 * 
 */
public class Retry extends AbstractErrorHandlerLayer<RetryConfig> {

	private RetryConfig config = new RetryConfig();

	private static Timer retryTimer = new Timer(true);

	public Retry() {
		super();
	}

	public Retry(int maxRetries, int initialDelay, int maxDelay,
			float backoffFactor) {
		super();
		this.config.setMaxRetries(maxRetries);
		this.config.setInitialDelay(initialDelay);
		this.config.setMaxDelay(maxDelay);
		this.config.setBackoffFactor(backoffFactor);
	}

	class RetryState extends JobState {

		int currentRetryCount = 0;

		public RetryState(Job job, List<? extends ActivityAnnotationContainer> activities) {
			super(job, activities);
		}

		/**
		 * Try to schedule a retry, returns true if a retry is scheduled, false
		 * if the retry count has already been reached (in which case no retry
		 * is scheduled
		 * 
		 * @return
		 */
		public boolean handleError() {
			if (currentRetryCount == config.getMaxRetries()) {
				return false;
			}
			int delay = (int) (config.getInitialDelay() * (Math.pow(config.getBackoffFactor(), currentRetryCount)));
			if (delay > config.getMaxDelay()) {
				delay = config.getMaxDelay();
			}
			TimerTask task = new TimerTask() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					currentRetryCount++;
					getBelow().receiveJob(job, activities);
				}

			};
			retryTimer.schedule(task, delay);
			return true;
		}

	}

	@Override
	protected JobState getStateObject(Job j, List<? extends ActivityAnnotationContainer> activities) {
		return new RetryState(j, activities);
	}

	public void configure(RetryConfig config) {
		this.config = config;
	}

	public RetryConfig getConfiguration() {
		return this.config;
	}
}
