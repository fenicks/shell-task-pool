package org.christiankakesa.applications.java.shelltaskpool;

//import org.apache.commons.logging.LogFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Store all informations about Batch and Jobs
 */
public final class Batch {
	// private static final org.apache.commons.logging.Log LOG =
	// LogFactory.getLog(Batch.class.getName());
	/**
	 * Static singleton idiom
	 * 
	 * @link http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
	 */
	public static final Batch INSTANCE = new Batch();
	private volatile String batchName;
	private volatile String batchId;
	private volatile Date batchStartDate;
	private volatile Date batchEndDate;
	private volatile BatchStatus batchStatus = BatchStatus.NONE;
	private volatile long jobCounterId = 0;
	private volatile long jobSuccess = 0;
	private volatile long jobFailed = 0;
	private List<JobExecution> jobExecutionList = (List<JobExecution>) Collections
			.synchronizedList(new ArrayList<JobExecution>());

	private Batch() {
	}

	public static Batch getInstance() {
		return INSTANCE;
	}

	public String getBatchName() {
		return batchName;
	}

	public void setBatchName(final String batchName) {
		if (batchName == null) {
			return;
		}
		/** Set the batch name and batch id only if no name given */
		if (this.batchName == null) {
			this.batchName = batchName;
			this.batchId = Utils.hexSHA1(this.batchName);
		}
	}

	public String getBatchId() {
		return batchId;
	}

	public Date getBatchStartDate() {
		return batchStartDate;
	}

	public void setBatchStartDate(Date batchStartDate) {
		this.setBatchStatus(BatchStatus.STARTED);
		this.batchStartDate = batchStartDate;
	}

	public Date getBatchEndDate() {
		return batchEndDate;
	}

	public void setBatchEndDate(Date batchEndDate) {
		this.batchEndDate = batchEndDate;
		this.setBatchStatusFromSuccessFailed();
	}

	/**
	 * Get the string representation of batch duration.
	 * <ul>
	 * <li>format : "00:00:00" - "hours:minutes:seconds"</li>
	 * </ul>
	 * @return string duration formated
	 */
	public String getBatchDuration() {
		return Utils.buildDurationFromDates(this.getBatchEndDate(),
				this.getBatchStartDate());
	}

	public BatchStatus getBatchStatus() {
		return batchStatus;
	}

	public void setBatchStatus(BatchStatus batchStatus) {
		this.batchStatus = batchStatus;
	}

	/**
	 * Look at the jobSuccess and jobFailed to determine last Batch status.
	 * 
	 * @return void
	 */
	public void setBatchStatusFromSuccessFailed() {
		if (this.jobFailed == 0 && this.jobSuccess >= 1) {
			/** Batch completed success full */
			this.setBatchStatus(BatchStatus.COMPLETED);
		} else if (this.jobFailed > 0 && this.jobSuccess >= 1) {
			/** Batch completed but there are job failed */
			this.setBatchStatus(BatchStatus.COMPLETED_WITH_ERROR);
		} else {
			/** All jobs failed */
			this.setBatchStatus(BatchStatus.FAILED);
		}
	}

	public long getJobSuccess() {
		return jobSuccess;
	}

	public void setJobSuccess(long jobSuccess) {
		this.jobSuccess = jobSuccess;
	}

	public void incrementJobSuccess() {
		synchronized (Batch.class) {
			this.jobSuccess++;
		}
	}

	public long getJobFailed() {
		return jobFailed;
	}

	public void setJobFailed(long jobFailed) {
		this.jobFailed = jobFailed;
	}

	public void incrementJobFailed() {
		synchronized (Batch.class) {
			this.jobFailed++;
		}
	}

	/**
	 * Add job to the jobExecutionList and set a job ID.
	 * @param je
	 */
	public void addJobToExecute(final JobExecution je) {
		if (jobExecutionList.add(je)) {
			if (this.jobExecutionList.size() == 1) {
				this.batchStatus = BatchStatus.RUNNING;
			}
			synchronized (Batch.class) {
				++jobCounterId;
				je.setId(jobCounterId);
			}
		}
	}

	public List<JobExecution> getJobExecutionList() {
		return jobExecutionList;
	}

	public static enum BatchStatus {
		NONE, STARTED, RUNNING, FAILED, COMPLETED_WITH_ERROR, COMPLETED
	}
}